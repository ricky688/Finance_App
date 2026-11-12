package com.example.vibefinance.transit

import android.content.Context
import com.example.vibefinance.util.BackupStateCodec
import com.example.vibefinance.util.GnssPoint
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28, 34])
class NfcTransitStoreTest {
    private lateinit var context: Context
    private val now get() = System.currentTimeMillis()
    private val file get() = File(context.noBackupFilesDir, "nfc_transit_candidates.json")
    @Before fun setup() {
        context = RuntimeEnvironment.getApplication()
        NfcTransitStore.clear(context)
    }
    @Test fun settingsDefaultOffAndRoundTripInExistingTypedBackup() {
        assertFalse(MtrTransitSettings.isNfcEnabled(context))
        MtrTransitSettings.setNfcEnabled(context, true)
        val preferences = BackupStateCodec.preferences(context)
        assertTrue(preferences.getJSONObject("vibe_finance_prefs").getJSONObject("nfc_origin_enabled").getBoolean("value"))
        MtrTransitSettings.setNfcEnabled(context, false)
        BackupStateCodec.applyPreferences(context, preferences)
        assertTrue(MtrTransitSettings.isNfcEnabled(context))
    }
    @Test fun candidateFileIsPrivateNoBackupAndPaymentExclusionSurvivesReload() {
        val at = now
        val id = NfcTransitStore.begin(context, at)!!
        assertTrue(NfcTransitStore.locate(context, id, GnssPoint(22.32, 114.21, timestamp = at, accuracyMeters = 20f)))
        assertTrue(file.exists())
        assertEquals(1, NfcTransitStore.origins(context, at + 600_000).size)
        NfcTransitStore.payment(context, at + 1)
        assertTrue(NfcTransitStore.origins(context, at + 600_000).isEmpty())
        NfcTransitStore.clear(context)
        assertFalse(file.exists())
    }
    @Test fun paymentBeforeContactSkipsLocationRequestCandidate() {
        val at = now
        NfcTransitStore.payment(context, at)
        assertNull(NfcTransitStore.begin(context, at + 1))
    }
    @Test fun interruptedAtomicWriteRestoresPreviousEvidence() {
        val at = now
        NfcTransitStore.begin(context, at)
        val backup = File(file.path + ".bak")
        assertTrue(file.renameTo(backup))
        file.writeText("broken partial replacement")
        NfcTransitStore.origins(context, at + 600_000)
        assertEquals(1, NfcTapHistory.decode(file.readText(), now).taps.size)
    }
    @Test fun corruptOrOversizedPrivateStateDoesNotCreateOrigin() {
        file.parentFile!!.mkdirs()
        for (raw in listOf("invalid json", "x".repeat(40_000))) {
            file.writeText(raw)
            assertTrue(NfcTransitStore.origins(context, now).isEmpty())
        }
    }
    @Test fun legacyAndroidDoesNotLoadApi36Callback() {
        MtrTransitSettings.setNfcEnabled(context, true)
        try {
            NfcTransitMonitor.connected(context)
            assertEquals(NfcOriginStatus.UNSUPPORTED, NfcTransitMonitor.status.value)
        } finally { NfcTransitMonitor.disconnected() }
    }
}
