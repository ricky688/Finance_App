package com.example.vibefinance.wear.contract

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class WearAccountOption(
    val id: Long,
    val name: String,
    val maskedLast4: String = ""
)

data class WearPendingItem(
    val id: String,
    val merchant: String,
    val amountText: String,
    val transactionType: String,
    val sourceLabel: String,
    val detectedAt: Long,
    val suggestedAccountId: Long = -1L,
    val suggestedAccountName: String = "",
    val canConfirmIncome: Boolean = false,
    val canConfirmTransfer: Boolean = false,
    val candidateAccounts: List<WearAccountOption> = emptyList()
)

data class WearRecentTransaction(
    val id: Long,
    val groupId: String,
    val merchant: String,
    val amountText: String,
    val category: String,
    val accountName: String,
    val timestamp: Long,
    val canQuickUndo: Boolean = false
)

data class WearUpcomingBill(
    val name: String,
    val amountText: String,
    val dueDateText: String,
    val daysUntilDue: Int
)

/**
 * Extended display & interaction snapshot for Wear OS companion, Tile, and Watch Face Complications.
 * All financial calculations and money formatting remain exclusively on the phone.
 */
data class WearCompanionSnapshot(
    val revision: Long,
    val status: BudgetStatus,
    val generatedAt: Long,
    val freshUntil: Long,
    val expiresAt: Long,
    val calculationDate: String,
    val zoneId: String,
    val dailyAmount: String = "",
    val compactDailyAmount: String = "",
    val periodAmount: String = "",
    val daysLeft: Int = 0,
    val dailyProgress: Float = 0f,
    val periodProgress: Float = 0f,
    val isOverspent: Boolean = false,
    val motion: String = "MINIMAL",
    val pendingEnabled: Boolean = false,
    val recentEnabled: Boolean = false,
    val pendingItems: List<WearPendingItem> = emptyList(),
    val recentTransactions: List<WearRecentTransaction> = emptyList(),
    val upcomingBill: WearUpcomingBill? = null,
    val quickEntryAccounts: List<WearAccountOption> = emptyList(),
    val quickEntryCategories: List<String> = emptyList()
) {
    fun isFresh(now: Long): Boolean = now >= generatedAt && now < freshUntil
    fun isUsable(now: Long): Boolean = now >= generatedAt && now < expiresAt

    fun encode(): ByteArray {
        validate()
        return ByteArrayOutputStream().also { bytes ->
            DataOutputStream(bytes).use { out ->
                out.writeInt(VERSION)
                out.writeLong(revision)
                out.writeUTF(status.name)
                out.writeLong(generatedAt)
                out.writeLong(freshUntil)
                out.writeLong(expiresAt)
                out.writeUTF(calculationDate)
                out.writeUTF(zoneId)
                out.writeUTF(dailyAmount)
                out.writeUTF(compactDailyAmount)
                out.writeUTF(periodAmount)
                out.writeInt(daysLeft)
                out.writeFloat(dailyProgress)
                out.writeFloat(periodProgress)
                out.writeBoolean(isOverspent)
                out.writeUTF(motion)
                out.writeBoolean(pendingEnabled)
                out.writeBoolean(recentEnabled)

                out.writeInt(pendingItems.size)
                for (item in pendingItems) {
                    out.writeUTF(item.id)
                    out.writeUTF(item.merchant)
                    out.writeUTF(item.amountText)
                    out.writeUTF(item.transactionType)
                    out.writeUTF(item.sourceLabel)
                    out.writeLong(item.detectedAt)
                    out.writeLong(item.suggestedAccountId)
                    out.writeUTF(item.suggestedAccountName)
                    out.writeBoolean(item.canConfirmIncome)
                    out.writeBoolean(item.canConfirmTransfer)
                    out.writeInt(item.candidateAccounts.size)
                    for (acc in item.candidateAccounts) {
                        out.writeLong(acc.id)
                        out.writeUTF(acc.name)
                        out.writeUTF(acc.maskedLast4)
                    }
                }

                out.writeInt(recentTransactions.size)
                for (tx in recentTransactions) {
                    out.writeLong(tx.id)
                    out.writeUTF(tx.groupId)
                    out.writeUTF(tx.merchant)
                    out.writeUTF(tx.amountText)
                    out.writeUTF(tx.category)
                    out.writeUTF(tx.accountName)
                    out.writeLong(tx.timestamp)
                    out.writeBoolean(tx.canQuickUndo)
                }

                out.writeBoolean(upcomingBill != null)
                if (upcomingBill != null) {
                    out.writeUTF(upcomingBill.name)
                    out.writeUTF(upcomingBill.amountText)
                    out.writeUTF(upcomingBill.dueDateText)
                    out.writeInt(upcomingBill.daysUntilDue)
                }

                out.writeInt(quickEntryAccounts.size)
                for (acc in quickEntryAccounts) {
                    out.writeLong(acc.id)
                    out.writeUTF(acc.name)
                    out.writeUTF(acc.maskedLast4)
                }

                out.writeInt(quickEntryCategories.size)
                for (cat in quickEntryCategories) {
                    out.writeUTF(cat)
                }
            }
        }.toByteArray().also { require(it.size <= MAX_PAYLOAD_BYTES) }
    }

    private fun validate() {
        require(revision > 0 && generatedAt > 0)
        require(freshUntil > generatedAt && freshUntil - generatedAt <= FRESH_AGE_MS)
        require(expiresAt >= freshUntil && expiresAt - generatedAt <= MAX_STALE_AGE_MS)
        val zone = ZoneId.of(zoneId)
        val date = LocalDate.parse(calculationDate)
        require(Instant.ofEpochMilli(generatedAt).atZone(zone).toLocalDate() == date)
        val midnight = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        require(expiresAt <= midnight)
        require(daysLeft >= 0 && motion in setOf("FULL", "REDUCED", "MINIMAL"))
        require(dailyProgress.isFinite() && dailyProgress in 0f..1f)
        require(periodProgress.isFinite() && periodProgress in 0f..1f)
        require(pendingItems.size <= MAX_PENDING_ITEMS)
        require(recentTransactions.size <= MAX_RECENT_ITEMS)
        require(quickEntryAccounts.size <= MAX_QUICK_ACCOUNTS)
        require(quickEntryCategories.size <= MAX_QUICK_CATEGORIES)

        if (status == BudgetStatus.READY) {
            require(daysLeft > 0 && dailyAmount.isNotBlank() && periodAmount.isNotBlank())
        } else {
            require(dailyAmount.isEmpty() && compactDailyAmount.isEmpty() && periodAmount.isEmpty() && daysLeft == 0)
        }
        if (status == BudgetStatus.HIDDEN) {
            require(pendingItems.isEmpty() && recentTransactions.isEmpty() && upcomingBill == null && quickEntryAccounts.isEmpty())
        }
        if (!pendingEnabled) {
            require(pendingItems.isEmpty())
        }
        if (!recentEnabled) {
            require(recentTransactions.isEmpty() && upcomingBill == null && quickEntryAccounts.isEmpty())
        }
        for (item in pendingItems) {
            require(item.id.isNotBlank() && item.id.length <= 128)
            require(item.merchant.length <= 120 && item.amountText.isNotBlank())
            require(item.candidateAccounts.size <= MAX_CANDIDATE_ACCOUNTS)
        }
    }

    companion object {
        const val VERSION = 1
        const val FRESH_AGE_MS = 15 * 60 * 1000L
        const val MAX_STALE_AGE_MS = 2 * 60 * 60 * 1000L
        const val MAX_PAYLOAD_BYTES = 32 * 1024
        const val MAX_PENDING_ITEMS = 10
        const val MAX_CANDIDATE_ACCOUNTS = 6
        const val MAX_RECENT_ITEMS = 5
        const val MAX_QUICK_ACCOUNTS = 8
        const val MAX_QUICK_CATEGORIES = 10
        const val DATA_PATH = "/vibefinance/companion/v1"
        const val PAYLOAD_KEY = "companion_snapshot"

        fun decode(bytes: ByteArray): WearCompanionSnapshot {
            require(bytes.size in 1..MAX_PAYLOAD_BYTES)
            return DataInputStream(ByteArrayInputStream(bytes)).use { input ->
                require(input.readInt() == VERSION)
                val revision = input.readLong()
                val status = BudgetStatus.valueOf(input.readUTF())
                val generatedAt = input.readLong()
                val freshUntil = input.readLong()
                val expiresAt = input.readLong()
                val calculationDate = input.readUTF()
                val zoneId = input.readUTF()
                val dailyAmount = input.readUTF()
                val compactDailyAmount = input.readUTF()
                val periodAmount = input.readUTF()
                val daysLeft = input.readInt()
                val dailyProgress = input.readFloat()
                val periodProgress = input.readFloat()
                val isOverspent = input.readBoolean()
                val motion = input.readUTF()
                val pendingEnabled = input.readBoolean()
                val recentEnabled = input.readBoolean()

                val pendingCount = input.readInt()
                require(pendingCount in 0..MAX_PENDING_ITEMS)
                val pendingItems = List(pendingCount) {
                    val id = input.readUTF()
                    val merchant = input.readUTF()
                    val amountText = input.readUTF()
                    val txType = input.readUTF()
                    val sourceLabel = input.readUTF()
                    val detectedAt = input.readLong()
                    val suggestedAccId = input.readLong()
                    val suggestedAccName = input.readUTF()
                    val canIncome = input.readBoolean()
                    val canTransfer = input.readBoolean()
                    val candCount = input.readInt()
                    require(candCount in 0..MAX_CANDIDATE_ACCOUNTS)
                    val candidates = List(candCount) {
                        WearAccountOption(input.readLong(), input.readUTF(), input.readUTF())
                    }
                    WearPendingItem(
                        id, merchant, amountText, txType, sourceLabel, detectedAt,
                        suggestedAccId, suggestedAccName, canIncome, canTransfer, candidates
                    )
                }

                val recentCount = input.readInt()
                require(recentCount in 0..MAX_RECENT_ITEMS)
                val recentTransactions = List(recentCount) {
                    WearRecentTransaction(
                        input.readLong(), input.readUTF(), input.readUTF(), input.readUTF(),
                        input.readUTF(), input.readUTF(), input.readLong(), input.readBoolean()
                    )
                }

                val hasUpcoming = input.readBoolean()
                val upcomingBill = if (hasUpcoming) {
                    WearUpcomingBill(input.readUTF(), input.readUTF(), input.readUTF(), input.readInt())
                } else null

                val accCount = input.readInt()
                require(accCount in 0..MAX_QUICK_ACCOUNTS)
                val quickAccounts = List(accCount) {
                    WearAccountOption(input.readLong(), input.readUTF(), input.readUTF())
                }

                val catCount = input.readInt()
                require(catCount in 0..MAX_QUICK_CATEGORIES)
                val quickCategories = List(catCount) { input.readUTF() }

                require(input.available() == 0)
                WearCompanionSnapshot(
                    revision = revision,
                    status = status,
                    generatedAt = generatedAt,
                    freshUntil = freshUntil,
                    expiresAt = expiresAt,
                    calculationDate = calculationDate,
                    zoneId = zoneId,
                    dailyAmount = dailyAmount,
                    compactDailyAmount = compactDailyAmount,
                    periodAmount = periodAmount,
                    daysLeft = daysLeft,
                    dailyProgress = dailyProgress,
                    periodProgress = periodProgress,
                    isOverspent = isOverspent,
                    motion = motion,
                    pendingEnabled = pendingEnabled,
                    recentEnabled = recentEnabled,
                    pendingItems = pendingItems,
                    recentTransactions = recentTransactions,
                    upcomingBill = upcomingBill,
                    quickEntryAccounts = quickAccounts,
                    quickEntryCategories = quickCategories
                ).also { it.validate() }
            }
        }
    }
}

