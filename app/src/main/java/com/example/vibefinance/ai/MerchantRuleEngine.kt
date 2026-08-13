package com.example.vibefinance.ai

import android.content.Context
import android.content.SharedPreferences
import java.util.Locale

object MerchantRuleEngine {

    private const val PREFS_NAME = "vibe_merchant_rules"
    
    // Built-in default rules for common merchants
    private val defaultRules = mapOf(
        "starbucks" to "Food",
        "mcdonalds" to "Food",
        "mcdonald's" to "Food",
        "kfc" to "Food",
        "subway" to "Food",
        "wellcome" to "Food",
        "parknshop" to "Food",
        "7-eleven" to "Food",
        "711" to "Food",
        "circle k" to "Food",
        "mtr" to "Transport",
        "kmb" to "Transport",
        "uber" to "Transport",
        "taxi" to "Transport",
        "uniqlo" to "Shopping",
        "zara" to "Shopping",
        "amazon" to "Shopping",
        "apple" to "Shopping",
        "netflix" to "Utilities",
        "spotify" to "Utilities",
        "hkt" to "Utilities",
        "clp" to "Utilities"
    )

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Recommends a category for a given merchant/description input based on learned rules.
     */
    fun suggestCategory(context: Context, description: String): String? {
        val cleanInput = description.lowercase(Locale.ROOT).trim()
        if (cleanInput.isEmpty()) return null

        val prefs = getPrefs(context)

        // 1. Check user learned rules in SharedPreferences
        val allUserRules = prefs.all
        for ((key, value) in allUserRules) {
            if (value is String && cleanInput.contains(key.lowercase(Locale.ROOT))) {
                return value
            }
        }

        // 2. Fall back to built-in rules
        for ((key, category) in defaultRules) {
            if (cleanInput.contains(key)) {
                return category
            }
        }

        return null
    }

    /**
     * Learns a new categorization rule when user manually selects or edits a category for a merchant.
     */
    fun learnRule(context: Context, description: String, category: String) {
        val cleanInput = description.lowercase(Locale.ROOT).trim()
        if (cleanInput.isEmpty() || category.isEmpty()) return

        // Extract key merchant word (first word or full short phrase)
        val keyword = cleanInput.split(" ").firstOrNull { it.length >= 3 } ?: cleanInput
        
        getPrefs(context).edit().putString(keyword, category).apply()
    }
}
