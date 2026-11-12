package com.example.vibefinance.wear

import android.content.Context
import android.content.SharedPreferences
import androidx.work.*
import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.data.repository.BudgetRepository
import com.example.vibefinance.util.CoordinatedPreferences
import com.example.vibefinance.wear.contract.*
import com.google.android.gms.wearable.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

object PhoneBudgetBridge {
    const val SHARE_KEY = "wear_budget_sharing"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private var observations: Job? = null
    private var started = false
    private var listener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    fun start(context: Context) {
        if (started) return
        started = true
        val app = context.applicationContext
        listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key in setOf(SHARE_KEY, "hide_amounts", "app_lock_enabled", "motion_level")) configure(app)
        }
        prefs(app).registerOnSharedPreferenceChangeListener(listener)
        configure(app)
    }

    fun prefs(context: Context): SharedPreferences = CoordinatedPreferences.get(context, "vibe_finance_prefs")

    private fun configure(context: Context) {
        val enabled = prefs(context).getBoolean(SHARE_KEY, false)
        if (!enabled && !bridgePrefs(context).getBoolean("published", false)) return
        val manager = WorkManager.getInstance(context)
        if (enabled) {
            manager.enqueueUniquePeriodicWork("wear-budget-refresh", ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<PhoneBudgetWorker>(15, TimeUnit.MINUTES).build())
            if (observations?.isActive != true) observations = scope.launch {
                try {
                    InMemoryDatabase.ensureInitialized(context)
                    combine(InMemoryDatabase.budgets, InMemoryDatabase.transactions) { _, _ -> Unit }
                        .collect { publishSafely(context) }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    android.util.Log.w("WearBudget", "Budget observation unavailable")
                }
            }
            scope.launch { publishSafely(context) }
        } else {
            observations?.cancel()
            observations = null
            manager.cancelUniqueWork("wear-budget-refresh")
            // A persisted publication flag lets an offline watch receive a tombstone later.
            if (bridgePrefs(context).getBoolean("published", false)) scope.launch { publishSafely(context) }
        }
    }

    private fun bridgePrefs(context: Context) = context.getSharedPreferences("wear_budget_bridge", Context.MODE_PRIVATE)

    suspend fun publishSafely(context: Context): Boolean = try {
        publish(context)
        true
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        android.util.Log.w("WearBudget", "Budget publication unavailable")
        false
    }

    private suspend fun publish(context: Context) = mutex.withLock {
        val prefs = prefs(context)
        val allowed = prefs.getBoolean(SHARE_KEY, false) && !prefs.getBoolean("hide_amounts", false) &&
            !prefs.getBoolean("app_lock_enabled", false)
        val zone = ZoneId.systemDefault()
        var calculationDate = LocalDate.now(zone)
        val info = if (allowed) {
            InMemoryDatabase.ensureInitialized(context)
            var result = BudgetRepository().getDailyAllowanceFlow().first()
            if (LocalDate.now(zone) != calculationDate) {
                calculationDate = LocalDate.now(zone)
                result = BudgetRepository().getDailyAllowanceFlow().first()
            }
            result
        } else null
        val now = System.currentTimeMillis()
        val bridge = bridgePrefs(context)
        val revision = maxOf(now, bridge.getLong("revision", 0) + 1)
        check(bridge.edit().putLong("revision", revision).putBoolean("published", true).commit())
        // Recheck after IO so a simultaneous privacy change never renews a visible lease.
        val stillAllowed = allowed && prefs.getBoolean(SHARE_KEY, false) &&
            !prefs.getBoolean("hide_amounts", false) && !prefs.getBoolean("app_lock_enabled", false)
        val snapshot = snapshotForWear(info, revision, now, zone, stillAllowed,
            prefs.getString("motion_level", "MINIMAL").orEmpty())
        val request = PutDataMapRequest.create(WearBudgetSnapshot.DATA_PATH).apply {
            dataMap.putByteArray(WearBudgetSnapshot.PAYLOAD_KEY, snapshot.encode())
        }.asPutDataRequest().setUrgent()
        Wearable.getDataClient(context).putDataItem(request).await()
    }
}

class PhoneBudgetWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = if (PhoneBudgetBridge.publishSafely(applicationContext))
        Result.success() else Result.retry()
}

class PhoneBudgetService : WearableListenerService() {
    override fun onMessageReceived(event: MessageEvent) {
        if (event.path != WearBudgetSnapshot.REFRESH_PATH || event.data.isNotEmpty()) return
        // Work survives service teardown and limits repeated refresh requests to one pending job.
        WorkManager.getInstance(this).enqueueUniqueWork("wear-budget-request", ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<PhoneBudgetWorker>().build())
    }
}
