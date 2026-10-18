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

    fun formatHkd(amount: Double, showPositiveSign: Boolean = false): String {
        val isNeg = amount < 0.0
        val absVal = kotlin.math.abs(amount)
        val formatted = String.format(Locale.US, "%,.2f", absVal)
        return when {
            isNeg -> "-HK$ $formatted"
            showPositiveSign && amount > 0.0 -> "+HK$ $formatted"
            else -> "HK$ $formatted"
        }
    }

    fun formatSignedAmount(amount: Double, prefix: String = "HK$"): String {
        val isNeg = amount < 0.0
        val absVal = kotlin.math.abs(amount)
        val formatted = String.format(Locale.US, "%,.2f", absVal)
        val space = if (prefix.length > 1) " " else ""
        return if (isNeg) "-$prefix$space$formatted" else "$prefix$space$formatted"
    }

    fun formatSignedCompact(amount: Double, prefix: String = "$"): String {
        val isNeg = amount < 0.0
        val absVal = kotlin.math.abs(amount)
        val formatted = String.format(Locale.US, "%,.0f", absVal)
        return if (isNeg) "-$prefix$formatted" else "$prefix$formatted"
    }

    fun formatOriginal(amount: Double, currencyCode: String): String {
        val curr = supportedCurrencies.find { it.code.equals(currencyCode, ignoreCase = true) }
            ?: return String.format(Locale.US, "%.2f %s", amount, currencyCode)
        return String.format(Locale.US, "%s %s %,.2f", curr.flagEmoji, curr.symbol, amount)
    }
}
