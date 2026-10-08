package com.example.vibefinance.ui.history

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import com.example.vibefinance.ui.FinanceUiState
import com.example.vibefinance.ui.FinanceIntent
import kotlinx.coroutines.launch
import com.example.vibefinance.R
import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.data.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

/** Opt-in live-data check. ARTEMIS verified this path on Waydroid with the user's import.
 * Run with -e importedTransfer true; every edited record is restored in finally. */
@RunWith(AndroidJUnit4::class)
class ImportedTransactionTransferTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun current(id: Long) = InMemoryDatabase.transactions.value.single { it.id == id }
    private fun expenses() = InMemoryDatabase.transactions.value.filter {
        it.amount > 0 && it.toAccountId == null && !it.isBalanceAdjustment
    }.sumOf { it.amount }

    @Test fun importedExpensesAndIncomeConvertPersistAndRestoreWithoutLosingData() {
        assumeTrue("Explicit authorization is required to exercise imported device data",
            InstrumentationRegistry.getArguments().getString("importedTransfer") == "true")
        InMemoryDatabase.initialize(context)
        assertEquals(868, InMemoryDatabase.transactions.value.size)
        rule.setContent { ImportedHistory() }
        val originalTransactions = InMemoryDatabase.transactions.value
        val originalAccounts = InMemoryDatabase.accounts.value
        assertEquals(14, originalAccounts.size)
        assertEquals(52831.49, expenses(), 1e-6)
        val cash = originalAccounts.single { it.name.equals("Cash", true) }
        val bank = originalAccounts.single { it.name.equals("Mox", true) }
        val credit = originalAccounts.single { it.name.equals("AEON card", true) }
        val recent = originalTransactions.sortedByDescending { it.timestamp }
        val expense = recent.first { it.amount > 0 && it.toAccountId == null && !it.isBalanceAdjustment }
        val income = recent.first { it.amount < 0 && it.toAccountId == null && !it.isBalanceAdjustment }
        val originals = listOf(expense, income)
        try {
            for (original in originals) {
                openRow(original.id)
                rule.onNodeWithTag("EditTransactionTransfer").performClick().assertIsSelected()
                rule.onNodeWithTag("EditTransactionSave").assertIsNotEnabled()
                rule.onNodeWithTag("EditTransactionCategory").assertDoesNotExist()
                rule.onNodeWithTag("EditTransactionDailyBudget").assertDoesNotExist()
                val source = if (original.amount > 0) bank else cash
                val destination = if (original.amount > 0) cash else credit
                chooseAccount("EditTransactionSource", source.id)
                chooseAccount("EditTransactionDestination", destination.id)
                // Selecting the destination as the source must clear the destination and disable Save.
                chooseAccount("EditTransactionSource", destination.id)
                rule.onNodeWithTag("EditTransactionSave").assertIsNotEnabled()
                chooseAccount("EditTransactionSource", source.id)
                chooseAccount("EditTransactionDestination", destination.id)
                rule.onNodeWithText(context.getString(R.string.btn_cancel)).performClick()
                rule.waitUntil(5_000) { rule.onAllNodes(hasTestTag("EditTransactionSave")).fetchSemanticsNodes().isEmpty() }
                assertEquals(original, current(original.id))
                assertEquals(originalAccounts, InMemoryDatabase.accounts.value)

                openRow(original.id)
                rule.onNodeWithTag("EditTransactionTransfer").performClick()
                chooseAccount("EditTransactionSource", source.id)
                chooseAccount("EditTransactionDestination", destination.id)
                rule.onNodeWithTag("EditTransactionSave").assertIsEnabled().performClick()
                rule.waitUntil(10_000) { current(original.id).toAccountId == destination.id }
                val stored = current(original.id)
                assertEquals(868, InMemoryDatabase.transactions.value.size)
                assertEquals(original.timestamp, stored.timestamp); assertEquals(original.description, stored.description)
                assertEquals(abs(original.amount), stored.amount, 0.0)
                assertEquals("Transfer", stored.category); assertTrue(stored.isExcludedFromDailyBudget)
                assertEquals(source.id, stored.accountId)
                assertEquals(source.type == AccountType.CC, stored.sourceWasCreditCard)
                assertEquals(destination.type == AccountType.CC, stored.destinationWasCreditCard)
                originalAccounts.forEach { account ->
                    var expected = account.balance
                    if (account.id == original.accountId) expected -= original.amount *
                        if (original.sourceWasCreditCard == true) 1.0 else -1.0
                    if (account.id == source.id) expected += abs(original.amount) *
                        if (source.type == AccountType.CC) 1.0 else -1.0
                    if (account.id == destination.id) expected += abs(original.amount) *
                        if (destination.type == AccountType.CC) -1.0 else 1.0
                    assertEquals(account.name, expected, InMemoryDatabase.accounts.value.single { it.id == account.id }.balance, 1e-6)
                }
                assertEquals(52831.49 - if (original.amount > 0) original.amount else 0.0, expenses(), 1e-6)
                // Reload the persisted data and recreate the editor host; keep the live database intact.
                runBlocking(Dispatchers.IO) { InMemoryDatabase.reloadAfterFullRestore(context) }
                assertEquals(stored, current(original.id))
                rule.activityRule.scenario.recreate()
                rule.activityRule.scenario.onActivity { it.setContent { ImportedHistory() } }
                openRow(original.id)
                rule.onNodeWithTag("EditTransactionTransfer").assertIsSelected()
                rule.onNodeWithTag("EditTransactionSource").assertTextContains(source.name)
                rule.onNodeWithTag("EditTransactionDestination").assertTextContains(destination.name)
                rule.onNodeWithText(context.getString(R.string.btn_cancel)).performClick()
                runBlocking(Dispatchers.IO) { TransactionRepository().updateTransaction(original, current(original.id)) }
                rule.waitUntil(5_000) { current(original.id) == original }
            }
        } finally {
            runBlocking(Dispatchers.IO) {
                originals.forEach { original ->
                    val stored = current(original.id)
                    if (stored != original) TransactionRepository().updateTransaction(original, stored)
                }
                InMemoryDatabase.reloadAfterFullRestore(context)
            }
        }
        assertEquals(originalTransactions, InMemoryDatabase.transactions.value)
        originalAccounts.forEach { account ->
            val stored = InMemoryDatabase.accounts.value.single { it.id == account.id }
            assertEquals(account.balance, stored.balance, 1e-6)
            assertEquals(account.copy(balance = stored.balance), stored)
        }
        assertEquals(52831.49, expenses(), 1e-6)
    }
    @Composable private fun ImportedHistory() {
        val transactions by InMemoryDatabase.transactions.collectAsState()
        val accounts by InMemoryDatabase.accounts.collectAsState()
        val rules by InMemoryDatabase.categoryMergeRules.collectAsState()
        val scope = rememberCoroutineScope()
        MaterialTheme {
            HistoryScreen(FinanceUiState(isLoading = false, transactions = transactions,
                accounts = accounts, categoryMergeRules = rules), onIntent = { intent ->
                if (intent is FinanceIntent.EditTransaction) scope.launch(Dispatchers.IO) {
                    TransactionRepository().updateTransaction(intent.newTx, intent.oldTx)
                }
            })
        }
    }
    private fun openRow(id: Long) {
        rule.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("TransactionRow_$id"))
        rule.onNodeWithTag("TransactionRow_$id").performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTestTag("EditTransactionTransfer")).fetchSemanticsNodes().isNotEmpty() }
    }
    private fun chooseAccount(tag: String, id: Long) {
        rule.onNodeWithTag(tag).performScrollTo().performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTestTag("${tag}Menu")).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("${tag}Option_$id").performScrollTo().performClick()
        rule.waitUntil(5_000) { rule.onAllNodes(hasTestTag("${tag}Menu")).fetchSemanticsNodes().isEmpty() }
    }
}
