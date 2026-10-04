package com.example.vibefinance.data

import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.data.entity.historyAmountFor
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionPresentationTest {
    @Test
    fun incomeHasTheSamePositiveSignInGlobalAndCreditCardHistory() {
        // Regression for the reported Mox income: debt decreases, but the event is still income.
        val income = transaction(-300.0).copy(sourceWasCreditCard = true, category = "Income")

        assertEquals(300.0, income.historyAmountFor(), 0.0)
        assertEquals(300.0, income.historyAmountFor(SOURCE_ACCOUNT), 0.0)
    }

    @Test
    fun ordinaryIncomeAndExpenseSignsDoNotDependOnCreditCardSnapshots() {
        // Includes older imported entries with no account-type snapshot.
        listOf<Boolean?>(false, true, null).forEach { creditCard ->
            val expense = transaction(45.0).copy(sourceWasCreditCard = creditCard)
            val income = transaction(-300.0).copy(sourceWasCreditCard = creditCard)

            assertEquals(-45.0, expense.historyAmountFor(), 0.0)
            assertEquals(-45.0, expense.historyAmountFor(SOURCE_ACCOUNT), 0.0)
            assertEquals(300.0, income.historyAmountFor(), 0.0)
            assertEquals(300.0, income.historyAmountFor(SOURCE_ACCOUNT), 0.0)
        }
    }

    @Test
    fun creditCardCashAdvanceIsOutgoingFromTheCardAndIncomingToTheBank() {
        val transfer = transaction(100.0).copy(
            toAccountId = DESTINATION_ACCOUNT,
            sourceWasCreditCard = true,
            destinationWasCreditCard = false
        )

        assertEquals(-100.0, transfer.historyAmountFor(), 0.0)
        assertEquals(-100.0, transfer.historyAmountFor(SOURCE_ACCOUNT), 0.0)
        assertEquals(100.0, transfer.historyAmountFor(DESTINATION_ACCOUNT), 0.0)
    }

    @Test
    fun repaymentIsOutgoingFromTheBankAndIncomingToTheCreditCard() {
        val repayment = transaction(100.0).copy(
            toAccountId = DESTINATION_ACCOUNT,
            sourceWasCreditCard = false,
            destinationWasCreditCard = true
        )

        assertEquals(-100.0, repayment.historyAmountFor(SOURCE_ACCOUNT), 0.0)
        assertEquals(100.0, repayment.historyAmountFor(DESTINATION_ACCOUNT), 0.0)
    }

    @Test
    fun legacyTransfersWithoutSnapshotsKeepBothAccountDirections() {
        val transfer = transaction(100.0).copy(toAccountId = DESTINATION_ACCOUNT)

        assertEquals(-100.0, transfer.historyAmountFor(SOURCE_ACCOUNT), 0.0)
        assertEquals(100.0, transfer.historyAmountFor(DESTINATION_ACCOUNT), 0.0)
        assertEquals(0.0, transfer.historyAmountFor(OTHER_ACCOUNT), 0.0)
    }

    @Test
    fun selfTransfersHaveNoIncomingOrOutgoingAmount() {
        val selfTransfer = transaction(100.0).copy(toAccountId = SOURCE_ACCOUNT)

        assertEquals(0.0, selfTransfer.historyAmountFor(), 0.0)
        assertEquals(0.0, selfTransfer.historyAmountFor(SOURCE_ACCOUNT), 0.0)
        assertEquals(0.0, selfTransfer.historyAmountFor(OTHER_ACCOUNT), 0.0)
    }

    @Test
    fun adjustmentsKeepAccountBalanceDeltaAndGlobalNetWorthDirection() {
        // Asset balances and outstanding card debt have opposite net-worth effects.
        listOf(false, true).forEach { creditCard ->
            listOf(-25.0, 25.0).forEach { balanceDelta ->
                val adjustment = transaction(if (creditCard) balanceDelta else -balanceDelta).copy(
                    sourceWasCreditCard = creditCard,
                    isBalanceAdjustment = true,
                    balanceAdjustmentDelta = balanceDelta
                )

                assertEquals(balanceDelta, adjustment.historyAmountFor(SOURCE_ACCOUNT), 0.0)
                assertEquals(
                    if (creditCard) -balanceDelta else balanceDelta,
                    adjustment.historyAmountFor(),
                    0.0
                )
            }
        }
    }

    @Test
    fun transactionsDoNotContributeToAnUnrelatedAccountHistory() {
        val income = transaction(-300.0)
        val adjustment = transaction(-25.0).copy(
            isBalanceAdjustment = true,
            balanceAdjustmentDelta = 25.0
        )

        assertEquals(0.0, income.historyAmountFor(OTHER_ACCOUNT), 0.0)
        assertEquals(0.0, adjustment.historyAmountFor(OTHER_ACCOUNT), 0.0)
    }

    private fun transaction(amount: Double) = TransactionEntity(
        amount = amount,
        category = "Food & Dining",
        timestamp = 0L,
        accountId = SOURCE_ACCOUNT
    )

    private companion object {
        const val SOURCE_ACCOUNT = 11L
        const val DESTINATION_ACCOUNT = 22L
        const val OTHER_ACCOUNT = 33L
    }
}
