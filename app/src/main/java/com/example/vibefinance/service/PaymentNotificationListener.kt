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
import com.example.vibefinance.R
import com.example.vibefinance.util.appString
import java.util.Locale

/**
 * Privacy-First On-Device Notification Reader & Payment Transformer.
 * Processes payment alerts, digital wallet notifications (Google Wallet, Samsung Pay, Smart Octopus),
 * bank push notifications, and payment SMS locally on-device with zero network/cloud calls.
 */
class PaymentNotificationListener : NotificationListenerService() {

    override fun onCreate() {
        super.onCreate()
        // A notification listener can start before the Activity in a fresh process.
        InMemoryDatabase.initialize(this)
        PendingPaymentStore.initialize(this)
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
        val summaryText = (extras.getCharSequence(Notification.EXTRA_TEXT) ?: "").toString()
        val expandedText = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT) ?: "").toString()
        // Some bank alerts put the complete merchant and amount only in the expanded text.
        val text = expandedText.takeIf { it.isNotBlank() && it.length > summaryText.length }
            ?: summaryText

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
        if (!isPaymentNotification(title, text, packageName)) {
            android.util.Log.d("VibeFinanceNotification", "Notification from '$packageName' ('$title') filtered out: not a payment transaction.")
            return
        }

        android.util.Log.d("VibeFinanceNotification", "Valid payment notification from: $packageName, title: '$title', text: '$text'")

        // Store the candidate before returning from the callback. A process death during a
        // delayed batch window must not erase a detected payment.
        val parsed = parseNotification(title, text, packageName) ?: return
        queuePayment(
            this,
            parsed.copy(
                sourcePackage = packageName,
                detectedAt = sbn.postTime.takeIf { it > 0L } ?: System.currentTimeMillis(),
                notificationKey = sbn.key.orEmpty(),
                rawTitle = if (parsed.rawTitle.isNotBlank()) parsed.rawTitle else title,
                rawText = if (parsed.rawText.isNotBlank()) parsed.rawText else text
            )
        )
    }

    data class ParsedPayment(
        val amount: Double,
        val merchant: String,
        val assetName: String,
        val sourcePackage: String = "",
        val detectedAt: Long = System.currentTimeMillis(),
        val notificationKey: String = "",
        val cardLast4: String? = null,
        val balanceRemaining: Double? = null,
        val isTopUp: Boolean = false,
        val rawTitle: String = "",
        val rawText: String = "",
        val transactionType: String = "EXPENSE"
    )

    companion object {
        private val nonExpenseEnglish = Regex(
            "\\b(?:refund(?:ed|s)?|revers(?:ed|al)|chargeback|cashback|rebate|deposit(?:ed|s)?|payroll|salary|dividend|credited|incoming|receiv(?:e|ed|ing)|top[- ]?up|recharge|repayment)\\b",
            RegexOption.IGNORE_CASE
        )
        private val nonExpenseChinese = listOf(
            "退款", "退回", "退貨", "退货", "退刷", "撤銷", "撤销", "沖正", "冲正",
            "返現", "返现", "回贈", "回赠", "已收款", "收款成功", "收到款",
            "您收到", "轉入", "转入", "存入", "存款", "入金", "增值", "充值",
            "儲值", "储值", "還款", "还款", "薪金", "工資", "工资", "股息",
            "付款失敗", "付款失败", "交易失敗", "交易失败", "交易已取消",
            "待付款", "待支付", "付款請求", "付款请求", "付款提醒", "繳費提醒", "缴费提醒"
        )
        private val balanceText = Regex(
            "\\bbalance\\b|\\bremaining value\\b|\\bavailable funds\\b|餘額|余额|結餘|结余",
            RegexOption.IGNORE_CASE
        )
        private val outgoingEnglish = Regex(
            "\\b(?:paid|spent|payment|purchased|charged|transferred|debited|sent)\\b",
            RegexOption.IGNORE_CASE
        )
        private val smartOctopusPurchase = Regex(
            "(?i)(?:^|\\b)smart octopus\\s+(?:HKD|HK\\$|\\$)?\\s*[0-9]+(?:\\.[0-9]{1,2})?\\s+\\S+"
        )
        private val smartOctopusFullPattern = Regex(
            "(?i)(?:^|\\b)smart octopus\\s+(?:HKD|HK\\$|\\$)?\\s*([0-9]+(?:\\.[0-9]{1,2})?)\\s+(.+?)(?:\\s+(?:餘額|余额|結餘|结余|remaining\\s+value|balance)[:：]?\\s*(-)?\\s*(?:HKD|HK\\$|\\$)?\\s*(-)?\\s*([0-9]+(?:\\.[0-9]{1,2})?))?$",
            RegexOption.IGNORE_CASE
        )
        private val androidOctopusTitlePattern = Regex(
            "(?i)(?:android\\s*(?:版\\s*)?[八8][達逹]通|android\\s*octopus|octopus\\s+on\\s+android)"
        )
        private val androidOctopusZhPattern = Regex(
            "(?i)(?:(?:Android版)?八達通|Octopus)?[:：]?\\s*在\\s+(.+?)\\s+支付\\s*(?:HKD|HK\\$|\\$)?\\s*([0-9]+(?:\\.[0-9]{1,2})?)(?:[。;；\\s]+(?:餘額|余额|結餘|结余|remaining\\s+value|balance)[:：]?\\s*(-)?\\s*(?:HKD|HK\\$|\\$)?\\s*(-)?\\s*([0-9]+(?:\\.[0-9]{1,2})?))?",
            RegexOption.IGNORE_CASE
        )
        private val androidOctopusEnPattern = Regex(
            "(?i)(?:Octopus|八達通)?[:：]?\\s*(?:paid|spent)\\s+(?:HKD|HK\\$|\\$)?\\s*([0-9]+(?:\\.[0-9]{1,2})?)\\s+(?:at|to)\\s+(.+?)(?:[.;\\s]+(?:balance|remaining\\s+value|餘額|余额)[:：]?\\s*(-)?\\s*(?:HKD|HK\\$|\\$)?\\s*(-)?\\s*([0-9]+(?:\\.[0-9]{1,2})?))?$",
            RegexOption.IGNORE_CASE
        )
        private val balancePattern = Regex(
            "(?i)(?:餘額|余额|結餘|结余|remaining\\s+value|balance)[:：]?\\s*(-)?\\s*(?:HKD|HK\\$|\\$)?\\s*(-)?\\s*([0-9]+(?:\\.[0-9]{1,2})?)",
            RegexOption.IGNORE_CASE
        )
        private val smartOctopusTitle = Regex(
            "(?i)^smart octopus\\s+(?:HKD|HK\\$|\\$)?\\s*([0-9]+(?:\\.[0-9]{1,2})?)\\s+(.+)$"
        )
        private val octopusMerchantPayment = Regex(
            "(?i)^OCL\\*\\s*OCTOPUS\\s+\\S+\\s+(?:HKD|HK\\$|\\$)?\\s*[0-9]+(?:\\.[0-9]{1,2})?$"
        )
        private val walletCardAmount = Regex("(?i)\\bamount:\\s*(?:HKD|HK\\$|\\$)?\\s*[0-9]+(?:\\.[0-9]{1,2})?")
        private val walletCardLabel = Regex("(?i)\\b(?:card|via):")
        private val localCurrencyAmount = Regex(
            "(?:HKD|HK\\$|\\$)\\s*([0-9]+(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?)|([0-9]+(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?)\\s*(?:HKD|HK\\$)",
            RegexOption.IGNORE_CASE
        )
        private val bocGoUnionPayTitle = Regex(
            "(?i)\\b(?:BOC\\s+Go|中銀\\s*Go)\\b(?:.*\\bUnion\\s*Pay\\b)?"
        )
        private val transitTicketLabel = Regex("\\btransit[-\\s]+ticket\\b", RegexOption.IGNORE_CASE)
        val octopusTopUpBankTransfer = Regex(
            "(?i)(?:你已成功)?由銀行戶口轉[賬帳]\\s*(?:HKD|HK\\$|\\$)?\\s*([0-9]+(?:\\.[0-9]{1,2})?)\\s*至八達通(?:\\s*[*•●xX-]{1,8}(\\d{4}))?"
        )
        val octopusTopUpGeneral = Regex(
            "(?i)(?:已成功|成功|已)?(?:自動)?(?:增值|充值|儲值)\\s*(?:八達通)?(?:\\s*[*•●xX-]{1,8}(\\d{4}))?\\s*(?:HKD|HK\\$|\\$)?\\s*([0-9]+(?:\\.[0-9]{1,2})?)"
        )
        val octopusTopUpEnglish = Regex(
            "(?i)(?:successfully\\s+)?(?:transferred|topped[- ]?up)\\s*(?:HKD|HK\\$|\\$)?\\s*([0-9]+(?:\\.[0-9]{1,2})?)\\s*(?:from\\s+bank\\s+account\\s+)?to\\s+octopus(?:\\s*[*•●xX-]{1,8}(\\d{4}))?"
        )

        fun isOctopusTopUp(title: String, text: String): Boolean {
            val combined = "$title $text"
            return octopusTopUpBankTransfer.containsMatchIn(combined) ||
                octopusTopUpGeneral.containsMatchIn(combined) ||
                octopusTopUpEnglish.containsMatchIn(combined) ||
                (combined.contains("八達通") && (combined.contains("由銀行戶口轉賬") || combined.contains("由銀行戶口轉帳")))
        }

        /** Bank card transit alerts omit an outgoing verb but identify a card, merchant, and spend. */
        private fun isBocGoUnionPayTransitPurchase(title: String, text: String): Boolean {
            if (!bocGoUnionPayTitle.containsMatchIn(title) || balanceText.containsMatchIn(text)) return false
            val transitLabel = transitTicketLabel.find(text) ?: return false
            val amount = localCurrencyAmount.findAll(text).singleOrNull() ?: return false
            if (localCurrencyAmount.containsMatchIn(title) || amount.range.first <= transitLabel.range.last) return false
            val merchant = text.substring(transitLabel.range.last + 1, amount.range.first).trim()
            return merchant.any(Char::isLetter)
        }

        /**
         * Privacy-First Payment Intent Guard:
         * Rejects non-expense bank alerts as well as OTPs, login notices, and promotions.
         */
        fun isPaymentNotification(title: String, text: String, packageName: String = ""): Boolean {
            if (packageName.isNotBlank() && InMemoryDatabase.findMatchingTemplate(packageName, "$title $text".trim()) != null) {
                return true
            }
            if (isOctopusTopUp(title, text)) {
                return true
            }

            val combined = "$title $text".lowercase(Locale.US)

            // A banking app may mention "payment" in a refund, deposit, or incoming
            // transfer. Never treat those as a spend, even when an amount is present.
            if (nonExpenseEnglish.containsMatchIn(combined) ||
                nonExpenseChinese.any { combined.contains(it) }
            ) return false

            // Negative Keywords: OTP, security codes, promo ads, login alerts, statement notices
            val negativeKeywords = listOf(
                "otp", "one-time password", "verification code", "驗證碼", "安全碼", "動態密碼",
                "security code", "activation code", "promo", "promotion", "discount", "coupon",
                "offer", "優惠", "推廣", "獎賞", "抽獎", "earn up to", "apply now", "upgrade now",
                "logged in", "login from", "登入", "登錄", "新登入", "password reset", "密碼重置",
                "statement is ready", "結單已備妥", "e-statement", "monthly statement", "download now",
                "update available", "version", "battery", "storage", "charging",
                "payment due", "bill due", "payment reminder", "payment request", "request for payment",
                "payment pending", "pending payment", "payment failed", "failed payment",
                "transaction declined", "declined transaction", "transaction failed", "failed transaction",
                "purchase declined", "payment cancelled", "payment canceled", "scheduled payment",
                "upcoming payment", "pre-authorisation", "pre-authorization"
            )
            if (negativeKeywords.any { combined.contains(it) }) {
                return false
            }

            // High-confidence Transaction Grammar Engine match for active expense payments
            val grammarResult = TransactionGrammarEngine.parse(title, text, packageName)
            if (grammarResult != null && grammarResult.confidence >= 0.9f && grammarResult.amount > 0.0 && grammarResult.intent == com.example.vibefinance.data.entity.ParsedTransactionType.EXPENSE) {
                return true
            }

            // Positive Payment Indicators:
            val positiveIndicators = listOf(
                "已付款", "成功付款", "已扣款", "消費", "支出",
                "支付成功", "扣款成功", "付款：", "已完成付款", "付款卡：", "支付"
            )
            // Reject non-payment / promotional alerts from Octopus App where title is just "八達通", "八達通銀包", "Octopus", etc.
            val isIrrelevantOctopusTitle = (title.contains("八達通") || title.contains("octopus", ignoreCase = true)) &&
                !androidOctopusTitlePattern.containsMatchIn(title) &&
                !smartOctopusPurchase.containsMatchIn(title) &&
                !smartOctopusPurchase.containsMatchIn(text)
            if (isIrrelevantOctopusTitle) {
                return false
            }

            val isAndroidOctopusPayment = androidOctopusTitlePattern.containsMatchIn(title) &&
                (androidOctopusZhPattern.containsMatchIn(text) || androidOctopusEnPattern.containsMatchIn(text) ||
                 androidOctopusZhPattern.containsMatchIn(title) || androidOctopusEnPattern.containsMatchIn(title))
            val isSmartOctopusPayment = smartOctopusPurchase.containsMatchIn(title) || smartOctopusPurchase.containsMatchIn(text)
            val isOctopusPayment = isAndroidOctopusPayment || isSmartOctopusPayment

            return outgoingEnglish.containsMatchIn(combined) ||
                positiveIndicators.any { combined.contains(it) } ||
                isOctopusPayment ||
                octopusMerchantPayment.matches(text.trim()) ||
                (walletCardAmount.containsMatchIn(text) && walletCardLabel.containsMatchIn(text)) ||
                isBocGoUnionPayTransitPurchase(title, text)
        }

        /**
         * Privacy-First On-Device Card Last-4 Digit Extractor.
         * Pure deterministic regex parsing from real notification text or title.
         * Under NO circumstances generates random digits; returns null if not found.
         */
        fun extractCardLast4(text: String, title: String = ""): String? {
            val combined = "$title $text"

            // 1. Explicit indicators: "ending in 1234", "尾號1234", "末4位1234", "後4位1234", "卡號末四位1234"
            val explicitPattern = Regex(
                "(?i)(?:ending\\s+(?:in|with)?|尾號[為是：:]?|末[四4]位[為是：:]?|後[四4]位[為是：:]?|卡號[為是：:]?|卡號末[四4]位[為是：:]?)\\s*([0-9]{4})(?!\\d)"
            )
            explicitPattern.find(combined)?.groupValues?.get(1)?.let { return it }

            // 2. Masked card pattern: ••1234, **** 1234, *****1234, x-1234, ...1234, -1234
            val maskedPattern = Regex(
                "(?:[•●*xX]{1,8}|-)\\s*([0-9]{4})(?!\\d)"
            )
            maskedPattern.find(combined)?.groupValues?.get(1)?.let { return it }

            // 3. Card type indicator followed by 4 digits: "Visa 1234", "Mastercard 5678", "信用卡 9999", "Amex 4321"
            val cardTypePattern = Regex(
                "(?i)(?:visa|mastercard|master|amex|credit card|debit card|信用卡|扣賬卡|簽帳卡)\\s*(?:no\\.?|#)?\\s*(?:[•●*xX-]{1,4}\\s*)?([0-9]{4})(?!\\d)"
            )
            cardTypePattern.find(combined)?.groupValues?.get(1)?.let { return it }

            // 4. Card in parentheses: "Visa (1234)", "信用卡(1234)"
            val parenthesesPattern = Regex(
                "(?i)(?:card|visa|mastercard|master|amex|信用卡|扣賬卡)\\s*\\(([0-9]{4})\\)"
            )
            parenthesesPattern.find(combined)?.groupValues?.get(1)?.let { return it }

            return null
        }

        private fun resolveAssetName(
            title: String,
            text: String,
            detectedCardLast4: String?,
            isBocGoUnionPayTransit: Boolean = false,
            fallbackHint: String = "Wallet (Cash)"
        ): String {
            val combinedAll = "$title $text".lowercase(Locale.US)
            val cardHint = Regex(
                "(?i)(?:visa|mastercard|amex|card|信用卡|扣賬卡)\\s*(?:[•*xX-]{1,4}\\s*)?\\d{4}"
            ).find("$title $text")?.value?.trim()
            return when {
                isBocGoUnionPayTransit -> title.trim()
                cardHint != null -> cardHint
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
                combinedAll.contains("octopus") || combinedAll.contains("八達通") -> "八達通"
                detectedCardLast4 != null -> "Card ••$detectedCardLast4"
                title.isNotBlank() -> title.trim()
                else -> fallbackHint
            }
        }

        /**
         * On-device privacy-first parser for payment notifications.
         * Pure deterministic parsing without cloud inference, ensuring user privacy.
         */
        fun parseNotification(title: String, text: String, packageName: String): ParsedPayment? {
            if (title.isBlank() && text.isBlank()) return null

            // 1. User's custom visual templates (Highest priority)
            val customMatch = InMemoryDatabase.findMatchingTemplate(packageName, "$title $text".trim())
            if (customMatch != null) {
                val (tmpl, res) = customMatch
                return ParsedPayment(
                    amount = res.amount,
                    merchant = res.merchant,
                    assetName = tmpl.name,
                    cardLast4 = res.cardLast4 ?: extractCardLast4(text, title),
                    rawTitle = title,
                    rawText = text,
                    transactionType = res.transactionType.name
                )
            }

            if (!isPaymentNotification(title, text, packageName)) return null

            // Account balances use HKD. Do not silently post a foreign-currency amount as HKD.
            val foreignCurrency = Regex(
                "(?i)(?<![A-Z])(?:USD|EUR|GBP|RMB|CNY|TWD|NTD)(?![A-Z])|NT\\$|[€£¥]"
            )
            if (foreignCurrency.containsMatchIn("$title $text")) return null

            // Deterministic card last-4 extraction from real notification text
            val detectedCardLast4 = extractCardLast4(text, title)

            // 0. Octopus top-up parsing (e.g. "你已成功由銀行戶口轉賬 HKD 300.0 至八達通 *****1719。")
            if (isOctopusTopUp(title, text)) {
                var amount: Double? = null
                var topUpCardLast4: String? = detectedCardLast4

                val m1 = octopusTopUpBankTransfer.find("$title $text")
                if (m1 != null) {
                    amount = m1.groupValues[1].replace(",", "").toDoubleOrNull()
                    if (topUpCardLast4 == null && m1.groupValues.size > 2 && m1.groupValues[2].isNotBlank()) {
                        topUpCardLast4 = m1.groupValues[2]
                    }
                }
                if (amount == null) {
                    val m2 = octopusTopUpGeneral.find("$title $text")
                    if (m2 != null) {
                        if (topUpCardLast4 == null && m2.groupValues.size > 1 && m2.groupValues[1].isNotBlank()) {
                            topUpCardLast4 = m2.groupValues[1]
                        }
                        amount = m2.groupValues.lastOrNull()?.replace(",", "")?.toDoubleOrNull()
                    }
                }
                if (amount == null) {
                    val m3 = octopusTopUpEnglish.find("$title $text")
                    if (m3 != null) {
                        amount = m3.groupValues[1].replace(",", "").toDoubleOrNull()
                        if (topUpCardLast4 == null && m3.groupValues.size > 2 && m3.groupValues[2].isNotBlank()) {
                            topUpCardLast4 = m3.groupValues[2]
                        }
                    }
                }
                if (amount == null) {
                    val m4 = localCurrencyAmount.find(text) ?: localCurrencyAmount.find(title)
                    if (m4 != null) {
                        amount = (m4.groupValues[1].ifEmpty { m4.groupValues[2] }).replace(",", "").toDoubleOrNull()
                    }
                }

                if (amount != null && amount > 0.0) {
                    val merchant = if (text.contains("增值") || title.contains("增值")) {
                        "八達通增值"
                    } else if (text.contains("轉賬") || text.contains("轉帳")) {
                        "銀行戶口轉賬至八達通"
                    } else {
                        "八達通增值"
                    }
                    return ParsedPayment(
                        amount = amount,
                        merchant = merchant,
                        assetName = "八達通",
                        cardLast4 = topUpCardLast4 ?: detectedCardLast4,
                        isTopUp = true
                    )
                }
            }

            val combined = "$title $text".lowercase(Locale.US)
            val isOctopusAppPackage = packageName.contains("com.octopuscards", ignoreCase = true) ||
                packageName.contains("com.octopus.wallet", ignoreCase = true) ||
                packageName.equals("octopus", ignoreCase = true)
            val isAndroidOctopusTitle = androidOctopusTitlePattern.containsMatchIn(title)
            val isSmartOctopus = smartOctopusPurchase.containsMatchIn(title) || smartOctopusPurchase.containsMatchIn(text)

            // If the notification originates from the official Octopus App, strictly require that
            // the title identifies "Android版八達通" (or Smart Octopus for transit). Any general or
            // promotional notification with titles like "八達通", "八達通銀包", "Octopus", or promotional headlines
            // must NOT be recorded as an expense.
            if (isOctopusAppPackage && !isAndroidOctopusTitle && !isSmartOctopus) {
                return null
            }

            // 1. Android Octopus parsing (e.g. Android版八達通):
            // Only parse as Android Octopus if title explicitly matches the Android Octopus title
            // (e.g. "Android版八達通", "Android 八達通", "Android版八逹通", "Octopus on Android", "Android Octopus").
            if (isAndroidOctopusTitle) {
                for (candidate in listOf(text, title)) {
                    val zhMatch = androidOctopusZhPattern.find(candidate.trim())
                    if (zhMatch != null) {
                        val merchant = zhMatch.groupValues[1].trim()
                        val amount = zhMatch.groupValues[2].toDoubleOrNull()
                        val neg1 = zhMatch.groupValues.getOrNull(3)
                        val neg2 = zhMatch.groupValues.getOrNull(4)
                        val balStr = zhMatch.groupValues.getOrNull(5)?.takeIf { it.isNotBlank() }
                        var balanceRemaining: Double? = if (balStr != null) {
                            val num = balStr.toDoubleOrNull()
                            if (num != null) {
                                if (neg1 == "-" || neg2 == "-") -num else num
                            } else null
                        } else null

                        if (balanceRemaining == null) {
                            val other = if (candidate == text) title else text
                            val balMatch = balancePattern.find(other)
                            if (balMatch != null) {
                                val bNeg1 = balMatch.groupValues.getOrNull(1)
                                val bNeg2 = balMatch.groupValues.getOrNull(2)
                                val bStr = balMatch.groupValues.getOrNull(3)?.takeIf { it.isNotBlank() }
                                val num = bStr?.toDoubleOrNull()
                                if (num != null) {
                                    balanceRemaining = if (bNeg1 == "-" || bNeg2 == "-") -num else num
                                }
                            }
                        }

                        if (amount != null && amount > 0.0 && merchant.isNotBlank()) {
                            return ParsedPayment(
                                amount = amount,
                                merchant = merchant,
                                assetName = "八達通",
                                cardLast4 = detectedCardLast4,
                                balanceRemaining = balanceRemaining
                            )
                        }
                    }

                    val enMatch = androidOctopusEnPattern.find(candidate.trim())
                    if (enMatch != null) {
                        val amount = enMatch.groupValues[1].toDoubleOrNull()
                        val merchant = enMatch.groupValues[2].trim()
                        val neg1 = enMatch.groupValues.getOrNull(3)
                        val neg2 = enMatch.groupValues.getOrNull(4)
                        val balStr = enMatch.groupValues.getOrNull(5)?.takeIf { it.isNotBlank() }
                        var balanceRemaining: Double? = if (balStr != null) {
                            val num = balStr.toDoubleOrNull()
                            if (num != null) {
                                if (neg1 == "-" || neg2 == "-") -num else num
                            } else null
                        } else null

                        if (balanceRemaining == null) {
                            val other = if (candidate == text) title else text
                            val balMatch = balancePattern.find(other)
                            if (balMatch != null) {
                                val bNeg1 = balMatch.groupValues.getOrNull(1)
                                val bNeg2 = balMatch.groupValues.getOrNull(2)
                                val bStr = balMatch.groupValues.getOrNull(3)?.takeIf { it.isNotBlank() }
                                val num = bStr?.toDoubleOrNull()
                                if (num != null) {
                                    balanceRemaining = if (bNeg1 == "-" || bNeg2 == "-") -num else num
                                }
                            }
                        }

                        if (amount != null && amount > 0.0 && merchant.isNotBlank()) {
                            return ParsedPayment(
                                amount = amount,
                                merchant = merchant,
                                assetName = "八達通",
                                cardLast4 = detectedCardLast4,
                                balanceRemaining = balanceRemaining
                            )
                        }
                    }
                }
            }

            // 2. Smart Octopus parsing:
            // Supports Samsung Wallet, Octopus app, or Shell notifications where title or text has Smart Octopus.
            // Extracts spend amount, merchant name, and the remaining card balance (餘額 / remaining value).
            for (candidate in listOf(text, title)) {
                val match = smartOctopusFullPattern.find(candidate.trim())
                if (match != null) {
                    val amount = match.groupValues[1].toDoubleOrNull()
                    val merchant = match.groupValues[2].trim()
                    val neg1 = match.groupValues.getOrNull(3)
                    val neg2 = match.groupValues.getOrNull(4)
                    val balStr = match.groupValues.getOrNull(5)?.takeIf { it.isNotBlank() }
                    var balanceRemaining: Double? = if (balStr != null) {
                        val num = balStr.toDoubleOrNull()
                        if (num != null) {
                            if (neg1 == "-" || neg2 == "-") -num else num
                        } else null
                    } else null

                    if (balanceRemaining == null) {
                        val other = if (candidate == text) title else text
                        val balMatch = balancePattern.find(other)
                        if (balMatch != null) {
                            val bNeg1 = balMatch.groupValues.getOrNull(1)
                            val bNeg2 = balMatch.groupValues.getOrNull(2)
                            val bStr = balMatch.groupValues.getOrNull(3)?.takeIf { it.isNotBlank() }
                            val num = bStr?.toDoubleOrNull()
                            if (num != null) {
                                balanceRemaining = if (bNeg1 == "-" || bNeg2 == "-") -num else num
                            }
                        }
                    }
                    if (amount != null && amount > 0.0 && merchant.isNotBlank()) {
                        return ParsedPayment(
                            amount = amount,
                            merchant = merchant,
                            assetName = "Smart Octopus",
                            cardLast4 = detectedCardLast4,
                            balanceRemaining = balanceRemaining
                        )
                    }
                }
            }

            val isOctopusPayment = (isAndroidOctopusTitle && (
                androidOctopusZhPattern.containsMatchIn(text) ||
                androidOctopusZhPattern.containsMatchIn(title) ||
                androidOctopusEnPattern.containsMatchIn(text) ||
                androidOctopusEnPattern.containsMatchIn(title)
            )) || isSmartOctopus

            val spendIdentified = isOctopusPayment || smartOctopusPurchase.containsMatchIn(title) || smartOctopusPurchase.containsMatchIn(text)
            val amounts = (localCurrencyAmount.findAll(text) + localCurrencyAmount.findAll(title))
                .mapNotNull { match ->
                    (match.groupValues[1].ifEmpty { match.groupValues[2] })
                        .replace(",", "").toDoubleOrNull()
                }.distinct().toList()
            if (!spendIdentified && (
                    amounts.size > 1 || balanceText.containsMatchIn("$title $text")
                )
            ) return null

            val lowerPkg = packageName.lowercase(Locale.US)
            val lowerTitle = title.lowercase(Locale.US)
            val lowerText = text.lowercase(Locale.US)
            val isBocGoUnionPayTransit = isBocGoUnionPayTransitPurchase(title, text)

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
                    return ParsedPayment(amount, merchant, assetName, cardLast4 = detectedCardLast4)
                }
            }

            // 2. Samsung Wallet / Smart Octopus
            if (isSamsung) {
                // Text ends with amount, e.g. "OCL* OCTOPUS AD1741037 HK$300.00"
                val amountRegex = Regex("(?:[A-Z]{2,3})?\\s*\\$?([0-9.,]+)$")
                val amountMatch = amountRegex.find(text)
                if (amountMatch != null) {
                    val amount = amountMatch.groupValues[1].replace(",", "").toDoubleOrNull()
                    if (amount != null && amount > 0.0) {
                        val merchant = text.substring(0, amountMatch.range.first).trim().trimEnd { it == '$' || it == 'K' || it == 'H' || it == ' ' }.trim()
                        val isOctopus = text.contains("octopus", ignoreCase = true) ||
                            text.contains("八達通") ||
                            text.contains("八逹通") ||
                            title.contains("octopus", ignoreCase = true) ||
                            title.contains("八達通") ||
                            title.contains("八逹通") ||
                            merchant.contains("OCTOPUS", ignoreCase = true) ||
                            merchant.startsWith("OCL*", ignoreCase = true)
                        val assetName = when {
                            isOctopus -> "Smart Octopus"
                            title.isNotBlank() -> title.trim()
                            else -> "Samsung Wallet"
                        }
                        return ParsedPayment(amount, if (merchant.isBlank()) "Samsung Wallet Merchant" else merchant, assetName, cardLast4 = detectedCardLast4)
                    }
                }
            }

            // 2.5. Transaction Grammar Engine (Preposition Anchors & Grammar State Machine)
            val grammarResult = TransactionGrammarEngine.parse(title, text, packageName)
            if (grammarResult != null && grammarResult.amount > 0.0 && grammarResult.confidence >= 0.9f) {
                val assetName = resolveAssetName(
                    title = title,
                    text = text,
                    detectedCardLast4 = grammarResult.cardLast4 ?: detectedCardLast4,
                    isBocGoUnionPayTransit = isBocGoUnionPayTransit,
                    fallbackHint = grammarResult.assetHint
                )
                return ParsedPayment(
                    amount = grammarResult.amount,
                    merchant = grammarResult.merchant,
                    assetName = assetName,
                    cardLast4 = grammarResult.cardLast4 ?: detectedCardLast4,
                    rawTitle = title,
                    rawText = text,
                    transactionType = grammarResult.intent.name
                )
            }

            // 3. Digital Payment Apps & Bank Alerts (PayMe, FPS, Alipay, WeChat, banks).
            val match = localCurrencyAmount.find(text) ?: localCurrencyAmount.find(title)
            
            if (match != null) {
                val rawAmountStr = (match.groupValues[1].ifEmpty { match.groupValues[2] }).replace(",", "")
                val amount = rawAmountStr.toDoubleOrNull()

                if (amount != null && amount > 0.0) {
                    val assetName = resolveAssetName(
                        title = title,
                        text = text,
                        detectedCardLast4 = detectedCardLast4,
                        isBocGoUnionPayTransit = isBocGoUnionPayTransit
                    )

                    // Extract Merchant Name cleanly
                    val cleanText = text.replace(match.value, "")
                        .replace(Regex("(?:Paid|Spent|Purchase|Payment of|Transaction at|Transferred|Sent|Amount:|金額：|付款|成功付款|已扣款|消費|支出|轉賬|轉帳)\\s*(?:to|at|於|至)?\\s*", RegexOption.IGNORE_CASE), "")
                        .trim()
                    val merchant = if (isBocGoUnionPayTransit) {
                        // Keep the transport context while dropping the notification timestamp.
                        val label = transitTicketLabel.find(text)!!
                        text.substring(label.range.first, match.range.first).trim().take(80)
                    } else if (cleanText.isNotBlank()) {
                        cleanText.take(40).trim()
                    } else if (title.isNotBlank()) {
                        title.trim()
                    } else {
                        "Auto Payment"
                    }

                    return ParsedPayment(amount, merchant, assetName, cardLast4 = detectedCardLast4)
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
                combined.contains("增值") || combined.contains("充值") || combined.contains("儲值") ||
                combined.contains("轉賬至八達通") || combined.contains("轉帳至八達通") || combined.contains("至八達通") ||
                combined.contains("top-up") || combined.contains("top up") -> "Top-up"

                combined.contains("sushiro") || combined.contains("壽司郎") ||
                combined.contains("mcdonald") || combined.contains("麥當勞") ||
                combined.contains("starbucks") || combined.contains("星巴克") ||
                combined.contains("cafe") || combined.contains("food") || combined.contains("restaurant") ||
                combined.contains("eatery") || combined.contains("bbmsl") || combined.contains("coffee") ||
                combined.contains("bakery") || combined.contains("tea") || combined.contains("drink") ||
                combined.contains("飲食") || combined.contains("餐廳") || combined.contains("堂食") || combined.contains("外賣") ||
                combined.contains("餐飲") || combined.contains("會所") -> "Food & Drink"
                
                combined.contains("citybus") || combined.contains("城巴") ||
                combined.contains("transit-ticket") || combined.contains("transit ticket") ||
                combined.contains("kowloo") || combined.contains("kowloon") ||
                combined.contains("mtr") || combined.contains("港鐵") ||
                combined.contains("bus") || combined.contains("九巴") || combined.contains("kmb") ||
                combined.contains("龍運") || combined.contains("lwb") || combined.contains("新巴") ||
                combined.contains("taxi") || combined.contains("的士") || combined.contains("uber") ||
                combined.contains("transport") || combined.contains("moto") || combined.contains("ferry") ||
                combined.contains("交通") || combined.contains("車費") || combined.contains("地鐵") -> "Transport"
                
                combined.contains("parknshop") || combined.contains("百佳") ||
                combined.contains("wellcome") || combined.contains("惠康") ||
                combined.contains("hktvmall") || combined.contains("market") ||
                combined.contains("supermarket") || combined.contains("grocery") ||
                combined.contains("groceries") || combined.contains("target") || combined.contains("walmart") ||
                combined.contains("costco") || combined.contains("超市") || combined.contains("街市") ||
                combined.contains("7-eleven") || combined.contains("7-11") ||
                combined.contains("circle k") || combined.contains("ok便利店") -> "Groceries"
                
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

                combined.contains("零售") || combined.contains("shopping") || combined.contains("retail") ||
                combined.contains("百貨") || combined.contains("商場") || combined.contains("購物") ||
                combined.contains("淘寶") || combined.contains("taobao") || combined.contains("aeon") ||
                combined.contains("sogo") || combined.contains("donki") || combined.contains("驚安之殿堂") -> "Shopping"
                
                else -> "Other"
            }
        }

        /** Queue a candidate until a real account is chosen, or use a remembered choice. */
        fun queuePayment(context: Context, payment: ParsedPayment): Boolean {
            val sourcePackage = payment.sourcePackage.ifBlank { return false }
            val queued = PendingPaymentStore.enqueue(
                context = context,
                sourcePackage = sourcePackage,
                assetHint = payment.assetName,
                merchant = payment.merchant,
                amount = payment.amount,
                detectedAt = payment.detectedAt,
                notificationKey = payment.notificationKey,
                cardLast4 = payment.cardLast4,
                balanceRemaining = payment.balanceRemaining,
                isTopUp = payment.isTopUp,
                rawTitle = payment.rawTitle,
                rawText = payment.rawText,
                transactionType = payment.transactionType
            ) ?: return false
            val rememberedId = PendingPaymentStore.rememberedAccountId(context, queued)
            val rememberedAccount = InMemoryDatabase.accounts.value.firstOrNull { it.id == rememberedId }
            if (rememberedAccount != null && PendingPaymentStore.accept(context, queued.id, rememberedAccount.id, true)) {
                showLoggedNotification(context, queued.amount, queued.merchant, rememberedAccount.name, queued.balanceRemaining, queued.isTopUp)
            }
            return true
        }

        /**
         * Helper to process notifications for automated tests, ADB shell, or Waydroid testing.
         */
        suspend fun processNotificationForTest(context: Context, title: String, text: String, packageName: String): Boolean {
            val parsed = parseNotification(title, text, packageName) ?: return false
            return queuePayment(context, parsed.copy(sourcePackage = packageName))
        }

        suspend fun processBatchForTest(context: Context, items: List<com.example.vibefinance.ai.RawNotificationItem>): List<ParsedPayment> {
            val synthesizer = com.example.vibefinance.ai.GeminiNanoSynthesizer(context)
            val consolidated = synthesizer.synthesizeBatch(items)
            for (p in consolidated) {
                queuePayment(context, p)
            }
            return consolidated
        }

        private fun showLoggedNotification(
            context: Context,
            amount: Double,
            merchant: String,
            assetName: String,
            balanceRemaining: Double? = null,
            isTopUp: Boolean = false
        ) {
            val channelId = "payment_logging"
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    context.appString(R.string.nf_auto_log_channel),
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

            val balanceSuffix = if (balanceRemaining != null) {
                context.appString(R.string.nf_logged_balance_suffix, balanceRemaining)
            } else ""
            val titleStr = if (isTopUp) {
                context.appString(R.string.nf_logged_topup_title)
            } else {
                context.appString(R.string.nf_logged_expense_title)
            }
            val contentStr = if (isTopUp) {
                context.appString(R.string.nf_logged_topup_message, amount, merchant, assetName, balanceSuffix)
            } else {
                context.appString(R.string.nf_logged_expense_message, amount, merchant, assetName, balanceSuffix)
            }
            val notification = Notification.Builder(context, channelId)
                .setContentTitle(titleStr)
                .setContentText(contentStr)
                .setSmallIcon(android.R.drawable.ic_menu_save)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            manager.notify(System.currentTimeMillis().toInt(), notification)
        }
    }
}
