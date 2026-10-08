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
    val balanceRemaining: Double? = null,
    val isTopUp: Boolean = false,
    val rawTitle: String = "",
    val rawText: String = "",
    val transactionType: String = "EXPENSE"
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
        val prefs = com.example.vibefinance.util.CoordinatedPreferences.get(context.applicationContext, PREFS_NAME)
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
                    balanceRemaining = if (item.has("balanceRemaining") && !item.isNull("balanceRemaining")) item.getDouble("balanceRemaining") else null,
                    isTopUp = item.optBoolean("isTopUp", false),
                    rawTitle = item.optString("rawTitle", ""),
                    rawText = item.optString("rawText", ""),
                    transactionType = item.optString("transactionType", "EXPENSE")
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
        balanceRemaining: Double? = null,
        isTopUp: Boolean = false,
        rawTitle: String = "",
        rawText: String = "",
        transactionType: String = "EXPENSE"
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

        val isOctopusTopUpTx = isTopUp || transactionType == "TRANSFER" ||
            merchant.contains("OCL*", ignoreCase = true) || merchant.contains("OCTOPUS", ignoreCase = true)

        if (isOctopusTopUpTx) {
            // Deduplicate concurrent top-up notifications (e.g. Bank app + Samsung Wallet) within 90s
            val duplicateInPending = _pending.value.any {
                (it.isTopUp || it.transactionType == "TRANSFER" || it.merchant.contains("OCL*", ignoreCase = true) || it.merchant.contains("OCTOPUS", ignoreCase = true)) &&
                    kotlin.math.abs(it.amount - amount) < 0.01 &&
                    kotlin.math.abs(it.detectedAt - detectedAt) <= 90_000L
            }
            if (duplicateInPending) {
                return null
            }

            val duplicateInTransactions = InMemoryDatabase.transactions.value.any {
                (it.category == "Top-up" || it.toAccountId != null || it.description.contains("OCL*", ignoreCase = true) || it.description.contains("OCTOPUS", ignoreCase = true)) &&
                    kotlin.math.abs(kotlin.math.abs(it.amount) - amount) < 0.01 &&
                    kotlin.math.abs(it.timestamp - detectedAt) <= 90_000L
            }
            if (duplicateInTransactions) {
                return null
            }
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
            balanceRemaining = balanceRemaining,
            isTopUp = isTopUp,
            rawTitle = rawTitle,
            rawText = rawText,
            transactionType = transactionType
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

    fun isOctopusHint(hint: String): Boolean {
        val h = hint.lowercase(Locale.ROOT)
        return h.contains("octopus") || h.contains("八達通") || h.contains("八逹通")
    }

    fun isOctopusPayment(payment: PendingPayment): Boolean {
        val hint = payment.assetHint.lowercase(Locale.ROOT)
        val pkg = payment.sourcePackage.lowercase(Locale.ROOT)
        val merch = payment.merchant.lowercase(Locale.ROOT)
        return isOctopusHint(hint) ||
            pkg.contains("octopus") ||
            merch.contains("octopus") ||
            merch.contains("八達通") ||
            merch.contains("八逹通") ||
            merch.startsWith("ocl*")
    }

    fun isOctopusAccount(account: AccountEntity): Boolean {
        val name = account.name.lowercase(Locale.ROOT)
        val nick = account.nickname?.lowercase(Locale.ROOT).orEmpty()
        return isOctopusHint(name) || isOctopusHint(nick) ||
            name in setOf("octopus", "smart octopus", "android octopus", "android版八達通", "手機八達通", "八達通", "八逹通")
    }

    fun addNotificationAlias(account: AccountEntity, newAlias: String): AccountEntity {
        val existing = account.getNotificationAliasList().toMutableList()
        val trimmed = newAlias.trim()
        if (trimmed.isNotBlank() && existing.none { it.equals(trimmed, ignoreCase = true) }) {
            existing.add(trimmed)
            return account.copy(notificationAliases = existing.joinToString(", "))
        }
        return account
    }

    fun findMatchingAccount(payment: PendingPayment, accounts: List<AccountEntity>): AccountEntity? {
        if (accounts.isEmpty()) return null

        // 0. User's explicit notification aliases / IDs (HIGHEST PRIORITY)
        for (acc in accounts) {
            val aliases = acc.getNotificationAliasList()
            if (aliases.isEmpty()) continue
            for (alias in aliases) {
                val a = alias.lowercase(Locale.ROOT)
                // Match card last 4 digits
                if (!payment.cardLast4.isNullOrBlank() && (a == payment.cardLast4 || a == "•••• ${payment.cardLast4}")) {
                    return acc
                }
                // Match asset hint
                val hint = payment.assetHint.trim().lowercase(Locale.ROOT)
                if (hint.isNotBlank()) {
                    if (hint == a || (a.length >= 2 && hint.contains(a)) || (hint.length >= 3 && a.contains(hint))) {
                        return acc
                    }
                }
                // Match merchant if merchant contains alias
                val merch = payment.merchant.trim().lowercase(Locale.ROOT)
                if (merch.isNotBlank() && a.length >= 3 && (merch.contains(a) || a.contains(merch))) {
                    return acc
                }
            }
        }

        // 1. Direct card last 4 match
        if (!payment.cardLast4.isNullOrBlank()) {
            val last4 = payment.cardLast4
            accounts.firstOrNull { it.cardLast4 == last4 }?.let { return it }
            accounts.firstOrNull { it.name.contains(last4) || it.nickname?.contains(last4) == true }?.let { return it }
        }

        // 2. Octopus matching: robust matching against user custom names (e.g. "octopus", "Octopus", "八達通")
        if (isOctopusPayment(payment)) {
            // First: if payment has cardLast4, check if any Octopus account matches that cardLast4
            if (!payment.cardLast4.isNullOrBlank()) {
                accounts.firstOrNull { isOctopusAccount(it) && it.cardLast4 == payment.cardLast4 }?.let { return it }
            }
            // Second: exact name/nickname match (e.g. user set custom card name as "octopus", "八達通", "Smart Octopus")
            accounts.firstOrNull {
                val n = it.name.trim().lowercase(Locale.ROOT)
                val nk = it.nickname?.trim()?.lowercase(Locale.ROOT).orEmpty()
                n == "octopus" || nk == "octopus" || n == "八達通" || nk == "八達通" || n == "smart octopus" || nk == "smart octopus"
            }?.let { return it }
            // Third: any account containing octopus or 八達通
            accounts.firstOrNull { isOctopusAccount(it) }?.let { return it }
            // Fourth: fallback if user only has a single Cash / Wallet account
            accounts.firstOrNull {
                it.name.trim().lowercase(Locale.ROOT) in setOf("wallet", "wallet (cash)", "現金", "錢包")
            }?.let { return it }
        }

        // 3. BOC Go / UnionPay match
        if (isBocGoHint(payment.assetHint) || payment.merchant.contains("BOC Go", ignoreCase = true) || payment.merchant.contains("中銀 Go")) {
            findBocGoMatch(accounts)?.let { return it }
        }

        val hint = payment.assetHint.trim().lowercase(Locale.ROOT)
        val merch = payment.merchant.lowercase(Locale.ROOT)

        // 4. Dedicated Wallets & Bank matchers
        when {
            hint.contains("payme") || merch.contains("payme") ->
                accounts.firstOrNull { it.name.contains("payme", ignoreCase = true) }?.let { return it }
            hint.contains("alipay") || hint.contains("支付寶") || merch.contains("alipay") || merch.contains("支付寶") ->
                accounts.firstOrNull { it.name.contains("alipay", ignoreCase = true) || it.name.contains("支付寶") || it.name.contains("支付宝") }?.let { return it }
            hint.contains("wechat") || hint.contains("微信") || merch.contains("wechat") || merch.contains("微信") ->
                accounts.firstOrNull { it.name.contains("wechat", ignoreCase = true) || it.name.contains("微信") }?.let { return it }
            hint.contains("fps") || hint.contains("轉數快") || merch.contains("fps") || merch.contains("轉數快") ->
                accounts.firstOrNull { it.name.contains("fps", ignoreCase = true) || it.name.contains("轉數快") || it.name.contains("转数快") }?.let { return it }
            hint.contains("hsbc") || hint.contains("匯豐") || merch.contains("hsbc") || merch.contains("匯豐") ->
                accounts.firstOrNull { it.name.contains("hsbc", ignoreCase = true) || it.name.contains("匯豐") || it.name.contains("汇丰") }?.let { return it }
            hint.contains("hang seng") || hint.contains("恒生") || merch.contains("hang seng") || merch.contains("恒生") ->
                accounts.firstOrNull { it.name.contains("hang seng", ignoreCase = true) || it.name.contains("恒生") }?.let { return it }
            hint.contains("mox") || merch.contains("mox") ->
                accounts.firstOrNull { it.name.contains("mox", ignoreCase = true) }?.let { return it }
            hint.contains("citi") || hint.contains("花旗") || merch.contains("citi") || merch.contains("花旗") ->
                accounts.firstOrNull { it.name.contains("citi", ignoreCase = true) || it.name.contains("花旗") }?.let { return it }
        }

        // 5. Normalized string containment / matching
        fun norm(s: String) = s.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9\\u4e00-\\u9fa5]"), "")
        val normHint = norm(payment.assetHint)
        if (normHint.length >= 2) {
            accounts.firstOrNull { norm(it.name) == normHint || (it.nickname?.let { nk -> norm(nk) == normHint } == true) }?.let { return it }
            accounts.firstOrNull {
                val n = norm(it.name)
                val nk = it.nickname?.let(::norm).orEmpty()
                (n.length >= 2 && normHint.contains(n)) || (nk.length >= 2 && normHint.contains(nk)) ||
                    (n.length >= 2 && n.contains(normHint))
            }?.let { return it }
        }

        return null
    }

    @Synchronized
    fun rememberedAccountId(context: Context, payment: PendingPayment): Long? {
        initialize(context)
        val pkg = payment.sourcePackage.lowercase(Locale.ROOT)
        val hint = payment.assetHint.trim().lowercase(Locale.ROOT)
        val accounts = InMemoryDatabase.accounts.value

        val explicitId = remembered[rememberKey(payment)]
            ?: (!payment.cardLast4.isNullOrBlank()).let { if (it) remembered["$pkg|card:${payment.cardLast4}"] else null }
            ?: remembered["$pkg|$hint"]
            ?: (!payment.cardLast4.isNullOrBlank()).let { if (it) remembered["card:${payment.cardLast4}"] else null }
            ?: remembered[hint]

        if (explicitId != null) {
            val account = accounts.firstOrNull { it.id == explicitId }
            if (account != null && canRememberChoice(payment, account)) {
                return explicitId
            }
        }

        // Auto-match against user accounts when there is a deterministic/clear match
        val matched = findMatchingAccount(payment, accounts)
        if (matched != null && canRememberChoice(payment, matched)) {
            return matched.id
        }

        return null
    }

    fun isGenericHint(hint: String): Boolean {
        val h = hint.lowercase(Locale.ROOT)
        val genericHints = setOf(
            "google pay", "google wallet", "samsung pay", "samsung wallet", "payme", "fps",
            "alipay", "wechat pay", "wallet (cash)", "hsbc", "citi", "chase",
            "bank of china", "hang seng bank", "mox bank"
        )
        return h in genericHints || h.endsWith(" merchant")
    }

    /** Only a specific card/account hint may be used to route a future payment without asking. */
    fun canRememberChoice(payment: PendingPayment, account: AccountEntity): Boolean {
        // If physical card last 4 digits match between payment and account, it is explicitly the same card
        if (!payment.cardLast4.isNullOrBlank() && !account.cardLast4.isNullOrBlank() && payment.cardLast4 == account.cardLast4) {
            return true
        }

        val hint = payment.assetHint.trim().lowercase(Locale.ROOT)
        if (hint.isBlank()) return false
        if (hint.contains('…') || hint.contains("...")) return false
        val accountName = account.name.trim().lowercase(Locale.ROOT)
        val accountNickname = account.nickname?.trim()?.lowercase(Locale.ROOT).orEmpty()

        // Check user's explicitly configured aliases on this account
        if (account.getNotificationAliasList().any {
            val a = it.lowercase(Locale.ROOT)
            a == hint || (a.length >= 3 && hint.contains(a)) || (hint.length >= 3 && a.contains(hint)) ||
                (!payment.cardLast4.isNullOrBlank() && a == payment.cardLast4)
        }) {
            return true
        }

        // Dedicated Octopus matching: auto-logging without manual friction
        if (isOctopusPayment(payment)) {
            return isOctopusAccount(account) || account.type == AccountType.CASH ||
                accountName in setOf("smart octopus", "octopus", "八達通", "android版八達通", "手機八達通", "wallet", "wallet (cash)", "現金", "錢包")
        }

        // BOC Go / BOC UnionPay cards can bind to any account matching BOC Go or BOC credit cards
        if (isBocGoHint(hint)) {
            return isBocGoAccount(accountName) || isBocUnionPayAccount(accountName) || (account.type == AccountType.CC && isBocAccount(accountName)) || isBocAccount(accountName)
        }

        // Dedicated payment wallets with matching explicit name
        if (hint in setOf("payme", "alipay", "wechat pay", "fps")) {
            return hint == accountName || (hint.contains("payme") && accountName.contains("payme")) || ((hint.contains("alipay") || hint.contains("支付寶")) && (accountName.contains("alipay") || accountName.contains("支付寶")))
        }

        // Generic hints without a specific card cannot be safely remembered
        if (isGenericHint(hint)) return false
        if (Regex("(?:^|\\D)\\d{4}(?:\\D|$)").containsMatchIn(hint)) return true

        fun norm(s: String) = s.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9\\u4e00-\\u9fa5]"), "")
        val normHint = norm(hint)
        val normAcc = norm(accountName)
        val normNick = norm(accountNickname)
        if (normHint.isNotEmpty()) {
            if (normAcc.length >= 6 && (normHint == normAcc || normHint.contains(normAcc) || normAcc.contains(normHint))) {
                return true
            }
            if (normNick.length >= 6 && (normHint == normNick || normHint.contains(normNick) || normNick.contains(normNick))) {
                return true
            }
        }

        return false
    }

    /** Atomic balance and transaction persistence plus a stable event ID prevent double logging. */
    @Synchronized
    fun accept(context: Context, paymentId: String, accountId: Long, rememberChoice: Boolean): Boolean {
        initialize(context)
        val payment = _pending.value.firstOrNull { it.id == paymentId } ?: return false
        val account = InMemoryDatabase.accounts.value.firstOrNull { it.id == accountId } ?: return false
        val transactionKey = "notification:${payment.id}"
        val isTopUp = payment.isTopUp || payment.transactionType == "TRANSFER"
        val isIncome = !isTopUp && payment.transactionType == "INCOME"
        val isRepayment = payment.transactionType == "REPAYMENT"

        val isDestinationOctopus = isOctopusAccount(account)
        val destinationOctopus = if (isTopUp && !isDestinationOctopus) {
            InMemoryDatabase.accounts.value.firstOrNull { it.id != account.id && isOctopusAccount(it) }
        } else null

        val toAccountId: Long?
        val txAmount: Double
        val txCategory: String
        val isExcluded: Boolean

        when {
            isTopUp -> {
                txCategory = "Top-up"
                isExcluded = true
                if (destinationOctopus != null) {
                    toAccountId = destinationOctopus.id
                    txAmount = payment.amount
                } else if (!isDestinationOctopus) {
                    toAccountId = null
                    txAmount = payment.amount
                } else {
                    toAccountId = null
                    txAmount = -payment.amount
                }
            }
            isIncome -> {
                toAccountId = null
                txAmount = -payment.amount
                txCategory = "Salary"
                isExcluded = true
            }
            isRepayment -> {
                toAccountId = null
                txAmount = payment.amount
                txCategory = "Repayment"
                isExcluded = true
            }
            else -> {
                toAccountId = null
                txAmount = payment.amount
                txCategory = PaymentNotificationListener.determineCategory(payment.merchant)
                isExcluded = false
            }
        }

        val inserted = InMemoryDatabase.insertNotificationExpenseIfAbsent(
            TransactionEntity(
                amount = txAmount,
                category = txCategory,
                timestamp = payment.detectedAt.coerceAtMost(System.currentTimeMillis()),
                accountId = account.id,
                toAccountId = toAccountId,
                description = payment.merchant,
                groupId = transactionKey,
                isExcludedFromDailyBudget = isExcluded
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
        if (rememberChoice && canRememberChoice(payment, currentAcc)) {
            val key = rememberKey(payment)
            remembered[key] = currentAcc.id
            val pkg = payment.sourcePackage.lowercase(Locale.ROOT)
            val hint = payment.assetHint.trim()
            val hintLower = hint.lowercase(Locale.ROOT)
            remembered["$pkg|$hintLower"] = currentAcc.id
            if (isBocGoHint(hintLower)) {
                remembered["boc_go"] = currentAcc.id
                remembered[hintLower] = currentAcc.id
            }
            if (isOctopusPayment(payment)) {
                remembered["$pkg|smart octopus"] = currentAcc.id
                remembered["$pkg|octopus"] = currentAcc.id
                remembered["$pkg|八達通"] = currentAcc.id
            }
            if (!payment.cardLast4.isNullOrBlank()) {
                remembered["$pkg|card:${payment.cardLast4}"] = currentAcc.id
                remembered["card:${payment.cardLast4}"] = currentAcc.id
            }

            // Auto-persist alias into account's notificationAliases
            if (hint.isNotBlank() && !isGenericHint(hint)) {
                val updatedWithAlias = addNotificationAlias(currentAcc, hint)
                if (updatedWithAlias != currentAcc) {
                    currentAcc = updatedWithAlias
                    accChanged = true
                }
            }
            if (!payment.cardLast4.isNullOrBlank()) {
                val updatedWithCard = addNotificationAlias(currentAcc, payment.cardLast4)
                if (updatedWithCard != currentAcc) {
                    currentAcc = updatedWithCard
                    accChanged = true
                }
            }
        }
        if (accChanged) {
            InMemoryDatabase.updateAccount(currentAcc)
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

    @Synchronized
    fun updatePayment(paymentId: String, updated: PendingPayment) {
        _pending.value = _pending.value.map { if (it.id == paymentId) updated else it }
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

    @Synchronized
    internal fun reloadAfterFullRestore(context: Context) {
        initialized = false
        initialize(context)
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
                    put("isTopUp", payment.isTopUp)
                    put("rawTitle", payment.rawTitle)
                    put("rawText", payment.rawText)
                    put("transactionType", payment.transactionType)
                })
            }
        }
        val rememberedJson = JSONObject().apply {
            remembered.forEach { (key, accountId) -> put(key, accountId) }
        }
        val seenJson = JSONObject().apply {
            seen.forEach { (key, timestamp) -> put(key, timestamp) }
        }
        com.example.vibefinance.util.CoordinatedPreferences.get(context.applicationContext, PREFS_NAME)
            .edit()
            .putString(PENDING_KEY, pendingJson.toString())
            .putString(REMEMBERED_KEY, rememberedJson.toString())
            .putString(SEEN_KEY, seenJson.toString())
            .commit()
    }
}
