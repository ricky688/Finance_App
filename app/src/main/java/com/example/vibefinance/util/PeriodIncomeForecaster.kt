package com.example.vibefinance.util

import com.example.vibefinance.data.entity.TransactionEntity
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class PeriodIncomeForecast(val amount: Double, val periodsUsed: Int, val partialHistory: Boolean)

/** Learns from up to three preceding periods; recent recorded income has weights 3, 2, 1. */
object PeriodIncomeForecaster {
    fun estimate(startDate: Long, endDate: Long, transactions: List<TransactionEntity>,
        today: LocalDate = LocalDate.now(), zone: ZoneId = ZoneId.systemDefault()): PeriodIncomeForecast? {
        if (endDate < startDate) return null
        val start = Instant.ofEpochMilli(startDate).atZone(zone).toLocalDate()
        val end = Instant.ofEpochMilli(endDate).atZone(zone).toLocalDate().plusDays(1)
        val days = ChronoUnit.DAYS.between(start, end)
        val months = ChronoUnit.MONTHS.between(YearMonth.from(start), YearMonth.from(end))
        val monthly = months > 0 && start.plusMonths(months) == end
        val todayStart = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val history = transactions.filter { it.timestamp < todayStart && it.amount.isFinite() && !it.isBalanceAdjustment }
        val firstDate = history.minOfOrNull { it.timestamp }?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
            ?: return null
        var weightedIncome = BigDecimal.ZERO
        var totalWeight = 0L
        var periods = 0
        var partial = false
        for (index in 0L..2L) {
            val from = if (monthly) start.minusMonths(months * (index + 1)) else start.minusDays(days * (index + 1))
            val until = if (monthly) start.minusMonths(months * index) else start.minusDays(days * index)
            if (until > today || firstDate >= until) continue
            val fromMillis = from.atStartOfDay(zone).toInstant().toEpochMilli()
            val untilMillis = until.atStartOfDay(zone).toInstant().toEpochMilli()
            val income = history.asSequence().filter {
                it.timestamp >= fromMillis && it.timestamp < untilMillis && it.toAccountId == null && it.amount < 0
            }.fold(BigDecimal.ZERO) { sum, tx -> sum + BigDecimal.valueOf(-tx.amount) }
            val weight = 3L - index
            weightedIncome += income * BigDecimal.valueOf(weight)
            totalWeight += weight
            periods++
            partial = partial || firstDate > from
        }
        if (periods == 0) return null
        val amount = weightedIncome.divide(BigDecimal.valueOf(totalWeight), 2, RoundingMode.HALF_UP).toDouble()
        return amount.takeIf { it.isFinite() }?.let { PeriodIncomeForecast(it, periods, partial) }
    }
}
