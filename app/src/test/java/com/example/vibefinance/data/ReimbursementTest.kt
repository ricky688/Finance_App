package com.example.vibefinance.data

import com.example.vibefinance.data.entity.*
import com.example.vibefinance.data.repository.*
import com.example.vibefinance.ui.history.HistoryKind
import com.example.vibefinance.ui.history.HistoryQuery
import com.example.vibefinance.util.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.Executors

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ReimbursementTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private val repo = TransactionRepository()
    private var bank = 0L
    private var card = 0L
    @Before fun setup() {
        InMemoryDatabase.initialize(context); InMemoryDatabase.clearAllTables()
        bank = InMemoryDatabase.insertAccount(AccountEntity(name = "Bank", type = AccountType.BANK, balance = 1000.0, icon = "bank"))
        card = InMemoryDatabase.insertAccount(AccountEntity(name = "Card", type = AccountType.CC, balance = 200.0, icon = "card"))
    }
    @After fun cleanup() { File(context.filesDir, "vibefinance_data.json.new").deleteRecursively(); InMemoryDatabase.clearAllTables() }
    private fun balance(id: Long) = InMemoryDatabase.accounts.value.single { it.id == id }.balance
    private suspend fun expense(amount: Double = 100.0, excluded: Boolean = false, timestamp: Long = System.currentTimeMillis() - 1000): TransactionEntity {
        val id = repo.insertTransaction(TransactionEntity(amount = amount, category = "Food", timestamp = timestamp, accountId = bank,
            isExcludedFromDailyBudget = excluded))
        return InMemoryDatabase.transactions.value.single { it.id == id }
    }
    private fun fails(block: () -> Unit) { try { block(); fail("Expected rejection") } catch (_: IllegalArgumentException) {} }
    @Test fun excessAndStandaloneRestoreBudgetWithoutIncomeAndSurviveReload() = runTest {
        val parent = expense(200.0)
        val today = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        InMemoryDatabase.insertBudget(BudgetEntity("ACTIVE_PERIOD", 1000.0, today, today + 86400000L))
        InMemoryDatabase.recordReimbursement(parent, 230.0, bank, "extra", "excess")
        val standaloneId = InMemoryDatabase.recordReimbursement(null, 40.0, bank, "unlinked", "standalone")
        assertEquals(1070.0, balance(bank), 0.0)
        assertEquals(1070.0, BudgetRepository().getDailyAllowanceFlow().first().monthlyRemaining, 0.0)
        assertTrue(HistoryQuery(kind = HistoryKind.INCOME).apply(InMemoryDatabase.transactions.value, InMemoryDatabase.accounts.value).isEmpty())
        assertEquals(2, HistoryQuery(kind = HistoryKind.REIMBURSEMENT).apply(InMemoryDatabase.transactions.value, InMemoryDatabase.accounts.value).size)
        val snapshot = InMemoryDatabase.snapshotForBackup()
        BackupStateCodec.validateData(snapshot, BackupStateCodec.preferences(context), emptySet())
        InMemoryDatabase.initialize(context)
        val stored = InMemoryDatabase.transactions.value.single { it.id == standaloneId }
        assertTrue(stored.isUnlinkedReimbursement)
        assertNull(stored.reimbursementOf)
        assertEquals(CategoryKind.EXPENSE, stored.categoryKind)
        InMemoryDatabase.deleteTransactionWithBalances(standaloneId)
        assertEquals(1030.0, balance(bank), 0.0)
    }

    @Test fun partialRepeatedAndFullRepaymentsAreDurableAndIdempotent() = runTest {
        val original = expense()
        val id = InMemoryDatabase.recordReimbursement(original, 30.0, bank, "午餐", "first")
        assertEquals(930.0, balance(bank), 0.0)
        assertEquals(id, InMemoryDatabase.recordReimbursement(original, 30.0, bank, "午餐", "first"))
        assertEquals(930.0, balance(bank), 0.0)
        InMemoryDatabase.recordReimbursement(original, 70.0, bank, "remaining", "second")
        assertEquals(1000.0, balance(bank), 0.0)
        assertEquals(0, Reimbursements.outstanding(original, InMemoryDatabase.transactions.value).compareTo(java.math.BigDecimal.ZERO))
        InMemoryDatabase.initialize(context)
        assertEquals(2, InMemoryDatabase.transactions.value.count { it.reimbursementOf == original.id })
        assertEquals(1000.0, balance(bank), 0.0)
        val next = repo.insertTransaction(original.copy(id = 0, amount = 1.0))
        assertTrue(next > InMemoryDatabase.transactions.value.filter { it.reimbursementOf != null }.maxOf { it.id })
    }
    @Test fun excessStaleInvalidPrecisionAndInvalidAccountsDoNotChangeState() = runTest {
        val original = expense()
        InMemoryDatabase.recordReimbursement(original, 80.0, bank, "", "first")
        val before = InMemoryDatabase.transactions.value; val accounts = InMemoryDatabase.accounts.value
        listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY, 0.001).forEach { amount ->
            fails { InMemoryDatabase.recordReimbursement(original, amount, bank, "", "bad-$amount") }
        }
        fails { InMemoryDatabase.recordReimbursement(original.copy(amount = 90.0), 1.0, bank, "", "stale") }
        fails { InMemoryDatabase.recordReimbursement(original, 1.0, Long.MAX_VALUE, "", "account") }
        assertEquals(before, InMemoryDatabase.transactions.value); assertEquals(accounts, InMemoryDatabase.accounts.value)
    }
    @Test fun moneyReceivedTodayRestoresCurrentBudgetAndIsNotIncome() = runTest {
        val zone = ZoneId.systemDefault(); val today = LocalDate.now()
        fun millis(day: LocalDate) = day.atStartOfDay(zone).toInstant().toEpochMilli()
        val original = expense(timestamp = millis(today.minusDays(40)))
        InMemoryDatabase.insertBudget(BudgetEntity("ACTIVE_PERIOD", 1000.0, millis(today), millis(today.plusDays(9))))
        InMemoryDatabase.recordReimbursement(original, 30.0, bank, "return", "today")
        val budget = BudgetRepository().getDailyAllowanceFlow().first()
        assertEquals(1030.0, budget.monthlyRemaining, 0.0)
        assertEquals(130.0, budget.dailyRemaining, 0.0)
        val records = InMemoryDatabase.transactions.value
        assertTrue(HistoryQuery(kind = HistoryKind.INCOME).apply(records, InMemoryDatabase.accounts.value).isEmpty())
        assertEquals(1, HistoryQuery(kind = HistoryKind.REIMBURSEMENT).apply(records, InMemoryDatabase.accounts.value).size)
        assertEquals(CategoryKind.EXPENSE, records.last().categoryKind)
        val oldCredit = records.last().copy(timestamp = millis(today.minusDays(10)))
        assertEquals(0.0, requireNotNull(PeriodIncomeForecaster.estimate(millis(today), millis(today.plusDays(29)), listOf(original, oldCredit), today)).amount, 0.0)
    }
    @Test fun exclusionAndCreditCardBalanceDirectionArePreserved() = runTest {
        val original = expense(excluded = true)
        val id = InMemoryDatabase.recordReimbursement(original, 30.0, card, "", "credit")
        assertEquals(170.0, balance(card), 0.0)
        val receipt = InMemoryDatabase.transactions.value.single { it.id == id }
        assertTrue(receipt.isExcludedFromDailyBudget)
        assertTrue(receipt.sourceWasCreditCard == true)
        InMemoryDatabase.deleteTransactionWithBalances(id)
        assertEquals(200.0, balance(card), 0.0)
    }
    @Test fun linkedOriginalCannotBeRemovedOrReducedAndUndoKeepsLinks() = runTest {
        val original = expense()
        InMemoryDatabase.recordReimbursement(original, 60.0, bank, "", "part")
        fails { InMemoryDatabase.deleteTransactionWithBalances(original.id) }
        InMemoryDatabase.updateTransactionWithBalances(original.copy(amount = 50.0), original)
        val records = InMemoryDatabase.transactions.value
        val deleted = requireNotNull(InMemoryDatabase.batchTransactions(records, BatchTransactionOperation.Delete))
        assertEquals(1000.0, balance(bank), 0.0)
        InMemoryDatabase.restoreTransactionBatch(deleted)
        assertEquals(records, InMemoryDatabase.transactions.value)
        assertEquals(1010.0, balance(bank), 0.0)
    }
    @Test fun writeFailurePreservesBalancesRecordsAndRetryIdentity() = runTest {
        val original = expense()
        val blocked = File(context.filesDir, "vibefinance_data.json.new").apply { mkdirs() }
        File(blocked, "block").writeText("x")
        val before = InMemoryDatabase.transactions.value
        try { InMemoryDatabase.recordReimbursement(original, 20.0, bank, "", "retry"); fail("Expected storage failure") } catch (_: java.io.IOException) {}
        assertEquals(before, InMemoryDatabase.transactions.value); assertEquals(900.0, balance(bank), 0.0)
        blocked.deleteRecursively()
        InMemoryDatabase.recordReimbursement(original, 20.0, bank, "", "retry")
        assertEquals(920.0, balance(bank), 0.0)
    }
    @Test fun fractionalLegacyInstallmentsUseDisplayedCentPrecision() = runTest {
        val original = expense(amount = 10.0 / 3.0)
        InMemoryDatabase.recordReimbursement(original, 3.34, bank, "", "extra")
        InMemoryDatabase.recordReimbursement(original, 3.33, bank, "", "rounded")
        assertEquals(0, Reimbursements.outstanding(original, InMemoryDatabase.transactions.value).compareTo(java.math.BigDecimal.ZERO))
    }
    @Test fun backupValidatesRepaymentLinksAndAmounts() = runTest {
        val original = expense()
        InMemoryDatabase.recordReimbursement(original, 30.0, bank, "", "backup")
        val snapshot = InMemoryDatabase.snapshotForBackup()
        val prefs = BackupStateCodec.preferences(context)
        BackupStateCodec.validateData(snapshot, prefs, emptySet())
        val receipt = snapshot.getJSONArray("transactions").getJSONObject(1)
        receipt.put("reimbursementOf", Long.MAX_VALUE)
        fails { BackupStateCodec.validateData(snapshot, prefs, emptySet()) }
        receipt.put("reimbursementOf", original.id).put("amount", -100.01)
        BackupStateCodec.validateData(snapshot, prefs, emptySet())
        receipt.put("amount", -0.001)
        fails { BackupStateCodec.validateData(snapshot, prefs, emptySet()) }
    }
    @Test fun concurrentPartialRepaymentsCannotOverpay() = runTest {
        val original = expense()
        val pool = Executors.newFixedThreadPool(2)
        try {
            val attempts = (1..2).map { index -> pool.submit<Boolean> {
                runCatching { InMemoryDatabase.recordReimbursement(original, 60.0, bank, "", "race-$index") }.isSuccess
            } }
            assertEquals(2, attempts.count { it.get() })
            assertEquals(1020.0, balance(bank), 0.0)
        } finally { pool.shutdownNow() }
    }
}
