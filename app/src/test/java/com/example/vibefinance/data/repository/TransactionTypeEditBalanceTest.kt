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

    @Test
    fun iconOnlyEditsPreserveFinancialFieldsBalancesAndSurviveReload() = runTest {
        val repository = TransactionRepository()
        for (type in AccountType.entries) {
            val accountId = InMemoryDatabase.insertAccount(AccountEntity(name = "Icon $type", type = type, balance = 400.0, icon = "bank"))
            val id = repository.insertTransaction(TransactionEntity(amount = 9.9, category = "Food", timestamp = 1_700_000_000_000L,
                accountId = accountId, description = "咖啡", isExcludedFromDailyBudget = true))
            val before = InMemoryDatabase.transactions.value.single { it.id == id }
            val balanceBefore = balance(accountId)
            for (icon in listOf("emoji:☕", "symbol:COFFEE", null)) {
                val current = InMemoryDatabase.transactions.value.single { it.id == id }
                val edited = current.withHistoryEdit(9.9, false, "Food", "咖啡", false, icon)
                repository.updateTransaction(edited, current)
                InMemoryDatabase.reloadAfterFullRestore(context)
                assertEquals(before.copy(customIcon = icon), InMemoryDatabase.transactions.value.single { it.id == id })
                assertEquals(balanceBefore, balance(accountId), 0.000001)
            }
        }
    }
    @Test
    fun editingExpenseAccountReversesOldAccountAndDebitsNewAccount() = runTest {
        verifyBankAccountSwitch(amount = 200.0, originalEffect = -200.0)
    }

    @Test
    fun editingIncomeAccountReversesOldAccountAndCreditsNewAccount() = runTest {
        verifyBankAccountSwitch(amount = -200.0, originalEffect = 200.0)
    }

    private suspend fun verifyBankAccountSwitch(amount: Double, originalEffect: Double) {
        val repository = TransactionRepository()
        val accountA = InMemoryDatabase.insertAccount(
            AccountEntity(name = "Account A", type = AccountType.BANK, balance = 1000.0, icon = "bank")
        )
        val accountB = InMemoryDatabase.insertAccount(
            AccountEntity(name = "Account B", type = AccountType.BANK, balance = 2000.0, icon = "bank")
        )
        val id = repository.insertTransaction(TransactionEntity(
            amount = amount, category = if (amount < 0) "Salary" else "Food",
            timestamp = System.currentTimeMillis() - 1000L, accountId = accountA,
            isExcludedFromDailyBudget = amount < 0
        ))
        val original = InMemoryDatabase.transactions.value.single { it.id == id }
        assertEquals(1000.0 + originalEffect, balance(accountA), .001)
        repository.updateTransaction(original.withHistoryEdit(
            magnitude = kotlin.math.abs(amount), isIncome = amount < 0, category = original.category,
            description = original.description, countsTowardDailyBudget = !original.isExcludedFromDailyBudget,
            sourceAccountId = accountB
        ), original)
        InMemoryDatabase.reloadAfterFullRestore(context)
        assertEquals(1000.0, balance(accountA), .001)
        assertEquals(2000.0 + originalEffect, balance(accountB), .001)
        assertEquals(original.copy(accountId = accountB), InMemoryDatabase.transactions.value.single { it.id == id })
        repository.deleteTransaction(InMemoryDatabase.transactions.value.single { it.id == id })
        assertEquals(2000.0, balance(accountB), .001)
    }

    @Test
    fun accountSwitchPreservesMetadataAndUsesNewAccountDebtSemanticsForEveryTypePair() = runTest {
        val repository = TransactionRepository()
        for (oldType in AccountType.entries) {
            for (newType in AccountType.entries) {
                for (amount in listOf(200.0, -200.0)) {
                    val accountA = InMemoryDatabase.insertAccount(
                        AccountEntity(name = "Old $oldType", type = oldType, balance = 1000.0, icon = "bank")
                    )
                    val accountB = InMemoryDatabase.insertAccount(
                        AccountEntity(name = "New $newType", type = newType, balance = 2000.0, icon = "bank")
                    )
                    val id = repository.insertTransaction(TransactionEntity(
                        amount = amount, category = if (amount < 0) "Salary" else "Food",
                        timestamp = System.currentTimeMillis() - 1000L, accountId = accountA,
                        description = "轉帳戶 ☕", customIcon = "emoji:☕", isExcludedFromDailyBudget = true,
                        installmentNumber = 2, totalInstallments = 4, groupId = "installments:$accountA"
                    ))
                    val original = InMemoryDatabase.transactions.value.single { it.id == id }
                    repository.updateTransaction(original.withHistoryEdit(
                        magnitude = 200.0, isIncome = amount < 0, category = original.category,
                        description = original.description, countsTowardDailyBudget = false,
                        sourceAccountId = accountB
                    ), original)
                    InMemoryDatabase.reloadAfterFullRestore(context)
                    val moved = InMemoryDatabase.transactions.value.single { it.id == id }
                    assertEquals(original.copy(accountId = accountB, sourceWasCreditCard = newType == AccountType.CC), moved)
                    assertEquals(1000.0, balance(accountA), .001)
                    assertEquals(2000.0 + amount * if (newType == AccountType.CC) 1.0 else -1.0, balance(accountB), .001)
                    repository.updateTransaction(moved.withHistoryEdit(
                        magnitude = 200.0, isIncome = amount < 0, category = moved.category,
                        description = moved.description, countsTowardDailyBudget = false,
                        sourceAccountId = accountA
                    ), moved)
                    assertEquals(2000.0, balance(accountB), .001)
                    repository.deleteTransaction(InMemoryDatabase.transactions.value.single { it.id == id })
                    assertEquals(1000.0, balance(accountA), .001)
                }
            }
        }
    }

    private fun balance(id: Long): Double = InMemoryDatabase.accounts.value.single { it.id == id }.balance
}
