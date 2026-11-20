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
import java.io.File
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], manifest = Config.NONE, application = Application::class)
class CompanionCacheAndComplicationTest {
    private val now = Instant.parse("2026-11-20T04:00:00Z").toEpochMilli()
    private lateinit var cache: CompanionCache
    private val phones = setOf("phone")

    private fun packet(
        revision: Long = 1,
        status: BudgetStatus = BudgetStatus.READY,
        compact: String = "$320",
        progress: Float = 0.72f
    ) = WearCompanionSnapshot(
        revision = revision,
        status = status,
        generatedAt = now,
        freshUntil = now + WearCompanionSnapshot.FRESH_AGE_MS,
        expiresAt = now + WearCompanionSnapshot.MAX_STALE_AGE_MS,
        calculationDate = "2026-11-20",
        zoneId = "UTC",
        dailyAmount = if (status == BudgetStatus.READY) "HK$ 320" else "",
        compactDailyAmount = if (status == BudgetStatus.READY) compact else "",
        periodAmount = if (status == BudgetStatus.READY) "HK$ 2,400.00" else "",
        daysLeft = if (status == BudgetStatus.READY) 5 else 0,
        dailyProgress = if (status == BudgetStatus.READY) progress else 0f,
        periodProgress = if (status == BudgetStatus.READY) 0.5f else 0f
    ).encode()

    @Before
    fun setup() {
        val ctx = RuntimeEnvironment.getApplication()
        File(ctx.noBackupFilesDir, "companion-cache-v1").delete()
        File(ctx.noBackupFilesDir, "pending-commands-v1").delete()
        cache = CompanionCache(ctx)
    }

    @Test
    fun twoStageStalenessTransitionsFreshToStaleToHardExpired() {
        assertTrue(cache.accept("phone", packet(), phones, now, 100L))

        val fresh = cache.read(now + 60_000L, 60_100L)
        assertTrue(fresh.fresh)
        assertFalse(fresh.stale)
        assertFalse(fresh.expired)
        assertEquals("HK$ 320", fresh.snapshot?.dailyAmount)

        // At 20 minutes: soft-stale (still readable, flagged stale)
        val stale = cache.read(now + 20 * 60_000L, 20 * 60_000L + 100L)
        assertFalse(stale.fresh)
        assertTrue(stale.stale)
        assertFalse(stale.expired)
        assertEquals("HK$ 320", stale.snapshot?.dailyAmount)

        // At 2 hours: hard expired & wiped from disk, watermark preserved against replay
        val expired = cache.read(now + WearCompanionSnapshot.MAX_STALE_AGE_MS, WearCompanionSnapshot.MAX_STALE_AGE_MS + 100L)
        assertTrue(expired.expired)
        assertNull(expired.snapshot)
        val diskBytes = File(RuntimeEnvironment.getApplication().noBackupFilesDir, "companion-cache-v1").readBytes()
        assertFalse(String(diskBytes).contains("HK$"))
        assertFalse(cache.accept("phone", packet(revision = 1), phones, now, WearCompanionSnapshot.MAX_STALE_AGE_MS + 200L))
    }

    @Test
    fun offlineCommandQueuePersistsDeduplicatesAndRemoves() {
        val cmd1 = WearCommand("req-a", now, WearCommandAction.QUICK_ADD_EXPENSE, accountId = 1L, category = "Food & Drink", amount = 35.0)
        val cmd2 = WearCommand("req-b", now + 1000L, WearCommandAction.QUICK_ADD_EXPENSE, accountId = 1L, category = "Transport", amount = 12.5)
        cache.enqueueOfflineCommand(cmd1)
        cache.enqueueOfflineCommand(cmd1) // duplicate requestId replaced, not doubled
        cache.enqueueOfflineCommand(cmd2)

        val reloaded = CompanionCache(RuntimeEnvironment.getApplication())
        assertEquals(2, reloaded.queuedCommands().size)
        reloaded.removeQueuedCommand("req-a")
        assertEquals(listOf("req-b"), reloaded.queuedCommands().map { it.requestId })
    }

    @Test
    fun complicationDisplayRespectsPrivacyExpiryAndCompactFitting() {
        cache.accept("phone", packet(), phones, now, 100L)
        val ready = BudgetComplicationService.resolveComplicationDisplay(
            companion = cache.read(now + 1000L, 1100L),
            legacy = CachedBudget(),
            todayLabel = "Today",
            hiddenLabel = "Hidden",
            syncLabel = "Sync",
            daysShort = { "${it}d" },
            daysLong = { "$it days left" }
        )
        assertTrue(ready.hasMoney)
        assertEquals("$320", ready.shortText)
        assertEquals("5d", ready.shortTitle)
        assertEquals("HK$ 320 · 72% · 5 days left", ready.longText)
        assertEquals(72, ready.percent)
        assertFalse(ready.isOverspent)
        assertEquals(0.72f, ready.rangedValue, 0.001f)
        assertTrue(ready.validUntil > ready.validFrom)

        // Hidden must contain zero money
        cache.accept("phone", packet(revision = 2, status = BudgetStatus.HIDDEN), phones, now, 200L)
        val hidden = BudgetComplicationService.resolveComplicationDisplay(
            companion = cache.read(now + 2000L, 2200L),
            legacy = CachedBudget(),
            todayLabel = "Today",
            hiddenLabel = "Hidden",
            syncLabel = "Sync",
            daysShort = { "${it}d" },
            daysLong = { "$it days left" }
        )
        assertFalse(hidden.hasMoney)
        assertEquals("--", hidden.shortText)
        assertEquals("Hidden", hidden.shortTitle)
        assertEquals(0f, hidden.rangedValue, 0.001f)
    }
}
