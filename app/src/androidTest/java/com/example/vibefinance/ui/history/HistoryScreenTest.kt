package com.example.vibefinance.ui.history

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.ui.FinanceUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import com.example.vibefinance.ui.FinanceIntent
import com.example.vibefinance.R
import androidx.test.platform.app.InstrumentationRegistry
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.assertTextContains
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class HistoryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun customExpenseIconEditKeepsAmountCategoryAndBudgetChoice() {
        val expense = TransactionEntity(id = 1L, amount = 9.9, category = "Food", accountId = 1L,
            timestamp = System.currentTimeMillis(), description = "咖啡", isExcludedFromDailyBudget = true)
        val submitted = AtomicReference<FinanceIntent.EditTransaction?>()
        composeTestRule.setContent {
            HistoryScreen(state = FinanceUiState(isLoading = false, transactions = listOf(expense)),
                onIntent = { if (it is FinanceIntent.EditTransaction) submitted.set(it) })
        }
        composeTestRule.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("TransactionRow_1"))
        composeTestRule.onNodeWithTag("TransactionRow_1").performClick()
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodes(hasTestTag("ExpenseIconChoose")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("ExpenseIconChoose").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("ExpenseIconSymbol_COFFEE").performClick()
        composeTestRule.onNodeWithTag("ExpenseIconApply").performClick()
        composeTestRule.onNodeWithTag("EditTransactionSave").performClick()
        composeTestRule.waitUntil(5_000) { submitted.get() != null }
        org.junit.Assert.assertEquals(expense.copy(customIcon = "symbol:COFFEE"), submitted.get()!!.newTx)
    }

    @Test
    fun swipeRight_abort_returnsToSettled() {
        // Setup mock data
        val mockTransaction = TransactionEntity(
            id = 1L,
            amount = 100.0,
            category = "Groceries",
            timestamp = System.currentTimeMillis(),
            accountId = 1L,
            description = "Test Swipe"
        )
        val mockState = FinanceUiState(
            isLoading = false,
            transactions = listOf(mockTransaction)
        )

        composeTestRule.setContent {
            HistoryScreen(
                state = mockState,
                onIntent = {}
            )
        }

        // Find the transaction by its testTag
        composeTestRule.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("TransactionRow_1"))
        val transactionNode = composeTestRule.onNodeWithTag("TransactionRow_1")
        transactionNode.assertIsDisplayed()

        // Release before the recurring-style 40% detachment threshold.
        transactionNode.performTouchInput {
            down(centerLeft)
            moveBy(Offset(viewConfiguration.touchSlop + 50f, 0f))
            up()
        }

        composeTestRule.waitForIdle()

        // Verify the row returns to its settled position and does NOT trigger the Edit state (Dialog)
        val editTitle = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.edit_transaction_title)
        composeTestRule.onNodeWithText(editTitle).assertDoesNotExist()
    }

    @Test
    fun swipeRight_full_triggersEditFlow() {
        // Setup mock data
        val mockTransaction = TransactionEntity(
            id = 1L,
            amount = 100.0,
            category = "Groceries",
            timestamp = System.currentTimeMillis(),
            accountId = 1L,
            description = "Test Swipe"
        )
        val mockState = FinanceUiState(
            isLoading = false,
            transactions = listOf(mockTransaction)
        )

        composeTestRule.setContent {
            HistoryScreen(
                state = mockState,
                onIntent = {}
            )
        }

        // Find the transaction by its testTag
        composeTestRule.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("TransactionRow_1"))
        val transactionNode = composeTestRule.onNodeWithTag("TransactionRow_1")
        transactionNode.assertIsDisplayed()

        // Swipe beyond the recurring-style 40% detachment threshold.
        transactionNode.performTouchInput {
            swipeRight(startX = 0f, endX = right)
        }

        composeTestRule.waitForIdle()

        // Verify the dismiss confirmation flow is triggered properly (Edit Transaction Dialog appears)
        val editTitle = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.edit_transaction_title)
        composeTestRule.onNodeWithText(editTitle).assertIsDisplayed()
    }

    @Test
    fun swipeLeft_deleteAndUndo() {
        val mockTransaction = TransactionEntity(
            id = 1L,
            amount = 100.0,
            category = "Groceries",
            timestamp = System.currentTimeMillis(),
            accountId = 1L,
            description = "Test Swipe"
        )
        
        composeTestRule.setContent {
            var transactions by remember { mutableStateOf(listOf(mockTransaction)) }
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { padding ->
                HistoryScreen(
                    state = FinanceUiState(isLoading = false, transactions = transactions),
                    onIntent = { intent ->
                        if (intent is FinanceIntent.DeleteTransaction) {
                            transactions = transactions.filter { it.id != intent.tx.id }
                            scope.launch {
                                val result = snackbarHostState.showSnackbar("Transaction deleted", "Undo")
                                if (result == SnackbarResult.ActionPerformed) {
                                    transactions = transactions + intent.tx
                                }
                            }
                        }
                    },
                    modifier = Modifier.padding(padding)
                )
            }
        }

        composeTestRule.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("TransactionRow_1"))
        val transactionNode = composeTestRule.onNodeWithTag("TransactionRow_1")
        transactionNode.assertIsDisplayed()

        // Swipe Left (End to Start) to trigger Delete
        transactionNode.performTouchInput {
            swipeLeft(startX = right, endX = 0f)
        }
        
        composeTestRule.waitForIdle()

        // Assert item is removed from layout
        transactionNode.assertDoesNotExist()
        
        // Assert Snackbar Undo button is displayed
        val undoNode = composeTestRule.onNodeWithText("Undo")
        undoNode.assertIsDisplayed()
        
        // Click Undo
        undoNode.performClick()
        
        composeTestRule.waitForIdle()
        
        // Assert item is restored back to the list
        composeTestRule.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("TransactionRow_1"))
        composeTestRule.onNodeWithTag("TransactionRow_1").assertIsDisplayed()
    }

    @Test
    fun dailyNetIncludesIncomeWithoutTreatingTransfersAsSpending() {
        val today = System.currentTimeMillis()
        val income = TransactionEntity(
            id = 1L, amount = -2500.0, category = "Income", timestamp = today,
            accountId = 1L, description = "Paycheck"
        )
        val transfer = TransactionEntity(
            id = 2L, amount = 200.0, category = "Transfer", timestamp = today,
            accountId = 1L, toAccountId = 2L, description = "Card payment"
        )
        composeTestRule.setContent {
            HistoryScreen(state = FinanceUiState(isLoading = false, transactions = listOf(income, transfer)), onIntent = {})
        }

        val expected = InstrumentationRegistry.getInstrumentation().targetContext
            .getString(R.string.daily_net_format, "+", 2500.0)
        composeTestRule.onNodeWithTag("HistoryList").performScrollToNode(hasText(expected))
        composeTestRule.onNodeWithText(expected).assertIsDisplayed()
    }

    @Test
    fun income300HasTheSamePositiveAmountInGlobalAndCardFilteredHistory() {
        val account = AccountEntity(id = 1L, name = "Mox", type = AccountType.CC, balance = 500.0, icon = "credit_card")
        val income = TransactionEntity(
            id = 300L, amount = -300.0, category = "Salary", timestamp = System.currentTimeMillis(),
            accountId = account.id, description = "Mox income regression", sourceWasCreditCard = true
        )
        val filter = mutableStateOf<Long?>(null)
        composeTestRule.setContent {
            HistoryScreen(
                state = FinanceUiState(isLoading = false, accounts = listOf(account), transactions = listOf(income)),
                onIntent = {}, accountFilterId = filter.value
            )
        }
        composeTestRule.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("TransactionRow_300"))
        composeTestRule.onNodeWithText("+HK$300.00").assertIsDisplayed()
        composeTestRule.runOnIdle { filter.value = account.id }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("TransactionRow_300"))
        composeTestRule.onNodeWithText("+HK$300.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("-HK$300.00").assertDoesNotExist()
    }

    @Test
    fun tapEventCanChangeExpenseToIncomeAndEditItsCategory() {
        val saved = AtomicReference<FinanceIntent.EditTransaction?>()
        val expense = TransactionEntity(
            id = 301L, amount = 50.0, category = "Food", timestamp = System.currentTimeMillis(),
            accountId = 1L, description = "Type conversion regression"
        )
        composeTestRule.setContent {
            var transactions by remember { mutableStateOf(listOf(expense)) }
            HistoryScreen(
                state = FinanceUiState(isLoading = false, transactions = transactions),
                onIntent = { intent ->
                    if (intent is FinanceIntent.EditTransaction) {
                        saved.set(intent)
                        transactions = listOf(intent.newTx)
                    }
                }
            )
        }
        composeTestRule.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("TransactionRow_301"))
        composeTestRule.onNodeWithTag("TransactionRow_301").performClick()
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodes(hasTestTag("EditTransactionIncome")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("EditTransactionExpense").assertIsSelected()
        composeTestRule.onNodeWithTag("EditTransactionIncome").performClick().assertIsSelected()
        composeTestRule.onNodeWithTag("EditTransactionDailyBudget").assertDoesNotExist()
        composeTestRule.onNodeWithTag("EditTransactionAmount").performTextReplacement("300.00")
        chooseCategory("Salary")
        composeTestRule.onNodeWithTag("EditTransactionSave").performClick()
        composeTestRule.waitUntil(5_000) { saved.get() != null }
        org.junit.Assert.assertEquals(-300.0, saved.get()!!.newTx.amount, 0.0)
        org.junit.Assert.assertEquals("Salary", saved.get()!!.newTx.category)
        org.junit.Assert.assertTrue(saved.get()!!.newTx.isExcludedFromDailyBudget)
        composeTestRule.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("TransactionRow_301"))
        composeTestRule.onNodeWithText("+HK$300.00").assertIsDisplayed()
    }

    @Test
    fun tapIncomeCanChangeItToExpenseAndEnableDailyBudget() {
        val saved = AtomicReference<FinanceIntent.EditTransaction?>()
        val income = TransactionEntity(
            id = 302L, amount = -300.0, category = "Salary", timestamp = System.currentTimeMillis(),
            accountId = 1L, isExcludedFromDailyBudget = true, description = "Income correction"
        )
        composeTestRule.setContent {
            HistoryScreen(
                state = FinanceUiState(isLoading = false, transactions = listOf(income)),
                onIntent = { if (it is FinanceIntent.EditTransaction) saved.set(it) }
            )
        }
        composeTestRule.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("TransactionRow_302"))
        composeTestRule.onNodeWithTag("TransactionRow_302").performClick()
        composeTestRule.onNodeWithTag("EditTransactionIncome").assertIsSelected()
        composeTestRule.onNodeWithTag("EditTransactionExpense").performClick().assertIsSelected()
        composeTestRule.onNodeWithTag("EditTransactionDailyBudget").assertIsDisplayed()
        chooseCategory("Shopping")
        composeTestRule.onNodeWithTag("EditTransactionSave").performClick()
        composeTestRule.waitUntil(5_000) { saved.get() != null }
        org.junit.Assert.assertEquals(300.0, saved.get()!!.newTx.amount, 0.0)
        org.junit.Assert.assertFalse(saved.get()!!.newTx.isExcludedFromDailyBudget)
    }

    @Test
    fun transferEditorDoesNotOfferExpenseIncomeConversion() {
        val transfer = TransactionEntity(
            id = 303L, amount = 200.0, category = "Transfer", timestamp = System.currentTimeMillis(),
            accountId = 1L, toAccountId = 2L, description = "Card repayment"
        )
        composeTestRule.setContent {
            HistoryScreen(state = FinanceUiState(isLoading = false, transactions = listOf(transfer)), onIntent = {})
        }
        composeTestRule.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("TransactionRow_303"))
        composeTestRule.onNodeWithTag("TransactionRow_303").performClick()
        composeTestRule.onNodeWithTag("EditTransactionAmount").assertIsDisplayed()
        composeTestRule.onNodeWithTag("EditTransactionIncome").assertDoesNotExist()
        composeTestRule.onNodeWithTag("EditTransactionExpense").assertDoesNotExist()
    }

    @Test
    fun categoryMenuPreservesCurrentCategoryAcrossTypeChangeAndCancelDiscardsDraft() {
        val saved = AtomicReference<FinanceIntent.EditTransaction?>()
        val expense = TransactionEntity(
            id = 304L, amount = 25.0, category = "Custom Metro",
            timestamp = System.currentTimeMillis(), accountId = 1L, description = "Menu preservation"
        )
        composeTestRule.setContent {
            HistoryScreen(
                state = FinanceUiState(isLoading = false, transactions = listOf(expense)),
                onIntent = { if (it is FinanceIntent.EditTransaction) saved.set(it) }
            )
        }
        composeTestRule.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("TransactionRow_304"))
        composeTestRule.onNodeWithTag("TransactionRow_304").performClick()
        composeTestRule.onNodeWithTag("EditTransactionIncome").performClick().assertIsSelected()
        composeTestRule.onNodeWithTag("EditTransactionCategory").assertTextContains("Custom Metro")
        openCategoryMenu()
        composeTestRule.onNodeWithTag("EditTransactionCategoryOption_Custom Metro")
            .assertIsDisplayed().assertIsSelected()
        selectOpenCategory("Salary")
        composeTestRule.onNodeWithTag("EditTransactionCategory").assertTextContains("Salary")
        val cancel = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.btn_cancel)
        composeTestRule.onNodeWithText(cancel).performClick()
        composeTestRule.waitForIdle()
        org.junit.Assert.assertNull(saved.get())
        composeTestRule.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("TransactionRow_304"))
        composeTestRule.onNodeWithText("-HK$25.00").assertIsDisplayed()
    }

    @Test
    fun categoryMenuCanScrollToAndSavePreviouslyUsedCustomCategory() {
        val saved = AtomicReference<FinanceIntent.EditTransaction?>()
        val expense = TransactionEntity(
            id = 305L, amount = 25.0, category = "Food", timestamp = System.currentTimeMillis(),
            accountId = 1L, description = "Scrollable menu"
        )
        val older = (1..24).map { index ->
            expense.copy(id = 400L + index, category = "Custom category %02d".format(index),
                timestamp = expense.timestamp - 86_400_000L, description = "Earlier category")
        }
        composeTestRule.setContent {
            HistoryScreen(
                state = FinanceUiState(isLoading = false, transactions = listOf(expense) + older),
                onIntent = { if (it is FinanceIntent.EditTransaction) saved.set(it) }
            )
        }
        composeTestRule.onNodeWithTag("HistoryList").performScrollToNode(hasTestTag("TransactionRow_305"))
        composeTestRule.onNodeWithTag("TransactionRow_305").performClick()
        chooseCategory("Custom category 24")
        composeTestRule.onNodeWithTag("EditTransactionCategory").assertTextContains("Custom category 24")
        composeTestRule.onNodeWithTag("EditTransactionSave").performClick()
        composeTestRule.waitUntil(5_000) { saved.get() != null }
        org.junit.Assert.assertEquals("Custom category 24", saved.get()!!.newTx.category)
        org.junit.Assert.assertEquals(25.0, saved.get()!!.newTx.amount, 0.0)
    }

    // Verified on Waydroid with ARTEMIS observations and ADB before test authoring:
    // a read-only anchor opens a separate scrollable popup; choosing a row closes it.
    private fun openCategoryMenu() {
        composeTestRule.onNodeWithTag("EditTransactionCategory").performClick()
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodes(hasTestTag("EditTransactionCategoryMenu"))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun selectOpenCategory(category: String) {
        val tag = "EditTransactionCategoryOption_$category"
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag(tag).performScrollTo().assertIsDisplayed().performClick()
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodes(hasTestTag("EditTransactionCategoryMenu"))
                .fetchSemanticsNodes().isEmpty()
        }
    }

    private fun chooseCategory(category: String) {
        openCategoryMenu()
        selectOpenCategory(category)
    }
}
