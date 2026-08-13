package com.example.vibefinance.ui.history

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
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
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.performClick

@RunWith(AndroidJUnit4::class)
class HistoryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

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
            transactions = listOf(mockTransaction)
        )

        composeTestRule.setContent {
            HistoryScreen(
                state = mockState,
                onIntent = {}
            )
        }

        // Find the transaction by its testTag
        val transactionNode = composeTestRule.onNodeWithTag("TransactionRow_1")
        transactionNode.assertIsDisplayed()

        // Simulate an early release (Abort) - swipe horizontally by less than 25% threshold
        transactionNode.performTouchInput {
            down(centerLeft)
            // Move right by a small amount (e.g. 10% of width) to not cross the 25% threshold
            moveBy(Offset(viewConfiguration.touchSlop + 50f, 0f))
            up()
        }

        composeTestRule.waitForIdle()

        // Verify the row returns to its settled position and does NOT trigger the Edit state (Dialog)
        composeTestRule.onNodeWithText("Edit Transaction").assertDoesNotExist()
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
            transactions = listOf(mockTransaction)
        )

        composeTestRule.setContent {
            HistoryScreen(
                state = mockState,
                onIntent = {}
            )
        }

        // Find the transaction by its testTag
        val transactionNode = composeTestRule.onNodeWithTag("TransactionRow_1")
        transactionNode.assertIsDisplayed()

        // Simulate a full swipe right that exceeds the 25% threshold
        transactionNode.performTouchInput {
            swipeRight(startX = 0f, endX = right)
        }

        composeTestRule.waitForIdle()

        // Verify the dismiss confirmation flow is triggered properly (Edit Transaction Dialog appears)
        composeTestRule.onNodeWithText("Edit Transaction").assertIsDisplayed()
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
                    state = FinanceUiState(transactions = transactions),
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
        composeTestRule.onNodeWithTag("TransactionRow_1").assertIsDisplayed()
    }
}
