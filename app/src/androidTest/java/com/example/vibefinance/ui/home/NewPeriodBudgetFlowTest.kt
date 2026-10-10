package com.example.vibefinance.ui.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vibefinance.R
import com.example.vibefinance.data.repository.DailyBudgetInfo
import com.example.vibefinance.ui.main.BudgetSheetDestination
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Daily Set budget -> budget editor -> new-period form and Back explored with ARTEMIS/ADB.
 * These fixtures exercise the actual entry card and setup form without writing the user's ledger.
 */
@RunWith(AndroidJUnit4::class)
class NewPeriodBudgetFlowTest {
    @get:Rule val rule = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private var destination by mutableStateOf(BudgetSheetDestination.NONE)
    private var budget by mutableStateOf(EMPTY)
    private var saves = 0
    private var dismissals = 0

    private fun show() {
        rule.setContent {
            // The active budget's decorative wave continuously recomposes; keep this
            // fixture focused on navigation without changing Android animation settings.
            CompositionLocalProvider(LocalAmbientMotionEnabled provides false) {
            MaterialTheme {
                HeroDailyBudgetCard(budgetInfo = budget, onOpenRecalcSheet = {},
                    onOpenBudgetDialog = { destination = BudgetSheetDestination.fromDaily(budget.totalMonthlyBudget) })
                if (destination == BudgetSheetDestination.NEW_PERIOD) {
                    NewPeriodBudgetSheet(budgetInfo = budget, onConfirmNewPeriod = { amount, start, end ->
                        saves++
                        budget = budget.copy(totalMonthlyBudget = amount, monthlyRemaining = amount,
                            dailyAllowance = amount / 30, dailyRemaining = amount / 30, daysLeft = 30,
                            startDate = start, endDate = end)
                        destination = BudgetSheetDestination.NONE
                    }, onDismiss = {
                        dismissals++
                        destination = destination.dismissNewPeriod()
                    })
                }
            }
            }
        }
        rule.waitForIdle()
    }

    private fun openSetup() {
        rule.onNodeWithText(context.getString(R.string.btn_set_budget)).performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().isNotEmpty() }
        assertEquals(BudgetSheetDestination.NEW_PERIOD, destination)
        assertFalse(destination.showsSettings)
    }

    @Test fun settingFirstBudget_savesOnce_andReturnsToDailyWithoutOpeningAnEditor() {
        show()
        openSetup()
        rule.onNode(hasSetTextAction()).performTextReplacement("2300")
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        rule.waitForIdle()
        rule.onNodeWithTag("NewPeriodBudgetList").performScrollToNode(hasTestTag("NewPeriodBudgetConfirm"))
        rule.onNodeWithTag("NewPeriodBudgetConfirm").performClick()
        rule.waitUntil(5_000) { destination == BudgetSheetDestination.NONE }
        rule.waitForIdle()
        assertEquals(1, saves)
        assertEquals("Saving must not also call the cancellation callback", 0, dismissals)
        assertEquals(2300.0, budget.totalMonthlyBudget, 0.0)
        assertTrue(budget.endDate > budget.startDate)
        assertFalse(destination.showsSettings)
        rule.onAllNodes(hasSetTextAction()).assertCountEquals(0)
        // A later deliberate edit still routes to the existing-period editor.
        assertEquals(BudgetSheetDestination.EDIT_PERIOD, BudgetSheetDestination.fromDaily(budget.totalMonthlyBudget))
    }

    @Test fun cancellingFirstBudget_preservesNoBudget_andCanOpenSetupAgain() {
        show()
        openSetup()
        rule.onNode(hasSetTextAction()).performTextReplacement("2300")
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        rule.waitForIdle()
        androidx.test.espresso.Espresso.pressBack()
        rule.waitUntil(5_000) { destination == BudgetSheetDestination.NONE }
        rule.waitForIdle()
        assertEquals(0, saves)
        assertEquals(1, dismissals)
        assertEquals(EMPTY, budget)
        openSetup()
        rule.onNode(hasSetTextAction()).assertTextContains("1500")
    }

    private companion object {
        val EMPTY = DailyBudgetInfo(0.0, 0.0, 0.0, 0.0, 0.0, 0, 0L, 0L)
    }
}
