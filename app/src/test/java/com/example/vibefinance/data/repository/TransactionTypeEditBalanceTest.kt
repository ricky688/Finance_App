package com.example.vibefinance.data.repository

import android.content.Context
import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.data.entity.historyAmountFor
import com.example.vibefinance.ui.history.withHistoryEdit
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Regression for the ARTEMIS-explored History editor and the reported Mox +300 case. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TransactionTypeEditBalanceTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        InMemoryDatabase.initialize(context)
        InMemoryDatabase.clearAllTables()
    }

    @After
    fun tearDown() {
        InMemoryDatabase.clearAllTables()
    }

    @Test
    fun incomeExpenseRoundTripReversesThePreviousEffectExactlyOnceForEveryAccountType() = runTest {
        val repository = TransactionRepository()
        for (type in AccountType.entries) {
            val accountId = InMemoryDatabase.insertAccount(
                AccountEntity(name = "Mox $type", type = type, balance = 400.0, icon = "bank")
            )
            val id = repository.insertTransaction(
                TransactionEntity(
                    amount = -300.0, category = "Salary", timestamp = System.currentTimeMillis() - 1_000L,
                    accountId = accountId, description = "Income regression", isExcludedFromDailyBudget = true
                )
            )
            val income = InMemoryDatabase.transactions.value.single { it.id == id }
            val incomeBalance = if (type == AccountType.CC) 100.0 else 700.0
            assertEquals(incomeBalance, balance(accountId), 0.001)
            assertEquals(300.0, income.historyAmountFor(), 0.001)
            assertEquals(300.0, income.historyAmountFor(accountId), 0.001)

            val expense = income.withHistoryEdit(
                magnitude = 300.0, isIncome = false, category = "Shopping",
                description = "Changed to expense", countsTowardDailyBudget = true
            )
            repository.updateTransaction(expense, income)
            val storedExpense = InMemoryDatabase.transactions.value.single { it.id == id }
            assertEquals(if (type == AccountType.CC) 700.0 else 100.0, balance(accountId), 0.001)
            assertEquals(-300.0, storedExpense.historyAmountFor(), 0.001)
            assertEquals(-300.0, storedExpense.historyAmountFor(accountId), 0.001)
            assertFalse(storedExpense.isExcludedFromDailyBudget)

            val restoredIncome = storedExpense.withHistoryEdit(
                magnitude = 300.0, isIncome = true, category = "Salary",
                description = "Changed back to income", countsTowardDailyBudget = true
            )
            repository.updateTransaction(restoredIncome, storedExpense)
            assertEquals(incomeBalance, balance(accountId), 0.001)
            assertTrue(InMemoryDatabase.transactions.value.single { it.id == id }.isExcludedFromDailyBudget)
            assertEquals(1, InMemoryDatabase.transactions.value.count { it.id == id })

            // Deletion must reverse the final income, not either prior expense state.
            repository.deleteTransaction(InMemoryDatabase.transactions.value.single { it.id == id })
            assertEquals(400.0, balance(accountId), 0.001)
        }
    }

    @Test
    fun typeChangeAndMagnitudeEditApplyTheNewAmountWithoutDoubleCounting() = runTest {
        val repository = TransactionRepository()
        val accountId = InMemoryDatabase.insertAccount(
            AccountEntity(name = "Mox", type = AccountType.BANK, balance = 1_000.0, icon = "bank")
        )
        val id = repository.insertTransaction(TransactionEntity(
            amount = 50.0, category = "Food", timestamp = System.currentTimeMillis() - 1_000L,
            accountId = accountId
        ))
        assertEquals(950.0, balance(accountId), 0.001)
        val expense = InMemoryDatabase.transactions.value.single { it.id == id }
        val income = expense.withHistoryEdit(300.0, true, "Salary", "Corrected deposit", false)
        repository.updateTransaction(income, expense)
        assertEquals(1_300.0, balance(accountId), 0.001)
        assertEquals(-300.0, InMemoryDatabase.transactions.value.single { it.id == id }.amount, 0.001)
        assertEquals("Salary", InMemoryDatabase.transactions.value.single { it.id == id }.category)
    }

    private fun balance(id: Long): Double = InMemoryDatabase.accounts.value.single { it.id == id }.balance
}
