@file:OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)

package com.example.vibefinance.ui.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import com.example.vibefinance.data.entity.TransactionEntity
import org.junit.Assert.assertEquals
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.geometry.Offset
import kotlin.math.abs
import org.junit.Assert.assertTrue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vibefinance.ui.FinanceUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Daily calendar and its controls explored on Waydroid with ARTEMIS; fixtures do not persist data. */
@RunWith(AndroidJUnit4::class)
class StaticDailyCardInteractionTest {
    @get:Rule val rule = createComposeRule()

    @Test fun trendKeepsTactilePressWithoutHoverAndModeButtonsRemainInteractive() {
        rule.setContent { MaterialTheme {
            DailySpendingLineChart(FinanceUiState(isLoading = false), Modifier.testTag("trend"))
        } }
        assertNoCardHover("trend")
        rule.onNodeWithText("Daily").assertHasClickAction().performClick()
        rule.onNodeWithText("Cumulative").assertHasClickAction().performClick()
    }

    @Test fun rhythmAndCalendarKeepTactilePressWithoutHoverAndSwitcherRemainsInteractive() {
        rule.setContent { MaterialTheme {
            SpendsCalendar(FinanceUiState(isLoading = false, budgetInfo =
                com.example.vibefinance.data.repository.DailyBudgetInfo(1000.0, 0.0, 1000.0, 100.0, 100.0, 10,
                    System.currentTimeMillis(), System.currentTimeMillis() + 86400000L)), Modifier.testTag("calendar"))
        } }
        assertNoCardHover("calendar")
        rule.onNodeWithText("Month", substring = true).performClick()
        assertNoCardHover("calendar")
        rule.onNodeWithText("Habit", substring = true).performClick()
        assertNoCardHover("calendar")
    }

    @Test fun informationalSpendingCardKeepsTactilePressWithoutHover() {
        rule.setContent { MaterialTheme {
            MinMaxSpentCard(isMin = true, spends = emptyList(), modifier = Modifier.testTag("minimum"))
        } }
        assertNoCardHover("minimum")
    }
    @Test fun lightCategoryAnalyticsOnlyHighlightsActionTargets() = verifyCategoryAnalytics(false)
    @Test fun darkCategoryAnalyticsOnlyHighlightsActionTargets() = verifyCategoryAnalytics(true)

    private fun verifyCategoryAnalytics(dark: Boolean) {
        val opened = mutableListOf<String>()
        rule.setContent { MaterialTheme(if (dark) darkColorScheme() else lightColorScheme()) {
            CategoryDonutChart(FinanceUiState(isLoading = false, transactions = listOf(
                TransactionEntity(id = 1, amount = 20.0, category = "Transport", accountId = 1, timestamp = 0),
                TransactionEntity(id = 2, amount = 10.0, category = "Food", accountId = 1, timestamp = 0)
            )), onCategoryClick = { opened.add(it) })
        } }
        assertNoCardHover("DailyCategoryAnalyticsCard")
        rule.runOnIdle { assertTrue("Background press does not navigate", opened.isEmpty()) }
        val card = rule.onNodeWithTag("DailyCategoryAnalyticsCard")
        val before = card.captureToImage().toPixelMap()
        val title = rule.onNodeWithTag("DailyCategoryAnalyticsTitle", useUnmergedTree = true).assertIsEnabled()
        title.performMouseInput { enter(center) }
        rule.waitForIdle()
        val hovered = card.captureToImage().toPixelMap()
        assertTrue("Title hover must not tint the whole card", samePixel(before[before.width / 2, 10], hovered[hovered.width / 2, 10]))
        title.performMouseInput { click(); exit() }
        rule.runOnIdle { assertEquals(listOf("Transport"), opened) }
        rule.onNodeWithTag("DailyCategoryAnalyticsItem_Food").performMouseInput { enter(center); click(); exit() }
        rule.runOnIdle { assertEquals(listOf("Transport", "Food"), opened) }
        // The title retains its existing selected-category destination.
        title.performMouseInput { enter(center); click(); exit() }
        rule.runOnIdle { assertEquals(listOf("Transport", "Food", "Food"), opened) }
    }

    @Test fun emptyCategoryAnalyticsKeepsTactilePressWithoutNavigation() {
        var opened = 0
        rule.setContent { MaterialTheme {
            CategoryDonutChart(FinanceUiState(isLoading = false), onCategoryClick = { opened++ })
        } }
        assertNoCardHover("DailyCategoryAnalyticsCard")
        val title = rule.onNodeWithTag("DailyCategoryAnalyticsTitle", useUnmergedTree = true).assertIsNotEnabled()
        title.performMouseInput { enter(center); click(); exit() }
        rule.runOnIdle { assertEquals(0, opened) }
    }

    private fun assertNoCardHover(tag: String) {
        val card = rule.onNodeWithTag(tag).assertHasClickAction()
        val before = card.captureToImage().toPixelMap()
        card.performMouseInput { enter(Offset(width / 2f, 10f)) }
        rule.waitForIdle()
        val hovered = card.captureToImage().toPixelMap()
        val a = before[before.width / 2, 10]
        val b = hovered[hovered.width / 2, 10]
        assertTrue("$tag background must not highlight on hover",
            samePixel(a, b))
        card.performMouseInput { exit() }
        rule.waitForIdle()
        val resting = card.captureToImage().toPixelMap()
        card.performTouchInput { down(Offset(width / 2f, 10f)) }
        rule.mainClock.advanceTimeBy(220)
        rule.waitForIdle()
        val pressed = card.captureToImage().toPixelMap()
        val corner = with(rule.density) { 6.dp.roundToPx() }
        val old = resting[corner, corner]
        val held = pressed[corner, corner]
        assertTrue("$tag keeps shape morphing on press",
            abs(old.red - held.red) + abs(old.green - held.green) + abs(old.blue - held.blue) + abs(old.alpha - held.alpha) > 0.03f)
        card.performTouchInput { up() }
        rule.waitForIdle()
        // Android's hardware ripple uses real time, independently of the Compose frame clock.
        val baseline = resting[resting.width / 2, 10]
        rule.waitUntil(2_000) {
            val settled = card.captureToImage().toPixelMap()
            val current = settled[settled.width / 2, 10]
            samePixel(baseline, current)
        }
    }

    // PixelCopy/native ripple blending can round by one 8-bit channel step.
    private fun samePixel(a: Color, b: Color): Boolean = maxOf(
        abs(a.red - b.red), abs(a.green - b.green), abs(a.blue - b.blue), abs(a.alpha - b.alpha)
    ) <= 1f / 255f + 0.00001f

}
