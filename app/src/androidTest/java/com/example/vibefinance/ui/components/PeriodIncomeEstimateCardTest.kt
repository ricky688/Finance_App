package com.example.vibefinance.ui.components

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.data.repository.DailyBudgetInfo
import com.example.vibefinance.ui.FinanceIntent
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.IncomeEstimateResult
import com.example.vibefinance.ui.accounts.AccountsScreen
import com.example.vibefinance.ui.recurring.RecurringScreen
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Assets/Recurring and edit -> keyboard -> cancel explored with ARTEMIS before these tests.
 * All save results and data mutations remain in the fixture; no device ledger/preferences change.
 */
@RunWith(AndroidJUnit4::class)
class PeriodIncomeEstimateCardTest {
    @get:Rule val rule = createComposeRule()
    private fun day(date: String) = LocalDate.parse(date).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private val period = DailyBudgetInfo(900.0, 0.0, 900.0, 30.0, 30.0, 31, day("2026-08-20"), day("2026-09-19"))
    private var state by mutableStateOf(FinanceUiState(isLoading = false, budgetInfo = period,
        accounts = listOf(AccountEntity(id = 1, name = "Bank", type = AccountType.BANK, balance = 500.0, icon = "bank"))))
    private var recurring by mutableStateOf(false)
    private val requests = mutableListOf<FinanceIntent.SetPeriodIncomeEstimate>()
    private val onIntent: (FinanceIntent) -> Unit = { intent ->
        if (intent is FinanceIntent.SetPeriodIncomeEstimate) {
            requests += intent
            state = state.copy(isSavingIncomeEstimate = true, incomeEstimateResult = null)
        }
    }
    private fun show(pages: Boolean = false, restoration: StateRestorationTester? = null, wide: Boolean = false) {
        val content: @Composable () -> Unit = {
            val configuration = Configuration(LocalConfiguration.current).apply { setLocale(Locale.ENGLISH) }
            val context = LocalContext.current.createConfigurationContext(configuration)
            val density = LocalDensity.current
            CompositionLocalProvider(LocalConfiguration provides configuration, LocalContext provides context,
                LocalDensity provides if (wide) Density(density.density * 0.5f, 1.7f) else density) {
                MaterialTheme {
                    if (!pages) PeriodIncomeEstimateCard(state, onIntent)
                    else if (recurring) RecurringScreen(state, onIntent)
                    else AccountsScreen(state, onIntent)
                }
            }
        }
        if (restoration == null) rule.setContent(content) else restoration.setContent(content)
        rule.waitForIdle()
    }
    private fun open(pages: Boolean = false) {
        if (pages) rule.onNodeWithTag("EditPeriodIncomeEstimate").performScrollTo()
        rule.onNodeWithTag("EditPeriodIncomeEstimate").performClick()
        rule.onNodeWithTag("IncomeEstimateInput").assertIsDisplayed()
    }
    private fun finish(saved: Boolean) {
        rule.runOnIdle {
            val request = requests.last()
            state = state.copy(isSavingIncomeEstimate = false,
                budgetInfo = if (saved) state.budgetInfo!!.copy(estimatedIncome = request.amount) else state.budgetInfo,
                incomeEstimateResult = IncomeEstimateResult(request.requestId, saved))
        }
        rule.waitForIdle()
    }

