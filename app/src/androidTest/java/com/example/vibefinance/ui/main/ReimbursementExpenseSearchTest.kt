package com.example.vibefinance.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.ui.preferences.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

/** Income repayment entry/empty state explored with ARTEMIS; fixtures never persist records. */
class ReimbursementExpenseSearchTest {
    @get:Rule val rule = createComposeRule()
    private val day = LocalDate.now().withDayOfMonth(1).minusMonths(1).withDayOfMonth(26)
    private val zone = ZoneId.systemDefault()
    private fun tx(id: Long, date: LocalDate = day, hour: Int = 12, minute: Int = 0, text: String = "Other") =
        TransactionEntity(id = id, amount = 100.0, category = "Food", accountId = 1,
            timestamp = date.atTime(hour, minute).atZone(zone).toInstant().toEpochMilli(), description = text)
    private val records = listOf(
        tx(10, hour = 0, minute = 1, text = "Café early"), tx(11, hour = 23, minute = 59, text = "Café late"),
        tx(12, day.plusDays(1), text = "Café next"), tx(13, day.minusDays(1), text = "Taxi").copy(category = "Transport"),
        tx(15, text = "Fully paid").copy(amount = 10.0), tx(16, day.plusDays(1)).copy(amount = -10.0, reimbursementOf = 15),
        tx(17, LocalDate.now().plusDays(1), text = "Future"), tx(18).copy(toAccountId = 2), tx(19).copy(amount = -50.0),
        tx(90, day.plusDays(1)).copy(amount = -20.0, reimbursementOf = 10)) +
        (100L..129L).map { tx(it, day.plusDays(1), hour = 13, text = "Other $it") }
    private var selected by mutableLongStateOf(0L)
    private val selections = mutableListOf<Long>()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun show(motion: MotionLevel = MotionLevel.FULL, blur: Float = 1f, dark: Boolean = false,
        restoration: StateRestorationTester? = null, latest: Long? = null) {
        val content: @Composable () -> Unit = {
            CompositionLocalProvider(LocalExperience provides ExperiencePreferences(motion = motion, blur = blur)) {
                MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                    Box(Modifier.fillMaxWidth().padding(16.dp)) {
                        ReimbursementExpenseSelector(records, selected, latestExpenseTimestamp = latest) { selected = it.id; selections += it.id }
                    }
                }
            }
        }
        if (restoration == null) rule.setContent(content) else restoration.setContent(content)
        rule.onNodeWithTag("ReimbursementExpense").performClick()
    }
    private fun count(number: Int) = rule.onNodeWithTag("ReimbursementResultCount")
        .assertTextContains(context.getString(R.string.history_results_count, number))
    private fun clickCalendarDay(day: Int) {
        val dayNumber = Regex("(?<![0-9])$day(?![0-9])")
        val accessibilityDate = SemanticsMatcher("Calendar date for day $day") { node ->
            val descriptions = node.config.getOrNull(androidx.compose.ui.semantics.SemanticsProperties.ContentDescription).orEmpty()
            val dates = node.config.getOrNull(androidx.compose.ui.semantics.SemanticsProperties.Text).orEmpty().map { it.text }
            (descriptions + dates).any { dayNumber.containsMatchIn(it) }
        }
        val cell = accessibilityDate and hasClickAction() and hasAnyAncestor(hasTestTag("ReimbursementDatePicker"))
        if (rule.onAllNodes(cell).fetchSemanticsNodes().isEmpty()) {
            throw AssertionError("Calendar semantics:\n" + rule.onNodeWithTag("ReimbursementDatePicker", useUnmergedTree = true).printToString(maxDepth = 18))
        }
        rule.onNode(cell).performClick()
    }
    private fun select26() {
        rule.onNodeWithTag("ReimbursementDateFilter").performClick()
        rule.onNodeWithTag("ReimbursementDatePicker").assertExists()
        clickCalendarDay(26)
        rule.onNodeWithTag("ReimbursementDateApply").assertIsEnabled().performClick()
        rule.onNodeWithTag("ReimbursementDatePicker").assertDoesNotExist()
    }

    @Test fun calendarDay26CombinesWithSearchClearsAndSelectsTheOriginalSnapshot() {
        show(); count(35)
        select26(); count(3)
        rule.onNodeWithTag("ReimbursementSource_10").assertIsDisplayed()
        rule.onNodeWithTag("ReimbursementSource_11").assertIsDisplayed()
        rule.onNodeWithTag("ReimbursementSource_15").assertIsDisplayed()
        rule.onNodeWithTag("ReimbursementSearch").performClick().performTextReplacement("Café")
        count(2)
        rule.onNodeWithTag("ReimbursementDateFilter").performClick()
        clickCalendarDay(25)
        rule.onNodeWithText(context.getString(R.string.btn_cancel)).performClick()
        count(2)
        rule.onNodeWithTag("ReimbursementDateClear").performClick()
        count(3)
        rule.onNodeWithTag("ReimbursementSearch").performTextReplacement("late")
        count(1)
        rule.onNodeWithTag("ReimbursementSource_11").performClick()
        rule.onNodeWithTag("ReimbursementSearchDialog").assertDoesNotExist()
        rule.onNodeWithTag("ReimbursementExpense").assertTextContains("Café late")
        assertEquals(listOf(11L), selections)
    }

    @Test fun searchFiltersAnimateInsideFixedBoundsAndClearUnneededBlur() {
        show()
        rule.waitUntil(5000) { rule.onAllNodesWithTag("ReimbursementSearch_end_blur").fetchSemanticsNodes().isNotEmpty() }
        val viewport = rule.onNodeWithTag("ReimbursementSearchScrollViewport").getUnclippedBoundsInRoot()
        rule.onNodeWithTag("ReimbursementSearchResults").performScrollToIndex(15)
        rule.onNodeWithTag("ReimbursementSearch_start_blur").assertExists()
        rule.mainClock.autoAdvance = false
        rule.onNodeWithTag("ReimbursementSearch").performTextReplacement("late")
        rule.mainClock.advanceTimeBy(32)
        val middle = rule.onNodeWithTag("ReimbursementSearchScrollViewport").captureToImage().toPixelMap()
        rule.mainClock.advanceTimeBy(2000)
        val settled = rule.onNodeWithTag("ReimbursementSearchScrollViewport").captureToImage().toPixelMap()
        var changed = 0
        for (y in 0 until middle.height step 3) for (x in 0 until middle.width step 3) {
            if (middle[x, y] != settled[x, y]) changed++
        }
        assertTrue("Filtering should pass through a visible intermediate frame", changed > 10)
        assertEquals(viewport, rule.onNodeWithTag("ReimbursementSearchScrollViewport").getUnclippedBoundsInRoot())
        rule.mainClock.autoAdvance = true
        count(1)
        rule.onNodeWithTag("ReimbursementSearch_start_blur").assertDoesNotExist()
        rule.onNodeWithTag("ReimbursementSearch_end_blur").assertDoesNotExist()
    }

    @Test fun reducedMotionAndBlurOffKeepDarkSearchUsableAndEmptyFeedbackClear() {
        show(MotionLevel.REDUCED, blur = 0f, dark = true)
        rule.onNodeWithTag("ReimbursementSearch_end_fade").assertExists()
        rule.onNodeWithTag("ReimbursementSearch_end_blur").assertDoesNotExist()
        rule.onNodeWithTag("ReimbursementSearch").performTextReplacement("沒有符合的項目")
        count(0)
        rule.onNodeWithTag("ReimbursementSearchEmpty").assertIsDisplayed()
        assertTrue(selections.isEmpty())
        rule.onNodeWithTag("ReimbursementSearch").performTextReplacement("FOOD")
        count(34)
    }

    @Test fun minimalMotionRestoresSelectedDateAndSearchWithoutSelectingATransaction() {
        val restoration = StateRestorationTester(rule)
        show(MotionLevel.MINIMAL, restoration = restoration)
        select26()
        rule.onNodeWithTag("ReimbursementSearch").performTextReplacement("Café")
        restoration.emulateSavedInstanceStateRestore()
        count(2)
        rule.onNodeWithTag("ReimbursementDateClear").assertExists()
        rule.onNodeWithTag("ReimbursementSearch").assertTextContains("Café")
        val bounds = rule.onNodeWithTag("ReimbursementSearchScrollViewport").getUnclippedBoundsInRoot()
        rule.mainClock.autoAdvance = false
        rule.onNodeWithTag("ReimbursementSearch").performTextReplacement("missing")
        rule.mainClock.advanceTimeBy(32)
        rule.onNodeWithTag("ReimbursementSearchEmpty").assertExists()
        rule.mainClock.advanceTimeBy(1000)
        assertEquals(bounds, rule.onNodeWithTag("ReimbursementSearchScrollViewport").getUnclippedBoundsInRoot())
        rule.mainClock.autoAdvance = true
        count(0)
        assertEquals(0L, selected)
    }
    @Test fun earlierReceiptOn26CanChooseLaterExpenseOn26() {
        val earlyReceipt = day.atTime(8, 0).atZone(zone).toInstant().toEpochMilli()
        show(latest = earlyReceipt)
        count(4)
        select26(); count(3)
        rule.onNodeWithTag("ReimbursementSource_11").assertIsDisplayed().performClick()
        assertEquals(listOf(11L), selections)
        assertTrue(records.first { it.id == 11L }.timestamp > earlyReceipt)
    }

    @Test fun closeFloatsOverListAndLastTransactionStaysReachable() {
        show()
        val viewport = rule.onNodeWithTag("ReimbursementSearchScrollViewport").getUnclippedBoundsInRoot()
        val close = rule.onNodeWithTag("ReimbursementSearchClose").getUnclippedBoundsInRoot()
        assertTrue("Close must float inside the list height instead of taking a footer row", close.top < viewport.bottom)
        rule.onNodeWithText(context.getString(R.string.btn_close)).assertDoesNotExist()
        rule.onNodeWithTag("ReimbursementSearchResults").performScrollToIndex(34)
        val last = rule.onNodeWithTag("ReimbursementSource_13").getUnclippedBoundsInRoot()
        assertTrue("Safe scroll padding must keep the last result above the floating action", last.bottom <= close.top)
        assertEquals(close, rule.onNodeWithTag("ReimbursementSearchClose").getUnclippedBoundsInRoot())
        rule.onNodeWithTag("ReimbursementSearchClose").performClick()
        rule.onNodeWithTag("ReimbursementSearchDialog").assertDoesNotExist()
        assertTrue(selections.isEmpty())
    }

}
