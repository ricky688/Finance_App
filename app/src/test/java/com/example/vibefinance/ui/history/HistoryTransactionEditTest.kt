package com.example.vibefinance.ui.history

import com.example.vibefinance.data.entity.TransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/** Editor path grounded by ARTEMIS: tap a History row, edit its fields, then Save or Cancel. */
class HistoryTransactionEditTest {
    private val expense = TransactionEntity(
        id = 18L,
        amount = 35.5,
        category = "Food",
        timestamp = 1_700_000_000_000L,
        accountId = 42L,
        description = "Coffee",
        sourceWasCreditCard = true
    )

    @Test
    fun expenseToIncomeKeepsMagnitudeAndExcludesDailyBudget() {
        val edited = expense.withHistoryEdit(
            magnitude = 35.5,
            isIncome = true,
            category = "Refund",
            description = "Coffee refund",
            countsTowardDailyBudget = true
        )

        assertEquals(-35.5, edited.amount, 0.0)
        assertTrue(edited.isExcludedFromDailyBudget)
        assertEquals(
            expense.copy(
                amount = -35.5,
                category = "Refund",
                description = "Coffee refund",
                isExcludedFromDailyBudget = true
            ),
            edited
        )
    }

    @Test
    fun incomeToExpenseUsesPositiveMagnitudeAndSelectedBudgetChoice() {
        val income = expense.copy(amount = -35.5, isExcludedFromDailyBudget = true)

        val dailyExpense = income.withHistoryEdit(35.5, false, "Food", "Coffee", true)
        val nonDailyExpense = income.withHistoryEdit(35.5, false, "Food", "Coffee", false)

        assertEquals(35.5, dailyExpense.amount, 0.0)
        assertFalse(dailyExpense.isExcludedFromDailyBudget)
        assertEquals(35.5, nonDailyExpense.amount, 0.0)
        assertTrue(nonDailyExpense.isExcludedFromDailyBudget)
    }

    @Test
    fun transferEditKeepsDirectionDestinationAndBudgetExclusion() {
        val transfer = expense.copy(
            toAccountId = 43L,
            category = "Transfer",
            destinationWasCreditCard = false,
            isExcludedFromDailyBudget = true
        )

        val edited = transfer.withHistoryEdit(50.0, true, "Transfer", "Payment", true)

        assertEquals(transfer.copy(amount = 50.0, description = "Payment"), edited)
        val legacyNegativeTransfer = transfer.copy(amount = -35.5)
        assertEquals(
            -50.0,
            legacyNegativeTransfer.withHistoryEdit(50.0, false, "Transfer", "Payment", false).amount,
            0.0
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun balanceAdjustmentCannotBeEditedAsExpenseOrIncome() {
        expense.copy(isBalanceAdjustment = true, balanceAdjustmentDelta = 35.5)
            .withHistoryEdit(35.5, true, "Income", "Correction", false)
    }

    @Test
    fun invalidMagnitudesAreRejected() {
        listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach { magnitude ->
            try {
                expense.withHistoryEdit(magnitude, false, "Food", "Coffee", true)
                fail("Expected invalid magnitude to be rejected: $magnitude")
            } catch (_: IllegalArgumentException) {
                // The editor only accepts finite positive amounts before applying the selected sign.
            }
        }
    }
}
