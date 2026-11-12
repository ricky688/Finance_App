package com.example.vibefinance.ui.history

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vibefinance.R
import com.example.vibefinance.data.entity.*
import com.example.vibefinance.ui.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** History edit path explored with ARTEMIS on Waydroid; fixtures never reach persistent stores. */
class HistoryReimbursementEditTest {
    @get:Rule val rule = createComposeRule()
    private val now = System.currentTimeMillis()
    private val expense = TransactionEntity(id = 10, amount = 100.0, category = "Food", accountId = 1,
        timestamp = now - 10000, description = "Advance A")
    private val otherExpense = expense.copy(id = 11, amount = 80.0, category = "Shopping",
        timestamp = now - 9000, description = "Advance B", isExcludedFromDailyBudget = true)
    private val receipt = expense.copy(id = 20, amount = -30.0, reimbursementOf = 10,
        timestamp = now - 8000, description = "Receipt")
    private val income = receipt.copy(id = 22, amount = -25.0, reimbursementOf = null,
        timestamp = now - 5000, category = "Salary", description = "Income", isExcludedFromDailyBudget = true)
    private val intents = mutableListOf<FinanceIntent>()
    private var result by mutableStateOf<BatchTransactionResult?>(null)
    private var records by mutableStateOf(listOf(expense, otherExpense, receipt,
        receipt.copy(id = 21, amount = -20.0, timestamp = now - 7000), income,
        expense.copy(id = 12, timestamp = now - 1000, description = "Newer advance")))

