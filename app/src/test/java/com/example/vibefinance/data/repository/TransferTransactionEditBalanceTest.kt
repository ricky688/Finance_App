package com.example.vibefinance.data.repository

import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.ui.history.withHistoryEdit
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

/** Transfer conversion path explored with ARTEMIS on the imported 868-record dataset. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TransferTransactionEditBalanceTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private val repository = TransactionRepository()
    @Before fun setUp() { InMemoryDatabase.initialize(context); InMemoryDatabase.clearAllTables() }
    @After fun tearDown() {
        File(context.filesDir, "vibefinance_data.json.new").deleteRecursively()
        InMemoryDatabase.clearAllTables()
    }
    private fun account(type: AccountType) = InMemoryDatabase.insertAccount(
        AccountEntity(name = "$type account", type = type, balance = 1_000.0, icon = "bank"))
    private fun balance(id: Long) = InMemoryDatabase.accounts.value.single { it.id == id }.balance
    private suspend fun insert(source: Long, income: Boolean = false, future: Boolean = false): TransactionEntity {
        val id = repository.insertTransaction(TransactionEntity(amount = if (income) -9.9 else 9.9,
            category = "🍜 Food", description = "測試 ☕", accountId = source,
            timestamp = System.currentTimeMillis() + if (future) 86_400_000L else -1_000L))
        return InMemoryDatabase.transactions.value.single { it.id == id }
    }
    @Test fun incomeAndExpenseConvertAndRoundTripForEverySourceDestinationType() = runTest {
        for (sourceType in AccountType.entries) for (destType in AccountType.entries) for (income in listOf(false, true)) {
            val source = account(sourceType); val dest = account(destType)
            val original = insert(source, income)
            val before = InMemoryDatabase.accounts.value
            val transfer = original.withHistoryEdit(9.9, income, original.category, original.description, true,
                isTransfer = true, destinationAccountId = dest)
            repository.updateTransaction(transfer, original)
            assertEquals(if (sourceType == AccountType.CC) 1_009.9 else 990.1, balance(source), 1e-9)
            assertEquals(if (destType == AccountType.CC) 990.1 else 1_009.9, balance(dest), 1e-9)
            val stored = InMemoryDatabase.transactions.value.single { it.id == original.id }
            assertEquals(original.id, stored.id); assertEquals(original.timestamp, stored.timestamp)
            assertEquals("Transfer", stored.category); assertEquals(original.description, stored.description)
            assertTrue(stored.isExcludedFromDailyBudget)
            InMemoryDatabase.reloadAfterFullRestore(context)
            assertEquals(stored, InMemoryDatabase.transactions.value.single { it.id == original.id })
            repository.updateTransaction(original, stored)
            before.forEach { assertEquals(it.balance, balance(it.id), 1e-9) }
            assertEquals(original, InMemoryDatabase.transactions.value.single { it.id == original.id })
        }
    }
    @Test fun changingSourceAndDestinationRefreshesSnapshotsAndReversesAllOldEffects() = runTest {
        val oldSource = account(AccountType.CC); val newSource = account(AccountType.BANK)
        val dest = account(AccountType.CC); val otherDest = account(AccountType.CASH)
        val original = insert(oldSource)
        val transfer = original.withHistoryEdit(20.0, false, "Food", "Moved", true,
            isTransfer = true, sourceAccountId = newSource, destinationAccountId = dest)
        repository.updateTransaction(transfer, original)
        var stored = InMemoryDatabase.transactions.value.single()
        assertFalse(stored.sourceWasCreditCard!!); assertTrue(stored.destinationWasCreditCard!!)
        assertEquals(1_000.0, balance(oldSource), 1e-9)
        assertEquals(980.0, balance(newSource), 1e-9); assertEquals(980.0, balance(dest), 1e-9)
        repository.updateTransaction(stored.copy(toAccountId = otherDest), stored)
        stored = InMemoryDatabase.transactions.value.single()
        assertFalse(stored.destinationWasCreditCard!!)
        assertEquals(1_000.0, balance(dest), 1e-9); assertEquals(1_020.0, balance(otherDest), 1e-9)
        repository.updateTransaction(original, stored)
        assertEquals(1_009.9, balance(oldSource), 1e-9); assertEquals(1_000.0, balance(newSource), 1e-9)
        assertEquals(1_000.0, balance(otherDest), 1e-9)
    }
    @Test fun invalidOrMissingAccountsLeaveRecordBalancesAndDiskUnchanged() = runTest {
        val source = account(AccountType.BANK); val original = insert(source)
        val accounts = InMemoryDatabase.accounts.value
        val disk = File(context.filesDir, "vibefinance_data.json").readText()
        for (invalid in listOf(original.copy(toAccountId = source), original.copy(toAccountId = 9999),
            original.copy(accountId = 9999, toAccountId = source))) {
            try { repository.updateTransaction(invalid, original); fail("Expected invalid transfer rejection") }
            catch (_: IllegalArgumentException) { }
            assertEquals(accounts, InMemoryDatabase.accounts.value)
            assertEquals(listOf(original), InMemoryDatabase.transactions.value)
            assertEquals(disk, File(context.filesDir, "vibefinance_data.json").readText())
        }
    }
    @Test fun failedDiskWritePreservesOriginalFinancialState() = runTest {
        val source = account(AccountType.BANK); val dest = account(AccountType.CASH)
        val original = insert(source); val accounts = InMemoryDatabase.accounts.value
        val file = File(context.filesDir, "vibefinance_data.json"); val disk = file.readText()
        File(context.filesDir, "vibefinance_data.json.new").mkdir()
        try { repository.updateTransaction(original.copy(toAccountId = dest), original); fail("Expected storage failure") }
        catch (_: java.io.IOException) { }
        assertEquals(accounts, InMemoryDatabase.accounts.value)
        assertEquals(listOf(original), InMemoryDatabase.transactions.value); assertEquals(disk, file.readText())
    }
    @Test fun futureConversionsDoNotChangeCurrentBalances() = runTest {
        val source = account(AccountType.BANK); val dest = account(AccountType.CC)
        val original = insert(source, future = true)
        repository.updateTransaction(original.withHistoryEdit(9.9, false, "Food", "Later", true,
            isTransfer = true, destinationAccountId = dest), original)
        assertEquals(1_000.0, balance(source), 0.0); assertEquals(1_000.0, balance(dest), 0.0)
    }
    @Test fun staleEditCannotApplyBalancesTwice() = runTest {
        val source = account(AccountType.BANK); val dest = account(AccountType.CASH); val original = insert(source)
        val transfer = original.withHistoryEdit(9.9, false, "Food", "Transfer", true,
            isTransfer = true, destinationAccountId = dest)
        repository.updateTransaction(transfer, original); val accounts = InMemoryDatabase.accounts.value
        try { repository.updateTransaction(transfer, original); fail("Expected stale edit rejection") }
        catch (_: IllegalArgumentException) { }
        assertEquals(accounts, InMemoryDatabase.accounts.value)
    }
    @Test fun conversionDetachesOnlyThisRecordFromInstallmentPlan() {
        val original = TransactionEntity(id = 10, amount = 50.0, category = "Food", accountId = 1,
            timestamp = 1, groupId = "plan", installmentNumber = 2, totalInstallments = 6)
        val transfer = original.withHistoryEdit(50.0, false, "Food", "", true,
            isTransfer = true, destinationAccountId = 2)
        assertNull(transfer.groupId); assertNull(transfer.installmentNumber); assertNull(transfer.totalInstallments)
        assertEquals(10L, transfer.id); assertEquals(1L, transfer.timestamp)
    }
}
