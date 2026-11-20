package com.example.vibefinance.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionDateTimeTest {
    private val hk = ZoneId.of("Asia/Hong_Kong")
    private val original = LocalDateTime.of(2026, 11, 20, 0, 30, 15, 250_000_000).atZone(hk).toInstant().toEpochMilli()

    @Test
    fun datePickerMillis_useLocalDay_notUtcDay() {
        // 00:30 in Hong Kong is still 19 Nov in UTC; the picker must show 20 Nov.
        assertEquals(LocalDate.of(2026, 11, 20).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            TransactionDateTime.toDatePickerMillis(original, hk))
    }

    @Test
    fun withDate_keepsWallClockTimeAndSeconds() {
        val picked = LocalDate.of(2026, 10, 3).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val moved = TransactionDateTime.localDateTime(TransactionDateTime.withDate(original, picked, hk), hk)
        assertEquals(LocalDateTime.of(2026, 10, 3, 0, 30, 15, 250_000_000), moved)
    }

    @Test
    fun withTime_keepsCalendarDay() {
        val moved = TransactionDateTime.localDateTime(TransactionDateTime.withTime(original, 23, 5, hk), hk)
        assertEquals(LocalDateTime.of(2026, 11, 20, 23, 5, 15, 250_000_000), moved)
    }

    @Test(expected = IllegalArgumentException::class)
    fun withTime_rejectsInvalidHour() {
        TransactionDateTime.withTime(original, 24, 0, hk)
    }
}
