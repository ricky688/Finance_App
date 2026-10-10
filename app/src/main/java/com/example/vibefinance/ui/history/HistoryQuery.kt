package com.example.vibefinance.ui.history

import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.TransactionEntity
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import kotlin.math.abs

enum class HistoryKind { ALL, EXPENSE, INCOME, TRANSFER, ADJUSTMENT }
data class HistoryQuery(
    val text: String = "", val account: Long? = null, val category: String? = null,
    val kind: HistoryKind = HistoryKind.ALL, val from: String = "", val through: String = "",
    val minimum: String = "", val maximum: String = ""
) {
    fun valid(): Boolean = date(from) != null && date(through) != null &&
        number(minimum) != null && number(maximum) != null &&
        (from.isBlank() || through.isBlank() || date(from)!! <= date(through)!!) &&
        (minimum.isBlank() || maximum.isBlank() || number(minimum)!! <= number(maximum)!!)
    private fun date(value: String): LocalDate? = if (value.isBlank()) LocalDate.MIN else runCatching { LocalDate.parse(value) }.getOrNull()?.takeIf { it.year in 1..9999 }
    private fun number(value: String): Double? = if (value.isBlank()) 0.0 else value.replace(",", "").toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0 }
    fun apply(transactions: List<TransactionEntity>, accounts: List<AccountEntity>, zone: ZoneId = ZoneId.systemDefault()): List<TransactionEntity> {
        if (!valid()) return emptyList()
        val start = if (from.isBlank()) Long.MIN_VALUE else date(from)!!.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = if (through.isBlank()) Long.MAX_VALUE else date(through)!!.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val lower = number(minimum)!!
        val upper = if (maximum.isBlank()) Double.POSITIVE_INFINITY else number(maximum)!!
        val byId = accounts.associateBy { it.id }
        val words = text.trim().lowercase(Locale.ROOT).split(Regex("\\s+")).filter { it.isNotEmpty() }
        return transactions.filter { tx ->
            val matchKind = when (kind) {
                HistoryKind.ALL -> true
                HistoryKind.ADJUSTMENT -> tx.isBalanceAdjustment
                HistoryKind.TRANSFER -> tx.toAccountId != null && !tx.isBalanceAdjustment
                HistoryKind.EXPENSE -> tx.toAccountId == null && !tx.isBalanceAdjustment && tx.amount > 0
                HistoryKind.INCOME -> tx.toAccountId == null && !tx.isBalanceAdjustment && tx.amount < 0
            }
            val amount = abs(if (tx.isBalanceAdjustment) tx.balanceAdjustmentDelta ?: tx.amount else tx.amount)
            val search = listOf(tx.description, tx.category, byId[tx.accountId]?.name.orEmpty(),
                byId[tx.accountId]?.nickname.orEmpty(), byId[tx.toAccountId]?.name.orEmpty(),
                byId[tx.toAccountId]?.nickname.orEmpty(), String.format(Locale.US, "%.2f", amount),
                String.format(Locale.US, "%,.2f", amount)).joinToString(" ").lowercase(Locale.ROOT)
            matchKind && (account == null || tx.accountId == account || tx.toAccountId == account) &&
                (category == null || tx.category.equals(category, true)) && tx.timestamp >= start && tx.timestamp < end &&
                amount >= lower && amount <= upper && words.all { search.contains(it) }
        }
    }
}
