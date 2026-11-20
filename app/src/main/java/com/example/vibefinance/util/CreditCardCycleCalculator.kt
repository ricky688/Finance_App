package com.example.vibefinance.util

import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.TransactionEntity
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

/** All windows are half-open, at local day boundaries (including DST changes). */
class CreditCardCycleCalculator(
    private val clock: Clock = Clock.systemDefaultZone(),
    private val zone: ZoneId = clock.zone
) {
    data class Cycle(val start: LocalDate, val closing: LocalDate, val startMillis: Long, val endExclusiveMillis: Long)
    data class Metrics(val cycle: Cycle, val spent: Double, val utilization: Float?, val remaining: Double?)
    val today: LocalDate get() = LocalDate.now(clock.withZone(zone))
    fun date(month: YearMonth, day: Int): LocalDate = month.atDay(day.coerceIn(1, month.lengthOfMonth()))
    fun paymentDay(account: AccountEntity): Int? = account.paymentDeadline?.takeIf { it in 1..31 }
        ?: account.paymentDate?.takeIf { it in 1..31 }
    fun dueDate(account: AccountEntity, month: YearMonth): LocalDate? = paymentDay(account)?.let { date(month, it) }
    fun nextDue(account: AccountEntity): LocalDate? {
        val month = YearMonth.from(today)
        val due = dueDate(account, month) ?: return null
        return if (due < today) dueDate(account, month.plusMonths(1)) else due
    }
    private fun window(start: LocalDate, close: LocalDate) = Cycle(start, close,
        start.atStartOfDay(zone).toInstant().toEpochMilli(), close.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli())
    private fun closingCycle(account: AccountEntity, month: YearMonth): Cycle {
        val day = account.billingDate?.takeIf { it in 1..31 }
            ?: return window(month.atDay(1), month.atEndOfMonth())
        return window(date(month.minusMonths(1), day).plusDays(1), date(month, day))
    }
    private fun cycleForDay(account: AccountEntity, day: LocalDate): Cycle {
        val month = YearMonth.from(day)
        val cycle = closingCycle(account, month)
        return if (day <= cycle.closing) cycle else closingCycle(account, month.plusMonths(1))
    }
    fun currentCycle(account: AccountEntity): Cycle = cycleForDay(account, today)
    fun shiftCycle(account: AccountEntity, cycle: Cycle, offset: Int): Cycle =
        closingCycle(account, YearMonth.from(cycle.closing).plusMonths(offset.toLong()))

    fun cycleForTransaction(account: AccountEntity, transaction: TransactionEntity): Cycle =
        cycleForDay(account, java.time.Instant.ofEpochMilli(transaction.timestamp).atZone(zone).toLocalDate())
    /** A payment reminder refers to the statement closing strictly before its due date. */
    fun statementCycle(account: AccountEntity, due: LocalDate): Cycle {
        val month = YearMonth.from(due)
        val day = account.billingDate?.takeIf { it in 1..31 }
            ?: return window(month.minusMonths(1).atDay(1), month.minusMonths(1).atEndOfMonth())
        val closingMonth = if (date(month, day) < due) month else month.minusMonths(1)
        return window(date(closingMonth.minusMonths(1), day).plusDays(1), date(closingMonth, day))
    }
    fun transactionsInCycle(account: AccountEntity, transactions: List<TransactionEntity>, cycle: Cycle): Sequence<TransactionEntity> {
        val now = clock.millis()
        val previous = shiftCycle(account, cycle, -1)
        val next = shiftCycle(account, cycle, 1)
        return transactions.asSequence().filter { tx ->
            if (tx.accountId != account.id || tx.toAccountId != null || tx.isBalanceAdjustment || tx.timestamp > now) {
                return@filter false
            }
            val source = when (tx.statementCycleOffset) {
                -1 -> next
                1 -> previous
                else -> cycle
            }
            tx.timestamp >= source.startMillis && tx.timestamp < source.endExclusiveMillis
        }
    }
    fun metrics(account: AccountEntity, transactions: List<TransactionEntity>, cycle: Cycle = currentCycle(account)): Metrics {
        val spent = transactionsInCycle(account, transactions, cycle).sumOf { it.amount }
        val limit = account.creditLimit?.takeIf { it.isFinite() && it > 0 }
        return Metrics(cycle, spent, limit?.let { (spent / it).coerceIn(0.0, 1.0).toFloat() },
            limit?.let { (it - spent).coerceAtLeast(0.0) })
    }
}
