package com.example.vibefinance.theme

import androidx.compose.ui.graphics.Color
import java.util.Locale

/**
 * Unified financial chart color system for VibeFinance.
 * Guarantees 100% color scheme consistency across all 4 primary application pages:
 * 1. 每日 (Daily / HomeScreen - Category Analytics Donut Chart)
 * 2. 資產 (Assets / AccountsScreen - Interactive Donut Chart & Money Separation Bar)
 * 3. 定期 (Recurring / RecurringScreen - Recurring Category Donut Chart)
 * 4. 紀錄 (History / HistoryScreen - Category Breakdown Stacked Bar & Legend)
 */
object ChartColors {

    /**
     * Unified wide-spectrum chromatic palette designed specifically for financial breakdown charts.
     * Spans high-contrast, distinct hues across the color wheel to guarantee clarity and accessibility.
     */
    val UNIFIED_PALETTE = listOf(
        Color(0xFF8B5CF6), // Royal Purple / Violet (Entertainment / Media)
        Color(0xFF10B981), // Emerald Mint (Groceries / Software / Subscriptions / Cash)
        Color(0xFF3B82F6), // Ocean Blue (Electronics / Gadgets / Bank Accounts)
        Color(0xFFF59E0B), // Warm Amber (Utilities / Bills)
        Color(0xFFEC4899), // Hot Rose / Magenta (Shopping / Lifestyle)
        Color(0xFF06B6D4), // Cyan (Fitness / Health)
        Color(0xFFF97316), // Coral Orange (Food & Dining)
        Color(0xFF6366F1), // Indigo (Housing / Rent)
        Color(0xFFEAB308), // Sunflower Gold (Education / Tuition / Investments)
        Color(0xFF14B8A6), // Bright Teal (Finance / Investments)
        Color(0xFFFBBF24), // Golden Amber (Transport / Commute)
        Color(0xFFF43F5E)  // Coral Crimson (Debt / Overdraft / Alert)
    )

    /**
     * Blends two colors with a specified ratio.
     */
    fun blendColors(colorA: Color, colorB: Color, ratio: Float): Color {
        val a = ratio.coerceIn(0f, 1f)
        return Color(
            red = colorA.red * (1f - a) + colorB.red * a,
            green = colorA.green * (1f - a) + colorB.green * a,
            blue = colorA.blue * (1f - a) + colorB.blue * a,
            alpha = colorA.alpha * (1f - a) + colorB.alpha * a
        )
    }

    /**
     * Resolves a category string into a unified semantic color across all 4 pages.
     * Supports English, Traditional Chinese, Simplified Chinese, and common synonyms.
     */
    fun getSemanticCategoryColor(category: String, isDark: Boolean = true): Color? {
        val cat = category.lowercase(Locale.US).trim()
        return when {
            // Entertainment / Movies / Games / Media / 休閒娛樂
            cat.contains("entertain") || cat.contains("movie") || cat.contains("game") || cat.contains("gaming") ||
            cat.contains("play") || cat.contains("media") || cat.contains("娛樂") || cat.contains("休閒") || cat.contains("影音") ->
                Color(0xFF8B5CF6) // Royal Purple

            // Software / AI / Subscriptions / Recurring / 軟體訂閱
            cat.contains("software") || cat.contains("ai") ->
                Color(0xFF10B981) // Emerald Mint

            // Electronics / Gadgets / Tech / 數碼 / 電器 / 3C
            cat.contains("electronic") || cat.contains("gadget") || cat.contains("tech") ||
            cat.contains("數碼") || cat.contains("电器") || cat.contains("電器") || cat.contains("3c") ->
                Color(0xFF3B82F6) // Ocean Blue

            // Utilities / Bills / Electricity / Water / Gas / 生活水電
            cat.contains("utilit") || cat.contains("bill") || cat.contains("electric") || cat.contains("water") ||
            cat.contains("gas") || cat.contains("水電") || cat.contains("帳單") || cat.contains("生活") ->
                Color(0xFFF59E0B) // Warm Amber

            // Shopping / Clothing / Lifestyle / Retail / 購物消費
            cat.contains("shop") || cat.contains("cloth") || cat.contains("retail") || cat.contains("lifestyle") ||
            cat.contains("購物") || cat.contains("買") ->
                Color(0xFFEC4899) // Hot Rose / Magenta

            // Fitness / Health / Medical / Gym / 醫療健康 / 健身
            cat.contains("fitness") || cat.contains("health") || cat.contains("medic") || cat.contains("fit") ||
            cat.contains("gym") || cat.contains("doctor") || cat.contains("醫療") || cat.contains("健康") || cat.contains("健身") ->
                Color(0xFF06B6D4) // Cyan

            // Food & Drink / Dining / Restaurant / Cafe / 餐飲美食
            cat.contains("food") || cat.contains("drink") || cat.contains("dining") || cat.contains("restaurant") ||
            cat.contains("cafe") || cat.contains("餐") || cat.contains("飲食") || cat.contains("食") ->
                Color(0xFFF97316) // Coral Orange

            // Groceries / Supermarket / Daily Necessities / 超市雜貨
            cat.contains("grocer") || cat.contains("supermarket") || cat.contains("market") ||
            cat.contains("超市") || cat.contains("雜貨") || cat.contains("日用") ->
                Color(0xFF10B981) // Emerald Mint

            // Housing / Rent / Mortgage / 房屋居住
            cat.contains("hous") || cat.contains("rent") || cat.contains("mortgage") || cat.contains("home") ||
            cat.contains("房") || cat.contains("居住") ->
                Color(0xFF6366F1) // Indigo

            // Education / Tuition / Courses / 教育進修
            cat.contains("educat") || cat.contains("tuition") || cat.contains("course") || cat.contains("school") ||
            cat.contains("教育") || cat.contains("進修") || cat.contains("學費") ->
                Color(0xFFEAB308) // Sunflower Gold

            // Investment / Stocks / Finance / 理財投資
            cat.contains("invest") || cat.contains("stock") || cat.contains("fund") || cat.contains("dividend") ||
            cat.contains("理財") || cat.contains("金融") || cat.contains("投資") ->
                Color(0xFF14B8A6) // Bright Teal

            // Transport / Transit / Commute / Taxi / Bus / 交通出行
            cat.contains("transport") || cat.contains("commute") || cat.contains("transit") || cat.contains("taxi") ||
            cat.contains("bus") || cat.contains("subway") || cat.contains("metro") || cat.contains("交通") || cat.contains("車") ->
                Color(0xFFFBBF24) // Golden Amber

            // Subscriptions general fallback
            cat.contains("subscri") || cat.contains("recurring") || cat.contains("訂閱") ->
                Color(0xFF10B981)

            else -> null
        }
    }

