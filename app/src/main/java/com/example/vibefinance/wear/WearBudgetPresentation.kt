package com.example.vibefinance.wear

import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.data.entity.SubscriptionEntity
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.data.repository.DailyBudgetInfo
import com.example.vibefinance.service.PendingPayment
import com.example.vibefinance.service.PendingPaymentStore
import com.example.vibefinance.wear.contract.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs

/** Maps the existing budget result; does not recalculate balances or rollover. */
internal fun snapshotForWear(
    info: DailyBudgetInfo?,
    revision: Long,
    now: Long,
    zone: ZoneId,
    allowed: Boolean,
    motion: String
): WearBudgetSnapshot {
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
            daily = formatWholeHkd(info.dailyRemaining)
            period = formatDecimalHkd(info.monthlyRemaining)
            days = info.daysLeft
        }
    }
    return WearBudgetSnapshot(
        revision, status, now, expires, today.toString(), zone.id,
        daily, period, days, motion.takeIf { it in setOf("FULL", "REDUCED", "MINIMAL") } ?: "MINIMAL"
    )
}

/**
 * Builds the extended companion snapshot for Wear OS app, M3E Tile, and Watch Face Complications.
 * Respects independent opt-in gates for pending transaction decisions and recent activity/quick log.
 */
internal fun companionSnapshotForWear(
    info: DailyBudgetInfo?,
    revision: Long,
    now: Long,
    zone: ZoneId,
    budgetAllowed: Boolean,
    pendingAllowed: Boolean,
    recentAllowed: Boolean,
    motion: String,
    accounts: List<AccountEntity>,
    pendingPayments: List<PendingPayment>,
    transactions: List<TransactionEntity>,
    subscriptions: List<SubscriptionEntity>
): WearCompanionSnapshot {
    val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
    val midnight = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    var freshUntil = minOf(now + WearCompanionSnapshot.FRESH_AGE_MS, midnight)
    var expiresAt = minOf(now + WearCompanionSnapshot.MAX_STALE_AGE_MS, midnight)
    var status = if (budgetAllowed) BudgetStatus.NO_BUDGET else BudgetStatus.HIDDEN
    var daily = ""
    var compactDaily = ""
    var period = ""
    var days = 0
    var dailyProgress = 0f
    var periodProgress = 0f
    var isOverspent = false

    if (budgetAllowed && info != null && info.startDate > 0 && now >= info.startDate) {
        val end = Instant.ofEpochMilli(info.endDate).atZone(zone).toLocalDate()
            .plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        if (now < end) {
            require(info.dailyRemaining.isFinite() && info.monthlyRemaining.isFinite())
            freshUntil = minOf(freshUntil, end)
            expiresAt = minOf(expiresAt, end)
            status = BudgetStatus.READY
            daily = formatWholeHkd(info.dailyRemaining)
            compactDaily = formatCompactDaily(info.dailyRemaining)
            period = formatDecimalHkd(info.monthlyRemaining)
            days = info.daysLeft
            isOverspent = info.dailyRemaining < 0.0
            dailyProgress = when {
                info.dailyRemaining <= 0.0 -> 0f
                info.dailyAllowance > 0.0 -> (info.dailyRemaining / info.dailyAllowance).toFloat().coerceIn(0f, 1f)
                else -> 0f
            }
            periodProgress = when {
                info.monthlyRemaining <= 0.0 -> 0f
                info.totalMonthlyBudget > 0.0 -> (info.monthlyRemaining / info.totalMonthlyBudget).toFloat().coerceIn(0f, 1f)
                else -> 0f
            }
        }
    }

    val safeMotion = motion.takeIf { it in setOf("FULL", "REDUCED", "MINIMAL") } ?: "MINIMAL"
    val effectivePending = budgetAllowed && pendingAllowed
    val effectiveRecent = budgetAllowed && recentAllowed

    val wearPending = if (effectivePending) {
        pendingPayments
            .sortedByDescending { it.detectedAt }
            .take(WearCompanionSnapshot.MAX_PENDING_ITEMS)
            .map { payment -> mapPendingItem(payment, accounts) }
    } else emptyList()

    val accountNames = accounts.associate { it.id to it.name }
    val wearRecent = if (effectiveRecent) {
        transactions
            .asSequence()
            .filter { !it.isBalanceAdjustment }
            .sortedByDescending { it.timestamp }
            .take(WearCompanionSnapshot.MAX_RECENT_ITEMS)
            .map { tx ->
                val sign = if (tx.amount < 0) "+" else "-"
                val amountStr = String.format(Locale.US, "%sHK$ %,.2f", sign, abs(tx.amount))
                val isRecentNotif = tx.groupId?.startsWith("notification:") == true &&
                    (now - tx.timestamp) in 0L..300_000L
                WearRecentTransaction(
                    id = tx.id,
                    groupId = tx.groupId.orEmpty().take(96),
                    merchant = tx.description.ifBlank { tx.category }.take(60),
                    amountText = amountStr,
                    category = tx.category.take(32),
                    accountName = (accountNames[tx.accountId] ?: "").take(32),
                    timestamp = tx.timestamp,
                    canQuickUndo = isRecentNotif
                )
            }
            .toList()
    } else emptyList()

    val wearUpcoming = if (effectiveRecent) {
        findNextUpcomingBill(subscriptions, today, zone)
    } else null

    val wearAccounts = if (effectiveRecent) {
        accounts
            .sortedWith(compareBy<AccountEntity> {
                when (it.type) {
                    AccountType.CASH -> 0
                    AccountType.DEBIT -> 1
                    AccountType.BANK -> 2
                    AccountType.CC -> 3
                    AccountType.INVESTMENT -> 4
                }
            }.thenBy { it.name })
            .take(WearCompanionSnapshot.MAX_QUICK_ACCOUNTS)
            .map { acc ->
                WearAccountOption(
                    id = acc.id,
                    name = acc.name.take(32),
                    maskedLast4 = acc.cardLast4?.takeLast(4).orEmpty()
                )
            }
    } else emptyList()

    val wearCategories = if (effectiveRecent) {
        DEFAULT_QUICK_CATEGORIES
    } else emptyList()

    return WearCompanionSnapshot(
        revision = revision,
        status = status,
        generatedAt = now,
        freshUntil = freshUntil,
        expiresAt = expiresAt,
        calculationDate = today.toString(),
        zoneId = zone.id,
        dailyAmount = daily,
        compactDailyAmount = compactDaily,
        periodAmount = period,
        daysLeft = days,
        dailyProgress = dailyProgress,
        periodProgress = periodProgress,
        isOverspent = isOverspent,
        motion = safeMotion,
        pendingEnabled = effectivePending,
        recentEnabled = effectiveRecent,
        pendingItems = wearPending,
        recentTransactions = wearRecent,
        upcomingBill = wearUpcoming,
        quickEntryAccounts = wearAccounts,
        quickEntryCategories = wearCategories
    )
}

