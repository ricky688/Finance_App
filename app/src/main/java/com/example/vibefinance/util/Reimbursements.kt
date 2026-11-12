package com.example.vibefinance.util

import com.example.vibefinance.data.entity.TransactionEntity
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.ZoneId

/** Linked or standalone expense credits, never ordinary earnings. */
object Reimbursements {
    fun isExpense(tx: TransactionEntity) = tx.amount.isFinite() && tx.amount > 0 && tx.toAccountId == null && !tx.isBalanceAdjustment && !tx.isReimbursement
    fun expenseOnOrBeforeDay(expenseAt: Long, receivedAt: Long, zone: ZoneId = ZoneId.systemDefault()): Boolean =
        !Instant.ofEpochMilli(expenseAt).atZone(zone).toLocalDate()
            .isAfter(Instant.ofEpochMilli(receivedAt).atZone(zone).toLocalDate())

    // Compare at HKD cent precision, including legacy fractional installment amounts.
    fun outstanding(expense: TransactionEntity, records: List<TransactionEntity>): BigDecimal =
        BigDecimal.valueOf(expense.amount).setScale(2, RoundingMode.HALF_UP).add(records.filter { it.reimbursementOf == expense.id }
            .fold(BigDecimal.ZERO) { sum, tx -> sum.add(BigDecimal.valueOf(tx.amount).setScale(2, RoundingMode.HALF_UP)) }).max(BigDecimal.ZERO)
    fun validate(records: List<TransactionEntity>) {
        records.filter { it.isReimbursement }.forEach {
            require(it.amount.isFinite() && it.amount < 0 && BigDecimal.valueOf(it.amount).stripTrailingZeros().scale() <= 2 &&
                it.toAccountId == null && !it.isBalanceAdjustment)
            require(!it.isUnlinkedReimbursement || it.reimbursementOf == null)
        }
        val byId = records.associateBy { it.id }
        records.filter { it.reimbursementOf != null }.groupBy { it.reimbursementOf!! }.forEach { (id, credits) ->
            val expense = requireNotNull(byId[id]) { "A reimbursed expense cannot be removed before its repayments" }
            require(isExpense(expense)) { "Repayments must reference an expense" }
            require(credits.all { it.id != id && it.amount.isFinite() && it.amount < 0 && BigDecimal.valueOf(it.amount).stripTrailingZeros().scale() <= 2 && expenseOnOrBeforeDay(expense.timestamp, it.timestamp) && it.toAccountId == null && !it.isBalanceAdjustment && it.isExcludedFromDailyBudget == expense.isExcludedFromDailyBudget })

        }
    }
}
