package com.example.vibefinance.data

import com.example.vibefinance.data.entity.*
import com.example.vibefinance.data.repository.*
import com.example.vibefinance.ui.history.HistoryKind
import com.example.vibefinance.ui.history.HistoryQuery
import com.example.vibefinance.ui.history.withHistoryEdit
import com.example.vibefinance.util.Reimbursements
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HistoryReimbursementEditTest {
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
    private fun stored(id: Long) = InMemoryDatabase.transactions.value.single { it.id == id }
    private suspend fun add(amount: Double, time: Long = System.currentTimeMillis() - 10000): TransactionEntity {
        val id = repo.insertTransaction(TransactionEntity(amount = amount, category = if (amount > 0) "Food" else "Salary",
            timestamp = time, accountId = bank, description = "測試"))
        return stored(id)
    }
    private fun edit(tx: TransactionEntity, parent: TransactionEntity?, amount: Double = kotlin.math.abs(tx.amount), account: Long = tx.accountId) =
        tx.withHistoryEdit(amount, true, "Salary", "收回 ↩️", true, sourceAccountId = account, repaymentExpense = parent)
    private fun fails(block: () -> Unit) { try { block(); fail("Expected rejection") } catch (_: IllegalArgumentException) {} }

    @Test fun incomeConversionKeepsIdentityBalanceAndDateWhileRestoringBudget() = runTest {
        val today = LocalDate.now(); val zone = ZoneId.systemDefault()
        val start = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val parent = add(100.0, start)
        val income = add(-30.0, System.currentTimeMillis() - 1)
        InMemoryDatabase.insertBudget(BudgetEntity("ACTIVE_PERIOD", 1000.0, start,
            today.plusDays(9).atStartOfDay(zone).toInstant().toEpochMilli()))
        val replacement = edit(income, parent)
        repo.updateTransaction(replacement, income, parent)
        assertEquals(930.0, balance(bank), 0.0)
        assertEquals(2, InMemoryDatabase.transactions.value.size)
        assertEquals(income.id, replacement.id); assertEquals(income.timestamp, replacement.timestamp)
        assertEquals(parent.id, replacement.reimbursementOf)
        assertFalse(replacement.isExcludedFromDailyBudget)
        assertEquals(930.0, BudgetRepository().getDailyAllowanceFlow().first().monthlyRemaining, 0.0)
        assertTrue(HistoryQuery(kind = HistoryKind.INCOME).apply(InMemoryDatabase.transactions.value, InMemoryDatabase.accounts.value).isEmpty())
        InMemoryDatabase.initialize(context)
        assertEquals(replacement, stored(income.id)); assertEquals(930.0, balance(bank), 0.0)
        assertTrue(add(1.0).id > income.id)
    }

    @Test fun updatingOwnRepaymentExcludesItsPreviousAmountFromTheCap() = runTest {
        val parent = add(100.0)
        val id = InMemoryDatabase.recordReimbursement(parent, 30.0, bank, "first", "first")
        InMemoryDatabase.recordReimbursement(parent, 20.0, bank, "other", "other")
        val old = stored(id)
        repo.updateTransaction(edit(old, parent, 80.0), old, parent)
        assertEquals(1000.0, balance(bank), 0.0)
        assertEquals(0, Reimbursements.outstanding(parent, InMemoryDatabase.transactions.value).signum())
        val current = stored(id); val before = InMemoryDatabase.transactions.value
        InMemoryDatabase.updateTransactionWithBalances(edit(current, parent, 80.01), current, parent)
        assertEquals(-80.01, stored(id).amount, 0.0); assertEquals(1000.01, balance(bank), 0.00001)
    }

    @Test fun standaloneCanLinkUnlinkAndBecomeIncomeWithoutChangingIdentity() = runTest {
        val parent = add(200.0)
        val id = InMemoryDatabase.recordReimbursement(null, 230.0, bank, "", "unlinked")
        var old = stored(id)
        val linked = edit(old, parent, 230.0)
        repo.updateTransaction(linked, old, parent)
        assertFalse(stored(id).isUnlinkedReimbursement)
        old = stored(id)
        repo.updateTransaction(old.withHistoryEdit(230.0, true, "Reimbursement", "", true, unlinkedRepayment = true), old)
        assertNull(stored(id).reimbursementOf)
        assertTrue(stored(id).isReimbursement)
        old = stored(id)
        repo.updateTransaction(old.withHistoryEdit(230.0, true, "Salary", "", true), old)
        assertFalse(stored(id).isReimbursement)
        assertEquals(1030.0, balance(bank), 0.0)
    }

    @Test fun accountMoveRelinkAndSwitchOffReverseOnlyThePreviousBalanceEffect() = runTest {
        val first = add(100.0); val second = add(80.0)
        val id = InMemoryDatabase.recordReimbursement(first, 30.0, bank, "", "move")
        var old = stored(id)
        repo.updateTransaction(edit(old, first, 50.0, card), old, first)
        assertEquals(820.0, balance(bank), 0.0); assertEquals(150.0, balance(card), 0.0)
        old = stored(id)
        repo.updateTransaction(edit(old, second), old, second)
        assertEquals(820.0, balance(bank), 0.0); assertEquals(150.0, balance(card), 0.0)
        assertEquals(100.0, Reimbursements.outstanding(first, InMemoryDatabase.transactions.value).toDouble(), 0.0)
        assertEquals(30.0, Reimbursements.outstanding(second, InMemoryDatabase.transactions.value).toDouble(), 0.0)
        old = stored(id)
        repo.updateTransaction(edit(old, null), old)
        assertNull(stored(id).reimbursementOf)
        assertTrue(stored(id).isExcludedFromDailyBudget)
        assertEquals(150.0, balance(card), 0.0)
        assertEquals(80.0, Reimbursements.outstanding(second, InMemoryDatabase.transactions.value).toDouble(), 0.0)
    }

    @Test fun staleParentSnapshotIsRejectedWithoutChangingTheReceipt() = runTest {
        val parent = add(100.0)
        val id = InMemoryDatabase.recordReimbursement(parent, 30.0, bank, "", "stale")
        val receipt = stored(id)
        repo.updateTransaction(parent.copy(description = "changed"), parent)
        val before = InMemoryDatabase.transactions.value
        fails { InMemoryDatabase.updateTransactionWithBalances(edit(receipt, parent, 50.0), receipt, parent) }
        assertEquals(before, InMemoryDatabase.transactions.value); assertEquals(930.0, balance(bank), 0.0)
    }

    @Test fun failedStorageRetainsReceiptBalancesAndAllowsTheSameEditToRetry() = runTest {
        val parent = add(100.0)
        val id = InMemoryDatabase.recordReimbursement(parent, 30.0, bank, "", "disk")
        val receipt = stored(id); val replacement = edit(receipt, parent, 50.0, card)
        val blocked = File(context.filesDir, "vibefinance_data.json.new").apply { mkdirs() }
        File(blocked, "block").writeText("x")
        try { repo.updateTransaction(replacement, receipt, parent); fail("Expected storage failure") } catch (_: java.io.IOException) {}
        assertEquals(receipt, stored(id)); assertEquals(930.0, balance(bank), 0.0); assertEquals(200.0, balance(card), 0.0)
        blocked.deleteRecursively()
        repo.updateTransaction(replacement, receipt, parent)
        assertEquals(900.0, balance(bank), 0.0); assertEquals(150.0, balance(card), 0.0)
    }

    @Test fun futureReceiptsLaterDayParentsSelfLinksAndInvestmentReceiversAreRejected() = runTest {
        val parent = add(100.0)
        val olderIncome = add(-30.0, java.time.Instant.ofEpochMilli(parent.timestamp).atZone(ZoneId.systemDefault()).minusDays(1).toInstant().toEpochMilli())
        fails { edit(olderIncome, parent) }
        fails { edit(parent, parent) }
        val futureIncome = add(-30.0, System.currentTimeMillis() + 86400000)
        fails { InMemoryDatabase.updateTransactionWithBalances(edit(futureIncome, parent), futureIncome, parent) }
        val investment = InMemoryDatabase.insertAccount(AccountEntity(name = "Investment", type = AccountType.INVESTMENT, balance = 0.0, icon = "bank"))
        val income = add(-30.0, System.currentTimeMillis() - 1)
        val before = InMemoryDatabase.transactions.value
        fails { InMemoryDatabase.updateTransactionWithBalances(edit(income, parent, account = investment), income, parent) }
        assertEquals(before, InMemoryDatabase.transactions.value); assertEquals(0.0, balance(investment), 0.0)
    }

    @Test fun sameDayLaterExpenseLinksWithoutChangingReceiptIdentityOrTimeAndSurvivesReload() = runTest {
        val day = LocalDate.now().withDayOfMonth(1).minusMonths(1).withDayOfMonth(26)
        val zone = ZoneId.systemDefault()
        val early = day.atTime(8, 0).atZone(zone).toInstant().toEpochMilli()
        val late = day.atTime(18, 0).atZone(zone).toInstant().toEpochMilli()
        val parent = add(100.0, late)
        val income = add(-30.0, early)
        repo.updateTransaction(edit(income, parent), income, parent)
        assertEquals(income.id, stored(income.id).id)
        assertEquals(early, stored(income.id).timestamp)
        assertEquals(parent.id, stored(income.id).reimbursementOf)
        assertEquals(930.0, balance(bank), 0.0)
        val second = InMemoryDatabase.recordReimbursement(parent, 20.0, bank, "Same day", "same-day", early)
        assertEquals(early, stored(second).timestamp)
        assertEquals(50.0, Reimbursements.outstanding(parent, InMemoryDatabase.transactions.value).toDouble(), 0.0)
        InMemoryDatabase.initialize(context)
        assertEquals(early, stored(income.id).timestamp)
        assertEquals(950.0, balance(bank), 0.0)
        val before = InMemoryDatabase.transactions.value
        fails { InMemoryDatabase.recordReimbursement(parent, 1.0, bank, "", "earlier-day",
            day.minusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()) }
        val future = add(10.0, System.currentTimeMillis() + 3600000)
        val current = stored(income.id)
        fails { InMemoryDatabase.updateTransactionWithBalances(current.copy(reimbursementOf = future.id), current, future) }
        assertEquals(current, stored(current.id))
        assertEquals(before.size + 1, InMemoryDatabase.transactions.value.size)
    }

    @Test fun linkedOriginalCannotBeConvertedAndEnteringRepaymentDetachesInstallmentMetadata() = runTest {
        val parent = add(100.0)
        InMemoryDatabase.recordReimbursement(parent, 30.0, bank, "", "linked")
        val before = InMemoryDatabase.transactions.value
        fails { InMemoryDatabase.updateTransactionWithBalances(edit(parent, null), parent) }
        assertEquals(before, InMemoryDatabase.transactions.value)
        val installment = add(20.0, System.currentTimeMillis() - 1).copy(installmentNumber = 1, totalInstallments = 6, groupId = "plan")
        val converted = edit(installment, parent)
        assertEquals(installment.id, converted.id)
        assertNull(converted.installmentNumber); assertNull(converted.totalInstallments); assertNull(converted.groupId)
    }
}
