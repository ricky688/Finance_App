package com.example.vibefinance.transit

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.cardemulation.CardEmulation
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.vibefinance.util.GnssLocationManager
import com.example.vibefinance.util.GnssPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal enum class NfcOriginStatus { OFF, UNSUPPORTED, DISCONNECTED, PERMISSION, NFC_OFF, WAITING, CONTACT, SAVED, NO_LOCATION, ERROR }

/** Passive RF observation only; never takes a wallet role or changes NFC routing. */
internal object NfcTransitMonitor {
    private val state = MutableStateFlow(NfcOriginStatus.DISCONNECTED)
    val status = state.asStateFlow()
    private var connection: Connection? = null

    fun connected(context: Context) { connection?.close(); connection = Connection(context.applicationContext).apply { start() } }
    fun disconnected() { connection?.close(); connection = null; state.value = NfcOriginStatus.DISCONNECTED }
    fun payment(at: Long) { connection?.payment(at) }
    fun refresh() { connection?.refresh() }

    private class Connection(private val context: Context) {
        private val handler = Handler(Looper.getMainLooper())
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))
        private val transitPrefs = MtrTransitSettings.getPrefs(context)
        private val nfcPrefs = MtrTransitSettings.nfcPrefs(context)
        private val loggingPrefs = context.getSharedPreferences("cashback_rules_prefs", Context.MODE_PRIVATE)
        private var closed = false
        private var registration: AutoCloseable? = null
        private var fixRequest: Deferred<GnssPoint?>? = null
        private var gate = NfcContactGate()
        private var lastRequest = Long.MIN_VALUE
        private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key in listOf("nfc_origin_enabled", "mtr_transit_tracking_enabled", "notification_logging_enabled")) handler.post { refresh() }
        }
        private fun enabled() = MtrTransitSettings.isEnabled(context) && MtrTransitSettings.isNfcEnabled(context) &&
            loggingPrefs.getBoolean("notification_logging_enabled", true)

        fun start() {
            nfcPrefs.registerOnSharedPreferenceChangeListener(listener)
            transitPrefs.registerOnSharedPreferenceChangeListener(listener)
            loggingPrefs.registerOnSharedPreferenceChangeListener(listener)
            refresh()
        }
        fun refresh() {
            if (closed) return
            registration?.close(); registration = null
            scope.coroutineContext.cancelChildren(); fixRequest = null
            gate = NfcContactGate(); lastRequest = Long.MIN_VALUE
            if (!enabled()) {
                state.value = NfcOriginStatus.OFF
                scope.launch { NfcTransitStore.clear(context) }
                return
            }
            if (Build.VERSION.SDK_INT < 36 || !context.packageManager.hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION)) {
                state.value = NfcOriginStatus.UNSUPPORTED; return
            }
            val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val background = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (!fine || !background) { state.value = NfcOriginStatus.PERMISSION; return }
            try {
                val adapter = NfcAdapter.getDefaultAdapter(context)
                if (adapter == null) { state.value = NfcOriginStatus.UNSUPPORTED; return }
                registration = register(adapter)
                state.value = if (adapter.isEnabled) NfcOriginStatus.WAITING else NfcOriginStatus.NFC_OFF
            } catch (_: Exception) { state.value = NfcOriginStatus.ERROR }
        }
        @RequiresApi(36)
        private fun register(adapter: NfcAdapter): AutoCloseable {
            val card = CardEmulation.getInstance(adapter)
            val callback = object : CardEmulation.NfcEventCallback {
                override fun onRemoteFieldChanged(detected: Boolean) { contact(detected) }
                override fun onNfcStateChanged(value: Int) { handler.post { refresh() } }
            }
            card.registerNfcEventCallback(ContextCompat.getMainExecutor(context), callback)
            return AutoCloseable { try { card.unregisterNfcEventCallback(callback) } catch (_: Exception) { state.value = NfcOriginStatus.ERROR } }
        }
        private fun contact(detected: Boolean) {
            if (closed) return
            if (!gate.accept(detected, SystemClock.elapsedRealtime()) || !enabled()) return
            val eventAt = System.currentTimeMillis()
            state.value = NfcOriginStatus.CONTACT
            scope.launch {
                try {
                    val id = NfcTransitStore.begin(context, eventAt) ?: return@launch
                    ensureActive()
                    if (!enabled()) return@launch
                    val elapsed = SystemClock.elapsedRealtime()
                    val request = fixRequest?.takeIf { it.isActive || elapsed - lastRequest in 0L..10_000L } ?: async {
                        GnssLocationManager.resolveFreshLocation(context, maxAgeMs = 15_000L, recordHistory = false, resolveName = false)
                    }.also { fixRequest = it; lastRequest = elapsed }
                    val fix = request.await()
                    ensureActive()
                    if (!enabled()) return@launch
                    val saved = fix != null && NfcTransitStore.locate(context, id, fix)
                    state.value = if (saved) NfcOriginStatus.SAVED else NfcOriginStatus.NO_LOCATION
                } catch (e: CancellationException) { throw e }
                catch (_: Exception) { state.value = NfcOriginStatus.ERROR }
            }
        }
        fun payment(at: Long) {
            if (closed || !enabled()) return
            scope.launch {
                try { NfcTransitStore.payment(context, at) }
                catch (_: Exception) { state.value = NfcOriginStatus.ERROR }
            }
        }
        fun close() {
            closed = true
            nfcPrefs.unregisterOnSharedPreferenceChangeListener(listener)
            transitPrefs.unregisterOnSharedPreferenceChangeListener(listener)
            loggingPrefs.unregisterOnSharedPreferenceChangeListener(listener)
            handler.removeCallbacksAndMessages(null)
            registration?.close(); registration = null
            scope.cancel()
        }
    }
}
