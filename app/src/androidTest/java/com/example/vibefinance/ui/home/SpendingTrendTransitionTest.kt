@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.example.vibefinance.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.preferences.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

/** Daily -> Spending Trend -> Daily/Cumulative and the existing curve were explored with ARTEMIS/ADB. */
@RunWith(AndroidJUnit4::class)
class SpendingTrendTransitionTest {
    @get:Rule val rule = createComposeRule()
    private var nonDailyAmount by mutableStateOf(9999.0)
    private fun show(motion: MotionLevel = MotionLevel.FULL, dark: Boolean = false, empty: Boolean = false) {
        val today = LocalDate.now()
        val entries = listOf(10.0, 40.0, 0.0, 80.0, 5.0, 60.0, 20.0).mapIndexed { index, amount ->
            TransactionEntity(id = index + 1L, accountId = 1, amount = amount, category = "Food",
                timestamp = today.minusDays(6L - index).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
        }
        val records = if (empty) emptyList() else entries + listOf(
            entries.last().copy(id = 8, amount = -1000.0),
            entries.last().copy(id = 9, amount = 5000.0, toAccountId = 2))
        rule.mainClock.autoAdvance = false
        rule.setContent {
            val configuration = Configuration(LocalConfiguration.current).apply { setLocale(Locale.ENGLISH) }
            val context = LocalContext.current.createConfigurationContext(configuration)
            CompositionLocalProvider(LocalConfiguration provides configuration, LocalContext provides context,
                LocalExperience provides ExperiencePreferences(motion = motion)) {
                MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme(),
                    motionScheme = MotionScheme.expressive()) {
                    Box(Modifier.width(360.dp).background(MaterialTheme.colorScheme.background)) {
                        DailySpendingLineChart(FinanceUiState(isLoading = false, transactions = if (empty) records else
                            records + entries.last().copy(id = 10, amount = nonDailyAmount, isExcludedFromDailyBudget = true)))
                    }
                }
            }
        }
        advance(1_500)
    }
    private fun advance(ms: Long) { rule.mainClock.advanceTimeBy(ms); rule.waitForIdle() }
    private fun mode(mode: DailyChartMode) = rule.onNodeWithTag("SpendingTrendMode_${mode.name}")
    private fun canvas() = rule.onNodeWithTag("SpendingTrendCanvas", useUnmergedTree = true)
    private fun image() = canvas().captureToImage()
    private fun amount(): String = rule.onAllNodes(
        hasTestTag("SpendingTrendSummaryValue") or hasAnyAncestor(hasTestTag("SpendingTrendSummaryValue")),
        useUnmergedTree = true).fetchSemanticsNodes().flatMap {
        it.config.getOrNull(SemanticsProperties.Text).orEmpty()
    }.joinToString("") { it.text }

    @Test fun fullMotionMorphsThroughIntermediateFrames_reversesSmoothly_andKeepsExactScrubValues() {
        show()
        val bounds = canvas().fetchSemanticsNode().boundsInRoot
        val cumulative = image()
        assertEquals("$215.00", amount())
        mode(DailyChartMode.DAILY).performClick()
        advance(80)
        mode(DailyChartMode.DAILY).assertIsSelected()
        val during = image()
        assertEquals(bounds, canvas().fetchSemanticsNode().boundsInRoot)
        advance(1_500)
        val daily = image()
        assertTrue(changedPixels(cumulative, during) > 100)
        assertTrue(changedPixels(during, daily) > 100)
        assertEquals("$20.00", amount())
        mode(DailyChartMode.CUMULATIVE).performClick(); advance(80)
        mode(DailyChartMode.DAILY).performClick(); advance(1_500)
        assertTrue("Rapid reversal must reach the same Daily chart", changedPixels(daily, image()) < 10)
        canvas().performTouchInput { click(Offset(width * .5f, height * .5f)) }
        advance(1_500)
        assertEquals("$80.00", amount())
        mode(DailyChartMode.CUMULATIVE).performClick(); advance(1_500)
        assertEquals("$130.00", amount())
        assertEquals(bounds, canvas().fetchSemanticsNode().boundsInRoot)
    }

    @Test fun reducedMotionUsesAShortTransition_inDarkTheme() {
        show(MotionLevel.REDUCED, dark = true)
        mode(DailyChartMode.DAILY).performClick(); advance(32)
        val during = image()
        advance(1_000)
        assertTrue(changedPixels(during, image()) > 100)
        assertEquals("$20.00", amount())
    }

    @Test fun minimalMotionReachesTheFinalChartImmediately() {
        show(MotionLevel.MINIMAL)
        mode(DailyChartMode.DAILY).performClick(); advance(32)
        val immediate = image()
        assertEquals("$20.00", amount())
        advance(1_500)
        assertEquals(0, changedPixels(immediate, image()))
        mode(DailyChartMode.CUMULATIVE).performClick(); advance(32)
        assertEquals("$215.00", amount())
    }

    @Test fun nonDailySpendingStacksInGrayWithoutChangingCurveOrSummary() {
        show(MotionLevel.MINIMAL)
        val cumulative = image()
        rule.runOnIdle { nonDailyAmount = 12000.0 }; advance(32)
        assertEquals("Cumulative curve must ignore gray spending", 0, changedPixels(cumulative, image()))
        assertEquals("$215.00", amount())
        mode(DailyChartMode.DAILY).performClick(); advance(32)
        assertEquals("$20.00", amount())
        rule.onNodeWithTag("SpendingTrendNonDailyValue", useUnmergedTree = true).assertTextContains("$12,000.00", substring = true)
        val pixels = image().toPixelMap()
        val gray = pixels[pixels.width - 4, pixels.height / 4]
        val background = pixels[pixels.width - 20, pixels.height / 4]
        assertTrue("Gray stack should be visible above the green daily bar",
            kotlin.math.abs(gray.red - background.red) + kotlin.math.abs(gray.green - background.green) > .1f)
        assertTrue("Non-daily segment must stay neutral gray", kotlin.math.abs(gray.red - gray.green) < .08f)
        mode(DailyChartMode.CUMULATIVE).performClick(); advance(32)
        assertEquals("$215.00", amount())
        assertEquals(0, changedPixels(cumulative, image()))
    }

    @Test fun emptyDataCanSwitchBothWays_withoutMovingOrResizingTheChart() {
        show(dark = true, empty = true)
        val bounds = canvas().fetchSemanticsNode().boundsInRoot
        mode(DailyChartMode.DAILY).performClick(); advance(1_500)
        assertEquals("$0.00", amount())
        mode(DailyChartMode.CUMULATIVE).performClick(); advance(1_500)
        assertEquals("$0.00", amount())
        assertEquals(bounds, canvas().fetchSemanticsNode().boundsInRoot)
    }

    private fun changedPixels(first: ImageBitmap, second: ImageBitmap): Int {
        assertEquals(first.width, second.width); assertEquals(first.height, second.height)
        val a = first.toPixelMap(); val b = second.toPixelMap()
        var changed = 0
        for (y in 0 until first.height) for (x in 0 until first.width) {
            val c = a[x, y]; val d = b[x, y]
            if (kotlin.math.abs(c.red - d.red) + kotlin.math.abs(c.green - d.green) +
                kotlin.math.abs(c.blue - d.blue) > .04f) changed++
        }
        return changed
    }
}
