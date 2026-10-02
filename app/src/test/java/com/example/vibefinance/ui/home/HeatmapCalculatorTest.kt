package com.example.vibefinance.ui.home

import com.example.vibefinance.data.entity.TransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class HeatmapCalculatorTest {

    @Test
    fun testMatrixDimensions() {
        val today = LocalDate.of(2026, 10, 2)
        val matrix = HeatmapCalculator.calculate(
            transactions = emptyList(),
            dailyBudget = 50.0,
            today = today,
            weeksCount = 16
        )

        assertEquals(16, matrix.weeks.size)
        matrix.weeks.forEach { week ->
            assertEquals(7, week.size)
        }
    }

    @Test
    fun testVibeClassificationAndStreak() {
        val today = LocalDate.of(2026, 10, 2)
        val zoneId = ZoneId.systemDefault()

        val t1 = TransactionEntity(
            id = 1,
            accountId = 1,
            amount = 25.0, // Under budget (25 <= 50 * 0.8 = 40)
            category = "Food",
            description = "Lunch",
            timestamp = today.atTime(12, 0).atZone(zoneId).toInstant().toEpochMilli()
        )
        val yesterday = today.minusDays(1)
        val t2 = TransactionEntity(
            id = 2,
            accountId = 1,
            amount = 45.0, // On target (40 < 45 <= 50)
            category = "Groceries",
            description = "Supermarket",
            timestamp = yesterday.atTime(18, 0).atZone(zoneId).toInstant().toEpochMilli()
        )
        val twoDaysAgo = today.minusDays(2)
        val t3 = TransactionEntity(
            id = 3,
            accountId = 1,
            amount = 70.0, // Over budget (70 > 50)
            category = "Shopping",
            description = "Shoes",
            timestamp = twoDaysAgo.atTime(15, 0).atZone(zoneId).toInstant().toEpochMilli()
        )

        val matrix = HeatmapCalculator.calculate(
            transactions = listOf(t1, t2, t3),
            dailyBudget = 50.0,
            today = today,
            weeksCount = 16
        )

        // Today and Yesterday were <= 50.0, TwoDaysAgo was 70.0 (over budget).
        // Streak is 2 days!
        assertEquals(2, matrix.currentStreak)
        assertEquals(3, matrix.totalTransactions)

        // Find today's day data
        val todayData = matrix.weeks.flatten().first { it.date == today }
        assertEquals(DaySpendingVibe.UNDER_BUDGET, todayData.vibe)
        assertEquals(25.0, todayData.spent, 0.01)
        assertTrue(todayData.isToday)

        // Find yesterday's day data
        val yesterdayData = matrix.weeks.flatten().first { it.date == yesterday }
        assertEquals(DaySpendingVibe.ON_TARGET, yesterdayData.vibe)
        assertEquals(45.0, yesterdayData.spent, 0.01)

        // Find two days ago data
        val twoDaysAgoData = matrix.weeks.flatten().first { it.date == twoDaysAgo }
        assertEquals(DaySpendingVibe.OVER_BUDGET, twoDaysAgoData.vibe)
        assertEquals(70.0, twoDaysAgoData.spent, 0.01)
    }
}
