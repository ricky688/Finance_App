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

/** ARTEMIS/ADB verified: Bento 2-Card layout, Category tag cloud, Full-width Asset Card with modal bottom sheet. */
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

    private fun amount(value: String) {
        compose.onNodeWithTag("EntryAmount").performScrollTo().performTextReplacement(value)
    }
    private fun assertAmount(value: String) {
        compose.onNodeWithTag("EntryAmount").assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.StateDescription, value))
    }
    private fun mode(value: TransactionMode) {
        val tag = "TransactionMode_${value.name}"
        compose.onNodeWithTag(tag).performScrollTo().performClick()
        compose.waitForIdle()
        compose.onNodeWithTag(tag).assertIsSelected()
    }
    private fun submit() {
        compose.onNodeWithTag("EntrySubmit").assertIsDisplayed().performClick()
    }
    private fun source(id: Long) {
        compose.onNodeWithTag("EntryAccount_card").performScrollTo().performClick()
        compose.onNodeWithTag("EntryAccount_row_$id").performScrollTo().performClick()
    }
    private fun destination(id: Long) {
        compose.onNodeWithTag("EntryDestination_card").performScrollTo().performClick()
        compose.onNodeWithTag("EntryDestination_row_$id").performScrollTo().performClick()
    }
    private fun note(value: String) {
        compose.onNodeWithTag("EntryDescription").performScrollTo().performTextReplacement(value)
        androidx.test.espresso.Espresso.closeSoftKeyboard()
    }

    @Test fun sharedDraftAndCurrencySurviveAllModesAndRecreation() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent(content())
        amount("10")
        note("午餐 🍜")
        source(2)
        compose.onNodeWithTag("EntryCurrency").performClick()
        compose.onNodeWithTag("EntryCurrency_USD").performScrollTo().performClick()
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("EntryConvertedAmount").fetchSemanticsNodes().isNotEmpty() }
        mode(TransactionMode.INCOME)
        compose.onNodeWithTag("EntryBudgetSection").assertDoesNotExist()
        assertAmount("10")
        mode(TransactionMode.TRANSFER)
        compose.onNodeWithTag("EntryCategory").assertDoesNotExist()
        destination(1)
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithTag("TransactionMode_TRANSFER").assertIsSelected()
        assertAmount("10")
        compose.onNodeWithTag("EntryDescription").assertTextContains("午餐 🍜")
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
        amount("15")
        note("Salary note")
        source(2)
        mode(TransactionMode.INCOME)
        compose.onNodeWithTag("EntryDestination_card").assertDoesNotExist()
        compose.onNodeWithTag("EntryBudgetSection").assertDoesNotExist()
        submit()
        assertEquals(-15.0, submitted!!.amount, 0.0)
        assertEquals(2L, submitted!!.accountId)
        assertEquals("Salary note", submitted!!.description)
        assertNull(submitted!!.toAccountId)
    }

    @Test fun transferRequiresDestinationAndSwitchingBackRestoresExpensePrediction() {
        compose.setContent(content())
        amount("6")
        mode(TransactionMode.TRANSFER)
        compose.onNodeWithTag("EntryCategory").assertDoesNotExist()
        compose.onNodeWithTag("EntryBudgetSection").assertDoesNotExist()
        compose.onNodeWithTag("EntrySubmit").assertIsNotEnabled()
        destination(2)
        compose.onNodeWithTag("EntrySubmit").assertIsEnabled()
        source(2) // A source cannot also be the previously selected destination.
        compose.onNodeWithTag("EntrySubmit").assertIsNotEnabled()
        mode(TransactionMode.EXPENSE)
        compose.onNodeWithTag("EntryDestination_card").assertDoesNotExist()
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
        amount("5")
        mode(TransactionMode.INCOME)
        mode(TransactionMode.TRANSFER)
        compose.onNodeWithTag("EntrySubmit").assertIsEnabled()
        submit()
        assertEquals(5.0, submitted!!.amount, 0.0)
        assertEquals(1L, submitted!!.accountId)
        assertEquals(2L, submitted!!.toAccountId)
        assertTrue(submitted!!.isExcludedFromDailyBudget)
    }

    @Test fun transferModeDisplaysBentoCardsWithoutAutoOpeningSheet() {
        compose.setContent(content(TransactionMode.EXPENSE))
        compose.onNodeWithTag("EntryAccount_card").assertIsDisplayed()
        compose.onNodeWithTag("EntryDestination_card").assertDoesNotExist()

        // Switch to Transfer mode
        mode(TransactionMode.TRANSFER)

        // Verify both asset cards are visible and NO bottom sheet is auto-opened
        compose.onNodeWithTag("EntryAccount_card").assertIsDisplayed()
        compose.onNodeWithTag("EntryDestination_card").assertIsDisplayed()
        compose.onAllNodesWithTag("EntryAccount_row_1").assertCountEquals(0)
        compose.onAllNodesWithTag("EntryDestination_row_1").assertCountEquals(0)

        // Tapping card opens the bottom sheet
        compose.onNodeWithTag("EntryAccount_card").performClick()
        compose.onNodeWithTag("EntryAccount_row_1").assertIsDisplayed()

        // Selecting account updates selection and closes sheet
        compose.onNodeWithTag("EntryAccount_row_1").performClick()
        compose.onAllNodesWithTag("EntryAccount_row_1").assertCountEquals(0)
    }

    @Test fun categoryTagCloudToggleAndAutoCollapseOnSelection() {
        compose.setContent(content(TransactionMode.EXPENSE))
        // Verify initially LazyRow is visible and FlowRow tag cloud is not
        compose.onNodeWithTag("EntryCategory").assertIsDisplayed()
        compose.onNodeWithTag("EntryCategory_cloud").assertDoesNotExist()

        // Toggle to expand
        compose.onNodeWithTag("EntryCategory_toggle").performScrollTo().performClick()
        compose.onNodeWithTag("EntryCategory_cloud").assertIsDisplayed()

        // Tap a category chip inside the cloud (e.g. food / utilities)
        compose.onAllNodes(hasTestTag("EntryCategory_row_Food") or hasTestTag("EntryCategory_row_Utilities") or hasTestTag("EntryCategory_row_Transport"))
            .onFirst().performClick()

        // Auto-collapses back to single row LazyRow
        compose.onNodeWithTag("EntryCategory").assertIsDisplayed()
        compose.onNodeWithTag("EntryCategory_cloud").assertDoesNotExist()
    }
}
