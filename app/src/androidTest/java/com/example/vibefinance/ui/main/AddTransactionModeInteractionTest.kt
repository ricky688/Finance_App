package com.example.vibefinance.ui.main

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vibefinance.R
import com.example.vibefinance.data.currency.*
import com.example.vibefinance.data.entity.*
import com.example.vibefinance.data.repository.DailyBudgetInfo
import com.example.vibefinance.ui.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** ARTEMIS/ADB verified: switch modes, retain note/expression, select destination, submit to memory. */
@RunWith(AndroidJUnit4::class)
class AddTransactionModeInteractionTest {
    @get:Rule val compose = createComposeRule()
    private val accounts = listOf(
        AccountEntity(1, "Bank", type = AccountType.BANK, balance = 1000.0, icon = "bank"),
        AccountEntity(2, "Card", type = AccountType.CC, balance = 0.0, icon = "card"))
    private var submitted: FinanceIntent.AddTransaction? = null
    private var dismissed = false
    private val loader = ExchangeRateLoader { currency, _ ->
        ExchangeRateResult(ExchangeRateQuote(currency, 8.2, "2026-10-12", 0))
    }
    private fun content(mode: TransactionMode = TransactionMode.EXPENSE, prefill: Long? = null): @Composable () -> Unit = {
        MaterialTheme {
            TransactionEntryHost(true, FinanceUiState(isLoading = false, accounts = accounts,
                budgetInfo = DailyBudgetInfo(1000.0, 200.0, 800.0, 50.0, 50.0, 20, 0, 0)),
                { if (it is FinanceIntent.AddTransaction) submitted = it }, { dismissed = true }, mode,
                emptyMap(), emptyMap(), exchangeRateLoader = loader, prefilledDestinationAccountId = prefill)
        }
    }
    private fun key(value: String) { compose.onNodeWithTag("EntryKey_$value").assertIsDisplayed().performClick() }
    private fun mode(value: TransactionMode) {
        val tag = "TransactionMode_${value.name}"
        compose.onNodeWithTag(tag).performScrollTo().performClick()
        compose.waitForIdle()
        compose.onNodeWithTag(tag).assertIsSelected()
    }
    private fun submit() { compose.onNodeWithTag("EntrySubmit").assertIsDisplayed().performClick() }
    private fun amount(expression: String) { compose.onNodeWithTag("EntryAmount").assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.StateDescription, expression)) }
    private fun source(id: Long) { compose.onNodeWithTag("EntryAccount_row_$id").performScrollTo().performClick() }
    private fun destination(id: Long) { compose.onNodeWithTag("EntryDestination_row_$id").performScrollTo().performClick() }
    private fun note(value: String) {
        compose.onNodeWithTag("EntryDescription").performScrollTo().performTextReplacement(value)
        androidx.test.espresso.Espresso.closeSoftKeyboard()
    }

    @Test fun sharedDraftAndCurrencySurviveAllModesAndRecreation() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent(content())
        key("6"); key("+")
        note("午餐 🍜")
        source(2)
        compose.onNodeWithTag("EntryCurrency").performClick()
        compose.onNodeWithTag("EntryCurrency_USD").performScrollTo().performClick()
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("EntryConvertedAmount").fetchSemanticsNodes().isNotEmpty() }
        mode(TransactionMode.INCOME)
        compose.onNodeWithTag("EntryBudgetSection").assertDoesNotExist()
        amount("6+")
        mode(TransactionMode.TRANSFER)
        compose.onNodeWithTag("EntryCategory").assertDoesNotExist()
        destination(1)
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithTag("TransactionMode_TRANSFER").assertIsSelected()
        amount("6+")
        compose.onNodeWithTag("EntryDescription").assertTextContains("午餐 🍜")
        key("4")
        submit()
        assertEquals(82.0, submitted!!.amount, 0.001)
        assertEquals(2L, submitted!!.accountId)
        assertEquals(1L, submitted!!.toAccountId)
        assertTrue(submitted!!.description.contains("午餐 🍜"))
        assertTrue(submitted!!.description.contains("USD"))
        assertTrue(dismissed)
    }

    @Test fun incomeSubmissionUsesSelectedModeAndPreservesNoteAndSource() {
        compose.setContent(content())
        key("1"); key("2"); key("+"); key("3")
        note("Salary note")
        source(2)
        mode(TransactionMode.INCOME)
        compose.onNodeWithTag("EntryDestination").assertDoesNotExist()
        compose.onNodeWithTag("EntryBudgetSection").assertDoesNotExist()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose.onNodeWithTag("EntryModeTitle").assertTextEquals(context.getString(R.string.quick_add_income))
        submit()
        assertEquals(-15.0, submitted!!.amount, 0.0)
        assertEquals(2L, submitted!!.accountId)
        assertEquals("Salary note", submitted!!.description)
        assertNull(submitted!!.toAccountId)
    }

    @Test fun transferRequiresDestinationAndSwitchingBackRestoresExpensePrediction() {
        compose.setContent(content())
        key("6")
        mode(TransactionMode.TRANSFER)
        compose.onNodeWithTag("EntryCategory").assertDoesNotExist()
        compose.onNodeWithTag("EntryBudgetSection").assertDoesNotExist()
        compose.onNodeWithTag("EntrySubmit").assertIsNotEnabled()
        destination(2)
        compose.onNodeWithTag("EntrySubmit").assertIsEnabled()
        source(2) // A source cannot also be the previously selected destination.
        compose.onNodeWithTag("EntrySubmit").assertIsNotEnabled()
        mode(TransactionMode.EXPENSE)
        compose.onNodeWithTag("EntryDestination").assertDoesNotExist()
        compose.onNodeWithTag("EntryBudgetSection").assertExists()
        compose.onNodeWithTag("EntryCategory").assertExists()
        compose.onNodeWithTag("EntrySubmit").assertIsEnabled()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose.onNodeWithText(context.getString(R.string.ui_main_budget_preview_remaining, "44")).performScrollTo().assertIsDisplayed()
        submit()
        assertEquals(6.0, submitted!!.amount, 0.0)
        assertEquals(2L, submitted!!.accountId)
        assertNull(submitted!!.toAccountId)
    }

    @Test fun calendarPrefilledTransferRetainsDestinationWhenSwitchingModes() {
        compose.setContent(content(TransactionMode.TRANSFER, 2))
        compose.onNodeWithTag("TransactionMode_TRANSFER").assertIsSelected()
        assertNull(submitted)
        key("5")
        mode(TransactionMode.INCOME)
        mode(TransactionMode.TRANSFER)
        compose.onNodeWithTag("EntrySubmit").assertIsEnabled()
        submit()
        assertEquals(5.0, submitted!!.amount, 0.0)
        assertEquals(1L, submitted!!.accountId)
        assertEquals(2L, submitted!!.toAccountId)
        assertTrue(submitted!!.isExcludedFromDailyBudget)
    }
}
