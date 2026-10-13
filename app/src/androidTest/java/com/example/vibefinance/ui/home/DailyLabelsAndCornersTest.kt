package com.example.vibefinance.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vibefinance.theme.LocalIsDarkTheme
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.components.datepicker.prettyWeekDay
import java.time.DayOfWeek
import java.util.Locale
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Chinese heatmap and both Daily toggles explored with ARTEMIS/ADB before these fixtures. */
@RunWith(AndroidJUnit4::class)
class DailyLabelsAndCornersTest {
    @get:Rule val rule = createComposeRule()
    private fun advance(ms: Long) { rule.mainClock.advanceTimeBy(ms); rule.waitForIdle() }

    private fun content(locale: String = "en", body: @Composable () -> Unit) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            val configuration = Configuration(LocalConfiguration.current).apply { setLocale(Locale.forLanguageTag(locale)) }
            val context = LocalContext.current.createConfigurationContext(configuration)
            CompositionLocalProvider(LocalConfiguration provides configuration, LocalContext provides context,
                LocalIsDarkTheme provides true) {
                MaterialTheme(darkColorScheme(primary = Color.Red, primaryContainer = Color.Red,
                    onPrimary = Color.White, onPrimaryContainer = Color.White, background = Color.Black)) {
                    Box(Modifier.width(360.dp).background(Color.Black)) { body() }
                }
            }
        }
        advance(1_500)
    }

    @Test fun chineseWeekdaysAreDistinctAndFitTheHeatmap() {
        val expected = listOf("一", "二", "三", "四", "五", "六", "日")
        listOf("zh-HK", "zh-Hant", "zh-CN").forEach { language ->
            assertEquals(language, expected, DayOfWeek.values().map { prettyWeekDay(it, Locale.forLanguageTag(language)) })
        }
        content("zh-Hant-HK") { M3ExpressiveHeatmap(emptyList(), 100.0, null, {}, {}) }
        expected.forEach { rule.onAllNodesWithText(it, useUnmergedTree = true).assertCountEquals(1) }
        rule.onAllNodesWithText("星", useUnmergedTree = true).assertCountEquals(0)
    }

    @Test fun habitAndMonthKeepOuterCornersWhileInnerCornersMorph() {
        content {
            var mode by remember { mutableStateOf(CalendarDisplayMode.HEATMAP) }
            ExpressiveDualViewSwitcher(mode, { mode = it })
        }
        verifyEnds(listOf("Habit", "Month"))
    }

    @Test fun spendingTrendKeepsOuterCornersWhileInnerCornersMorph() {
        content { DailySpendingLineChart(FinanceUiState(isLoading = false)) }
        verifyEnds(listOf("Cumulative", "Daily"))
    }

    private fun verifyEnds(labels: List<String>) {
        labels.forEachIndexed { index, label ->
            val button = rule.onNodeWithText(label)
            button.performClick()
            advance(1_500)
            val before = button.captureToImage()
            button.performTouchInput { down(center) }
            advance(1_500)
            val pressed = button.captureToImage()
            val outerRight = index == labels.lastIndex
            assertEquals("$label outer end stays rounded", cornerCoverage(before, outerRight),
                cornerCoverage(pressed, outerRight), 0.06f)
            assertTrue("$label inner corner changes shape",
                cornerCoverage(pressed, !outerRight) > cornerCoverage(before, !outerRight) + 0.06f)
            button.performTouchInput { cancel() }
            advance(700)
            assertEquals("$label restores its inner corner", cornerCoverage(before, !outerRight),
                cornerCoverage(button.captureToImage(), !outerRight), 0.06f)
        }
    }

    // Normalize the painted bounds so existing press scaling cannot masquerade as a corner morph.
    private fun cornerCoverage(image: ImageBitmap, right: Boolean): Float {
        val pixels = image.toPixelMap()
        fun painted(x: Int, y: Int): Boolean {
            val color = pixels[x, y]
            return color.red > color.green + 0.25f && color.red > color.blue + 0.25f
        }
        var left = image.width; var top = image.height; var end = -1; var bottom = -1
        for (y in 0 until image.height) for (x in 0 until image.width) if (painted(x, y)) {
            left = minOf(left, x); end = maxOf(end, x); top = minOf(top, y); bottom = maxOf(bottom, y)
        }
        check(end >= left && bottom >= top) { "Selected button must have a painted outline" }
        val side = minOf((bottom - top + 1) / 2, (end - left + 1) / 2)
        var count = 0
        for (y in 0 until side) for (x in 0 until side) {
            if (painted(if (right) end - x else left + x, top + y)) count++
        }
        return count.toFloat() / (side * side)
    }
}