enum class WearCommandAction {
    ACCEPT_PENDING,
    IGNORE_PENDING,
    UNDO_NOTIFICATION_PAYMENT,
    CHANGE_NOTIFICATION_CATEGORY,
    QUICK_ADD_EXPENSE,
    OPEN_ON_PHONE
}

/** Watch → Phone write/action command. Phone validates, executes idempotently, and replies. */
data class WearCommand(
    val requestId: String,
    val issuedAt: Long,
    val action: WearCommandAction,
    val paymentId: String = "",
    val accountId: Long = -1L,
    val rememberChoice: Boolean = true,
    val category: String = "",
    val amount: Double = 0.0,
    val note: String = "",
    val overrideType: String = "",
    val target: String = ""
) {
    fun encode(): ByteArray {
        validate()
        return ByteArrayOutputStream().also { bytes ->
            DataOutputStream(bytes).use { out ->
                out.writeInt(VERSION)
                out.writeUTF(requestId)
                out.writeLong(issuedAt)
                out.writeUTF(action.name)
                out.writeUTF(paymentId)
                out.writeLong(accountId)
                out.writeBoolean(rememberChoice)
                out.writeUTF(category)
                out.writeDouble(amount)
                out.writeUTF(note)
                out.writeUTF(overrideType)
                out.writeUTF(target)
            }
        }.toByteArray().also { require(it.size <= MAX_BYTES) }
    }

    private fun validate() {
        require(requestId.isNotBlank() && requestId.length <= 64)
        require(issuedAt > 0)
        require(paymentId.length <= 128 && category.length <= 64 && note.length <= 120 && overrideType.length <= 32 && target.length <= 128)
        when (action) {
            WearCommandAction.ACCEPT_PENDING -> require(paymentId.isNotBlank() && accountId > 0)
            WearCommandAction.IGNORE_PENDING, WearCommandAction.UNDO_NOTIFICATION_PAYMENT -> require(paymentId.isNotBlank())
            WearCommandAction.CHANGE_NOTIFICATION_CATEGORY -> require(paymentId.isNotBlank() && category.isNotBlank())
            WearCommandAction.QUICK_ADD_EXPENSE -> require(amount.isFinite() && amount > 0.0 && amount <= 999_999.99 && accountId > 0 && category.isNotBlank())
            WearCommandAction.OPEN_ON_PHONE -> require(target.isNotBlank())
        }
    }

    companion object {
        const val VERSION = 1
        const val MAX_BYTES = 2048
        const val COMMAND_PATH = "/vibefinance/command/v1"

        fun decode(bytes: ByteArray): WearCommand {
            require(bytes.size in 1..MAX_BYTES)
            return DataInputStream(ByteArrayInputStream(bytes)).use { input ->
                require(input.readInt() == VERSION)
                WearCommand(
                    requestId = input.readUTF(),
                    issuedAt = input.readLong(),
                    action = WearCommandAction.valueOf(input.readUTF()),
                    paymentId = input.readUTF(),
                    accountId = input.readLong(),
                    rememberChoice = input.readBoolean(),
                    category = input.readUTF(),
                    amount = input.readDouble(),
                    note = input.readUTF(),
                    overrideType = input.readUTF(),
                    target = input.readUTF()
                ).also { require(input.available() == 0); it.validate() }
            }
        }
    }
}