private fun mapPendingItem(payment: PendingPayment, accounts: List<AccountEntity>): WearPendingItem {
    val matched = PendingPaymentStore.findMatchingAccount(payment, accounts)
        ?: payment.cardLast4?.takeIf { it.isNotBlank() }?.let { last4 ->
            accounts.firstOrNull { it.cardLast4 == last4 }
        }
    val txType = when {
        payment.isTopUp -> "TOP_UP"
        payment.transactionType == "INCOME" -> "INCOME"
        payment.transactionType == "TRANSFER" -> "TRANSFER"
        else -> "EXPENSE"
    }
    val sourceLabel = buildString {
        val hint = payment.assetHint.trim()
        if (hint.isNotBlank()) append(hint)
        val last4 = payment.cardLast4?.takeLast(4)
        if (!last4.isNullOrBlank() && !hint.contains(last4)) {
            if (isNotEmpty()) append(" ")
            append("••$last4")
        }
    }.ifBlank { "Payment" }.take(40)

    val isTransferOrIncome = txType == "TRANSFER" || txType == "INCOME"
    val eligibleAccounts = if (isTransferOrIncome) {
        accounts.filter { it.type != AccountType.CC }
    } else {
        accounts
    }
    val orderedCandidates = buildList {
        if (matched != null && (!isTransferOrIncome || matched.type != AccountType.CC)) {
            add(matched)
        }
        eligibleAccounts.filter { it.id != matched?.id }.forEach { add(it) }
    }.take(WearCompanionSnapshot.MAX_CANDIDATE_ACCOUNTS).map { acc ->
        WearAccountOption(acc.id, acc.name.take(32), acc.cardLast4?.takeLast(4).orEmpty())
    }

    return WearPendingItem(
        id = payment.id,
        merchant = payment.merchant.ifBlank { sourceLabel }.take(60),
        amountText = formatDecimalHkd(payment.amount),
        transactionType = txType,
        sourceLabel = sourceLabel,
        detectedAt = payment.detectedAt,
        suggestedAccountId = matched?.takeIf { !isTransferOrIncome || it.type != AccountType.CC }?.id
            ?: orderedCandidates.firstOrNull()?.id ?: -1L,
        suggestedAccountName = matched?.takeIf { !isTransferOrIncome || it.type != AccountType.CC }?.name?.take(32)
            ?: orderedCandidates.firstOrNull()?.name.orEmpty(),
        canConfirmIncome = txType == "INCOME" || txType == "TRANSFER",
        canConfirmTransfer = txType == "TRANSFER" || txType == "INCOME",
        candidateAccounts = orderedCandidates
    )
}

private fun findNextUpcomingBill(
    subscriptions: List<SubscriptionEntity>,
    today: LocalDate,
    zone: ZoneId
): WearUpcomingBill? {
    val formatter = DateTimeFormatter.ofPattern("MMM d", Locale.US)
    val candidates = subscriptions.mapNotNull { sub ->
        if (sub.amount <= 0.0 || !sub.amount.isFinite()) return@mapNotNull null
        val dueDate = Instant.ofEpochMilli(sub.nextPaymentDate).atZone(zone).toLocalDate()
        val daysUntil = ChronoUnit.DAYS.between(today, dueDate).toInt()
        if (daysUntil in 0..30) {
            Triple(daysUntil, dueDate, sub)
        } else null
    }.sortedBy { it.first }

    val (daysUntil, dueDate, sub) = candidates.firstOrNull() ?: return null
    return WearUpcomingBill(
        name = sub.name.take(40),
        amountText = formatDecimalHkd(sub.amount),
        dueDateText = dueDate.format(formatter),
        daysUntilDue = daysUntil
    )
}

internal fun formatWholeHkd(value: Double): String =
    String.format(Locale.US, if (value < 0) "-HK$ %,.0f" else "HK$ %,.0f", abs(value))

internal fun formatDecimalHkd(value: Double): String =
    String.format(Locale.US, if (value < 0) "-HK$ %,.2f" else "HK$ %,.2f", abs(value))

internal fun formatCompactDaily(value: Double): String {
    val sign = if (value < 0) "-" else ""
    val absVal = abs(value)
    return when {
        absVal >= 10_000.0 -> String.format(Locale.US, "%s$%.1fk", sign, absVal / 1000.0)
        else -> String.format(Locale.US, "%s$%,.0f", sign, absVal)
    }
}

private val DEFAULT_QUICK_CATEGORIES = listOf(
    "Food & Drink",
    "Transport",
    "Groceries",
    "Shopping",
    "Entertainment",
    "Utilities",
    "Other"
)
