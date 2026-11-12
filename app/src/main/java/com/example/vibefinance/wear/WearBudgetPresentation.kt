package com.example.vibefinance.wear

import com.example.vibefinance.data.repository.DailyBudgetInfo
import com.example.vibefinance.wear.contract.*
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

/** Maps the existing budget result; does not recalculate balances or rollover. */
internal fun snapshotForWear(info: DailyBudgetInfo?, revision: Long, now: Long, zone: ZoneId,
    allowed: Boolean, motion: String): WearBudgetSnapshot {
    val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
    val midnight = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    var expires = minOf(now + WearBudgetSnapshot.MAX_AGE_MS, midnight)
    var status = if (allowed) BudgetStatus.NO_BUDGET else BudgetStatus.HIDDEN
    var daily = ""
    var period = ""
    var days = 0
    if (allowed && info != null && info.startDate > 0 && now >= info.startDate) {
        val end = Instant.ofEpochMilli(info.endDate).atZone(zone).toLocalDate()
            .plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        if (now < end) {
            require(info.dailyRemaining.isFinite() && info.monthlyRemaining.isFinite())
            expires = minOf(expires, end)
            status = BudgetStatus.READY
            daily = String.format(Locale.US, if (info.dailyRemaining < 0) "-HK$ %,.0f" else "HK$ %,.0f",
                kotlin.math.abs(info.dailyRemaining))
            period = String.format(Locale.US, "HK$ %,.2f", info.monthlyRemaining)
            days = info.daysLeft
        }
    }
    return WearBudgetSnapshot(revision, status, now, expires, today.toString(), zone.id,
        daily, period, days, motion.takeIf { it in setOf("FULL", "REDUCED", "MINIMAL") } ?: "MINIMAL")
}
