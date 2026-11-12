package com.example.vibefinance.data

import com.example.vibefinance.data.entity.*
import com.example.vibefinance.util.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File
import java.time.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SubscriptionIntervalTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private val zone = ZoneId.systemDefault()
    private var bank = 0L
    @Before fun setup() {
        InMemoryDatabase.initialize(context); InMemoryDatabase.clearAllTables()
        bank = InMemoryDatabase.insertAccount(AccountEntity(name = "Bank", type = AccountType.BANK, balance = 1000.0, icon = "bank"))
    }
    @After fun cleanup() { File(context.filesDir, "vibefinance_data.json.new").deleteRecursively(); InMemoryDatabase.clearAllTables() }
    private fun subscription(frequency: String = "Monthly", interval: Int = 1, amount: Double = 10.0,
        day: LocalDate = LocalDate.of(2026, 1, 31)) = SubscriptionEntity(name = "服務", amount = amount, category = "Other",
        frequency = frequency, interval = interval, nextPaymentDate = day.atTime(9, 0).atZone(zone).toInstant().toEpochMilli(), accountId = bank)
    @Test fun monthsYearsAndDaysUseTheRequestedIntervalAndClampShortMonths() {
        assertEquals(LocalDate.of(2026, 7, 31), SubscriptionSchedule.advance(LocalDate.of(2026, 1, 31), "Monthly", 6))
        assertEquals(LocalDate.of(2026, 9, 30), SubscriptionSchedule.advance(LocalDate.of(2026, 7, 31), "Monthly", 2))
        assertEquals(LocalDate.of(2026, 11, 30), SubscriptionSchedule.advance(LocalDate.of(2026, 9, 30), "Monthly", 2))
        assertEquals(LocalDate.of(2026, 2, 28), SubscriptionSchedule.advance(LocalDate.of(2024, 2, 29), "Yearly", 2))
        assertEquals(LocalDate.of(2026, 2, 2), SubscriptionSchedule.advance(LocalDate.of(2026, 1, 31), "Daily", 2))
    }
    @Test fun dailyCyclesKeepLocalTimeAcrossDaylightSaving() {
        val ny = ZoneId.of("America/New_York")
        val start = LocalDateTime.of(2026, 3, 7, 9, 30).atZone(ny)
        val sub = subscription("Daily", 2).copy(nextPaymentDate = start.toInstant().toEpochMilli())
        val next = Instant.ofEpochMilli(SubscriptionSchedule.advance(sub, zone = ny)).atZone(ny)
        assertEquals(LocalDateTime.of(2026, 3, 9, 9, 30), next.toLocalDateTime())
        assertEquals(47L, Duration.between(start.toInstant(), next.toInstant()).toHours())
    }
    @Test fun estimatesDivideByTheInterval() {
        assertEquals(10.0, SubscriptionSchedule.monthlyAmount(subscription(interval = 6, amount = 60.0)), 0.0001)
        assertEquals(43.33, SubscriptionSchedule.monthlyAmount(subscription("Weekly", 2, 20.0)), 0.0001)
        assertEquals(152.1875, SubscriptionSchedule.monthlyAmount(subscription("Daily", 2)), 0.0001)
        assertEquals(5.0, SubscriptionSchedule.monthlyAmount(subscription("Yearly", 2, 120.0)), 0.0001)
    }
    @Test fun missedCyclesChargeOnceEachAndPersistNextDateWithBalances() {
        val id = InMemoryDatabase.insertSubscription(subscription("Daily", 2, day = LocalDate.of(2026, 1, 4)))
        val now = LocalDate.of(2026, 1, 10).atTime(9, 0).atZone(zone).toInstant().toEpochMilli()
        var count = 0
        while (InMemoryDatabase.chargeSubscriptionIfDue(InMemoryDatabase.subscriptions.value.single(), now)) count++
        assertEquals(4, count)
        assertEquals(960.0, InMemoryDatabase.accounts.value.single().balance, 0.0)
        assertEquals(4, InMemoryDatabase.transactions.value.map { it.groupId }.distinct().size)
        val saved = InMemoryDatabase.subscriptions.value.single()
        assertEquals(id, saved.id)
        assertEquals(LocalDate.of(2026, 1, 12), Instant.ofEpochMilli(saved.nextPaymentDate).atZone(zone).toLocalDate())
        InMemoryDatabase.initialize(context)
        assertEquals(saved, InMemoryDatabase.subscriptions.value.single())
        assertEquals(960.0, InMemoryDatabase.accounts.value.single().balance, 0.0)
    }
    @Test fun staleAndFailedChargesDoNotAdvanceTheScheduleOrBalance() {
        val id = InMemoryDatabase.insertSubscription(subscription("Weekly", 2))
        val stored = InMemoryDatabase.subscriptions.value.single()
        assertFalse(InMemoryDatabase.chargeSubscriptionIfDue(stored.copy(name = "stale"), stored.nextPaymentDate))
        val blocked = File(context.filesDir, "vibefinance_data.json.new").apply { mkdirs() }
        File(blocked, "block").writeText("x")
        try { InMemoryDatabase.chargeSubscriptionIfDue(stored, stored.nextPaymentDate); fail("Expected write failure") } catch (_: java.io.IOException) {}
        assertEquals(stored, InMemoryDatabase.subscriptions.value.single()); assertTrue(InMemoryDatabase.transactions.value.isEmpty())
        assertEquals(1000.0, InMemoryDatabase.accounts.value.single().balance, 0.0)
        blocked.deleteRecursively()
        assertTrue(InMemoryDatabase.chargeSubscriptionIfDue(stored, stored.nextPaymentDate))
        assertFalse(InMemoryDatabase.chargeSubscriptionIfDue(stored, stored.nextPaymentDate))
        assertEquals(id, InMemoryDatabase.subscriptions.value.single().id)
    }
    @Test fun persistenceDefaultsLegacyIntervalAndBackupRejectsInvalidIntervals() {
        InMemoryDatabase.insertSubscription(subscription(interval = 6))
        val snapshot = InMemoryDatabase.snapshotForBackup()
        val prefs = BackupStateCodec.preferences(context)
        BackupStateCodec.validateData(snapshot, prefs, emptySet())
        val sub = snapshot.getJSONArray("subscriptions").getJSONObject(0)
        sub.put("interval", 0)
        try { BackupStateCodec.validateData(snapshot, prefs, emptySet()); fail("Invalid interval accepted") } catch (_: IllegalArgumentException) {}
        sub.remove("interval")
        File(context.filesDir, "vibefinance_data.json").writeText(snapshot.toString())
        InMemoryDatabase.initialize(context)
        assertEquals(1, InMemoryDatabase.subscriptions.value.single().interval)
    }
    @Test fun exportEncodesIntervalAndUsesActualMonthEndOccurrences() {
        val clock = Clock.fixed(LocalDate.of(2026, 1, 1).atStartOfDay(zone).toInstant(), zone)
        val sub = subscription("Daily", 2, day = LocalDate.of(2026, 1, 3)).copy(id = 1)
        val months = subscription("Monthly", 6).copy(id = 2)
        val ics = CalendarSyncHelper.generateIcsContent(context, emptyList(), listOf(sub, months), clock).replace("\r\n ", "")
        assertTrue(ics.contains("RRULE:FREQ=DAILY;INTERVAL=2"))
        assertTrue(ics.contains("RDATE:20260731T090000"))
        assertFalse(ics.contains("20260228T090000"))
    }
    @Test fun subscriptionPaidStatusCanBeToggledAndPersists() {
        val id = InMemoryDatabase.insertSubscription(subscription("Monthly", 1))
        val initial = InMemoryDatabase.subscriptions.value.single()
        assertFalse(initial.isPaid)
        assertNull(initial.lastPaidDate)

        val paidNow = 1730000000000L
        assertTrue(InMemoryDatabase.setSubscriptionPaidStatus(id, isPaid = true, now = paidNow))
        val afterPaid = InMemoryDatabase.subscriptions.value.single()
        assertTrue(afterPaid.isPaid)
        assertEquals(paidNow, afterPaid.lastPaidDate)

        // Verify JSON persistence
        InMemoryDatabase.initialize(context)
        val reloaded = InMemoryDatabase.subscriptions.value.single()
        assertTrue(reloaded.isPaid)
        assertEquals(paidNow, reloaded.lastPaidDate)

        // Toggle back to unpaid
        assertTrue(InMemoryDatabase.setSubscriptionPaidStatus(id, isPaid = false))
        val afterUnpaid = InMemoryDatabase.subscriptions.value.single()
        assertFalse(afterUnpaid.isPaid)
        assertNull(afterUnpaid.lastPaidDate)
    }
}
