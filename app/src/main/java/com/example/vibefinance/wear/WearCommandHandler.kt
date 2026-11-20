package com.example.vibefinance.wear

import android.content.Context
import android.content.Intent
import com.example.vibefinance.MainActivity
import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.service.PendingPaymentStore
import com.example.vibefinance.wear.contract.WearCommand
import com.example.vibefinance.wear.contract.WearCommandAction
import com.example.vibefinance.wear.contract.WearCommandResult
import com.example.vibefinance.wear.contract.WearCommandStatus
import org.json.JSONArray

/**
 * Authoritative, idempotent phone-side handler for Wear OS companion commands.
 * All writes delegate to existing phone stores and preserve privacy, app-lock, and deduplication rules.
 */
object WearCommandHandler {
    private const val PROCESSED_PREFS = "wear_command_ledger"
    private const val PROCESSED_KEY = "processed_ids"
    private const val MAX_PROCESSED_IDS = 200
    private val lock = Any()

    suspend fun handle(context: Context, command: WearCommand): WearCommandResult {
        val app = context.applicationContext
        val prefs = PhoneBudgetBridge.prefs(app)
        val sharingEnabled = prefs.getBoolean(PhoneBudgetBridge.SHARE_KEY, false)
        val hidden = prefs.getBoolean("hide_amounts", false)
        val appLocked = prefs.getBoolean("app_lock_enabled", false)
        val pendingEnabled = prefs.getBoolean(PhoneBudgetBridge.PENDING_DECISIONS_KEY, false)
        val recentEnabled = prefs.getBoolean(PhoneBudgetBridge.RECENT_AND_QUICK_KEY, false)

        if (!sharingEnabled || hidden || appLocked) {
            return WearCommandResult(command.requestId, WearCommandStatus.PRIVACY_BLOCKED, "Blocked by phone privacy settings")
        }

        val actionAllowed = when (command.action) {
            WearCommandAction.ACCEPT_PENDING, WearCommandAction.IGNORE_PENDING -> pendingEnabled
            WearCommandAction.UNDO_NOTIFICATION_PAYMENT,
            WearCommandAction.CHANGE_NOTIFICATION_CATEGORY,
            WearCommandAction.QUICK_ADD_EXPENSE -> recentEnabled
            WearCommandAction.OPEN_ON_PHONE -> true
        }
        if (!actionAllowed) {
            return WearCommandResult(command.requestId, WearCommandStatus.PRIVACY_BLOCKED, "Feature disabled on phone")
        }

        if (command.action == WearCommandAction.OPEN_ON_PHONE) {
            openOnPhone(app, command.target)
            return WearCommandResult(command.requestId, WearCommandStatus.SUCCESS, "Opened on phone", PhoneBudgetBridge.currentRevision(app))
        }

        InMemoryDatabase.ensureInitialized(app)
        PendingPaymentStore.initialize(app)

        val result = synchronized(lock) {
            if (isProcessed(app, command.requestId)) {
                return@synchronized WearCommandResult(
                    command.requestId,
                    WearCommandStatus.ALREADY_HANDLED,
                    "Already handled",
                    PhoneBudgetBridge.currentRevision(app)
                )
            }

            val outcome = when (command.action) {
                WearCommandAction.ACCEPT_PENDING -> handleAcceptPending(app, command)
                WearCommandAction.IGNORE_PENDING -> handleIgnorePending(app, command)
                WearCommandAction.UNDO_NOTIFICATION_PAYMENT -> handleUndo(command)
                WearCommandAction.CHANGE_NOTIFICATION_CATEGORY -> handleChangeCategory(command)
                WearCommandAction.QUICK_ADD_EXPENSE -> handleQuickAdd(command)
                WearCommandAction.OPEN_ON_PHONE -> WearCommandResult(command.requestId, WearCommandStatus.SUCCESS)
            }

            if (outcome.status == WearCommandStatus.SUCCESS || outcome.status == WearCommandStatus.ALREADY_HANDLED) {
                markProcessed(app, command.requestId)
            }
            outcome
        }

        if (result.status == WearCommandStatus.SUCCESS) {
            PhoneBudgetBridge.publishSafely(app)
            return result.copy(newRevision = PhoneBudgetBridge.currentRevision(app))
        }
        return result
    }

