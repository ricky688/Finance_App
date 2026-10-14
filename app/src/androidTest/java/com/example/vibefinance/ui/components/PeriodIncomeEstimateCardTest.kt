package com.example.vibefinance.ui.components

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.data.repository.DailyBudgetInfo
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.accounts.AccountsScreen
import com.example.vibefinance.ui.recurring.RecurringScreen
import com.example.vibefinance.ui.preferences.*
import com.example.vibefinance.util.PeriodIncomeForecaster
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Assets/Recurring cards explored through ARTEMIS and ADB before revision; memory-only fixtures. */
@RunWith(AndroidJUnit4::class)
class PeriodIncomeEstimateCardTest {
    @get:Rule val rule = createComposeRule()
    private val zone = ZoneId.systemDefault()
    private fun day(date: String) = LocalDate.parse(date).atStartOfDay(zone).toInstant().toEpochMilli()
    private val period = DailyBudgetInfo(900.0, 0.0, 900.0, 30.0, 30.0, 31, day("2026-10-01"), day("2026-10-31"))
    private fun tx(date: String, amount: Double) = TransactionEntity(amount = amount, category = "Salary",
        timestamp = day(date), accountId = 1)
    private val records = listOf(tx("2026-06-01", 10.0), tx("2026-07-15", -1000.0),
        tx("2026-08-15", -1000.0), tx("2026-09-15", -1000.0))
    private fun withForecast(records: List<TransactionEntity>, period: DailyBudgetInfo = this.period): FinanceUiState {
        val forecast = PeriodIncomeForecaster.estimate(period.startDate, period.endDate, records, LocalDate.parse("2026-10-14"), zone)
        return FinanceUiState(isLoading = false, transactions = records, budgetInfo = period.copy(
            estimatedIncome = forecast?.amount, incomeEstimatePeriods = forecast?.periodsUsed ?: 0,
            incomeEstimatePartialHistory = forecast?.partialHistory ?: false), accounts = listOf(
            AccountEntity(id = 1, name = "Bank", type = AccountType.BANK, balance = 500.0, icon = "bank")))
    }
    private var state by mutableStateOf(withForecast(records))
    private var recurring by mutableStateOf(false)
    private var hideAmounts by mutableStateOf(false)
    private fun show(pages: Boolean = false, wide: Boolean = false, restoration: StateRestorationTester? = null) {
        val content: @Composable () -> Unit = {
            val configuration = Configuration(LocalConfiguration.current).apply { setLocale(Locale.ENGLISH) }
            val context = LocalContext.current.createConfigurationContext(configuration)
            val density = LocalDensity.current
            CompositionLocalProvider(LocalConfiguration provides configuration, LocalContext provides context,
                LocalExperience provides ExperiencePreferences(hideAmounts = hideAmounts),
                LocalDensity provides if (wide) Density(density.density * .5f, 1.7f) else density) {
                MaterialTheme {
                    if (!pages) PeriodIncomeEstimateCard(state)
                    else if (recurring) RecurringScreen(state, {})
                    else AccountsScreen(state, {})
                }
            }
        }
        if (restoration == null) rule.setContent(content) else restoration.setContent(content)
        rule.waitForIdle()
    }
    private fun noManualEditor() {
        rule.onNodeWithTag("EditPeriodIncomeEstimate").assertDoesNotExist()
        rule.onNodeWithTag("PeriodIncomeEstimateDialog").assertDoesNotExist()
    }
    @Test fun assetsAndRecurringShowTheSameLearnedEstimateAndDailyDates_withoutMutation() {
        show(pages = true)
        rule.onNodeWithTag("PeriodIncomeEstimateCard").performScrollTo()
        rule.onNodeWithTag("PeriodIncomeEstimateAmount").assertTextEquals("HK$ 1,000.00")
        noManualEditor()
        rule.runOnIdle { recurring = true }
        rule.onNodeWithTag("PeriodIncomeEstimateCard").performScrollTo()
        rule.onNodeWithTag("PeriodIncomeEstimateAmount").assertTextEquals("HK$ 1,000.00")
        rule.onNodeWithTag("IncomeEstimatePeriod").assertTextEquals("Oct 1, 2026 – Oct 31, 2026")
        rule.onNodeWithTag("IncomeEstimateBasis").assertTextEquals("Based on 3 previous periods with recorded history.")
        noManualEditor()
        assertEquals(records, state.transactions); assertEquals(500.0, state.accounts.single().balance, 0.0)
        assertEquals(900.0, state.budgetInfo!!.totalMonthlyBudget, 0.0)
    }
    @Test fun noHistoryIsDifferentFromObservedZeroIncome_andNoBudgetExplainsSetup() {
        state = withForecast(emptyList()); show()
        rule.onNodeWithTag("PeriodIncomeEstimateAmount").assertTextEquals("Not enough history yet")
        rule.runOnIdle { state = withForecast(listOf(tx("2026-09-01", 50.0))) }
        rule.onNodeWithTag("PeriodIncomeEstimateAmount").assertTextEquals("HK$ 0.00")
        rule.runOnIdle { state = state.copy(budgetInfo = null) }
        rule.onNodeWithText("Set a budget period on Daily to see an automatic estimate.").assertExists()
        noManualEditor()
    }
    @Test fun changingRecordedIncomeAndDailyPeriodRecomputesAutomatically() {
        show()
        rule.runOnIdle { state = withForecast(records.dropLast(1) + tx("2026-09-15", -3000.0)) }
        rule.onNodeWithTag("PeriodIncomeEstimateAmount").assertTextEquals("HK$ 2,000.00")
        rule.runOnIdle { state = withForecast(records, period.copy(startDate = day("2026-12-01"), endDate = day("2026-12-31"))) }
        rule.onNodeWithTag("IncomeEstimatePeriod").assertTextEquals("Dec 1, 2026 – Dec 31, 2026")
        rule.onNodeWithTag("PeriodIncomeEstimateAmount").assertTextEquals("HK$ 1,000.00")
        noManualEditor()
    }
    @Test fun partialHistoryIsExplained_andAmountsRemainPrivate() {
        state = withForecast(listOf(tx("2026-09-15", -1200.25))); show()
        rule.onNodeWithText("History starts partway through a sampled period. The estimate may be less accurate.").assertExists()
        rule.onNodeWithTag("PeriodIncomeEstimateAmount").assertTextEquals("HK$ 1,200.25")
        rule.runOnIdle { hideAmounts = true }
        rule.onNodeWithTag("PeriodIncomeEstimateAmount").assertTextEquals("••••")
        rule.onNodeWithText("HK$ 1,200.25", useUnmergedTree = true).assertDoesNotExist()
    }
    @Test fun wideRecurringLargeFontAndRestorationKeepAutomaticEstimateReachable() {
        recurring = true
        val restoration = StateRestorationTester(rule)
        show(pages = true, wide = true, restoration = restoration)
        rule.onNodeWithTag("PeriodIncomeEstimateCard").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("PeriodIncomeEstimateAmount").assertTextEquals("HK$ 1,000.00")
        restoration.emulateSavedInstanceStateRestore()
        rule.onNodeWithTag("PeriodIncomeEstimateCard").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("PeriodIncomeEstimateAmount").assertTextEquals("HK$ 1,000.00")
        noManualEditor()
    }
}