    private fun show() {
        rule.setContent {
            MaterialTheme {
                HistoryScreen(FinanceUiState(isLoading = false, transactions = records, accounts = listOf(
                    AccountEntity(1, "Bank", type = AccountType.BANK, balance = 1000.0, icon = "bank"),
                    AccountEntity(2, "Card", type = AccountType.CC, balance = 100.0, icon = "card"),
                    AccountEntity(3, "Investment", type = AccountType.INVESTMENT, balance = 100.0, icon = "bank"))),
                    onIntent = { intents += it }, editResult = { result })
            }
        }
    }
    private fun open(id: Long) {
        rule.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("TransactionRow_$id"))
        rule.onNodeWithTag("TransactionRow_$id").performClick()
        rule.waitUntil(5000) { rule.onAllNodesWithTag("EditTransactionAmount").fetchSemanticsNodes().isNotEmpty() }
    }
    private fun source(id: Long) {
        rule.onNodeWithTag("ReimbursementExpense").performScrollTo().performClick()
        rule.onNodeWithTag("ReimbursementSource_$id").performScrollTo().performClick()
    }
    private fun amount(value: String) {
        rule.onNodeWithTag("EditTransactionAmount").performScrollTo().performTextReplacement(value)
        Espresso.closeSoftKeyboard()
    }
    private fun save() = rule.onNodeWithTag("EditTransactionSave").performClick()
    private fun succeed(intent: FinanceIntent.EditTransaction) {
        rule.runOnIdle {
            records = records.map { if (it.id == intent.newTx.id) intent.newTx else it }
            result = BatchTransactionResult(intent.requestId!!, false, success = true)
        }
        rule.waitUntil(5000) { rule.onAllNodesWithTag("EditTransactionSave").fetchSemanticsNodes().isEmpty() }
    }

    @Test fun incomeCanBecomeRepaymentWithoutCreatingAnotherRecord() {
        show(); open(22)
        rule.onNodeWithTag("EditTransactionReimbursement").performScrollTo().assertIsOff().performClick()
        rule.onNodeWithTag("EditTransactionReimbursement").assertIsOn()
        rule.onNodeWithTag("ReimbursementExpense").performScrollTo().performClick()
        rule.onNodeWithTag("ReimbursementSource_12").assertExists()
        rule.onNodeWithTag("ReimbursementSource_22").assertDoesNotExist()
        rule.onNodeWithTag("ReimbursementSource_12").performScrollTo().performClick()
        rule.onNodeWithTag("EditTransactionCategory").assertDoesNotExist()
        rule.onNodeWithTag("EditTransactionAccount").performScrollTo().performClick()
        rule.onNodeWithTag("EditTransactionAccountOption_3").assertDoesNotExist()
        rule.onNodeWithTag("EditTransactionAccountOption_1").performClick()
        save()
        rule.onNodeWithTag("EditTransactionSave").assertIsNotEnabled()
        val edit = intents.single() as FinanceIntent.EditTransaction
        assertEquals(income, edit.oldTx)
        assertEquals(income.id, edit.newTx.id)
        assertEquals(income.timestamp, edit.newTx.timestamp)
        assertEquals(-25.0, edit.newTx.amount, 0.0)
        assertEquals(12L, edit.newTx.reimbursementOf)
        assertEquals("Food", edit.newTx.category)
        assertFalse(edit.newTx.isExcludedFromDailyBudget)
        assertEquals(records.first { it.id == 12L }, edit.repaymentExpense)
        assertTrue(edit.repaymentExpense!!.timestamp > income.timestamp)
        succeed(edit)
        assertEquals(6, records.size)
    }

    @Test fun repaymentEditAllowsExcessAndRetainsFailureThenRetriesOnce() {
        show(); open(20)
        rule.onNodeWithTag("EditTransactionReimbursement").performScrollTo().assertIsOn()
        amount("80.01")
        rule.onNodeWithTag("EditTransactionSave").assertIsEnabled()
        amount("80")
        rule.onNodeWithTag("EditTransactionSave").assertIsEnabled()
        source(11)
        rule.onNodeWithTag("EditTransactionAccount").performScrollTo().performClick()
        rule.onNodeWithTag("EditTransactionAccountOption_2").performClick()
        rule.onNodeWithTag("EditTransactionDescription").performScrollTo().performTextReplacement("分次還錢 ↩️")
        Espresso.closeSoftKeyboard()
        save()
        rule.onNodeWithTag("EditTransactionSave").assertIsNotEnabled()
        rule.onNodeWithTag("EditTransactionReimbursement").assertIsNotEnabled()
        Espresso.pressBack()
        rule.onNodeWithTag("EditTransactionSave").assertExists()
        val first = intents.single() as FinanceIntent.EditTransaction
        rule.runOnIdle { result = BatchTransactionResult(first.requestId!!, false, error = "Storage unavailable") }
        rule.onNodeWithTag("EditTransactionError").performScrollTo().assertTextContains("Storage unavailable")
        rule.onNodeWithTag("EditTransactionDescription").assertTextContains("分次還錢 ↩️")
        save()
        assertEquals(2, intents.size)
        val retry = intents.last() as FinanceIntent.EditTransaction
        assertEquals(first.requestId, retry.requestId)
        assertEquals(receipt.id, retry.newTx.id)
        assertEquals(receipt.timestamp, retry.newTx.timestamp)
        assertEquals(-80.0, retry.newTx.amount, 0.0)
        assertEquals(2L, retry.newTx.accountId)
        assertEquals(11L, retry.newTx.reimbursementOf)
        assertEquals("Shopping", retry.newTx.category)
        assertTrue(retry.newTx.isExcludedFromDailyBudget)
        assertEquals(otherExpense, retry.repaymentExpense)
        succeed(retry)
    }

    @Test fun cancelPreservesRepaymentAndSwitchOffConvertsSameRecordToIncome() {
        show(); open(20)
        rule.onNodeWithTag("EditTransactionReimbursement").performScrollTo().performClick()
        rule.onNodeWithTag("EditTransactionCategory").assertExists()
        val cancel = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.btn_cancel)
        rule.onNodeWithText(cancel).performClick()
        assertTrue(intents.isEmpty())
        assertEquals(receipt, records.first { it.id == 20L })
        open(20)
        rule.onNodeWithTag("EditTransactionReimbursement").performScrollTo().assertIsOn().performClick()
        save()
        val edit = intents.single() as FinanceIntent.EditTransaction
        assertEquals(receipt.id, edit.newTx.id)
        assertEquals(receipt.timestamp, edit.newTx.timestamp)
        assertEquals(-30.0, edit.newTx.amount, 0.0)
        assertNull(edit.newTx.reimbursementOf)
        assertTrue(edit.newTx.isExcludedFromDailyBudget)
        succeed(edit)
    }
}
