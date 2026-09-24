package com.example.vibefinance.data.repository

import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.data.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepository @Inject constructor() {
    
    fun getActiveBudgetFlow(): Flow<BudgetEntity?> {
        return InMemoryDatabase.budgets.map { list -> list.find { it.id == "ACTIVE_PERIOD" } }
    }

    suspend fun insertBudget(budget: BudgetEntity) {
        InMemoryDatabase.insertBudget(budget)
    }

    fun getDailyAllowanceFlow(): Flow<DailyBudgetInfo> {
        val budgetFlow = getActiveBudgetFlow()
        val transactionsFlow = InMemoryDatabase.transactions

        return combine(budgetFlow, transactionsFlow) { budget, transactions ->
            if (budget == null) {
                return@combine DailyBudgetInfo(
                    totalMonthlyBudget = 0.0,
                    totalSpentThisMonth = 0.0,
                    monthlyRemaining = 0.0,
                    dailyAllowance = 0.0,
                    dailyRemaining = 0.0,
                    daysLeft = 1,
                    startDate = 0L,
                    endDate = 0L
                )
            }

            val today = LocalDate.now()
            val todayStart = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            // Filter non-transfer, non-excluded expenses within the custom budget period
            val periodExpenses = transactions.filter {
                it.toAccountId == null && 
                !it.isExcludedFromDailyBudget && 
                it.amount > 0 &&
                it.timestamp >= budget.startDate &&
                it.timestamp <= budget.endDate
            }

            val tomorrowStart = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            // Separate past expenses vs today's expenses within this period
            val pastExpensesSum = periodExpenses.filter { it.timestamp < todayStart }.sumOf { it.amount }
            val todayExpensesSum = periodExpenses.filter { it.timestamp >= todayStart && it.timestamp < tomorrowStart }.sumOf { it.amount }
            
            val totalSpent = pastExpensesSum + todayExpensesSum
            val remainingBudget = (budget.totalBudgetAmount - totalSpent).coerceAtLeast(0.0)
            val periodRemainingBeforeToday = (budget.totalBudgetAmount - pastExpensesSum).coerceAtLeast(0.0)

            val startLocal = Instant.ofEpochMilli(budget.startDate).atZone(ZoneId.systemDefault()).toLocalDate()
            val endLocal = Instant.ofEpochMilli(budget.endDate).atZone(ZoneId.systemDefault()).toLocalDate()
            val totalDaysInPeriod = (ChronoUnit.DAYS.between(startLocal, endLocal) + 1).coerceAtLeast(1).toDouble()
            val daysLeft = (ChronoUnit.DAYS.between(today, endLocal) + 1).coerceAtLeast(1).toInt()

            val standardBaseDaily = if (totalDaysInPeriod > 0) budget.totalBudgetAmount / totalDaysInPeriod else 0.0

            // Exact Buckwheat Rollover Engine
            val dailyAllowanceToday = when (budget.rolloverMode) {
                com.example.vibefinance.data.entity.RolloverMode.DISTRIBUTE_EVENLY -> {
                    // Buckwheat Distribute Evenly: Period remaining before today divided by remaining days (including today)
                    if (daysLeft > 0) periodRemainingBeforeToday / daysLeft else 0.0
                }
                com.example.vibefinance.data.entity.RolloverMode.ADD_TO_NEXT_DAY -> {
                    // Buckwheat Add to Next Day: Standard base daily + yesterday's unspent leftover
                    val yesterdayStart = today.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val yesterdayExpenses = periodExpenses.filter { it.timestamp >= yesterdayStart && it.timestamp < todayStart }.sumOf { it.amount }
                    val yesterdayLeftover = (standardBaseDaily - yesterdayExpenses).coerceAtLeast(0.0)
                    (standardBaseDaily + yesterdayLeftover).coerceAtLeast(0.0)
                }
            }
            val dailyRemaining = dailyAllowanceToday - todayExpensesSum

            // Buckwheat Recalculation for following days if today is overspent or for baseline
            val futureDays = (daysLeft - 1).coerceAtLeast(1)
            val remainingAfterToday = (budget.totalBudgetAmount - totalSpent).coerceAtLeast(0.0)
            val newDailyBudget = if (daysLeft > 1) remainingAfterToday / futureDays else remainingAfterToday

            val tomorrowAllowance = when {
                dailyRemaining < 0.0 -> newDailyBudget
                budget.rolloverMode == com.example.vibefinance.data.entity.RolloverMode.ADD_TO_NEXT_DAY -> standardBaseDaily
                else -> if (daysLeft > 1) (periodRemainingBeforeToday - todayExpensesSum) / daysLeft else dailyAllowanceToday
            }

            DailyBudgetInfo(
                totalMonthlyBudget = budget.totalBudgetAmount,
                totalSpentThisMonth = totalSpent,
                monthlyRemaining = remainingBudget,
                dailyAllowance = dailyAllowanceToday,
                dailyRemaining = dailyRemaining,
                daysLeft = daysLeft,
                startDate = budget.startDate,
                endDate = budget.endDate,
                rolloverMode = budget.rolloverMode,
                newDailyBudget = newDailyBudget,
                tomorrowAllowance = tomorrowAllowance
            )
        }
    }
}

data class DailyBudgetInfo(
    val totalMonthlyBudget: Double,
    val totalSpentThisMonth: Double,
    val monthlyRemaining: Double,
    val dailyAllowance: Double,
    val dailyRemaining: Double,
    val daysLeft: Int,
    val startDate: Long,
    val endDate: Long,
    val rolloverMode: com.example.vibefinance.data.entity.RolloverMode = com.example.vibefinance.data.entity.RolloverMode.DISTRIBUTE_EVENLY,
    val newDailyBudget: Double = 0.0,
    val tomorrowAllowance: Double = 0.0
)
