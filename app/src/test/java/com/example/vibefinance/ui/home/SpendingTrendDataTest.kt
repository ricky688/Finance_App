package com.example.vibefinance.ui.home

import com.example.vibefinance.data.entity.TransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class SpendingTrendDataTest {
    private val zone = ZoneId.of("Asia/Hong_Kong")
    private val today = LocalDate.of(2026, 10, 21)
    private val days = (6L downTo 0L).map(today::minusDays)
    private fun expense(amount: Double, day: LocalDate = today, excluded: Boolean = false) = TransactionEntity(
        accountId = 1, amount = amount, category = "Food", isExcludedFromDailyBudget = excluded,
        timestamp = day.atStartOfDay(zone).toInstant().toEpochMilli())

    @Test fun stacksNonDailySeparatelyWithoutChangingDailyOrCumulativeValues() {
        val records = listOf(expense(12.0, days.first()), expense(20.0), expense(5.0), expense(1200.0, excluded = true))
        val result = spendingTrendDays(records, days, zone)
        assertEquals(25.0, result.last().daily, 0.0)
        assertEquals(1200.0, result.last().nonDaily, 0.0)
        assertEquals(1225.0, result.last().daily + result.last().nonDaily, 0.0)
        assertEquals(37.0, result.sumOf { it.daily }, 0.0)
        assertEquals(7, result.size)
    }

    @Test fun nonDailyOnlyDayHasGraySpendingAndZeroCurveValue() {
        val result = spendingTrendDays(listOf(expense(40.0, excluded = true), expense(60.0, excluded = true)), days, zone)
        assertEquals(0.0, result.sumOf { it.daily }, 0.0)
        assertEquals(100.0, result.last().nonDaily, 0.0)
        assertEquals(0.0, result.first().nonDaily, 0.0)
    }

    @Test fun transfersIncomeAdjustmentsInvalidAmountsAndOutsideDaysNeverBecomeGrayExpenses() {
        val base = expense(999.0, excluded = true)
        val records = listOf(base.copy(toAccountId = 2), base.copy(amount = -999.0),
            base.copy(isBalanceAdjustment = true), base.copy(amount = 0.0),
            base.copy(amount = Double.NaN), base.copy(amount = Double.POSITIVE_INFINITY),
            expense(999.0, days.first().minusDays(1), true), expense(999.0, today.plusDays(1), true))
        assertEquals(0.0, spendingTrendDays(records, days, zone).sumOf { it.daily + it.nonDaily }, 0.0)
    }

    @Test fun bucketsUseTheProvidedLocalTimeZoneIncludingMidnightBoundaries() {
        val record = expense(8.0, excluded = true).copy(timestamp = Instant.parse("2026-10-20T16:30:00Z").toEpochMilli())
        val hk = spendingTrendDays(listOf(record), days, zone)
        val utc = spendingTrendDays(listOf(record), days, ZoneId.of("UTC"))
        assertEquals(8.0, hk.last().nonDaily, 0.0)
        assertEquals(0.0, hk[5].nonDaily, 0.0)
        assertEquals(8.0, utc[5].nonDaily, 0.0)
    }
}
