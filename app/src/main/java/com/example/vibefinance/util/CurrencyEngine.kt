package com.example.vibefinance.util

import java.util.Locale

data class CurrencyInfo(
    val code: String,
    val symbol: String,
    val flagEmoji: String
)

object CurrencyEngine {
    val supportedCurrencies = listOf(
        CurrencyInfo("HKD", "HK$", "🇭🇰"),
        CurrencyInfo("USD", "$", "🇺🇸"),
        CurrencyInfo("JPY", "¥", "🇯🇵"),
        CurrencyInfo("EUR", "€", "🇪🇺"),
        CurrencyInfo("GBP", "£", "🇬🇧"),
        CurrencyInfo("CNY", "¥", "🇨🇳"),
        CurrencyInfo("TWD", "NT$", "🇹🇼")
    )

    fun formatHkd(amount: Double): String {
        return String.format(Locale.US, "HK$ %,.2f", amount)
    }

    fun formatOriginal(amount: Double, currencyCode: String): String {
        val curr = supportedCurrencies.find { it.code.equals(currencyCode, ignoreCase = true) }
            ?: return String.format(Locale.US, "%.2f %s", amount, currencyCode)
        return String.format(Locale.US, "%s %s %,.2f", curr.flagEmoji, curr.symbol, amount)
    }
}
