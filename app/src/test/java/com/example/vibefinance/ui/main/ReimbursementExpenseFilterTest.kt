package com.example.vibefinance.ui.main

import com.example.vibefinance.data.entity.TransactionEntity
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class ReimbursementExpenseFilterTest {
    private val hk = ZoneId.of("Asia/Hong_Kong")
    private val day = LocalDate.of(2026, 10, 26)
    private fun expense(id: Long, date: LocalDate, hour: Int, minute: Int = 0, zone: ZoneId = hk,
        description: String = "Lunch", category: String = "Food") = TransactionEntity(id = id, amount = 100.0,
        accountId = 1, timestamp = date.atTime(hour, minute).atZone(zone).toInstant().toEpochMilli(),
        category = category, description = description)

    @Test fun selectedLocalDayIncludesBothEndsAndExcludesAdjacentDays() {
        val records = listOf(expense(1, day.minusDays(1), 23, 59), expense(2, day, 0, 1),
            expense(3, day, 23, 59), expense(4, day.plusDays(1), 0))
        assertEquals(listOf(2L, 3L), reimbursementExpensesForSearch(records, "", day.toEpochDay(), hk).map { it.id })
    }
    @Test fun dateMembershipUsesTheRequestedZoneRatherThanUtcDate() {
        val tx = expense(1, day, 0, 30)
        assertEquals(listOf(tx), reimbursementExpensesForSearch(listOf(tx), "", day.toEpochDay(), hk))
        assertTrue(reimbursementExpensesForSearch(listOf(tx), "", day.toEpochDay(), ZoneOffset.UTC).isEmpty())
        assertEquals(listOf(tx), reimbursementExpensesForSearch(listOf(tx), "", day.minusDays(1).toEpochDay(), ZoneOffset.UTC))
    }
    @Test fun dateAndUnicodeCategoryOrDescriptionQueriesIntersect() {
        val records = listOf(expense(1, day, 9, description = "午餐 ↩️"),
            expense(2, day, 10, description = "CAFE lunch"), expense(3, day.minusDays(1), 10, description = "cafe"))
        assertEquals(listOf(1L), reimbursementExpensesForSearch(records, " 午餐 ", day.toEpochDay(), hk).map { it.id })
        assertEquals(listOf(2L), reimbursementExpensesForSearch(records, "cafe", day.toEpochDay(), hk).map { it.id })
        assertEquals(listOf(1L, 2L), reimbursementExpensesForSearch(records, "FOOD", day.toEpochDay(), hk).map { it.id })
        assertTrue(reimbursementExpensesForSearch(records, "missing", day.toEpochDay(), hk).isEmpty())
    }
    @Test fun clearingDatePreservesQueryOrderAndOriginalSnapshots() {
        val records = listOf(expense(2, day.plusDays(1), 9), expense(1, day, 9))
        assertEquals(records, reimbursementExpensesForSearch(records, "Lunch", null, hk))
        assertEquals(listOf(1L), reimbursementExpensesForSearch(records, "Lunch", day.toEpochDay(), hk).map { it.id })
        assertEquals(100.0, records.first().amount, 0.0)
    }
    @Test fun daylightSavingRepeatedHourAndNextMidnightAreHandledAsLocalDays() {
        val zone = ZoneId.of("America/New_York"); val date = LocalDate.of(2026, 11, 1)
        val first = expense(1, date, 1, 30, zone)
        val repeated = first.copy(id = 2, timestamp = first.timestamp + 3600000)
        val nextDay = expense(3, date.plusDays(1), 0, zone = zone)
        assertEquals(listOf(1L, 2L), reimbursementExpensesForSearch(listOf(first, repeated, nextDay), "", date.toEpochDay(), zone).map { it.id })
    }
    @Test fun repaymentDayAllowsLaterTimesButRejectsFollowingDates() {
        val early = day.atTime(8, 0).atZone(hk).toInstant().toEpochMilli()
        val late = day.atTime(23, 59).atZone(hk).toInstant().toEpochMilli()
        assertTrue(com.example.vibefinance.util.Reimbursements.expenseOnOrBeforeDay(late, early, hk))
        assertTrue(com.example.vibefinance.util.Reimbursements.expenseOnOrBeforeDay(early - 86400000, early, hk))
        assertFalse(com.example.vibefinance.util.Reimbursements.expenseOnOrBeforeDay(
            day.plusDays(1).atStartOfDay(hk).toInstant().toEpochMilli(), early, hk))
    }
    @Test fun sameDayOrderingUsesCalendarDatesAcrossTheLongDstDay() {
        val zone = ZoneId.of("America/New_York"); val date = LocalDate.of(2026, 11, 1)
        val early = date.atTime(0, 1).atZone(zone).toInstant().toEpochMilli()
        val late = date.atTime(23, 59).atZone(zone).toInstant().toEpochMilli()
        assertTrue(late - early > 86400000)
        assertTrue(com.example.vibefinance.util.Reimbursements.expenseOnOrBeforeDay(late, early, zone))
        assertFalse(com.example.vibefinance.util.Reimbursements.expenseOnOrBeforeDay(
            date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli(), early, zone))
    }

}
