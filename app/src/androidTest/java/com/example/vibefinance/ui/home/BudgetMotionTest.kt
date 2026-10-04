package com.example.vibefinance.ui.home

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.graphics.toArgb
import kotlin.math.abs
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vibefinance.R
import com.example.vibefinance.data.repository.DailyBudgetInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Paths explored on Waydroid through ARTEMIS observation and ADB on 2026-10-03. */
@RunWith(AndroidJUnit4::class)
class BudgetMotionTest {
    @get:Rule val rule = createComposeRule()

    @Test fun todayWave_keepsMovingAfterTheFirstFiveSeconds() {
        rule.mainClock.autoAdvance = false
        val now = System.currentTimeMillis()
        rule.setContent {
            MaterialTheme {
                HeroDailyBudgetCard(
                    budgetInfo = DailyBudgetInfo(
                        totalMonthlyBudget = 1500.0, totalSpentThisMonth = 300.0,
                        monthlyRemaining = 1200.0, dailyAllowance = 100.0,
                        dailyRemaining = 50.0, daysLeft = 12,
                        startDate = now - 86_400_000L, endDate = now + 12 * 86_400_000L
                    ),
                    onOpenRecalcSheet = {}, onOpenBudgetDialog = {},
                    modifier = Modifier.width(360.dp)
                )
            }
        }
        assertStillMoving("HeroDailyBudgetCard")
    }

    @Test fun remainingWave_keepsMovingAfterTheFirstFiveSeconds() {
        remainingWave(650.0)
    }

    @Test fun remainingWave_isVisibleEvenAtFullBudget() {
        remainingWave(1000.0)
    }

    private fun remainingWave(remaining: Double) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            MaterialTheme {
                RestAndSpentBudgetCard(
                    remainingBudget = remaining, totalBudget = 1000.0,
                    modifier = Modifier.width(300.dp).height(180.dp)
                )
            }
        }
        assertStillMoving("RemainingBudgetCard")
    }

    private fun assertStillMoving(tag: String) {
        // Advance beyond the old one-shot animation, then sample different phases.
        rule.mainClock.advanceTimeBy(6_000)
        val first = rule.onNodeWithTag(tag).captureToImage()
        rule.mainClock.advanceTimeBy(1_072)
        val second = rule.onNodeWithTag(tag).captureToImage()
        assertTrue("The wave froze after its entrance", changedPixels(first, second) > 10)
    }

    @Test fun daysLeft_morphsToFlatThenRapidTogglesResumeTheWave() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            MaterialTheme { DaysLeftCard(Modifier.width(180.dp).height(180.dp), 12, 30) }
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val flat = context.getString(R.string.indicator_shape_flat)
        val wavy = context.getString(R.string.indicator_shape_wavy)
        rule.mainClock.advanceTimeBy(2_000)
        rule.onNodeWithTag("DaysLeftCard").performClick()
        rule.mainClock.advanceTimeBy(96)
        val duringMorph = rule.onNodeWithTag("DaysLeftIndicator", useUnmergedTree = true).captureToImage()
        rule.mainClock.advanceTimeBy(2_000)
        rule.onNodeWithText(flat).assertExists()
        val settledFlat = rule.onNodeWithTag("DaysLeftIndicator", useUnmergedTree = true).captureToImage()
        assertTrue("Flat switch should morph over several frames", changedPixels(duringMorph, settledFlat) > 10)
        rule.mainClock.advanceTimeBy(1_072)
        val flatLater = rule.onNodeWithTag("DaysLeftIndicator", useUnmergedTree = true).captureToImage()
        assertEquals("Flat mode should rest", 0, changedPixels(settledFlat, flatLater))
        repeat(3) {
            rule.onNodeWithTag("DaysLeftCard").performClick()
            rule.mainClock.advanceTimeBy(64)
        }
        rule.mainClock.advanceTimeBy(2_000)
        rule.onNodeWithText(wavy).assertExists()
        val firstWave = rule.onNodeWithTag("DaysLeftIndicator", useUnmergedTree = true).captureToImage()
        rule.mainClock.advanceTimeBy(1_072)
        assertTrue("Rapid toggles must leave a moving Wavy indicator", changedPixels(
            firstWave, rule.onNodeWithTag("DaysLeftIndicator", useUnmergedTree = true).captureToImage()
        ) > 10)
    }

    private fun changedPixels(a: ImageBitmap, b: ImageBitmap): Int {
        assertEquals(a.width, b.width)
        assertEquals(a.height, b.height)
        val before = a.toPixelMap()
        val after = b.toPixelMap()
        var changed = 0
        for (y in 0 until a.height) for (x in 0 until a.width) {
            // Waydroid screenshot dithering differs by up to four channel levels.
            val p = before[x, y].toArgb()
            val q = after[x, y].toArgb()
            if (listOf(0, 8, 16, 24).any { shift ->
                abs(((p ushr shift) and 255) - ((q ushr shift) and 255)) > 6
            }) changed++
        }
        return changed
    }
}
