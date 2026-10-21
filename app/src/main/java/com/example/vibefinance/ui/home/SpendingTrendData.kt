package com.example.vibefinance.ui.home

import com.example.vibefinance.data.entity.TransactionEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

internal data class SpendingTrendDay(val date: LocalDate, val daily: Double, val nonDaily: Double)

internal fun spendingTrendDays(
    transactions: List<TransactionEntity>,
    days: List<LocalDate>,
    zone: ZoneId,
): List<SpendingTrendDay> {
    val daily = days.associateWith { 0.0 }.toMutableMap()
    val nonDaily = days.associateWith { 0.0 }.toMutableMap()
    transactions.forEach { tx ->
        if (tx.toAccountId != null || tx.isBalanceAdjustment || tx.amount <= 0 || !tx.amount.isFinite()) return@forEach
        val date = Instant.ofEpochMilli(tx.timestamp).atZone(zone).toLocalDate()
        if (date !in daily) return@forEach
        val target = if (tx.isExcludedFromDailyBudget) nonDaily else daily
        target[date] = target.getValue(date) + tx.amount
    }
    return days.map { SpendingTrendDay(it, daily.getValue(it), nonDaily.getValue(it)) }
}
