package com.example.vibefinance.wear

import android.app.Application
import com.example.vibefinance.wear.contract.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.time.Instant

/** Storage policy runs against Android file APIs; OS 7 rendering is covered on-device. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], manifest = Config.NONE, application = Application::class)
class BudgetCacheTest {
    private val now = Instant.parse("2026-11-08T12:00:00Z").toEpochMilli()
    private lateinit var cache: BudgetCache
    private val phones = setOf("phone")
    private fun packet(revision: Long = 1, status: BudgetStatus = BudgetStatus.READY) = WearBudgetSnapshot(
        revision, status, now, now + 900_000, "2026-11-08", "UTC",
        if (status == BudgetStatus.READY) "HK$ 120" else "",
        if (status == BudgetStatus.READY) "HK$ 500.00" else "", if (status == BudgetStatus.READY) 3 else 0).encode()

    @Before fun setup() {
        val context = RuntimeEnvironment.getApplication()
        java.io.File(context.noBackupFilesDir, "budget-cache-v1").delete()
        cache = BudgetCache(context)
    }

    @Test fun revokeRejectsReplayEvenAfterRestart() {
        assertTrue(cache.accept("phone", packet(), phones, now, 10))
        assertTrue(cache.accept("phone", packet(2, BudgetStatus.HIDDEN), phones, now, 20))
        val restarted = BudgetCache(RuntimeEnvironment.getApplication())
        assertFalse(restarted.accept("phone", packet(), phones, now, 30))
        assertEquals(BudgetStatus.HIDDEN, restarted.read(now, 30).snapshot?.status)
    }
    @Test fun expirationErasesMoneyButRetainsWatermark() {
        cache.accept("phone", packet(), phones, now, 10)
        assertTrue(cache.read(now + 900_000, 900_010).expired)
        val bytes = java.io.File(RuntimeEnvironment.getApplication().noBackupFilesDir, "budget-cache-v1").readBytes()
        assertFalse(String(bytes).contains("HK$"))
        assertFalse(cache.accept("phone", packet(), phones, now, 900_011))
    }
    @Test fun rejectsUntrustedOrAmbiguousSource() {
        assertThrows(IllegalArgumentException::class.java) { cache.accept("other", packet(), phones, now, 10) }
        assertThrows(IllegalArgumentException::class.java) { cache.accept("phone", packet(), setOf("phone", "other"), now, 10) }
        assertNull(cache.read(now, 10).snapshot)
    }
    @Test fun wallClockRollbackAndElapsedClockResetHideAmounts() {
        cache.accept("phone", packet(), phones, now, 100)
        assertTrue(cache.read(now - 1, 110).expired)
        cache.accept("phone", packet(2), phones, now, 100)
        assertTrue(cache.read(now, 1).expired)
    }
}
