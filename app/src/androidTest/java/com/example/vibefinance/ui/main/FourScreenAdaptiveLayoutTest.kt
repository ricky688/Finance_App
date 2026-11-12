package com.example.vibefinance.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.accounts.AccountsScreen
import com.example.vibefinance.ui.history.HistoryScreen
import com.example.vibefinance.ui.home.HomeScreen
import com.example.vibefinance.ui.home.LocalAmbientMotionEnabled
import com.example.vibefinance.ui.recurring.RecurringScreen
import com.example.vibefinance.ui.preferences.*
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Layout-only memory fixtures; no taps, device scaling, records or screenshot baselines changed. */
class FourScreenAdaptiveLayoutTest {
    @get:Rule val rule = createComposeRule()
    private val state = FinanceUiState(isLoading = false)
    private fun show(width: Int = 1200, fontScale: Float = 1f, windowWidth: Int = width, content: @Composable () -> Unit) {
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(0.25f, fontScale),
                LocalMainWindowWidth provides windowWidth.dp,
                LocalExperience provides ExperiencePreferences(motion = MotionLevel.MINIMAL),
                LocalAmbientMotionEnabled provides false) {
                MaterialTheme { Box(Modifier.requiredSize(width.dp, 1000.dp).testTag("Fixture")) { content() } }
            }
        }
        rule.waitUntil(10000) { runCatching { rule.onAllNodesWithTag("Fixture").fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false) }
    }
    @Test fun dailyPlacesBudgetAndAllocationInSeparateColumns() {
        show { HomeScreen(state, {}) }
        val left = rule.onNodeWithTag("DailyLeftColumn").fetchSemanticsNode().boundsInRoot
        val right = rule.onNodeWithTag("DailyRightColumn").fetchSemanticsNode().boundsInRoot
        assertTrue(left.right < right.left)
        rule.onNodeWithTag("DailyCard_daily-hero").assertExists()
        rule.onNodeWithTag("DailyCard_daily-category-budgets").assertExists()
    }
    @Test fun mediumTwoColumnDailySupportsLargeTextWithoutChangingDeviceScale() {
        show(520, fontScale = 2f, windowWidth = 600) { HomeScreen(state, {}) }
        rule.onNodeWithTag("DailyDualColumn").assertExists()
        val left = rule.onNodeWithTag("DailyLeftColumn").fetchSemanticsNode().boundsInRoot
        val right = rule.onNodeWithTag("DailyRightColumn").fetchSemanticsNode().boundsInRoot
        assertTrue(left.right < right.left)
    }
    @Test fun narrowHeroAmountFitsWithoutEllipsisAtLargeFont() {
        val now = System.currentTimeMillis()
        val budget = com.example.vibefinance.data.repository.DailyBudgetInfo(
            totalMonthlyBudget = 9999999.0, totalSpentThisMonth = 0.0, monthlyRemaining = 9999999.0,
            dailyAllowance = 123456.78, dailyRemaining = 123456.78, daysLeft = 20,
            startDate = now - 86400000L, endDate = now + 20 * 86400000L)
        show(252, fontScale = 2f) { com.example.vibefinance.ui.home.HeroDailyBudgetCard(budget, {}, {}) }
        val text = com.example.vibefinance.ui.home.formatHeroTargetAmount(123456.78f, false, 9999999.0)
        val layouts = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
        rule.onNodeWithText(text).performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult) { it(layouts) }
        assertTrue(layouts.isNotEmpty())
        // MultiParagraph keeps the parent width even when Text shrinks to its ink width.
        assertTrue(layouts.all { layout ->
            layout.lineCount == 1 && !layout.isLineEllipsized(0) && !layout.didOverflowHeight &&
                layout.getLineRight(0) <= layout.size.width &&
                layout.size.width <= layout.layoutInput.constraints.maxWidth
        })
    }
    @Test fun compactDailyKeepsOriginalSingleFeed() {
        show(320) { HomeScreen(state, {}) }
        rule.onNodeWithTag("DailyDualColumn").assertDoesNotExist()
        rule.onNodeWithTag("DailyCardsList").assertExists()
    }
    @Test fun accountsKeepsSelectorAndInspectionPlaceholder() {
        show { AccountsScreen(state, {}) }
        rule.onNodeWithTag("AssetsList").assertExists()
        rule.onNodeWithTag("InspectionPlaceholder").assertExists()
    }
    @Test fun recurringSeparatesListFromRenewals() {
        show { RecurringScreen(state, {}) }
        val list = rule.onNodeWithTag("RecurringList").fetchSemanticsNode().boundsInRoot
        val timeline = rule.onNodeWithTag("RecurringRenewalTimeline").fetchSemanticsNode().boundsInRoot
        assertTrue(list.right < timeline.left)
    }
    @Test fun historyUsesNativeInspectionScene() {
        show { HistoryScreen(state, {}) }
        rule.onNodeWithTag("InspectionPlaceholder").assertExists()
    }
    @Test fun narrowMediumShowsSelectedDetailWithoutSqueezingList() {
        show(520) { AdaptiveRecordScene(7, {}, listContent = { Text("List", Modifier.testTag("ListPane")) },
            detailContent = { Text("Receipt", Modifier.testTag("ReceiptPane")) }) }
        rule.onNodeWithTag("ReceiptPane").assertExists()
        rule.onNodeWithTag("InspectionBack").assertIsDisplayed()
        rule.onNodeWithTag("ListPane").assertDoesNotExist()
    }
    @Test fun wideShowsSelectedDetailAlongsideList() {
        show { AdaptiveRecordScene(7, {}, listContent = { Box(Modifier.fillMaxSize().testTag("ListPane")) { Text("List") } },
            detailContent = { Text("Receipt", Modifier.testTag("ReceiptPane")) }) }
        val list = rule.onNodeWithTag("ListPane").fetchSemanticsNode().boundsInRoot
        assertTrue("List pane should keep a readable fixed width", list.width / 0.25f in 380f..420f)
        rule.onNodeWithTag("ReceiptPane").assertExists()
    }
    @Test fun wideDualColumnDisplaysIndependentPaneHeadersWhenRailActive() {
        show { HomeScreen(state = state, onIntent = {}, useRail = true) }
        rule.onNodeWithTag("DualColumnLeftHeader").assertExists()
        rule.onNodeWithTag("DualColumnRightHeader").assertExists()
        rule.onNodeWithTag("DualColumnCustomLayoutBtn").assertExists()
        rule.onNodeWithTag("DualColumnRecalcBtn").assertExists()
    }
    @Test fun phoneLayoutHidesDualColumnPaneHeaders() {
        show(320) { HomeScreen(state = state, onIntent = {}, useRail = false) }
        rule.onNodeWithTag("DualColumnLeftHeader").assertDoesNotExist()
        rule.onNodeWithTag("DualColumnRightHeader").assertDoesNotExist()
        rule.onNodeWithTag("DualColumnCustomLayoutBtn").assertDoesNotExist()
    }
}
