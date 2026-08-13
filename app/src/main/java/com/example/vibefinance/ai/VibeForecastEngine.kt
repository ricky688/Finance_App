package com.example.vibefinance.ai

import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.data.repository.DailyBudgetInfo
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Locale

data class VibeForecastResult(
    val actualDailyVelocity: Double,
    val projectedEndSpent: Double,
    val projectedEndBalance: Double,
    val projectedSavings: Double,
    val daysElapsed: Int,
    val daysLeft: Int,
    val status: ForecastStatus,
    val message: String
)

enum class ForecastStatus {
    EXCELLENT_SAVINGS,
    STEADY_PACE,
    OVERSPEND_RISK
}

object VibeForecastEngine {

    fun calculateForecast(
        budgetInfo: DailyBudgetInfo,
        transactions: List<TransactionEntity>
    ): VibeForecastResult {
        val totalBudget = budgetInfo.totalMonthlyBudget
        val totalSpent = budgetInfo.totalSpentThisMonth
        val daysLeft = budgetInfo.daysLeft.coerceAtLeast(1)

        val startLocalDate = if (budgetInfo.startDate > 0) {
            Instant.ofEpochMilli(budgetInfo.startDate).atZone(ZoneId.systemDefault()).toLocalDate()
        } else {
            LocalDate.now().minusDays(15)
        }
        val todayLocalDate = LocalDate.now()

        val daysElapsed = (ChronoUnit.DAYS.between(startLocalDate, todayLocalDate) + 1).coerceAtLeast(1).toInt()
        val totalDays = (daysElapsed + daysLeft).coerceAtLeast(1)

        val actualDailyVelocity = if (daysElapsed > 0) totalSpent / daysElapsed else budgetInfo.dailyAllowance
        val projectedEndSpent = totalSpent + (actualDailyVelocity * daysLeft)
        val projectedEndBalance = totalBudget - projectedEndSpent
        val projectedSavings = projectedEndBalance.coerceAtLeast(0.0)

        val status = when {
            projectedEndBalance < 0 -> ForecastStatus.OVERSPEND_RISK
            projectedSavings >= (totalBudget * 0.15) -> ForecastStatus.EXCELLENT_SAVINGS
            else -> ForecastStatus.STEADY_PACE
        }

        val message = when (status) {
            ForecastStatus.EXCELLENT_SAVINGS -> {
                String.format(
                    Locale.US,
                    "At your current pace of HK$ %,.0f/day, you are projected to save HK$ %,.0f by the end of the period!",
                    actualDailyVelocity,
                    projectedSavings
                )
            }
            ForecastStatus.STEADY_PACE -> {
                String.format(
                    Locale.US,
                    "Your spending pace is steady at HK$ %,.0f/day. Projected remaining budget: HK$ %,.0f.",
                    actualDailyVelocity,
                    projectedEndBalance
                )
            }
            ForecastStatus.OVERSPEND_RISK -> {
                val deficit = -projectedEndBalance
                String.format(
                    Locale.US,
                    "Caution: At HK$ %,.0f/day velocity, spending will exceed budget by HK$ %,.0f. Consider adjusting daily allowance to HK$ %,.0f.",
                    actualDailyVelocity,
                    deficit,
                    (budgetInfo.monthlyRemaining / daysLeft).coerceAtLeast(0.0)
                )
            }
        }

        return VibeForecastResult(
            actualDailyVelocity = actualDailyVelocity,
            projectedEndSpent = projectedEndSpent,
            projectedEndBalance = projectedEndBalance,
            projectedSavings = projectedSavings,
            daysElapsed = daysElapsed,
            daysLeft = daysLeft,
            status = status,
            message = message
        )
    }
}
