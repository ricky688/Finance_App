package com.example.vibefinance.data.entity

import java.util.Locale

enum class InterceptableApp(
    val id: String,
    val defaultEnabled: Boolean,
    val packageKeywords: List<String>,
    val requiredTitleKeywords: List<String> = emptyList()
) {
    GOOGLE_PAY(
        id = "google_pay",
        defaultEnabled = true,
        packageKeywords = listOf(
            "com.google.android.apps.walletnfcrel",
            "com.google.android.apps.nbu.paisa.user",
            "com.google.android.apps.wallet"
        )
    ),
    SAMSUNG_PAY(
        id = "samsung_pay",
        defaultEnabled = true,
        packageKeywords = listOf(
            "com.samsung.android.spay",
            "com.samsung.android.raja.spay",
            "com.samsung.android.spaymini",
            "com.samsung.android.samsungpay.gear"
        )
    ),
    OCTOPUS(
        id = "octopus",
        defaultEnabled = true,
        packageKeywords = listOf(
            "com.octopuscards.nfc_reader",
            "com.octopuscards.octopus_app",
            "com.octopus.wallet"
        ),
        requiredTitleKeywords = listOf(
            "android版八達通", "android版八逹通", "android 八達通", "android 八逹通", "android八達通", "android八逹通",
            "android octopus", "octopus on android", "smart octopus"
        )
    ),
    PAYME(
        id = "payme",
        defaultEnabled = true,
        packageKeywords = listOf(
            "hk.com.hsbc.payme",
            "com.hsbc.payme"
        )
    ),
    ALIPAY(
        id = "alipay",
        defaultEnabled = true,
        packageKeywords = listOf(
            "hk.alipay.payment",
            "hk.alipay.wallet",
            "com.alipay.hk",
            "com.eg.android.AlipayGphone"
        )
    ),
    WECHAT_PAY(
        id = "wechat_pay",
        defaultEnabled = false,
        packageKeywords = listOf(
            "com.tencent.mm"
        ),
        requiredTitleKeywords = listOf(
            "微信支付", "wechat pay", "微信買單", "微信轉賬", "微信轉帳", "支付憑證"
        )
    ),
    LINE_PAY(
        id = "line_pay",
        defaultEnabled = false,
        packageKeywords = listOf(
            "com.linepayplus.paa",
            "jp.naver.line.android"
        ),
        requiredTitleKeywords = listOf(
            "line pay", "line 錢包", "line wallet", "linepay"
        )
    ),
    HSBC(
        id = "hsbc_bank",
        defaultEnabled = true,
        packageKeywords = listOf(
            "com.hsbc.hbap.mobilebanking",
            "hk.com.hsbc.hsbchongkong",
            "com.hsbc.hbap"
        )
    ),
    HANG_SENG(
        id = "hang_seng_bank",
        defaultEnabled = true,
        packageKeywords = listOf(
            "com.hangseng.rbmobile"
        )
    ),
    BOCHK(
        id = "bochk",
        defaultEnabled = true,
        packageKeywords = listOf(
            "com.bankofchina.bochk.mobileapplication",
            "com.boc.bank"
        )
    ),
    MOX_BANK(
        id = "mox_bank",
        defaultEnabled = true,
        packageKeywords = listOf(
            "com.mox.bank"
        )
    ),
    CITIBANK(
        id = "citibank",
        defaultEnabled = true,
        packageKeywords = listOf(
            "com.citibank.mobile.hk",
            "com.citi.citimobile"
        )
    );

    fun matches(packageName: String, title: String, text: String): Boolean {
        val lowerPkg = packageName.lowercase(Locale.US)
        val lowerTitle = title.lowercase(Locale.US)
        val lowerText = text.lowercase(Locale.US)
        val isShell = lowerPkg.contains("shell")

        // Exact match or prefix match on real package name (or shell for automated testing)
        val matchesPkg = isShell || packageKeywords.any { kw ->
            val kwLower = kw.lowercase(Locale.US)
            lowerPkg == kwLower || lowerPkg.startsWith("$kwLower.")
        }
        if (!matchesPkg) return false

        // If shell testing, verify title/text contains the app keyword
        if (isShell) {
            val hasIdentifier = id.replace("_", " ").lowercase(Locale.US).split(" ").any { 
                lowerTitle.contains(it) || lowerText.contains(it) 
            } || (id == "octopus" && (lowerTitle.contains("八達通") || lowerText.contains("八達通")))
            if (!hasIdentifier && requiredTitleKeywords.none { lowerTitle.contains(it.lowercase(Locale.US)) }) {
                return false
            }
        }

        // If specific channel/title required (e.g. Octopus app payment alerts vs wallet promos, WeChat / LINE chat)
        if (requiredTitleKeywords.isNotEmpty()) {
            val titleMatches = if (this == OCTOPUS) {
                // For Octopus app, accept Android Octopus, Smart Octopus, OR authentic top-up notifications
                val isOctopusTopUp = (lowerTitle.contains("八達通") || lowerTitle.contains("octopus")) &&
                    (lowerText.contains("轉賬") || lowerText.contains("轉帳") || lowerText.contains("增值") ||
                     lowerText.contains("充值") || lowerText.contains("儲值") || lowerText.contains("top-up") || lowerText.contains("topped up"))
                requiredTitleKeywords.any { kw -> lowerTitle.contains(kw.lowercase(Locale.US)) } || isOctopusTopUp
            } else {
                requiredTitleKeywords.any { kw ->
                    val kwLower = kw.lowercase(Locale.US)
                    lowerTitle.contains(kwLower) || lowerText.contains(kwLower)
                }
            }
            if (!titleMatches) return false
        }

        return true
    }

    companion object {
        fun fromId(id: String): InterceptableApp? = values().find { it.id == id }
        fun defaultEnabledApps(): Set<String> = values().filter { it.defaultEnabled }.map { it.id }.toSet()
    }
}
