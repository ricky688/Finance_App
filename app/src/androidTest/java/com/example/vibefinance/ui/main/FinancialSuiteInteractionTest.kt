package com.example.vibefinance.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vibefinance.data.BatchTransactionOperation
import com.example.vibefinance.data.entity.*
import com.example.vibefinance.ui.*
import com.example.vibefinance.ui.history.HistoryScreen
import com.example.vibefinance.ui.home.SpendsCalendar
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

/** Paths verified through ARTEMIS + ADB on Waydroid. All mutations are captured in memory. */
@RunWith(AndroidJUnit4::class)
class FinancialSuiteInteractionTest {
    @get:Rule val compose = createComposeRule()
    private val accounts = listOf(
        AccountEntity(1, "Bank", type = AccountType.BANK, balance = 500.0, icon = "bank"),
        AccountEntity(2, "Card", type = AccountType.CC, balance = 900.0, icon = "card", creditLimit = 1000.0)
    )
    private fun tx(id: Long, amount: Double = 10.0, transfer: Boolean = false) = TransactionEntity(
        id, amount, "Other", System.currentTimeMillis() - 1000, 1, toAccountId = if (transfer) 2 else null,
        description = "Fixture $id")
    private fun waitTag(tag: String) = compose.waitUntil(5_000) {
        compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
    }
    private fun selectAll() {
        compose.onNodeWithTag("HistorySelectOpen").performClick()
        waitTag("HistoryBatchToolbar")
        compose.onNodeWithTag("HistoryBatchSelectAll").performClick()
    }
    @Test fun mixedIncomeExpenseSelectionOnlyAllowsAccountAndDelete() {
        compose.setContent { HistoryScreen(FinanceUiState(isLoading = false, accounts = accounts, transactions = listOf(tx(1), tx(2, -20.0))), {}) }
        selectAll()
        compose.onNodeWithTag("HistoryBatchCategory").assertIsNotEnabled()
        compose.onNodeWithTag("HistoryBatchAccount").assertIsEnabled()
        compose.onNodeWithTag("HistoryBatchDelete").assertIsEnabled()
    }
    @Test fun transferSelectionDisablesBothEditActions() {
        compose.setContent { HistoryScreen(FinanceUiState(isLoading = false, accounts = accounts, transactions = listOf(tx(1), tx(2, transfer = true))), {}) }
        selectAll()
        compose.onNodeWithTag("HistoryBatchCategory").assertIsNotEnabled()
        compose.onNodeWithTag("HistoryBatchAccount").assertIsNotEnabled()
        compose.onNodeWithTag("HistoryBatchDelete").assertIsEnabled()
    }
    @Test fun selectionRestoresAcrossRecreation() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent { HistoryScreen(FinanceUiState(isLoading = false, accounts = accounts, transactions = listOf(tx(1))), {}) }
        selectAll()
        restoration.emulateSavedInstanceStateRestore()
        waitTag("HistoryBatchToolbar")
        compose.onNodeWithText("1 selected").assertIsDisplayed()
    }
    @Test fun failedBatchRetainsSelectionAndConfirmationWhilePreventingDuplicates() {
        val submitted = AtomicReference<FinanceIntent.BatchTransactions>()
        val submissions = AtomicInteger()
        var result by mutableStateOf<BatchTransactionResult?>(null)
        compose.setContent {
            HistoryScreen(FinanceUiState(isLoading = false, accounts = accounts, transactions = listOf(tx(1))), {
                if (it is FinanceIntent.BatchTransactions) { submitted.set(it); submissions.incrementAndGet() }
            }, batchResult = result)
        }
        selectAll()
        compose.onNodeWithTag("HistoryBatchDelete").performClick()
        waitTag("HistoryBatchConfirmDelete")
        val confirm = compose.onNodeWithTag("HistoryBatchConfirmDelete").fetchSemanticsNode()
            .config[androidx.compose.ui.semantics.SemanticsActions.OnClick].action!!
        compose.runOnIdle { confirm(); confirm() } // Two callbacks before another composition frame.
        compose.waitUntil(5_000) { submitted.get() != null }
        assertEquals(1, submissions.get())
        compose.onNodeWithTag("HistoryBatchConfirmDelete").assertIsNotEnabled()
        assertEquals(BatchTransactionOperation.Delete, submitted.get().operation)
        compose.runOnIdle { result = BatchTransactionResult(submitted.get().requestId, false, error = "Storage unavailable") }
        compose.onNodeWithText("Storage unavailable").assertIsDisplayed()
        compose.onNodeWithTag("HistoryBatchConfirmDelete").assertIsEnabled()
    }
    @Test fun successfulBatchClosesSelectionAndUsesFrozenIds() {
        val submitted = AtomicReference<FinanceIntent.BatchTransactions>()
        var result by mutableStateOf<BatchTransactionResult?>(null)
        val records = listOf(tx(1), tx(2))
        compose.setContent {
            HistoryScreen(FinanceUiState(isLoading = false, accounts = accounts, transactions = records), {
                if (it is FinanceIntent.BatchTransactions) submitted.set(it)
            }, batchResult = result)
        }
        selectAll()
        compose.onNodeWithTag("HistoryBatchAccount").performClick()
        waitTag("HistoryBatchAccount_2")
        compose.onNodeWithTag("HistoryBatchAccount_2").performClick()
        compose.waitUntil(5_000) { submitted.get() != null }
        assertEquals(records, submitted.get().expected)
        assertEquals(BatchTransactionOperation.Account(2), submitted.get().operation)
        compose.runOnIdle { result = BatchTransactionResult(submitted.get().requestId, false, success = true) }
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("HistoryBatchToolbar").fetchSemanticsNodes().isEmpty() }
    }
    @Test fun longPressEntersSelectionAndSwipesDoNotDelete() {
        val changes = AtomicInteger()
        compose.setContent {
            HistoryScreen(FinanceUiState(isLoading = false, accounts = accounts, transactions = listOf(tx(1))), { changes.incrementAndGet() })
        }
        compose.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("TransactionRow_1"))
        compose.onNodeWithTag("TransactionRow_1").performTouchInput { longClick() }
        waitTag("HistoryBatchToolbar")
        compose.onNodeWithTag("HistorySelected_1").assertIsOn()
        compose.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("TransactionRow_1"))
        compose.onNodeWithTag("TransactionRow_1").performTouchInput { swipeLeft() }
        assertEquals(0, changes.get())
        compose.onNodeWithTag("HistorySelected_1").assertIsOn()
    }
    private fun entry(mode: TransactionMode = TransactionMode.EXPENSE, destination: Long? = null, onIntent: (FinanceIntent) -> Unit) {
        compose.setContent {
            MaterialTheme {
                AddExpenseSheetContent(FinanceUiState(isLoading = false, accounts = accounts),
                    onIntent, {}, mode, emptyMap(), emptyMap(), prefilledDestinationAccountId = destination)
            }
        }
    }
    private fun key(value: String) { compose.onNodeWithTag("EntryKey_$value").assertIsDisplayed().performClick() }
    @Test fun calculatorApplyEvaluatesWithoutEqualsAndCannotSubmitTwice() {
        val submitted = AtomicReference<FinanceIntent.AddTransaction>()
        val count = AtomicInteger()
        entry { if (it is FinanceIntent.AddTransaction) { submitted.set(it); count.incrementAndGet() } }
        key("1"); key("2"); key("0"); key("+"); key("4"); key("5")
        compose.onNodeWithTag("EntryCalculatorResult").assertTextContains("165.00", substring = true)
        compose.onNodeWithTag("EntrySubmit").assertIsDisplayed().performClick()
        assertEquals(165.0, submitted.get().amount, 0.0)
        compose.onNodeWithTag("EntrySubmit").assertIsNotEnabled()
        assertEquals(1, count.get())
    }
    @Test fun calculatorRejectsIncompleteAndNonpositiveResultsThenClearsOnHold() {
        entry {}
        key("1"); key("-")
        compose.onNodeWithTag("EntrySubmit").assertIsNotEnabled()
        key("2"); key("=")
        compose.onNodeWithTag("EntrySubmit").assertIsNotEnabled()
        compose.onNodeWithTag("EntryKey_back").performTouchInput { longClick() }
        key("5")
        compose.onNodeWithTag("EntrySubmit").assertIsEnabled()
    }
    @Test fun incomeKeepsNegativeStorageSign() {
        val submitted = AtomicReference<FinanceIntent.AddTransaction>()
        entry(TransactionMode.INCOME) { if (it is FinanceIntent.AddTransaction) submitted.set(it) }
        key("5"); key("+"); key("2")
        compose.onNodeWithTag("EntrySubmit").performClick()
        assertEquals(-7.0, submitted.get().amount, 0.0)
    }
    @Test fun transferUsesPrefilledCardWithoutAutoSubmitting() {
        val submitted = AtomicReference<FinanceIntent.AddTransaction>()
        entry(TransactionMode.TRANSFER, 2) { if (it is FinanceIntent.AddTransaction) submitted.set(it) }
        assertNull(submitted.get())
        key("5")
        compose.onNodeWithTag("EntrySubmit").assertIsDisplayed().performClick()
        assertEquals(1L, submitted.get().accountId)
        assertEquals(2L, submitted.get().toAccountId)
    }
    @Test fun freshlyPostedCardExpenseAppearsWithoutWaitingForMinuteTick() {
        var records by mutableStateOf<List<TransactionEntity>>(emptyList())
        compose.setContent { MaterialTheme { com.example.vibefinance.ui.accounts.CreditCardCycleBadges(accounts[1], records) } }
        compose.waitForIdle()
        compose.runOnIdle { records = listOf(tx(4, 25.0).copy(accountId = 2, timestamp = System.currentTimeMillis())) }
        compose.onNodeWithText("Cycle spent: HK$25.00").assertIsDisplayed()
    }
    @Test fun calendarWorksWithoutBudgetAndPayActionReturnsCardIdOnly() {
        val today = LocalDate.now()
        val card = accounts[1].copy(paymentDeadline = today.dayOfMonth, billingDate = today.minusDays(5).dayOfMonth)
        val paid = AtomicReference<Long>()
        compose.setContent { MaterialTheme { SpendsCalendar(
            FinanceUiState(isLoading = false, accounts = listOf(accounts[0], card)), onPayCard = { paid.set(it) }) } }
        compose.onNodeWithTag("CalendarDay_$today").performClick()
        waitTag("CalendarReminder_2")
        compose.onNodeWithTag("CalendarReminder_2").assertIsDisplayed()
        compose.onNodeWithTag("CalendarPay_2").performScrollTo().performClick()
        assertEquals(2L, paid.get())
        assertEquals(900.0, card.balance, 0.0)
    }
}
