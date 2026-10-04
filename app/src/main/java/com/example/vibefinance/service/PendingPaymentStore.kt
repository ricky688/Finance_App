package com.example.vibefinance.service

import android.content.Context
import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.data.entity.TransactionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.Locale

/** A payment alert waiting for the user to choose one of their real accounts. */
data class PendingPayment(
    val id: String,
    val fingerprint: String,
    val sourcePackage: String,
    val assetHint: String,
    val merchant: String,
    val amount: Double,
    val detectedAt: Long,
    val cardLast4: String? = null,
    val balanceRemaining: Double? = null
)

/**
 * A separate durable inbox keeps a notification from changing balances until its account is known.
 * Remembered choices are scoped to the posting app AND the card/wallet hint, so one bank app can
 * safely report payments from several cards.
 */
object PendingPaymentStore {
    private const val PREFS_NAME = "pending_payment_choices"
    private const val PENDING_KEY = "pending"
    private const val REMEMBERED_KEY = "remembered_accounts"
    private const val SEEN_KEY = "seen_alerts"
    private const val SEEN_RETENTION_MS = 7L * 24 * 60 * 60 * 1000
    private const val SAME_ALERT_WINDOW_MS = 15L * 60 * 1000
    private const val MAX_SEEN = 500

    private val _pending = MutableStateFlow<List<PendingPayment>>(emptyList())
    val pending = _pending.asStateFlow()

    private var initialized = false
    private var remembered = mutableMapOf<String, Long>()
    private var seen = linkedMapOf<String, Long>()

    @Synchronized
    fun initialize(context: Context) {
        if (initialized) return
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _pending.value = runCatching {
            val array = JSONArray(prefs.getString(PENDING_KEY, "[]"))
            (0 until array.length()).mapNotNull { index ->
                val item = array.optJSONObject(index) ?: return@mapNotNull null
                val amount = item.optDouble("amount", 0.0)
                val id = item.optString("id")
                if (id.isBlank() || !amount.isFinite() || amount <= 0.0) return@mapNotNull null
                PendingPayment(
                    id = id,
                    fingerprint = item.optString("fingerprint", id),
                    sourcePackage = item.optString("sourcePackage"),
                    assetHint = item.optString("assetHint"),
                    merchant = item.optString("merchant"),
                    amount = amount,
                    detectedAt = item.optLong("detectedAt", System.currentTimeMillis()),
                    cardLast4 = if (item.has("cardLast4") && !item.isNull("cardLast4")) item.getString("cardLast4") else null,
                    balanceRemaining = if (item.has("balanceRemaining") && !item.isNull("balanceRemaining")) item.getDouble("balanceRemaining") else null
                )
            }
        }.getOrDefault(emptyList())
        remembered = runCatching {
            val json = JSONObject(prefs.getString(REMEMBERED_KEY, "{}") ?: "{}")
            json.keys().asSequence().mapNotNull { key ->
                json.optLong(key, 0L).takeIf { it > 0L }?.let { key to it }
            }.toMap().toMutableMap()
        }.getOrDefault(mutableMapOf())
        seen = runCatching {
            val json = JSONObject(prefs.getString(SEEN_KEY, "{}") ?: "{}")
            json.keys().asSequence().map { key -> key to json.optLong(key) }.toMap().toMap(linkedMapOf())
        }.getOrDefault(linkedMapOf())
        trimSeen()
        initialized = true
    }

