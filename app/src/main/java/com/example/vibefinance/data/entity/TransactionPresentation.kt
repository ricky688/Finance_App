package com.example.vibefinance.data.entity

/**
 * The signed amount shown in History, from the selected account's perspective.
 *
 * Income and incoming transfers are positive; expenses and outgoing transfers are negative.
 * A credit card's balance stores debt, so its balance change must not determine this sign.
 * Balance adjustments keep their exact recorded balance delta in account History; the global
 * view keeps the stored net-worth direction. This does not change accounting or reversal rules.
 */
fun TransactionEntity.historyAmountFor(accountId: Long? = null): Double {
    if (accountId != null && accountId != this.accountId && accountId != toAccountId) return 0.0

    if (isBalanceAdjustment) {
        return if (accountId == null) -amount else balanceAdjustmentDelta ?: 0.0
    }

    if (toAccountId != null) {
        return when {
            this.accountId == toAccountId -> 0.0
            accountId == null -> -amount
            accountId == toAccountId -> amount
            else -> -amount
        }
    }

    return -amount
}
