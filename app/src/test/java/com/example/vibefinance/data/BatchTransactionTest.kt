package com.example.vibefinance.data

import com.example.vibefinance.data.entity.*
import com.example.vibefinance.data.repository.TransactionRepository
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BatchTransactionTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private val repo = TransactionRepository()
    private var bank = 0L
    private var credit = 0L
    @Before fun before() {
        InMemoryDatabase.initialize(context); InMemoryDatabase.clearAllTables()
        bank = InMemoryDatabase.insertAccount(AccountEntity(name = "Bank", type = AccountType.BANK, balance = 1000.0, icon = "bank"))
        credit = InMemoryDatabase.insertAccount(AccountEntity(name = "Card", type = AccountType.CC, balance = 0.0, icon = "card"))
    }
    @After fun after() { File(context.filesDir, "vibefinance_data.json.new").deleteRecursively(); InMemoryDatabase.clearAllTables() }
    private fun balance(id: Long) = InMemoryDatabase.accounts.value.single { it.id == id }.balance
    private suspend fun add(amount: Double, source: Long = bank, destination: Long? = null, future: Boolean = false): TransactionEntity {
        val id = repo.insertTransaction(TransactionEntity(amount = amount, category = "Other", accountId = source, toAccountId = destination,
            timestamp = System.currentTimeMillis() + if (future) 86400000 else -1000, customIcon = "emoji:🍜", groupId = "group", installmentNumber = 1, totalInstallments = 2))
        return InMemoryDatabase.transactions.value.single { it.id == id }
    }
    @Test fun mixedOrdinaryAccountMoveReversesOldEffectsAndKeepsMetadataAndFutureBalances() = runTest {
        val records = listOf(add(100.0), add(-30.0), add(20.0, future = true))
        repo.batchTransactions(records, BatchTransactionOperation.Account(credit))
        assertEquals(1000.0, balance(bank), 0.0); assertEquals(70.0, balance(credit), 0.0)
        val moved = InMemoryDatabase.transactions.value
        assertEquals(records.map { it.id }, moved.map { it.id })
        assertEquals(records.map { it.customIcon }, moved.map { it.customIcon })
        assertTrue(moved.all { it.sourceWasCreditCard == true && it.accountId == credit && it.groupId == "group" })
        InMemoryDatabase.initialize(context)
        assertEquals(moved, InMemoryDatabase.transactions.value)
    }
    @Test fun mixedTypesCategoryAndTransferAccountMoveRejectAllChanges() = runTest {
        val expense = add(10.0); val income = add(-5.0); val transfer = add(25.0, destination = credit)
        val accounts = InMemoryDatabase.accounts.value; val records = InMemoryDatabase.transactions.value
        assertFails { InMemoryDatabase.batchTransactions(listOf(expense, income), BatchTransactionOperation.Category("Food")) }
        assertFails { InMemoryDatabase.batchTransactions(listOf(expense, transfer), BatchTransactionOperation.Account(credit)) }
        assertEquals(records, InMemoryDatabase.transactions.value); assertEquals(accounts, InMemoryDatabase.accounts.value)
    }
    @Test fun categoryEditPreservesAllOtherFieldsAndBalances() = runTest {
        val records = listOf(add(10.0), add(20.0))
        val accounts = InMemoryDatabase.accounts.value
        repo.batchTransactions(records, BatchTransactionOperation.Category("Food"))
        assertEquals(records.map { it.copy(category = "Food") }, InMemoryDatabase.transactions.value)
        assertEquals(accounts, InMemoryDatabase.accounts.value)
    }
    @Test fun deleteUndoIncludesTransfersAndPreservesUnrelatedLaterChangesAndIds() = runTest {
        val records = listOf(add(100.0), add(50.0, destination = credit), add(20.0, source = credit), add(40.0, future = true))
        val removed = repo.batchTransactions(records, BatchTransactionOperation.Delete)!!
        assertEquals(1000.0, balance(bank), 0.0); assertEquals(0.0, balance(credit), 0.0)
        val other = add(-30.0)
        repo.restoreBatch(removed)
        assertEquals(880.0, balance(bank), 0.0); assertEquals(-30.0, balance(credit), 0.0)
        assertTrue(InMemoryDatabase.transactions.value.contains(other))
        assertTrue(InMemoryDatabase.transactions.value.containsAll(records))
        assertTrue(add(1.0).id > other.id)
        assertFails { InMemoryDatabase.restoreTransactionBatch(removed) }
    }
    @Test fun balanceAdjustmentsDeleteAndUndoUseRecordedDelta() = runTest {
        val record = TransactionEntity(id = 50, amount = 60.0, category = "Adjustment", accountId = bank,
            timestamp = System.currentTimeMillis() - 1000, isBalanceAdjustment = true, balanceAdjustmentDelta = 60.0)
        InMemoryDatabase.insertTransactions(listOf(record)); InMemoryDatabase.updateBalance(bank, 1060.0)
        val batch = InMemoryDatabase.batchTransactions(listOf(InMemoryDatabase.transactions.value.single { it.id == record.id }), BatchTransactionOperation.Delete)!!
        assertEquals(1000.0, balance(bank), 0.0)
        InMemoryDatabase.restoreTransactionBatch(batch)
        assertEquals(1060.0, balance(bank), 0.0)
    }
    @Test fun staleSnapshotRejectsWholeBatch() = runTest {
        val records = listOf(add(10.0), add(20.0))
        InMemoryDatabase.updateTransactionWithBalances(records[0].copy(description = "Changed"), records[0])
        val before = InMemoryDatabase.transactions.value
        assertFails { InMemoryDatabase.batchTransactions(records, BatchTransactionOperation.Delete) }
        assertEquals(before, InMemoryDatabase.transactions.value)
    }
    @Test fun storageFailurePreservesMemoryDiskAndAllowsRetry() = runTest {
        val records = listOf(add(10.0), add(20.0))
        val accounts = InMemoryDatabase.accounts.value
        val disk = File(context.filesDir, "vibefinance_data.json"); val bytes = disk.readText()
        val blocked = File(context.filesDir, "vibefinance_data.json.new"); blocked.mkdir()
        assertFails { InMemoryDatabase.batchTransactions(records, BatchTransactionOperation.Delete) }
        assertEquals(records, InMemoryDatabase.transactions.value); assertEquals(accounts, InMemoryDatabase.accounts.value); assertEquals(bytes, disk.readText())
        blocked.deleteRecursively()
        InMemoryDatabase.batchTransactions(records, BatchTransactionOperation.Delete)
        assertTrue(InMemoryDatabase.transactions.value.isEmpty())
    }
    private fun assertFails(block: () -> Unit) { try { block(); fail("Expected atomic rejection") } catch (_: IllegalArgumentException) {} catch (_: java.io.IOException) {} }
}