    @Synchronized
    fun enqueue(
        context: Context,
        sourcePackage: String,
        assetHint: String,
        merchant: String,
        amount: Double,
        detectedAt: Long,
        notificationKey: String = "",
        cardLast4: String? = null,
        balanceRemaining: Double? = null
    ): PendingPayment? {
        initialize(context)
        if (!amount.isFinite() || amount <= 0.0 || sourcePackage.isBlank()) return null
        // A notification update keeps its Android key; time bucketing that key can log it twice.
        // Without a key, use a short bucket so unrelated later purchases can still be recorded.
        val rawIdentity = if (notificationKey.isNotBlank()) {
            // Notification text can gain a merchant or card hint when the same Android alert updates.
            "$sourcePackage|$notificationKey|$amount"
        } else {
            "$sourcePackage|$assetHint|$merchant|$amount|${detectedAt / 120_000L}"
        }
        val fingerprint = MessageDigest.getInstance("SHA-256")
            .digest(rawIdentity.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        if (_pending.value.any {
                it.fingerprint == fingerprint &&
                    kotlin.math.abs(it.detectedAt - detectedAt) <= SAME_ALERT_WINDOW_MS
            }
        ) return null
        if (seen[fingerprint]?.let { System.currentTimeMillis() - it <= SAME_ALERT_WINDOW_MS } == true) {
            return null
        }
        val id = MessageDigest.getInstance("SHA-256")
            .digest("$fingerprint|$detectedAt".toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        val payment = PendingPayment(
            id = id,
            fingerprint = fingerprint,
            sourcePackage = sourcePackage,
            assetHint = assetHint,
            merchant = merchant,
            amount = amount,
            detectedAt = detectedAt,
            cardLast4 = cardLast4,
            balanceRemaining = balanceRemaining
        )
        _pending.value = _pending.value + payment
        persist(context)
        return payment
    }

    @Synchronized
    fun isBocGoHint(hint: String): Boolean {
        val h = hint.lowercase(Locale.ROOT)
        if (h.contains('…') || h.contains("...")) return false
        val hasBoc = h.contains("boc") || h.contains("中銀") || h.contains("bank of china")
        val hasGo = h.contains("go") || h.contains("大灣區")
        val hasUnionPay = h.contains("unionpay") || h.contains("union pay") || h.contains("銀聯")
        return (hasBoc && (hasGo || hasUnionPay)) || (hasGo && hasUnionPay)
    }

    fun isBocGoAccount(accountName: String): Boolean {
        val a = accountName.lowercase(Locale.ROOT)
        val hasBoc = a.contains("boc") || a.contains("中銀") || a.contains("bank of china")
        val hasGo = a.contains("go") || a.contains("大灣區")
        return (hasBoc && hasGo) || a == "boc go" || a == "中銀 go" || a.startsWith("boc go ") || a.startsWith("中銀 go ")
    }

    fun isBocUnionPayAccount(accountName: String): Boolean {
        val a = accountName.lowercase(Locale.ROOT)
        val hasBoc = a.contains("boc") || a.contains("中銀") || a.contains("bank of china")
        val hasUnionPay = a.contains("unionpay") || a.contains("union pay") || a.contains("銀聯")
        return hasBoc && hasUnionPay
    }

    fun isBocAccount(accountName: String): Boolean {
        val a = accountName.lowercase(Locale.ROOT)
        return a.contains("boc") || a.contains("中銀") || a.contains("bank of china")
    }

    fun findBocGoMatch(accounts: List<AccountEntity>): AccountEntity? {
        // 1. Direct BOC Go account (e.g. "BOC Go", "BOC Go Card", "中銀 Go", "中銀 Go 卡")
        accounts.firstOrNull { isBocGoAccount(it.name) }?.let { return it }
        // 2. BOC UnionPay account (e.g. "BOC UnionPay", "中銀銀聯")
        accounts.firstOrNull { isBocUnionPayAccount(it.name) }?.let { return it }
        // 3. BOC Credit Card account (e.g. "BOC Credit Card", "中銀信用卡")
        accounts.firstOrNull { it.type == AccountType.CC && isBocAccount(it.name) }?.let { return it }
        // 4. Any BOC account
        accounts.firstOrNull { isBocAccount(it.name) }?.let { return it }
        return null
    }

    @Synchronized
    fun rememberedAccountId(context: Context, payment: PendingPayment): Long? {
        initialize(context)
        val pkg = payment.sourcePackage.lowercase(Locale.ROOT)
        val hint = payment.assetHint.trim().lowercase(Locale.ROOT)
        val accountId = remembered[rememberKey(payment)]
            ?: (!payment.cardLast4.isNullOrBlank()).let { if (it) remembered["$pkg|card:${payment.cardLast4}"] else null }
            ?: remembered["$pkg|$hint"]
            ?: (!payment.cardLast4.isNullOrBlank()).let { if (it) remembered["card:${payment.cardLast4}"] else null }
            ?: remembered[hint]
            ?: (if (isBocGoHint(hint)) {
                remembered["boc_go"]
                    ?: remembered.entries.firstOrNull { (k, _) -> isBocGoHint(k) }?.value
                    ?: findBocGoMatch(InMemoryDatabase.accounts.value)?.id
            } else null)
            ?: return null
        val account = InMemoryDatabase.accounts.value.firstOrNull { it.id == accountId } ?: return null
        return accountId.takeIf { canRememberChoice(payment, account) }
    }

    /** Only a specific card/account hint may be used to route a future payment without asking. */
    fun canRememberChoice(payment: PendingPayment, account: AccountEntity): Boolean {
        // If physical card last 4 digits match between payment and account, it is explicitly the same card
        if (!payment.cardLast4.isNullOrBlank() && !account.cardLast4.isNullOrBlank() && payment.cardLast4 == account.cardLast4) {
            return true
        }

        val hint = payment.assetHint.trim().lowercase(Locale.ROOT)
        if (hint.isBlank()) return false
        // A display-truncated card title is not a stable identity. The missing suffix could
        // distinguish two cards posted by the same bank app, even when four digits remain.
        if (hint.contains('…') || hint.contains("...")) return false
        val accountName = account.name.trim().lowercase(Locale.ROOT)

        // Dedicated payment wallets can map to an account with the same explicit name.
        if (hint in setOf("payme", "alipay", "wechat pay", "octopus", "八達通", "android版八達通") && (hint == accountName || ((hint.contains("octopus") || hint.contains("八達通")) && (accountName.contains("octopus") || accountName.contains("八達通"))))) {
            return true
        }
        // Smart Octopus / Android Octopus / Octopus is the name in the payment alert; users commonly name
        // that same account Octopus or 八達通 or Wallet (Cash) or 現金.
        if (hint in setOf("smart octopus", "octopus", "八達通", "android版八達通") && (account.type == AccountType.CASH || accountName in setOf("smart octopus", "octopus", "八達通", "android版八達通", "手機八達通", "wallet", "wallet (cash)", "現金", "錢包"))) {
            return true
        }
        // BOC Go / BOC UnionPay cards can bind to any account matching BOC Go or BOC credit cards
        if (isBocGoHint(hint)) {
            if (isBocGoAccount(accountName) || isBocUnionPayAccount(accountName) || (account.type == AccountType.CC && isBocAccount(accountName)) || isBocAccount(accountName)) {
                return true
            }
        }
        val genericHints = setOf(
            "google pay", "google wallet", "samsung pay", "samsung wallet", "payme", "fps",
            "alipay", "wechat pay", "wallet (cash)", "hsbc", "citi", "chase",
            "bank of china", "hang seng bank", "mox bank"
        )
        if (hint in genericHints || hint.endsWith(" merchant")) return false
        if (Regex("(?:^|\\D)\\d{4}(?:\\D|$)").containsMatchIn(hint)) return true
        return accountName.length >= 6 && (hint == accountName || hint.contains(accountName))
    }

    /** Atomic balance and transaction persistence plus a stable event ID prevent double logging. */
    @Synchronized
    fun accept(context: Context, paymentId: String, accountId: Long, rememberChoice: Boolean): Boolean {
        initialize(context)
        val payment = _pending.value.firstOrNull { it.id == paymentId } ?: return false
        val account = InMemoryDatabase.accounts.value.firstOrNull { it.id == accountId } ?: return false
        val transactionKey = "notification:${payment.id}"
        val inserted = InMemoryDatabase.insertNotificationExpenseIfAbsent(
            TransactionEntity(
                amount = payment.amount,
                category = PaymentNotificationListener.determineCategory(payment.merchant),
                timestamp = payment.detectedAt.coerceAtMost(System.currentTimeMillis()),
                accountId = account.id,
                description = payment.merchant,
                groupId = transactionKey
            )
        )
        if (!inserted && InMemoryDatabase.transactions.value.none { it.groupId == transactionKey }) {
            return false
        }
        // Auto-bind authentic intercepted card last 4 digits to the account if not already set
        var currentAcc = InMemoryDatabase.accounts.value.firstOrNull { it.id == accountId } ?: account
        var accChanged = false
        if (currentAcc.cardLast4.isNullOrBlank() && !payment.cardLast4.isNullOrBlank()) {
            currentAcc = currentAcc.copy(cardLast4 = payment.cardLast4)
            accChanged = true
        }
        if (payment.balanceRemaining != null && payment.balanceRemaining.isFinite()) {
            currentAcc = currentAcc.copy(balance = payment.balanceRemaining)
            accChanged = true
        }
        if (accChanged) {
            InMemoryDatabase.updateAccount(currentAcc)
        }
        if (rememberChoice && canRememberChoice(payment, account)) {
            val key = rememberKey(payment)
            remembered[key] = account.id
            val pkg = payment.sourcePackage.lowercase(Locale.ROOT)
            val hint = payment.assetHint.trim().lowercase(Locale.ROOT)
            remembered["$pkg|$hint"] = account.id
            if (isBocGoHint(hint)) {
                remembered["boc_go"] = account.id
                remembered[hint] = account.id
            }
            if (!payment.cardLast4.isNullOrBlank()) {
                remembered["$pkg|card:${payment.cardLast4}"] = account.id
            }
        }
        markSeen(payment)
        _pending.value = _pending.value.filterNot { it.id == paymentId }
        persist(context)
        return true
    }

    @Synchronized
    fun ignore(context: Context, paymentId: String) {
        initialize(context)
        if (_pending.value.none { it.id == paymentId }) return
        markSeen(_pending.value.first { it.id == paymentId })
        _pending.value = _pending.value.filterNot { it.id == paymentId }
        persist(context)
    }

    private fun rememberKey(payment: PendingPayment): String {
        val pkg = payment.sourcePackage.lowercase(Locale.ROOT)
        val hint = payment.assetHint.trim().lowercase(Locale.ROOT)
        return if (!payment.cardLast4.isNullOrBlank()) {
            "$pkg|$hint|card:${payment.cardLast4}"
        } else {
            "$pkg|$hint"
        }
    }

    private fun markSeen(payment: PendingPayment) {
        seen[payment.fingerprint] = System.currentTimeMillis()
        trimSeen()
    }

    @Synchronized
    fun clearRememberedChoices(context: Context) {
        initialize(context)
        remembered.clear()
        persist(context)
    }

    /** Existing pending alerts refer to the old account collection after a replacement import. */
    @Synchronized
    fun clearForDataReplacement(context: Context) {
        initialize(context)
        _pending.value.forEach(::markSeen)
        _pending.value = emptyList()
        remembered.clear()
        persist(context)
    }

    private fun trimSeen() {
        val cutoff = System.currentTimeMillis() - SEEN_RETENTION_MS
        seen.entries.removeIf { it.value < cutoff }
        while (seen.size > MAX_SEEN) seen.remove(seen.keys.first())
    }

    private fun persist(context: Context) {
        val pendingJson = JSONArray().apply {
            _pending.value.forEach { payment ->
                put(JSONObject().apply {
                    put("id", payment.id)
                    put("fingerprint", payment.fingerprint)
                    put("sourcePackage", payment.sourcePackage)
                    put("assetHint", payment.assetHint)
                    put("merchant", payment.merchant)
                    put("amount", payment.amount)
                    put("detectedAt", payment.detectedAt)
                    payment.cardLast4?.let { put("cardLast4", it) }
                    payment.balanceRemaining?.let { put("balanceRemaining", it) }
                })
            }
        }
        val rememberedJson = JSONObject().apply {
            remembered.forEach { (key, accountId) -> put(key, accountId) }
        }
        val seenJson = JSONObject().apply {
            seen.forEach { (key, timestamp) -> put(key, timestamp) }
        }
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(PENDING_KEY, pendingJson.toString())
            .putString(REMEMBERED_KEY, rememberedJson.toString())
            .putString(SEEN_KEY, seenJson.toString())
            .commit()
    }
}
