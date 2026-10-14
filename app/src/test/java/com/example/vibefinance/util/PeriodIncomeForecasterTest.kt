package com.example.vibefinance.util

import com.example.vibefinance.data.entity.TransactionEntity
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class PeriodIncomeForecasterTest {
    private val zone = ZoneId.of("Asia/Hong_Kong")
    private fun day(date: String, zone: ZoneId = this.zone) = LocalDate.parse(date).atStartOfDay(zone).toInstant().toEpochMilli()
    private fun tx(date: String, amount: Double, zone: ZoneId = this.zone) = TransactionEntity(
        amount = amount, category = "Salary", timestamp = day(date, zone), accountId = 1)
    private val origin = tx("2026-06-01", 10.0)
    private fun estimate(records: List<TransactionEntity>, start: String = "2026-10-01", end: String = "2026-10-31",
        today: String = "2026-10-14", zone: ZoneId = this.zone) = PeriodIncomeForecaster.estimate(
        day(start, zone), day(end, zone), records, LocalDate.parse(today), zone)

    @Test fun recentIncomeHasMoreWeight_andOnlyIncomeCounts() {
        val records = listOf(origin, tx("2026-07-15", -1000.0), tx("2026-08-15", -2000.0),
            tx("2026-09-15", -3000.0).copy(isExcludedFromDailyBudget = true),
            tx("2026-09-16", -50000.0).copy(toAccountId = 2),
            tx("2026-09-17", -60000.0).copy(isBalanceAdjustment = true),
            tx("2026-09-18", 70000.0), tx("2026-10-01", -80000.0), tx("2026-11-01", -90000.0))
        val forecast = estimate(records)!!
        assertEquals(2333.33, forecast.amount, .00001)
        assertEquals(3, forecast.periodsUsed); assertFalse(forecast.partialHistory)
    }
    @Test fun zeroIncomeGapsCountInsteadOfSkippingToHigherIncomePeriods() {
        val forecast = estimate(listOf(tx("2026-07-01", -600.0), tx("2026-09-15", -300.0)))!!
        assertEquals(250.0, forecast.amount, 0.0); assertEquals(3, forecast.periodsUsed)
    }
    @Test fun partialFirstPeriodIsExplained_andPeriodsBeforeHistoryAreNotInventedAsZero() {
        val forecast = estimate(listOf(tx("2026-09-15", -1200.25)))!!
        assertEquals(1200.25, forecast.amount, 0.0); assertEquals(1, forecast.periodsUsed)
        assertTrue(forecast.partialHistory)
    }
    @Test fun noHistoryAndCurrentOnlyReturnNoEstimate_butObservedZeroIncomeIsZero() {
        assertNull(estimate(emptyList()))
        assertNull(estimate(listOf(tx("2026-10-01", -5000.0))))
        assertNull(estimate(listOf(tx("2026-11-01", -5000.0))))
        assertEquals(0.0, estimate(listOf(tx("2026-09-01", 50.0)))!!.amount, 0.0)
    }
    @Test fun monthlyDay31BoundariesClampWithoutDrifting() {
        val forecast = estimate(listOf(tx("2025-12-01", 1.0), tx("2026-02-28", -100.0),
            tx("2026-03-30", -50.0), tx("2026-02-27", -20.0), tx("2026-03-31", -999.0)),
            "2026-03-31", "2026-04-29", "2026-04-15")!!
        assertEquals(81.67, forecast.amount, .00001)
    }
    @Test fun customSevenDayPeriodUsesLocalDatesAcrossDst_andExclusiveEnd() {
        val zone = ZoneId.of("America/New_York")
        val records = listOf(tx("2026-02-01", 1.0, zone), tx("2026-03-08", -70.0, zone),
            tx("2026-03-14", -30.0, zone), tx("2026-03-15", -999.0, zone))
        assertEquals(50.0, estimate(records, "2026-03-15", "2026-03-21", "2026-03-17", zone)!!.amount, 0.0)
    }
    @Test fun leapFebruaryIncludesThe29thButNotMarchIncome() {
        val forecast = estimate(listOf(tx("2023-12-01", 1.0), tx("2024-02-29", -100.0),
            tx("2024-01-31", -10.0), tx("2024-03-01", -999.0)),
            "2024-03-01", "2024-03-31", "2024-03-15")!!
        assertEquals(53.33, forecast.amount, .00001)
    }
    @Test fun singleDayBudgetUsesTheThreePrecedingLocalDays() {
        val forecast = estimate(listOf(origin, tx("2026-10-13", -30.0), tx("2026-10-12", -20.0),
            tx("2026-10-11", -10.0)), "2026-10-14", "2026-10-14")!!
        assertEquals(23.33, forecast.amount, .00001)
    }
    @Test fun futureBudgetDoesNotLearnFromAnUnfinishedPeriod() {
        val records = listOf(origin, tx("2026-09-15", -100.0), tx("2026-10-01", -999.0))
        val forecast = estimate(records, "2026-11-01", "2026-11-30")!!
        assertEquals(66.67, forecast.amount, .00001); assertEquals(2, forecast.periodsUsed)
    }
    @Test fun invalidAmountsAndReversedPeriodAreSafe_andMoneyRoundsAtTheEnd() {
        assertNull(estimate(listOf(origin), "2026-10-31", "2026-10-01"))
        val forecast = estimate(listOf(origin, tx("2026-09-01", -.01), tx("2026-09-02", -.02),
            tx("2026-09-03", Double.NaN), tx("2026-09-04", Double.NEGATIVE_INFINITY)))!!
        assertEquals(.02, forecast.amount, 0.0)
    }
}
