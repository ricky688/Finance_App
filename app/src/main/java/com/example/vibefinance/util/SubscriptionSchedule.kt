package com.example.vibefinance.util

import com.example.vibefinance.data.entity.SubscriptionEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

/** All scheduling, estimates and exports use the same interval and month-end advancement. */
object SubscriptionSchedule {
    val frequencies = listOf("Monthly", "Yearly", "Weekly", "Daily")
    fun validate(sub: SubscriptionEntity) {
        require(sub.interval in 1..999) { "Invalid subscription interval" }
        require(sub.frequency.lowercase(Locale.ROOT) in setOf("daily", "weekly", "monthly", "yearly", "annual"))
        require(sub.amount.isFinite() && sub.amount > 0 && sub.nextPaymentDate >= 0)
    }
    fun advance(date: LocalDate, frequency: String, interval: Int = 1): LocalDate {
        require(interval in 1..999)
        return when (frequency.lowercase(Locale.ROOT)) {
            "daily" -> date.plusDays(interval.toLong())
            "weekly" -> date.plusWeeks(interval.toLong())
            "yearly", "annual" -> date.plusYears(interval.toLong())
            "monthly" -> date.plusMonths(interval.toLong())
            else -> error("Unknown subscription frequency")
        }
    }
    fun advance(sub: SubscriptionEntity, timestamp: Long = sub.nextPaymentDate, zone: ZoneId = ZoneId.systemDefault()): Long {
        validate(sub)
        val current = Instant.ofEpochMilli(timestamp).atZone(zone)
        return advance(current.toLocalDate(), sub.frequency, sub.interval).atTime(current.toLocalTime()).atZone(zone).toInstant().toEpochMilli()
    }
    fun monthlyAmount(sub: SubscriptionEntity): Double {
        validate(sub)
        return sub.amount / sub.interval * when (sub.frequency.lowercase(Locale.ROOT)) {
            "daily" -> 365.25 / 12.0
            "weekly" -> 4.333
            "yearly", "annual" -> 1.0 / 12.0
            else -> 1.0
        }
    }
}