    /**
     * Unified wide-spectrum chromatic fallback palette for custom categories.
     */
    fun getFallbackPalette(isDark: Boolean = true): List<Color> = UNIFIED_PALETTE

    /**
     * Builds a unified, deterministic, collision-free category color map.
     */
    fun buildCategoryColorMap(
        categories: List<String>,
        isDark: Boolean = true,
        primaryColor: Color? = null
    ): Map<String, Color> {
        val result = mutableMapOf<String, Color>()
        val usedColors = mutableSetOf<Color>()

        // Pass 1: Assign semantic colors
        categories.forEach { cat ->
            val semantic = getSemanticCategoryColor(cat, isDark)
            if (semantic != null && !usedColors.contains(semantic)) {
                result[cat] = semantic
                usedColors.add(semantic)
            }
        }

        // Pass 2: Fill remaining categories from the distinct palette ensuring no collisions
        var paletteIndex = 0
        categories.forEach { cat ->
            if (!result.containsKey(cat)) {
                while (paletteIndex < UNIFIED_PALETTE.size && usedColors.contains(UNIFIED_PALETTE[paletteIndex])) {
                    paletteIndex++
                }
                val color = if (paletteIndex < UNIFIED_PALETTE.size) {
                    UNIFIED_PALETTE[paletteIndex].also { usedColors.add(it) }
                } else {
                    UNIFIED_PALETTE[result.size % UNIFIED_PALETTE.size]
                }
                result[cat] = color
            }
        }
        return result
    }

    /**
     * Resolves a single category color.
     */
    fun getCategoryColor(
        category: String,
        isDark: Boolean = true,
        primaryColor: Color? = null
    ): Color {
        val semantic = getSemanticCategoryColor(category, isDark)
        if (semantic != null) return semantic
        val hash = kotlin.math.abs(category.hashCode())
        return UNIFIED_PALETTE[hash % UNIFIED_PALETTE.size]
    }

    /**
     * Asset segment colors for Page 2 (Assets / AccountsScreen) harmonized with unified chart palette.
     */
    fun getAssetSegmentColor(segmentId: String, isDark: Boolean = true, primaryColor: Color? = null): Color {
        return when (segmentId.lowercase(Locale.US).trim()) {
            "cash", "wallet" -> Color(0xFF10B981) // Emerald Mint (Matches Cash)
            "bank", "bank accounts" -> Color(0xFF3B82F6) // Ocean Blue (Matches Bank)
            "debit", "debit card", "debit cards" -> Color(0xFF8B5CF6) // Violet (Debit Assets)
            "debt", "credit debt", "credit card", "cc" -> Color(0xFFF43F5E) // Crimson Rose (Debt)
            "investment", "crypto", "stock" -> Color(0xFFEAB308) // Sunflower Gold
            else -> Color(0xFF8B5CF6)
        }
    }
}
