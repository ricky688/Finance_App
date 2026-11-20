package com.example.vibefinance.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Pure conversions for editing a transaction's date and time.
 *
 * Material's DatePicker works in UTC-midnight millis, while transactions are stored as epoch
 * millis in the device zone. Changing the date keeps the wall-clock time; changing the time keeps
 * the calendar date. Seconds and sub-seconds are kept so edits do not reorder same-minute records.
 */
object TransactionDateTime {
    /** The transaction's local calendar day as the UTC-midnight millis Material's DatePicker expects. */
    fun toDatePickerMillis(timestamp: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
        localDateTime(timestamp, zone).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    /** Moves [timestamp] to the day picked in a DatePicker ([utcDateMillis]), keeping its local time. */
    fun withDate(timestamp: Long, utcDateMillis: Long, zone: ZoneId = ZoneId.systemDefault()): Long {
        val date = Instant.ofEpochMilli(utcDateMillis).atZone(ZoneOffset.UTC).toLocalDate()
        return withDate(timestamp, date, zone)
    }

    fun withDate(timestamp: Long, date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Long =
        localDateTime(timestamp, zone).with(date).atZone(zone).toInstant().toEpochMilli()

    /** Sets the local hour and minute of [timestamp], keeping its calendar day. */
    fun withTime(timestamp: Long, hour: Int, minute: Int, zone: ZoneId = ZoneId.systemDefault()): Long {
        require(hour in 0..23 && minute in 0..59) { "Invalid time $hour:$minute" }
        return localDateTime(timestamp, zone).withHour(hour).withMinute(minute)
            .atZone(zone).toInstant().toEpochMilli()
    }

    fun localDateTime(timestamp: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zone)
}
