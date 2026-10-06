package com.example.vibefinance.ui.home

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vibefinance.data.repository.DailyBudgetInfo
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

/** Daily FAB / Expense / Income / Transfer explored with ARTEMIS on Waydroid 2026-10-06.
 * Real card renderers, memory-only budget, deterministic frame clock; never writes a ledger.
 */
@RunWith(AndroidJUnit4::class)
class AmbientOcclusionTest {
    @get:Rule val rule = createComposeRule()
    private val motionEnabled = mutableStateOf(true)

    @Test fun todayWavePausesWhenObscuredAndResumes() = verifyCard("HeroDailyBudgetCard") {
        HeroDailyBudgetCard(
            DailyBudgetInfo(1500.0, 300.0, 1200.0, 100.0, 50.0, 12,
                System.currentTimeMillis() - 86_400_000L, System.currentTimeMillis() + 12 * 86_400_000L),
            onOpenRecalcSheet = {}, onOpenBudgetDialog = {}, modifier = Modifier.width(360.dp)
        )
    }

    @Test fun remainingWavePausesWhenObscuredAndResumes() = verifyCard("RemainingBudgetCard") {
        RestAndSpentBudgetCard(remainingBudget = 650.0, totalBudget = 1000.0,
            modifier = Modifier.width(300.dp).height(180.dp))
    }

    @Test fun daysWavePausesWhenObscuredAndResumes() = verifyCard("DaysLeftCard") {
        DaysLeftCard(Modifier.width(180.dp).height(180.dp), 12, 30)
    }

    @Test fun phaseSurvivesOcclusionAndDoesNotCatchUpHiddenTime() {
        rule.mainClock.autoAdvance = false
        lateinit var phase: State<Float>
        rule.setContent {
            CompositionLocalProvider(LocalAmbientMotionEnabled provides motionEnabled.value) {
                phase = rememberAmbientWavePhase(periodMillis = 5000)
            }
        }
        rule.mainClock.advanceTimeBy(512)
        rule.runOnIdle { motionEnabled.value = false }
        rule.mainClock.advanceTimeBy(64)
        val paused = phase.value
        // Deliberately not a multiple of the 5000ms period: catch a hidden-time jump
        // rather than accidentally wrapping it back to the same phase.
        rule.mainClock.advanceTimeBy(10_736)
        assertEquals(paused, phase.value, 0f)
        rule.runOnIdle { motionEnabled.value = true }
        rule.mainClock.advanceTimeBy(64)
        val advance = (phase.value - paused + 1f) % 1f
        assertTrue("Resume must advance by visible frames, not hidden elapsed time", advance < 0.02f)
        rule.mainClock.advanceTimeBy(160)
        assertNotEquals(paused, phase.value)
    }

    private fun verifyCard(tag: String, content: @androidx.compose.runtime.Composable () -> Unit) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            MaterialTheme {
                CompositionLocalProvider(LocalAmbientMotionEnabled provides motionEnabled.value, content = content)
            }
        }
        rule.mainClock.advanceTimeBy(2000)
        rule.runOnIdle { motionEnabled.value = false }
        rule.mainClock.advanceTimeBy(512)
        val paused = rule.onNodeWithTag(tag).captureToImage()
        rule.mainClock.advanceTimeBy(1072)
        assertEquals("Occluded card should stop drawing its waves", 0,
            changedPixels(paused, rule.onNodeWithTag(tag).captureToImage()))
        rule.runOnIdle { motionEnabled.value = true }
        rule.mainClock.advanceTimeBy(1072)
        assertTrue("Dismissal should resume the wave", changedPixels(paused,
            rule.onNodeWithTag(tag).captureToImage()) > 10)
    }

    private fun changedPixels(a: ImageBitmap, b: ImageBitmap): Int {
        assertEquals(a.width, b.width)
        assertEquals(a.height, b.height)
        val before = a.toPixelMap(); val after = b.toPixelMap()
        var changed = 0
        for (y in 0 until a.height) for (x in 0 until a.width) {
            val p = before[x, y].toArgb(); val q = after[x, y].toArgb()
            if (listOf(0, 8, 16, 24).any { shift ->
                abs(((p ushr shift) and 255) - ((q ushr shift) and 255)) > 6
            }) changed++
        }
        return changed
    }
}