enum class WearCommandStatus {
    SUCCESS,
    ALREADY_HANDLED,
    EXPIRED_OR_MISSING,
    PRIVACY_BLOCKED,
    INVALID_REQUEST,
    ERROR
}

data class WearCommandResult(
    val requestId: String,
    val status: WearCommandStatus,
    val message: String = "",
    val newRevision: Long = 0L
) {
    fun encode(): ByteArray {
        require(requestId.isNotBlank() && requestId.length <= 64 && message.length <= 160)
        return ByteArrayOutputStream().also { bytes ->
            DataOutputStream(bytes).use { out ->
                out.writeInt(VERSION)
                out.writeUTF(requestId)
                out.writeUTF(status.name)
                out.writeUTF(message)
                out.writeLong(newRevision)
            }
        }.toByteArray().also { require(it.size <= 1024) }
    }

    companion object {
        const val VERSION = 1
        const val RESULT_PATH = "/vibefinance/command_result/v1"

        fun decode(bytes: ByteArray): WearCommandResult {
            require(bytes.size in 1..1024)
            return DataInputStream(ByteArrayInputStream(bytes)).use { input ->
                require(input.readInt() == VERSION)
                WearCommandResult(
                    requestId = input.readUTF(),
                    status = WearCommandStatus.valueOf(input.readUTF()),
                    message = input.readUTF(),
                    newRevision = input.readLong()
                ).also { require(input.available() == 0) }
            }
        }
    }
}
