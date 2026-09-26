package com.example.vibefinance.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.data.entity.TransactionEntity
import com.example.vibefinance.data.repository.AccountRepository
import com.example.vibefinance.data.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Privacy-First On-Device Notification Reader & Payment Transformer.
 * Processes payment alerts, digital wallet notifications (Google Wallet, Samsung Pay, Smart Octopus),
 * bank push notifications, and payment SMS locally on-device with zero network/cloud calls.
 */
class PaymentNotificationListener : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val nanoSynthesizer by lazy { com.example.vibefinance.ai.GeminiNanoSynthesizer(this) }
    private val notificationBuffer by lazy {
        NotificationBatchBuffer(serviceScope, windowMs = 3000L) { batch ->
            val consolidatedList = nanoSynthesizer.synthesizeBatch(batch)
            for (payment in consolidatedList) {
                recordTransaction(this@PaymentNotificationListener, payment.amount, payment.merchant, payment.assetName)
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName ?: ""
        
        // 1. Ignore own app notifications immediately to prevent loops
        if (packageName == applicationContext.packageName) {
            return
        }

        // 2. Ignore ongoing / sticky / foreground service notifications (e.g. music playback, timer, download progress, system status, VPN)
        if (sbn.isOngoing) {
            return
        }

        // 3. Ignore system, UI, and non-payment Android framework packages
        val systemPackages = setOf(
            "android",
            "com.android.systemui",
            "com.android.vending",
            "com.google.android.gms",
            "com.google.android.googlequicksearchbox",
            "com.google.android.apps.nexuslauncher",
            "com.android.launcher3",
            "com.google.android.apps.photos",
            "com.google.android.apps.messaging",
            "com.google.android.dialer",
            "com.google.android.apps.maps",
            "com.google.android.calendar",
            "com.google.android.youtube",
            "com.google.android.gm",
            "com.android.settings",
            "com.android.bluetooth",
            "com.android.nfc",
            "com.android.providers.downloads"
        )
        if (systemPackages.contains(packageName.lowercase(Locale.US))) {
            return
        }

        val extras = sbn.notification.extras ?: Bundle.EMPTY
        val title = (extras.getCharSequence(Notification.EXTRA_TITLE) ?: "").toString()
        val text = (extras.getCharSequence(Notification.EXTRA_TEXT) ?: "").toString()

        if (title.isBlank() && text.isBlank()) return

        // 4. Check if automatic logging is enabled in Settings
        if (!InMemoryDatabase.isNotificationLoggingEnabled) {
            android.util.Log.d("VibeFinanceNotification", "Notification logging is disabled in Settings.")
            return
        }

        // 5. Check if incoming notification matches user-selected intercept payment apps
        if (!InMemoryDatabase.isAppInterceptEnabled(packageName, title, text)) {
            android.util.Log.d("VibeFinanceNotification", "Notification from '$packageName' ('$title') is ignored (not selected in allowed apps).")
            return
        }

        // 6. Strict Payment Intent Guard: Filter out non-payment alerts (OTP, promos, marketing, login alerts)
        if (!isPaymentNotification(title, text)) {
            android.util.Log.d("VibeFinanceNotification", "Notification from '$packageName' ('$title') filtered out: not a payment transaction.")
            return
        }

        android.util.Log.d("VibeFinanceNotification", "Valid payment notification from: $packageName, title: '$title', text: '$text'")

        // Push to sliding window batch buffer for on-device Gemini Nano synthesis & deduplication
        val rawItem = com.example.vibefinance.ai.RawNotificationItem(
            packageName = packageName,
            title = title,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        notificationBuffer.push(rawItem)
    }

    data class ParsedPayment(
        val amount: Double,
        val merchant: String,
        val assetName: String
    )

    companion object {
        /**
         * Privacy-First Payment Intent Guard:
         * Rejects OTPs, login notices, promotional banners, and non-transactional messages.
         */
        fun isPaymentNotification(title: String, text: String): Boolean {
            val combined = "$title $text".lowercase(Locale.US)

            // Negative Keywords: OTP, security codes, promo ads, login alerts, statement notices
            val negativeKeywords = listOf(
                "otp", "one-time password", "verification code", "驗證碼", "安全碼", "動態密碼",
                "security code", "activation code", "promo", "promotion", "discount", "coupon",
                "offer", "優惠", "推廣", "獎賞", "抽獎", "earn up to", "apply now", "upgrade now",
                "logged in", "login from", "登入", "登錄", "新登入", "password reset", "密碼重置",
                "statement is ready", "結單已備妥", "e-statement", "monthly statement", "download now",
                "update available", "version", "battery", "storage", "charging"
            )
            if (negativeKeywords.any { combined.contains(it) }) {
                return false
            }

            // Positive Payment Indicators:
            val positiveIndicators = listOf(
                "paid", "spent", "payment", "purchased", "charged", "transferred",
                "debited", "sent", "amount:", "transaction", "card payment", "you paid", "you spent",
                "已付款", "成功付款", "已扣款", "消費", "支出", "轉賬", "轉帳", "交易金額", "金額：", "支付成功",
                "扣款成功", "付款：", "已完成付款", "smart octopus", "octopus", "八達通", "payme", "fps", "轉數快",
                "alipay", "支付寶", "wechat pay", "微信支付"
            )
            return positiveIndicators.any { combined.contains(it) }
        }

        /**
         * On-device privacy-first parser for payment notifications.
         * Pure deterministic parsing without cloud inference, ensuring user privacy.
         */
        fun parseNotification(title: String, text: String, packageName: String): ParsedPayment? {
            if (title.isBlank() && text.isBlank()) return null

            val lowerPkg = packageName.lowercase(Locale.US)
            val lowerTitle = title.lowercase(Locale.US)
            val lowerText = text.lowercase(Locale.US)

            val isShell = lowerPkg.contains("shell")
            val isGoogle = lowerPkg.contains("walletnfcrel") || lowerPkg.contains("nbu.paisa") || lowerTitle.contains("google wallet") || lowerTitle.contains("google pay") ||
                    (isShell && (text.contains("金額：") || text.contains("Amount:") || lowerText.contains("spent")))
            val isSamsung = lowerPkg.contains("spay") || lowerTitle.contains("samsung wallet") || lowerTitle.contains("samsung pay") ||
                    (isShell && !isGoogle && (text.contains("octopus") || title.contains("octopus")))

            // 1. Google Wallet / Google Pay notifications
            if (isGoogle) {
                val amountRegex = Regex("(?:金額：|Amount:|Spent|Paid)\\s*(?:[A-Z]{3}|[A-Z]{2})?\\s*\\$?([0-9.,]+)", RegexOption.IGNORE_CASE)
                val cardRegex = Regex("(?:付款卡：|Card:|Via)\\s*(.+)", RegexOption.IGNORE_CASE)

                val amountMatch = amountRegex.find(text) ?: amountRegex.find(title)
                val cardMatch = cardRegex.find(text)

                val amount = amountMatch?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()
                if (amount != null && amount > 0.0) {
                    val assetName = cardMatch?.groupValues?.get(1)?.trim() ?: "Google Pay"
                    val merchant = if (title.isNotBlank() && !title.contains("Google", ignoreCase = true)) title.trim() else "Google Pay Merchant"
                    return ParsedPayment(amount, merchant, assetName)
                }
            }

            // 2. Samsung Wallet / Smart Octopus
            if (isSamsung) {
                // Case A: Title is "Smart Octopus HK$7.70 Citybus"
                val smartOctopusRegex = Regex("(.+?)\\s+(?:[A-Z]{2,3})?\\s*\\$?([0-9.,]+)\\s+(.+)")
                val matchA = smartOctopusRegex.matchEntire(title)
                if (matchA != null) {
                    val assetName = matchA.groupValues[1].trim()
                    val amount = matchA.groupValues[2].replace(",", "").toDoubleOrNull()
                    val merchant = matchA.groupValues[3].trim()
                    if (amount != null && amount > 0.0) {
                        return ParsedPayment(amount, merchant, assetName)
                    }
                }

                // Case B: Text ends with amount, e.g. "OCL* OCTOPUS AD1741037 HK$300.00"
                val amountRegex = Regex("(?:[A-Z]{2,3})?\\s*\\$?([0-9.,]+)$")
                val amountMatch = amountRegex.find(text)
                if (amountMatch != null) {
                    val amount = amountMatch.groupValues[1].replace(",", "").toDoubleOrNull()
                    if (amount != null && amount > 0.0) {
                        val merchant = text.substring(0, amountMatch.range.first).trim().trimEnd { it == '$' || it == 'K' || it == 'H' || it == ' ' }.trim()
                        val assetName = if (title.isNotBlank()) title.trim() else "Samsung Wallet"
                        return ParsedPayment(amount, if (merchant.isBlank()) "Samsung Wallet Merchant" else merchant, assetName)
                    }
                }
            }

            // 3. General Payment Verification: Must pass payment intent check
            if (!isPaymentNotification(title, text)) {
                return null
            }

            // 4. Digital Payment Apps & Bank Alerts (PayMe, FPS, Alipay, WeChat, HSBC, Citi, Chase, Bank of China, etc.)
            // General Amount Extractor: Matches $100.00, HK$45.50, USD 29.99, 125.00 HKD, etc.
            val generalAmountRegex = Regex("(?:HK\\$|USD|EUR|GBP|RMB|NT\\$|\\$|¥|€|£)\\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?)|([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?)\\s*(?:HKD|USD|EUR|GBP|RMB)")
            val match = generalAmountRegex.find(text) ?: generalAmountRegex.find(title)
            
            if (match != null) {
                val rawAmountStr = (match.groupValues[1].ifEmpty { match.groupValues[2] }).replace(",", "")
                val amount = rawAmountStr.toDoubleOrNull()

                if (amount != null && amount > 0.0) {
                    // Extract Card/Bank or App Asset Name
                    val combinedAll = "$title $text".lowercase(Locale.US)
                    val assetName = when {
                        combinedAll.contains("payme") -> "PayMe"
                        combinedAll.contains("fps") || combinedAll.contains("轉數快") -> "FPS"
                        combinedAll.contains("alipay") || combinedAll.contains("淘寶") || combinedAll.contains("支付寶") -> "Alipay"
                        combinedAll.contains("wechat") || combinedAll.contains("微信支付") -> "WeChat Pay"
                        combinedAll.contains("hsbc") -> "HSBC"
                        combinedAll.contains("citi") -> "Citi"
                        combinedAll.contains("chase") -> "Chase"
                        combinedAll.contains("boc") || combinedAll.contains("bank of china") || combinedAll.contains("中銀") -> "Bank of China"
                        combinedAll.contains("hang seng") || combinedAll.contains("恒生") -> "Hang Seng Bank"
                        combinedAll.contains("mox") -> "Mox Bank"
                        combinedAll.contains("octopus") || combinedAll.contains("八達通") -> "Octopus"
                        title.isNotBlank() -> title.trim()
                        else -> "Wallet (Cash)"
                    }

                    // Extract Merchant Name cleanly
                    val cleanText = text.replace(match.value, "")
                        .replace(Regex("(?:Paid|Spent|Purchase|Payment of|Transaction at|Transferred|Sent|Amount:|金額：|付款|成功付款|已扣款|消費|支出|轉賬|轉帳)\\s*(?:to|at|於|至)?\\s*", RegexOption.IGNORE_CASE), "")
                        .trim()
                    val merchant = if (cleanText.isNotBlank()) cleanText.take(40).trim() else if (title.isNotBlank()) title.trim() else "Auto Payment"

                    return ParsedPayment(amount, merchant, assetName)
                }
            }

            return null
        }

        /**
         * Category Classifier using On-Device Keyword Matching.
         * Privacy-first classification with zero server connection.
         */
        fun determineCategory(merchant: String, titleText: String = ""): String {
            val combined = "$merchant $titleText".lowercase(Locale.US)
            return when {
                combined.contains("sushiro") || combined.contains("壽司郎") ||
                combined.contains("mcdonald") || combined.contains("麥當勞") ||
                combined.contains("starbucks") || combined.contains("星巴克") ||
                combined.contains("cafe") || combined.contains("food") || combined.contains("restaurant") ||
                combined.contains("eatery") || combined.contains("bbmsl") || combined.contains("coffee") ||
                combined.contains("bakery") || combined.contains("tea") || combined.contains("drink") ||
                combined.contains("飲食") || combined.contains("餐廳") || combined.contains("堂食") || combined.contains("外賣") -> "Food & Drink"
                
                combined.contains("citybus") || combined.contains("城巴") ||
                combined.contains("mtr") || combined.contains("港鐵") ||
                combined.contains("bus") || combined.contains("九巴") || combined.contains("kmb") ||
                combined.contains("taxi") || combined.contains("的士") || combined.contains("uber") ||
                combined.contains("transport") || combined.contains("moto") || combined.contains("ferry") ||
                combined.contains("交通") || combined.contains("車費") || combined.contains("地鐵") -> "Transport"
                
                combined.contains("parknshop") || combined.contains("百佳") ||
                combined.contains("wellcome") || combined.contains("惠康") ||
                combined.contains("hktvmall") || combined.contains("market") ||
                combined.contains("supermarket") || combined.contains("grocery") ||
                combined.contains("groceries") || combined.contains("target") || combined.contains("walmart") ||
                combined.contains("costco") || combined.contains("超市") || combined.contains("街市") -> "Groceries"
                
                combined.contains("clp") || combined.contains("中電") ||
                combined.contains("towngas") || combined.contains("煤氣") ||
                combined.contains("water") || combined.contains("水費") ||
                combined.contains("utility") || combined.contains("phone") || combined.contains("bill") ||
                combined.contains("telecom") || combined.contains("power") || combined.contains("電訊") -> "Utilities"

                combined.contains("netflix") || combined.contains("spotify") || combined.contains("hulu") ||
                combined.contains("disney") || combined.contains("cinema") || combined.contains("movie") ||
                combined.contains("steam") || combined.contains("nintendo") || combined.contains("playstation") ||
                combined.contains("戲院") || combined.contains("電影") || combined.contains("娛樂") -> "Entertainment"

                combined.contains("apple") || combined.contains("sony") || combined.contains("best buy") ||
                combined.contains("electronics") || combined.contains("computer") || combined.contains("bose") ||
                combined.contains("gadgets") || combined.contains("samsung") || combined.contains("豐澤") ||
                combined.contains("百老滙") -> "Electronics"
                
                else -> "Other"
            }
        }

        /**
         * Helper method for recording transactions (callable by test or listener)
         */
        suspend fun recordTransaction(context: Context, amount: Double, merchant: String, assetName: String) {
            val accounts = InMemoryDatabase.accounts.value
            
            // Find or match card/account
            val matchedAccount = accounts.find { account ->
                account.name.equals(assetName, ignoreCase = true) ||
                assetName.contains(account.name, ignoreCase = true) ||
                account.name.contains(assetName, ignoreCase = true)
            }

            val accountId = if (matchedAccount != null) {
                matchedAccount.id
            } else {
                // Create a new account matching the dynamic card
                val lowerAsset = assetName.lowercase(Locale.US)
                val type = when {
                    lowerAsset.contains("card") || lowerAsset.contains("mastercard") || 
                    lowerAsset.contains("visa") || lowerAsset.contains("credit") || 
                    lowerAsset.contains("amex") || lowerAsset.contains("aeon") || 
                    lowerAsset.contains("mox") || lowerAsset.contains("hsbc") -> AccountType.CC
                    
                    lowerAsset.contains("cash") || lowerAsset.contains("octopus") -> AccountType.CASH
                    else -> AccountType.BANK
                }
                val newAccount = AccountEntity(
                    name = assetName,
                    type = type,
                    balance = 0.0,
                    icon = when (type) {
                        AccountType.CC -> "credit_card"
                        AccountType.CASH -> "wallet"
                        else -> "bank"
                    },
                    creditLimit = if (type == AccountType.CC) 10000.0 else 0.0
                )
                AccountRepository().insertAccount(newAccount)
            }

            val category = determineCategory(merchant)
            val tx = TransactionEntity(
                amount = amount,
                category = category,
                timestamp = System.currentTimeMillis(),
                accountId = accountId,
                description = merchant
            )

            TransactionRepository().insertTransaction(tx)

            // Show a confirmation system notification
            showLoggedNotification(context, amount, merchant, assetName)
        }

        /**
         * Helper to process notifications for automated tests, ADB shell, or Waydroid testing.
         */
        suspend fun processNotificationForTest(context: Context, title: String, text: String, packageName: String): Boolean {
            val parsed = parseNotification(title, text, packageName) ?: return false
            recordTransaction(context, parsed.amount, parsed.merchant, parsed.assetName)
            return true
        }

        suspend fun processBatchForTest(context: Context, items: List<com.example.vibefinance.ai.RawNotificationItem>): List<ParsedPayment> {
            val synthesizer = com.example.vibefinance.ai.GeminiNanoSynthesizer(context)
            val consolidated = synthesizer.synthesizeBatch(items)
            for (p in consolidated) {
                recordTransaction(context, p.amount, p.merchant, p.assetName)
            }
            return consolidated
        }

        private fun showLoggedNotification(context: Context, amount: Double, merchant: String, assetName: String) {
            val channelId = "payment_logging"
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Automatic Logging",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                manager.createNotificationChannel(channel)
            }

            val intent = Intent(context, com.example.vibefinance.MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("navigate_tab", "history")
            }
            val pendingIntent = android.app.PendingIntent.getActivity(
                context,
                (merchant + amount).hashCode(),
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            val notification = Notification.Builder(context, channelId)
                .setContentTitle("Automatically Logged Expense")
                .setContentText("Logged $${String.format("%.2f", amount)} at $merchant to $assetName")
                .setSmallIcon(android.R.drawable.ic_menu_save)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            manager.notify(System.currentTimeMillis().toInt(), notification)
        }
    }
}
