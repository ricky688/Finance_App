package com.example.vibefinance.data.repository

import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.data.entity.BudgetEntity
import com.example.vibefinance.data.entity.TransactionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class BudgetRepositoryTest {

    @Test
    fun testCustomPeriodDailyAllowance_noExpenses() = runTest {
        val today = LocalDate.now()
        
        // Define a custom period: 10 days (from Today to Today + 9 days)
        val startDate = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endDate = today.plusDays(9).atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val daysLeft = 10 // ChronoUnit between today and today+9 is 9 + 1 = 10

        InMemoryDatabase.clearAllTables()
        InMemoryDatabase.insertBudget(
            BudgetEntity(
                id = "ACTIVE_PERIOD",
                totalBudgetAmount = 1000.0,
                startDate = startDate,
                endDate = endDate
            )
        )

        val repository = BudgetRepository()
        val result = repository.getDailyAllowanceFlow().first()

        // Daily Allowance should be 1000.0 / 10 days = 100.0
        val expectedAllowance = 1000.0 / daysLeft
        assertEquals(1000.0, result.totalMonthlyBudget, 0.01)
        assertEquals(0.0, result.totalSpentThisMonth, 0.01)
        assertEquals(expectedAllowance, result.dailyAllowance, 0.01)
        assertEquals(expectedAllowance, result.dailyRemaining, 0.01)
        assertEquals(daysLeft, result.daysLeft)
    }

    @Test
    fun testCustomPeriodDailyAllowance_withExpensesAndCarryOver() = runTest {
        val today = LocalDate.now()
        
        // Define a custom period: 5 days (from Yesterday to Today + 3 days)
        val yesterdayLocal = today.minusDays(1)
        val endLocal = today.plusDays(3)
        
        val startDate = yesterdayLocal.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endDate = endLocal.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val daysLeft = 4 // ChronoUnit between today and today+3 is 3 + 1 = 4 days left including today

        InMemoryDatabase.clearAllTables()
        InMemoryDatabase.insertBudget(
            BudgetEntity(
                id = "ACTIVE_PERIOD",
                totalBudgetAmount = 500.0,
                startDate = startDate,
                endDate = endDate
            )
        )

        // Past expense of $100 (yesterday - within the period)
        val yesterdayStart = yesterdayLocal.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val pastTx = TransactionEntity(id = 1, amount = 100.0, category = "Food", timestamp = yesterdayStart + 1000, accountId = 1)
        
        // Today's expense of $20 (today - within the period)
        val todayStart = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val todayTx = TransactionEntity(id = 2, amount = 20.0, category = "Transit", timestamp = todayStart + 1000, accountId = 1)

        // Excluded transfer of $50
        val transferTx = TransactionEntity(id = 3, amount = 50.0, category = "Transfer", timestamp = todayStart, accountId = 1, toAccountId = 2)

        // Excluded out-of-period transaction (e.g. logged before startDate)
        val beforePeriodTx = TransactionEntity(id = 4, amount = 90.0, category = "Legacy", timestamp = startDate - 10000, accountId = 1)

        InMemoryDatabase.insertTransaction(pastTx)
        InMemoryDatabase.insertTransaction(todayTx)
        InMemoryDatabase.insertTransaction(transferTx)
        InMemoryDatabase.insertTransaction(beforePeriodTx)

        val repository = BudgetRepository()
        val result = repository.getDailyAllowanceFlow().first()

        // Total spent within period = $100 (past) + $20 (today) = $120 (transfer and out-of-period excluded!)
        assertEquals(120.0, result.totalSpentThisMonth, 0.01)

        // Allowance for today = (Total Budget - Past Spent) / Days Left
        // = (500.0 - 100.0) / 4 days = 100.0
        val expectedAllowance = (500.0 - 100.0) / daysLeft
        assertEquals(100.0, result.dailyAllowance, 0.01)

        // Remaining today = Allowance - Today Spent = 100.0 - 20.0 = 80.0
        val expectedRemaining = expectedAllowance - 20.0
        assertEquals(80.0, result.dailyRemaining, 0.01)
        assertEquals(daysLeft, result.daysLeft)
    }

    @Test
    fun testAddToNextDayRollover() = runTest {
        val today = LocalDate.now()
        val yesterdayLocal = today.minusDays(1)
        val endLocal = today.plusDays(3) // 5 days total (yesterday, today, +3 days)

        val startDate = yesterdayLocal.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endDate = endLocal.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        InMemoryDatabase.clearAllTables()
        InMemoryDatabase.insertBudget(
            BudgetEntity(
                id = "ACTIVE_PERIOD",
                totalBudgetAmount = 500.0,
                startDate = startDate,
                endDate = endDate,
                rolloverMode = com.example.vibefinance.data.entity.RolloverMode.ADD_TO_NEXT_DAY
            )
        )

        // Yesterday's expense: $20 (base daily is 500 / 5 = 100, so leftover is $80)
        val yesterdayStart = yesterdayLocal.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val yesterdayTx = TransactionEntity(id = 1, amount = 20.0, category = "Food", timestamp = yesterdayStart + 1000, accountId = 1)
        InMemoryDatabase.insertTransaction(yesterdayTx)

        val repository = BudgetRepository()
        val result = repository.getDailyAllowanceFlow().first()

        // Today's daily target should be 100 (base) + 80 (yesterday leftover) = 180.0
        assertEquals(180.0, result.dailyAllowance, 0.01)
        assertEquals(180.0, result.dailyRemaining, 0.01)
        assertEquals(100.0, result.tomorrowAllowance, 0.01)
    }

    @Test
    fun testOverdraftRecalculation() = runTest {
        val today = LocalDate.now()
        val yesterdayLocal = today.minusDays(1)
        val endLocal = today.plusDays(3) // 5 days total: 1 past, 4 left (today + 3 future days)

        val startDate = yesterdayLocal.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endDate = endLocal.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        InMemoryDatabase.clearAllTables()
        InMemoryDatabase.insertBudget(
            BudgetEntity(
                id = "ACTIVE_PERIOD",
                totalBudgetAmount = 500.0,
                startDate = startDate,
                endDate = endDate,
                rolloverMode = com.example.vibefinance.data.entity.RolloverMode.DISTRIBUTE_EVENLY
            )
        )

        // Yesterday spent $100
        val yesterdayStart = yesterdayLocal.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        InMemoryDatabase.insertTransaction(TransactionEntity(id = 1, amount = 100.0, category = "Food", timestamp = yesterdayStart + 1000, accountId = 1))

        // Today spent $160 (target was (500-100)/4 = 100.0) -> Overdraft by $60!
        val todayStart = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        InMemoryDatabase.insertTransaction(TransactionEntity(id = 2, amount = 160.0, category = "Shopping", timestamp = todayStart + 1000, accountId = 1))

        val repository = BudgetRepository()
        val result = repository.getDailyAllowanceFlow().first()

        assertEquals(100.0, result.dailyAllowance, 0.01)
        assertEquals(-60.0, result.dailyRemaining, 0.01) // Overdraft!

        // Total remaining after today = 500 - 100 - 160 = 240.0
        // Future days (excluding today) = 3 days
        // New daily budget for future days = 240 / 3 = 80.0
        assertEquals(80.0, result.newDailyBudget, 0.01)
        assertEquals(80.0, result.tomorrowAllowance, 0.01)
    }
}