    @Test fun assetsAndRecurringShareTheDailyPeriodAndCanClearTheEstimate() {
        show(pages = true); open(pages = true)
        rule.onNodeWithTag("IncomeEstimateInput").performTextInput("12345.67")
        rule.onNodeWithTag("SaveIncomeEstimate").performClick()
        assertEquals(period.startDate, requests.single().startDate); assertEquals(period.endDate, requests.single().endDate)
        rule.onNodeWithTag("SaveIncomeEstimate").assertIsNotEnabled()
        rule.onNodeWithTag("SaveIncomeEstimate").performTouchInput { click() }
        assertEquals(1, requests.size)
        finish(true)
        rule.onNodeWithTag("PeriodIncomeEstimateAmount").assertTextEquals("HK$ 12,345.67")
        rule.runOnIdle { recurring = true }
        rule.onNodeWithTag("PeriodIncomeEstimateCard").performScrollTo()
        rule.onNodeWithTag("PeriodIncomeEstimateAmount").assertTextEquals("HK$ 12,345.67")
        rule.onNodeWithTag("IncomeEstimatePeriod").assertTextEquals("Aug 20, 2026 – Sep 19, 2026")
        open(pages = true)
        rule.onNodeWithTag("IncomeEstimateInput").performTextClearance()
        rule.onNodeWithTag("SaveIncomeEstimate").performClick()
        assertNull(requests.last().amount)
        finish(true)
        rule.onNodeWithTag("PeriodIncomeEstimateAmount").assertTextEquals("Not set")
        assertEquals(500.0, state.accounts.single().balance, 0.0)
        assertEquals(900.0, state.budgetInfo!!.totalMonthlyBudget, 0.0)
        assertTrue(state.transactions.isEmpty())
    }
    @Test fun invalidInputIsBlockedAndFailureRetainsDraftForRetry() {
        show(); open()
        listOf("-1", "0.001", "NaN").forEach {
            rule.onNodeWithTag("IncomeEstimateInput").performTextReplacement(it)
            rule.onNodeWithTag("SaveIncomeEstimate").assertIsNotEnabled()
        }
        rule.onNodeWithTag("IncomeEstimateInput").performTextReplacement("1000.25")
        rule.onNodeWithTag("SaveIncomeEstimate").performClick()
        finish(false)
        rule.onNodeWithTag("IncomeEstimateInput").assertTextContains("1000.25")
        rule.onNodeWithTag("SaveIncomeEstimate").assertIsEnabled().performClick()
        assertEquals(2, requests.size); assertNotEquals(requests.first().requestId, requests.last().requestId)
        finish(true)
        rule.onNodeWithTag("PeriodIncomeEstimateDialog").assertDoesNotExist()
        rule.onNodeWithTag("PeriodIncomeEstimateAmount").assertTextEquals("HK$ 1,000.25")
    }
    @Test fun noBudgetDisablesEditingAndZeroIsDistinctFromUnset() {
        state = state.copy(budgetInfo = null)
        show()
        rule.onNodeWithTag("EditPeriodIncomeEstimate").assertDoesNotExist()
        rule.runOnIdle { state = state.copy(budgetInfo = period.copy(estimatedIncome = 0.0)) }
        rule.onNodeWithTag("PeriodIncomeEstimateAmount").assertTextEquals("HK$ 0.00")
        open(); rule.onNodeWithTag("IncomeEstimateInput").performTextReplacement("900")
        rule.onNodeWithTag("CancelIncomeEstimate").performClick()
        assertTrue(requests.isEmpty())
        rule.onNodeWithTag("PeriodIncomeEstimateAmount").assertTextEquals("HK$ 0.00")
    }
    @Test fun changingDailyPeriodDismissesOldDraftAndStartsUnset() {
        show(); open()
        rule.onNodeWithTag("IncomeEstimateInput").performTextInput("9000")
        rule.runOnIdle { state = state.copy(budgetInfo = period.copy(startDate = day("2026-09-20"), endDate = day("2026-10-19"))) }
        rule.onNodeWithTag("PeriodIncomeEstimateDialog").assertDoesNotExist()
        rule.onNodeWithTag("IncomeEstimatePeriod").assertTextEquals("Sep 20, 2026 – Oct 19, 2026")
        open()
        assertEquals("", rule.onNodeWithTag("IncomeEstimateInput").fetchSemanticsNode().config[SemanticsProperties.EditableText].text)
        assertTrue(requests.isEmpty())
    }
    @Test fun draftAndPendingSaveSurviveRestorationWithoutDuplicateSubmission() {
        val restoration = StateRestorationTester(rule)
        show(restoration = restoration); open()
        rule.onNodeWithTag("IncomeEstimateInput").performTextInput("123.45")
        restoration.emulateSavedInstanceStateRestore()
        rule.onNodeWithTag("IncomeEstimateInput").assertTextContains("123.45")
        rule.onNodeWithTag("SaveIncomeEstimate").performClick()
        restoration.emulateSavedInstanceStateRestore()
        rule.onNodeWithTag("SaveIncomeEstimate").assertIsNotEnabled()
        assertEquals(1, requests.size)
        finish(true)
        rule.onNodeWithTag("PeriodIncomeEstimateDialog").assertDoesNotExist()
    }
    @Test fun wideRecurringLayoutWithLargeFontsKeepsEstimateReachable() {
        recurring = true; state = state.copy(budgetInfo = period.copy(estimatedIncome = 8000.0))
        show(pages = true, wide = true)
        rule.onNodeWithTag("EditPeriodIncomeEstimate").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("PeriodIncomeEstimateAmount").assertTextEquals("HK$ 8,000.00")
        open(pages = true)
        rule.onNodeWithTag("CancelIncomeEstimate").performClick()
        assertTrue(requests.isEmpty())
    }
    @Test fun restoredDraftWithNoActiveOperationCanRetry() {
        val restoration = StateRestorationTester(rule)
        show(restoration = restoration); open()
        rule.onNodeWithTag("IncomeEstimateInput").performTextInput("750.25")
        rule.onNodeWithTag("SaveIncomeEstimate").performClick()
        rule.runOnIdle { state = state.copy(isSavingIncomeEstimate = false,
            incomeEstimateResult = IncomeEstimateResult("earlier-request", true)) }
        restoration.emulateSavedInstanceStateRestore()
        rule.onNodeWithTag("IncomeEstimateInput").assertTextContains("750.25")
        rule.onNodeWithTag("SaveIncomeEstimate").assertIsEnabled().performClick()
        assertEquals(2, requests.size)
        finish(true)
        rule.onNodeWithTag("PeriodIncomeEstimateDialog").assertDoesNotExist()
    }
}
