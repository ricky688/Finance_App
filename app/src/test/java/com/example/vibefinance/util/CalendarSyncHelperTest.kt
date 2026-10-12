package com.example.vibefinance.util

import android.content.Intent
import android.provider.CalendarContract
import com.example.vibefinance.data.entity.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.time.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CalendarSyncHelperTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private val zone = ZoneId.of("Asia/Hong_Kong")
    private val clock = Clock.fixed(Instant.parse("2026-01-01T04:00:00Z"), zone)
    private val card = AccountEntity(id = 4, name = "銀行, A; B\n中文", type = AccountType.CC, balance = 200.0, icon = "card", billingDate = 15, paymentDate = 31)
    @Test fun unicodeEscapingFoldingAndAlarmTimesAreValid() {
        val ics = CalendarSyncHelper.generateIcsContent(context, listOf(card.copy(name = "中文😀".repeat(50))), emptyList(), clock)
        assertTrue(ics.startsWith("BEGIN:VCALENDAR\r\n"))
        assertTrue(ics.endsWith("END:VCALENDAR\r\n"))
        assertTrue(ics.split("\r\n").all { it.toByteArray(Charsets.UTF_8).size <= 75 })
        assertTrue(ics.contains("DTSTART:20260131T090000"))
        assertTrue(ics.contains("TRIGGER:-P2D")); assertTrue(ics.contains("TRIGGER:PT0S")); assertFalse(ics.contains("-PT9H"))
        assertTrue(ics.contains("RRULE:FREQ=MONTHLY;BYMONTHDAY=31,-1;BYSETPOS=1"))
        assertEquals("銀行\\, A\\; B\\n中文", CalendarSyncHelper.escape(card.name))
    }
    @Test fun monthlyAndWeeklySubscriptionsRetainExistingScheduling() {
        fun subscription(id: Long, day: Int, frequency: String) = SubscriptionEntity(id, "服務", 20.0, "Other", frequency,
            LocalDate.of(2026, 1, day).atStartOfDay(zone).toInstant().toEpochMilli(), 1)
        val ics = CalendarSyncHelper.generateIcsContent(context, emptyList(), listOf(subscription(1, 31, "Monthly"), subscription(2, 20, "Monthly"), subscription(3, 20, "Weekly")), clock)
        val unfolded = ics.replace("\r\n ", "")
        assertTrue(unfolded.contains("RDATE:20260228T090000,20260328T090000"))
        assertTrue(unfolded.contains("20261228T090000")); assertFalse(unfolded.contains("20270128T090000"))
        assertTrue(unfolded.contains("RRULE:FREQ=MONTHLY\r\n")); assertTrue(unfolded.contains("RRULE:FREQ=WEEKLY"))
        assertEquals(3, Regex("BEGIN:VEVENT").findAll(ics).count())
    }
    @Test fun lateExportsFollowAlreadyClampedSubscriptionDatesWithoutMutatingOriginal() {
        val subscription = SubscriptionEntity(1, "服務", 1.0, "Other", "Monthly", LocalDate.of(2026, 1, 31).atStartOfDay(zone).toInstant().toEpochMilli(), 1)
        val later = Clock.fixed(Instant.parse("2026-03-01T04:00:00Z"), zone)
        val ics = CalendarSyncHelper.generateIcsContent(context, emptyList(), listOf(subscription), later)
        assertTrue(ics.contains("DTSTART:20260328T090000"))
        assertEquals(31, Instant.ofEpochMilli(subscription.nextPaymentDate).atZone(zone).dayOfMonth)
    }
    @Test fun nativeCalendarIntentUsesSelectedDateAndRecurringRuleWithoutProviderWrites() {
        val date = LocalDate.of(2026, 2, 28)
        val intent = CalendarSyncHelper.cardDeadlineIntent(context, card, date, zone)
        assertEquals(Intent.ACTION_INSERT, intent.action)
        assertEquals(CalendarContract.Events.CONTENT_URI, intent.data)
        assertEquals(date.atTime(9, 0).atZone(zone).toInstant().toEpochMilli(), intent.getLongExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, 0))
        assertEquals(CalendarSyncHelper.monthlyRule(31), intent.getStringExtra(CalendarContract.Events.RRULE))
    }
}
