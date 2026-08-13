package com.example.vibefinance.util

import java.util.Locale

data class CurrencyInfo(
    val code: String,
    val symbol: String,
    val flagEmoji: String,
    val rateToHkd: Double // 1 Foreign Unit = X HKD
)

object CurrencyEngine {
    val supportedCurrencies = listOf(
        CurrencyInfo("HKD", "HK$", "🇭🇰", 1.0),
        CurrencyInfo("USD", "$", "🇺🇸", 7.80),
        CurrencyInfo("JPY", "¥", "🇯🇵", 0.052),
        CurrencyInfo("EUR", "€", "🇪🇺", 8.45),
        CurrencyInfo("GBP", "£", "🇬🇧", 9.90),
        CurrencyInfo("CNY", "¥", "🇨🇳", 1.08),
        CurrencyInfo("TWD", "NT$", "🇹🇼", 0.24)
    )

    fun convertToHkd(amount: Double, currencyCode: String): Double {
        val curr = supportedCurrencies.find { it.code.equals(currencyCode, ignoreCase = true) } ?: return amount
        return amount * curr.rateToHkd
    }

    fun formatHkd(amount: Double): String {
        return String.format(Locale.US, "HK$ %,.2f", amount)
    }

    fun formatOriginal(amount: Double, currencyCode: String): String {
        val curr = supportedCurrencies.find { it.code.equals(currencyCode, ignoreCase = true) }
            ?: return String.format(Locale.US, "%.2f %s", amount, currencyCode)
        return String.format(Locale.US, "%s %s %,.2f", curr.flagEmoji, curr.symbol, amount)
    }
}
