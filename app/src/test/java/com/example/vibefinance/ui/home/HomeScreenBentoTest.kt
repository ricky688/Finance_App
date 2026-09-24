package com.example.vibefinance.ui.home

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibefinance.data.repository.DailyBudgetInfo
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class HomeScreenBentoTest {

    @Test
    fun testBuckwheatFinancialStateTransitions() {
        val now = System.currentTimeMillis()
        val tomorrow = now + 86400000L

        // State 0: No budget set (brand new / fresh launch)
        val noBudgetInfo = DailyBudgetInfo(
            totalMonthlyBudget = 0.0,
            totalSpentThisMonth = 0.0,
            monthlyRemaining = 0.0,
            dailyAllowance = 0.0,
            dailyRemaining = 0.0,
            daysLeft = 0,
            startDate = 0L,
            endDate = 0L
        )
        assertEquals(HeroDailyBudgetState.NO_BUDGET, calculateHeroDailyBudgetState(noBudgetInfo, now))

        // State 1: Normal healthy budget
        val normalInfo = DailyBudgetInfo(
            totalMonthlyBudget = 3000.0,
            totalSpentThisMonth = 500.0,
            monthlyRemaining = 2500.0,
            dailyAllowance = 100.0,
            dailyRemaining = 75.0,
            daysLeft = 25,
            startDate = now - 86400000L * 5,
            endDate = tomorrow + 86400000L * 24
        )
        assertEquals(HeroDailyBudgetState.NORMAL, calculateHeroDailyBudgetState(normalInfo, now))

        // State 2: Overdraft (spent more than daily allowance, but monthly budget still available)
        val overdraftInfo = DailyBudgetInfo(
            totalMonthlyBudget = 3000.0,
            totalSpentThisMonth = 650.0,
            monthlyRemaining = 2350.0,
            dailyAllowance = 100.0,
            dailyRemaining = -50.0,
            newDailyBudget = 97.9,
            daysLeft = 24,
            startDate = now - 86400000L * 6,
            endDate = tomorrow + 86400000L * 23
        )
        assertEquals(HeroDailyBudgetState.OVERDRAFT, calculateHeroDailyBudgetState(overdraftInfo, now))

        // State 3: Budget End (monthly budget exhausted)
        val budgetEndInfo = DailyBudgetInfo(
            totalMonthlyBudget = 3000.0,
            totalSpentThisMonth = 3100.0,
            monthlyRemaining = 0.0,
            dailyAllowance = 100.0,
            dailyRemaining = -100.0,
            newDailyBudget = 0.0,
            daysLeft = 10,
            startDate = now - 86400000L * 20,
            endDate = tomorrow + 86400000L * 9
        )
        assertEquals(HeroDailyBudgetState.BUDGET_END, calculateHeroDailyBudgetState(budgetEndInfo, now))

        // State 4: Period Ended (daysLeft <= 0 or past endDate)
        val periodEndedInfo = DailyBudgetInfo(
            totalMonthlyBudget = 3000.0,
            totalSpentThisMonth = 2800.0,
            monthlyRemaining = 200.0,
            dailyAllowance = 100.0,
            dailyRemaining = 50.0,
            daysLeft = 0,
            startDate = now - 86400000L * 30,
            endDate = now - 1000L
        )
        assertEquals(HeroDailyBudgetState.PERIOD_ENDED, calculateHeroDailyBudgetState(periodEndedInfo, now))
    }

    @Test
    fun testBentoResponsivePaddingRules() {
        // Compact devices (< 400dp, e.g. 360dp, 392dp Waydroid)
        assertEquals(12.dp, resolveBentoSpacing(360))
        assertEquals(12.dp, resolveScreenPadding(360))
        assertEquals(12.dp, resolveTopBarPadding(360))

        assertEquals(12.dp, resolveBentoSpacing(392))
        assertEquals(12.dp, resolveScreenPadding(392))
        assertEquals(12.dp, resolveTopBarPadding(392))

        // Normal / Expanded devices (>= 400dp, e.g. 412dp, 600dp)
        assertEquals(16.dp, resolveBentoSpacing(412))
        assertEquals(16.dp, resolveScreenPadding(412))
        assertEquals(20.dp, resolveTopBarPadding(412))

        assertEquals(16.dp, resolveBentoSpacing(600))
        assertEquals(16.dp, resolveScreenPadding(600))
        assertEquals(20.dp, resolveTopBarPadding(600))
    }

    @Test
    fun testHeroAmountFontSizeAutoScaling() {
        assertEquals(40.sp, calculateHeroAmountFontSize("HK$ 0"))
        assertEquals(36.sp, calculateHeroAmountFontSize("HK$ 50"))
        assertEquals(32.sp, calculateHeroAmountFontSize("HK$ 1,200"))
        assertEquals(28.sp, calculateHeroAmountFontSize("HK$ 12,500"))
        assertEquals(24.sp, calculateHeroAmountFontSize("HK$ 1,250,000"))
        assertEquals(28.sp, calculateHeroAmountFontSize("-HK$ 1,200"))
    }

    @Test
    fun testBentoStatCardValueFontSizeAutoScaling() {
        // Normal short amounts
        assertEquals(20.sp, calculateStatCardValueFontSize("$5.00", isCompact = true))
        assertEquals(22.sp, calculateStatCardValueFontSize("$5.00", isCompact = false))

        // Mid-sized amounts ($120.00 -> 7 chars)
        assertEquals(19.sp, calculateStatCardValueFontSize("$120.00", isCompact = true))

        // Large amounts ($1,250.00 -> 9 chars)
        assertEquals(17.sp, calculateStatCardValueFontSize("$1,250.00", isCompact = true))

        // Very large amounts ($12,500.00 -> 10 chars)
        assertEquals(17.sp, calculateStatCardValueFontSize("$12,500.00", isCompact = true))

        // Extreme amounts ($1,250,000.00 -> 13 chars)
        assertEquals(15.sp, calculateStatCardValueFontSize("$1,250,000.00", isCompact = true))

        // With explicitly specified font size
        assertEquals(24.sp * 0.85f, calculateStatCardValueFontSize("$120.00", isCompact = true, specifiedFontSize = 24.sp))
        assertEquals(24.sp * 0.75f, calculateStatCardValueFontSize("$12,500.00", isCompact = true, specifiedFontSize = 24.sp))
        assertEquals(24.sp, calculateStatCardValueFontSize("$120.00", isCompact = false, specifiedFontSize = 24.sp))
    }

    @Test
    fun testDaysLeftProgressCalculation() {
        assertEquals(1.0f, calculateDaysLeftProgress(30, 30), 0.001f)
        assertEquals(0.5f, calculateDaysLeftProgress(15, 30), 0.001f)
        assertEquals(0.1f, calculateDaysLeftProgress(3, 30), 0.001f)
        assertEquals(0.0f, calculateDaysLeftProgress(0, 30), 0.001f)
        assertEquals(0.0f, calculateDaysLeftProgress(-2, 30), 0.001f)
        assertEquals(1.0f, calculateDaysLeftProgress(35, 30), 0.001f)
        assertEquals(0.0f, calculateDaysLeftProgress(10, 0), 0.001f)
    }

    @Test
    fun testSpendingRatioHealthVibeMapping() {
        // Spent 20 of 100 -> ratio 0.20 <= 0.5
        assertEquals(DailyVibeCategory.SAVING_VIBE, resolveDailyVibeCategory(100.0, 80.0))
        // Spent 50 of 100 -> ratio 0.50 <= 0.5
        assertEquals(DailyVibeCategory.SAVING_VIBE, resolveDailyVibeCategory(100.0, 50.0))
        // Spent 75 of 100 -> ratio 0.75 <= 1.0
        assertEquals(DailyVibeCategory.STEADY_VIBE, resolveDailyVibeCategory(100.0, 25.0))
        // Spent 100 of 100 -> ratio 1.00 <= 1.0
        assertEquals(DailyVibeCategory.STEADY_VIBE, resolveDailyVibeCategory(100.0, 0.0))
        // Spent 120 of 100 -> ratio 1.20 > 1.0
        assertEquals(DailyVibeCategory.OVERSPENT_ALERT, resolveDailyVibeCategory(100.0, -20.0))
    }

    @Test
    fun testDateRangeAndDayCountCalculation() {
        val start = LocalDate.of(2026, 9, 1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = LocalDate.of(2026, 9, 30).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val dayCount = calculatePeriodTotalDays(start, end)
        assertEquals(30, dayCount)

        // Fallback for invalid or uninitialized dates
        assertEquals(30, calculatePeriodTotalDays(0L, 0L))
    }

    @Test
    fun testHeroTargetAmountFormattingAndDeficitSign() {
        // Period ended with deficit (negative remaining budget)
        val deficitFormatted = formatHeroTargetAmount(
            targetAmount = -500f,
            isPeriodEnded = true,
            monthlyRemaining = -500.0
        )
        assertEquals("-HK$ 500", deficitFormatted)

        // Period ended with positive remaining budget
        val surplusFormatted = formatHeroTargetAmount(
            targetAmount = 500f,
            isPeriodEnded = true,
            monthlyRemaining = 500.0
        )
        assertEquals("HK$ 500", surplusFormatted)

        // Active period normal
        val activeFormatted = formatHeroTargetAmount(
            targetAmount = 150f,
            isPeriodEnded = false,
            monthlyRemaining = 1500.0
        )
        assertEquals("HK$ 150", activeFormatted)

        // Active period overdraft with positive remaining
        val overdraftFormatted = formatHeroTargetAmount(
            targetAmount = 85f,
            isPeriodEnded = false,
            monthlyRemaining = 2000.0
        )
        assertEquals("HK$ 85", overdraftFormatted)
    }
}
