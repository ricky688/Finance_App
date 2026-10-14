package com.example.vibefinance.data

import com.example.vibefinance.data.entity.BudgetEntity
import com.example.vibefinance.data.repository.BudgetRepository
import com.example.vibefinance.util.BackupStateCodec
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
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
class PeriodIncomeEstimateTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private val disk get() = File(context.filesDir, "vibefinance_data.json")
    private val staging get() = File(context.filesDir, "vibefinance_data.json.new")
    private lateinit var budget: BudgetEntity
    @Before fun before() {
        InMemoryDatabase.initialize(context); InMemoryDatabase.clearAllTables()
        val start = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        budget = BudgetEntity(totalBudgetAmount = 900.0, startDate = start, endDate = start + 9 * 86_400_000L)
        InMemoryDatabase.insertBudget(budget)
    }
    @After fun after() { staging.deleteRecursively(); InMemoryDatabase.clearAllTables() }
    private fun save(amount: Double?) = InMemoryDatabase.setPeriodIncomeEstimate(budget.startDate, budget.endDate, amount)
    private fun rejected(action: () -> Unit) { assertTrue(runCatching(action).isFailure) }

    @Test fun legacyEstimatePersistsButNoLongerOverridesTheAutomaticForecast() = runTest {
        val repository = BudgetRepository()
        val before = repository.getDailyAllowanceFlow().first()
        val accounts = InMemoryDatabase.accounts.value; val transactions = InMemoryDatabase.transactions.value
        save(12000.25)
        val after = repository.getDailyAllowanceFlow().first()
        assertEquals(before, after)
        assertNull(after.estimatedIncome)
        InMemoryDatabase.initialize(context)
        assertEquals(12000.25, InMemoryDatabase.budgets.value.single().estimatedIncome!!, 0.0)
        assertEquals(accounts, InMemoryDatabase.accounts.value); assertEquals(transactions, InMemoryDatabase.transactions.value)
    }
    @Test fun recordedIncomeRecomputesTheForecastWithoutWritingAnEstimate() = runTest {
        val start = java.time.Instant.ofEpochMilli(budget.startDate).atZone(ZoneId.systemDefault()).toLocalDate()
        val account = InMemoryDatabase.insertAccount(com.example.vibefinance.data.entity.AccountEntity(
            name = "Bank", type = com.example.vibefinance.data.entity.AccountType.BANK, balance = 0.0, icon = "bank"))
        val previous = start.minusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        InMemoryDatabase.insertTransaction(com.example.vibefinance.data.entity.TransactionEntity(
            amount = -1500.25, category = "Salary", timestamp = previous, accountId = account))
        val before = disk.readText()
        val info = BudgetRepository().getDailyAllowanceFlow().first()
        assertEquals(1500.25, info.estimatedIncome!!, 0.0)
        assertEquals(1, info.incomeEstimatePeriods); assertTrue(info.incomeEstimatePartialHistory)
        assertNull(InMemoryDatabase.budgets.value.single().estimatedIncome)
        assertEquals(before, disk.readText())
        assertEquals(900.0, info.totalMonthlyBudget, 0.0); assertEquals(0.0, info.totalSpentThisMonth, 0.0)
        InMemoryDatabase.initialize(context)
        assertEquals(info, BudgetRepository().getDailyAllowanceFlow().first())
    }
    @Test fun zeroIsAnEstimateAndBlankClearsIt() {
        save(0.0); assertEquals(0.0, InMemoryDatabase.budgets.value.single().estimatedIncome!!, 0.0)
        save(null); InMemoryDatabase.initialize(context)
        assertNull(InMemoryDatabase.budgets.value.single().estimatedIncome)
    }
    @Test fun budgetAmountEditKeepsEstimateButNewPeriodStartsUnset() {
        save(15000.0)
        InMemoryDatabase.insertBudget(budget.copy(totalBudgetAmount = 1200.0))
        assertEquals(15000.0, InMemoryDatabase.budgets.value.single().estimatedIncome!!, 0.0)
        InMemoryDatabase.insertBudget(budget.copy(startDate = budget.endDate + 1, endDate = budget.endDate + 86_400_000L))
        assertNull(InMemoryDatabase.budgets.value.single().estimatedIncome)
    }
    @Test fun stalePeriodAndInvalidNumbersAreRejectedWithoutChanges() {
        save(500.0)
        val before = InMemoryDatabase.budgets.value; val bytes = disk.readText()
        rejected { InMemoryDatabase.setPeriodIncomeEstimate(budget.startDate + 1, budget.endDate, 10.0) }
        listOf(-1.0, Double.NaN, Double.POSITIVE_INFINITY).forEach { rejected { save(it) } }
        assertEquals(before, InMemoryDatabase.budgets.value); assertEquals(bytes, disk.readText())
    }
    @Test fun failedStorageKeepsMemoryAndDiskAndAllowsRetry() {
        val before = InMemoryDatabase.budgets.value; val bytes = disk.readText()
        staging.mkdir()
        rejected { save(9000.0) }
        assertEquals(before, InMemoryDatabase.budgets.value); assertEquals(bytes, disk.readText())
        staging.deleteRecursively(); save(9000.0)
        assertEquals(9000.0, InMemoryDatabase.budgets.value.single().estimatedIncome!!, 0.0)
    }
    @Test fun legacySnapshotWithoutEstimateStillLoads() {
        val data = JSONObject(disk.readText())
        assertFalse(data.getJSONArray("budgets").getJSONObject(0).has("estimatedIncome"))
        InMemoryDatabase.initialize(context)
        assertEquals(budget, InMemoryDatabase.budgets.value.single())
    }
    @Test fun backupValidatesOptionalEstimateAndRejectsInvalidTypesOrNegativeValues() {
        save(1234.56)
        val data = JSONObject(disk.readText()); val preferences = BackupStateCodec.preferences(context)
        BackupStateCodec.validateData(data, preferences, emptySet())
        val entry = data.getJSONArray("budgets").getJSONObject(0)
        entry.put("estimatedIncome", "1234.56")
        rejected { BackupStateCodec.validateData(data, preferences, emptySet()) }
        entry.put("estimatedIncome", -1)
        rejected { BackupStateCodec.validateData(data, preferences, emptySet()) }
        entry.remove("estimatedIncome")
        BackupStateCodec.validateData(data, preferences, emptySet())
    }
}