    private fun handleAcceptPending(context: Context, command: WearCommand): WearCommandResult {
        val pending = PendingPaymentStore.pending.value.firstOrNull { it.id == command.paymentId }
        if (pending == null) {
            val alreadyLogged = InMemoryDatabase.transactions.value.any { it.groupId == "notification:${command.paymentId}" }
            return if (alreadyLogged) {
                WearCommandResult(command.requestId, WearCommandStatus.ALREADY_HANDLED, "Already logged on phone")
            } else {
                WearCommandResult(command.requestId, WearCommandStatus.EXPIRED_OR_MISSING, "Item already handled or removed")
            }
        }
        val account = InMemoryDatabase.accounts.value.firstOrNull { it.id == command.accountId }
            ?: return WearCommandResult(command.requestId, WearCommandStatus.INVALID_REQUEST, "Account unavailable")

        val effectiveType = when (command.overrideType.uppercase()) {
            "INCOME", "TRANSFER", "EXPENSE" -> command.overrideType.uppercase()
            else -> pending.transactionType
        }
        val isNonTopUpTransferOrIncome = (!pending.isTopUp && effectiveType == "TRANSFER") || effectiveType == "INCOME"
        if (isNonTopUpTransferOrIncome && account.type == AccountType.CC) {
            return WearCommandResult(command.requestId, WearCommandStatus.INVALID_REQUEST, "Use a bank account for transfer/income")
        }
        if (effectiveType != pending.transactionType) {
            PendingPaymentStore.updatePayment(pending.id, pending.copy(transactionType = effectiveType))
        }
        val accepted = PendingPaymentStore.accept(context, pending.id, account.id, command.rememberChoice)
        return if (accepted) {
            WearCommandResult(command.requestId, WearCommandStatus.SUCCESS, "Logged")
        } else {
            WearCommandResult(command.requestId, WearCommandStatus.ALREADY_HANDLED, "Already handled")
        }
    }

    private fun handleIgnorePending(context: Context, command: WearCommand): WearCommandResult {
        val pending = PendingPaymentStore.pending.value.firstOrNull { it.id == command.paymentId }
            ?: return WearCommandResult(command.requestId, WearCommandStatus.ALREADY_HANDLED, "Already dismissed")
        PendingPaymentStore.ignore(context, pending.id)
        return WearCommandResult(command.requestId, WearCommandStatus.SUCCESS, "Ignored")
    }

    private fun handleUndo(command: WearCommand): WearCommandResult {
        val key = if (command.paymentId.startsWith("notification:")) command.paymentId else "notification:${command.paymentId}"
        val undone = InMemoryDatabase.undoNotificationPayment(key)
        return if (undone) {
            WearCommandResult(command.requestId, WearCommandStatus.SUCCESS, "Undone")
        } else {
            WearCommandResult(command.requestId, WearCommandStatus.EXPIRED_OR_MISSING, "Transaction not found")
        }
    }

    private fun handleChangeCategory(command: WearCommand): WearCommandResult {
        val key = if (command.paymentId.startsWith("notification:")) command.paymentId else "notification:${command.paymentId}"
        val changed = InMemoryDatabase.changeNotificationCategory(key, command.category.trim())
        return if (changed) {
            WearCommandResult(command.requestId, WearCommandStatus.SUCCESS, "Category updated")
        } else {
            WearCommandResult(command.requestId, WearCommandStatus.EXPIRED_OR_MISSING, "Transaction not found")
        }
    }

    private fun handleQuickAdd(command: WearCommand): WearCommandResult {
        val account = InMemoryDatabase.accounts.value.firstOrNull { it.id == command.accountId }
            ?: return WearCommandResult(command.requestId, WearCommandStatus.INVALID_REQUEST, "Account unavailable")
        val groupKey = "notification:wear-${command.requestId}"
        val tx = TransactionEntity(
            amount = command.amount,
            category = command.category.trim(),
            timestamp = System.currentTimeMillis(),
            accountId = account.id,
            description = command.note.trim().ifBlank { command.category.trim() },
            groupId = groupKey,
            isExcludedFromDailyBudget = false
        )
        val inserted = InMemoryDatabase.insertNotificationExpenseIfAbsent(tx)
        return if (inserted) {
            WearCommandResult(command.requestId, WearCommandStatus.SUCCESS, "Logged")
        } else {
            WearCommandResult(command.requestId, WearCommandStatus.ALREADY_HANDLED, "Already logged")
        }
    }

    private fun openOnPhone(context: Context, target: String) {
        val tab = when {
            target.startsWith("history") || target.startsWith("tx:") -> "history"
            target.startsWith("recurring") -> "recurring"
            target.startsWith("accounts") -> "accounts"
            else -> "home"
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_tab", tab)
        }
        runCatching { context.startActivity(intent) }
    }

    private fun isProcessed(context: Context, requestId: String): Boolean {
        val prefs = context.getSharedPreferences(PROCESSED_PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(PROCESSED_KEY, "[]") ?: "[]"
        val arr = runCatching { JSONArray(raw) }.getOrDefault(JSONArray())
        for (i in 0 until arr.length()) {
            if (arr.optString(i) == requestId) return true
        }
        return false
    }

    private fun markProcessed(context: Context, requestId: String) {
        val prefs = context.getSharedPreferences(PROCESSED_PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(PROCESSED_KEY, "[]") ?: "[]"
        val list = mutableListOf<String>()
        val arr = runCatching { JSONArray(raw) }.getOrDefault(JSONArray())
        for (i in 0 until arr.length()) {
            val v = arr.optString(i)
            if (v.isNotBlank() && v != requestId) list.add(v)
        }
        list.add(requestId)
        val trimmed = list.takeLast(MAX_PROCESSED_IDS)
        val out = JSONArray().apply { trimmed.forEach { put(it) } }
        prefs.edit().putString(PROCESSED_KEY, out.toString()).commit()
    }

    internal fun clearProcessedForTest(context: Context) {
        synchronized(lock) {
            context.getSharedPreferences(PROCESSED_PREFS, Context.MODE_PRIVATE).edit().clear().commit()
        }
    }
}
