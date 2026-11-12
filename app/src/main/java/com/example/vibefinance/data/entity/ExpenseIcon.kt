package com.example.vibefinance.data.entity

/** Stable identifiers, independent of localized labels and category names. */
enum class ExpenseSymbol {
    FOOD, COFFEE, SHOPPING, TRANSPORT, CAR, FLIGHT, HOME, BILLS,
    HEALTH, EDUCATION, FITNESS, GIFT, GAMES, MOVIES, PETS, WORK
}

object ExpenseIcon {
    fun symbol(symbol: ExpenseSymbol) = "symbol:${symbol.name}"
    fun emoji(text: String): String? = text.trim().takeIf(::isSingleEmoji)?.let { "emoji:$it" }

    fun normalize(value: String?): String? {
        if (value == null) return null
        if (value.startsWith("symbol:")) return runCatching {
            symbol(ExpenseSymbol.valueOf(value.removePrefix("symbol:")))
        }.getOrNull()
        if (value.startsWith("emoji:")) return emoji(value.removePrefix("emoji:"))
        return null
    }

    /** Emoji sequence syntax, independent of the device's ICU version (Android 24 splits skin tones). */
    private fun isSingleEmoji(text: String): Boolean {
        if (text.isEmpty() || text.length > 64) return false
        val points = text.codePoints().toArray()
        if (points.first() in 0x1F1E6..0x1F1FF) return points.size == 2 && points[1] in 0x1F1E6..0x1F1FF
        if (points.first() in 0x30..0x39 || points.first() == 0x23 || points.first() == 0x2A) {
            return (points.size == 2 && points[1] == 0x20E3) ||
                (points.size == 3 && points[1] == 0xFE0F && points[2] == 0x20E3)
        }
        var index = 0
        while (index < points.size) {
            val base = points[index++]
            if (!isEmojiBase(base)) return false
            if (index < points.size && points[index] == 0xFE0F) index++
            if (index < points.size && points[index] in 0x1F3FB..0x1F3FF) index++
            if (index < points.size && points[index] in 0xE0020..0xE007E) {
                if (base != 0x1F3F4) return false
                while (index < points.size && points[index] in 0xE0020..0xE007E) index++
                if (index >= points.size || points[index++] != 0xE007F) return false
            }
            if (index == points.size) return true
            if (points[index++] != 0x200D || index == points.size) return false
        }
        return false
    }

    private fun isEmojiBase(point: Int): Boolean {
        if (point in 0x1F1E6..0x1F1FF || point in 0x1F3FB..0x1F3FF) return false
        return point in 0x1F000..0x1FAFF || point in 0x2600..0x27FF || point in 0x231A..0x231B ||
            point in 0x23E9..0x23F3 || point in 0x23F8..0x23FA || point in 0x2194..0x2199 ||
            point in 0x21A9..0x21AA || point in 0x2B05..0x2B07 || point in 0x2B1B..0x2B1C ||
            point in setOf(0x00A9, 0x00AE, 0x203C, 0x2049, 0x2122, 0x2328, 0x23CF,
                0x24C2, 0x2B50, 0x2B55, 0x3030, 0x303D, 0x3297, 0x3299)
    }

    /**
     * Matches an expense transaction's category name or custom icon against one of the 16 built-in symbols.
     */
    fun matchExpenseSymbol(category: String, customIcon: String?): ExpenseSymbol? {
        val icon = normalize(customIcon)
        if (icon?.startsWith("symbol:") == true) {
            val symName = icon.removePrefix("symbol:")
            val parsed = runCatching { ExpenseSymbol.valueOf(symName) }.getOrNull()
            if (parsed != null) return parsed
        }
        val raw = category.trim()
        val normalized = raw.replaceFirst(Regex("^[^\\p{L}\\p{N}]+"), "").lowercase(java.util.Locale.ROOT).trim()
        return when {
            // Food / Dining / 餐飲 / 飲食
            normalized in listOf("food", "food & drink", "restaurant", "food & dining", "dining") ||
                raw.contains("餐飲") || raw.contains("飲食") || raw.contains("美食") || raw.contains("早餐") || raw.contains("午餐") || raw.contains("晚餐") || raw.contains("飯") || raw.contains("食") -> ExpenseSymbol.FOOD

            // Coffee / 咖啡
            normalized in listOf("coffee", "cafe", "starbucks") || raw.contains("咖啡") -> ExpenseSymbol.COFFEE

            // Shopping / 購物
            normalized in listOf("shopping", "clothing", "groceries", "supermarket", "retail") ||
                raw.contains("購物") || raw.contains("服飾") || raw.contains("超市") || raw.contains("買") -> ExpenseSymbol.SHOPPING

            // Transport / 交通
            normalized in listOf("transport", "transit", "commute", "bus", "taxi", "mtr", "subway", "train") ||
                raw.contains("交通") || raw.contains("地鐵") || raw.contains("公車") || raw.contains("巴士") || raw.contains("的士") || raw.contains("捷運") -> ExpenseSymbol.TRANSPORT

            // Car / 汽車
            normalized in listOf("car", "driving", "fuel", "gas", "parking") ||
                raw.contains("汽車") || raw.contains("加油") || raw.contains("泊車") || raw.contains("停車") || raw.contains("車") -> ExpenseSymbol.CAR

            // Flight / 飛行 / 旅行
            normalized in listOf("flight", "travel", "holiday", "airline", "vacation") ||
                raw.contains("飛行") || raw.contains("機票") || raw.contains("旅行") || raw.contains("旅遊") -> ExpenseSymbol.FLIGHT

            // Home / 居家
            normalized in listOf("home", "housing", "rent", "furniture") ||
                raw.contains("居家") || raw.contains("房租") || raw.contains("家具") || raw.contains("家") -> ExpenseSymbol.HOME

            // Bills / 帳單
            normalized in listOf("bills", "bill", "utilities", "electricity", "water", "fee", "tax") ||
                raw.contains("帳單") || raw.contains("水電") || raw.contains("電費") || raw.contains("繳費") || raw.contains("單") -> ExpenseSymbol.BILLS

            // Health / 醫療
            normalized in listOf("health", "medical", "hospital", "doctor", "pharmacy", "medicine") ||
                raw.contains("醫療") || raw.contains("看診") || raw.contains("藥") || raw.contains("診所") || raw.contains("醫院") -> ExpenseSymbol.HEALTH

            // Education / 教育
            normalized in listOf("education", "tuition", "school", "course", "books", "study") ||
                raw.contains("教育") || raw.contains("學費") || raw.contains("補習") || raw.contains("書") || raw.contains("課程") -> ExpenseSymbol.EDUCATION

            // Fitness / 運動
            normalized in listOf("fitness", "gym", "sport", "workout") ||
                raw.contains("運動") || raw.contains("健身") -> ExpenseSymbol.FITNESS

            // Gift / 禮物
            normalized in listOf("gift", "present", "redpocket") ||
                raw.contains("禮物") || raw.contains("紅包") || raw.contains("送禮") -> ExpenseSymbol.GIFT

            // Games / 遊戲
            normalized in listOf("games", "game", "gaming", "steam", "playstation", "nintendo") ||
                raw.contains("遊戲") || raw.contains("電玩") -> ExpenseSymbol.GAMES

            // Movies / 電影 / 休閒娛樂
            normalized in listOf("movies", "movie", "entertainment", "cinema", "netflix") ||
                raw.contains("電影") || raw.contains("休閒娛樂") || raw.contains("娛樂") || raw.contains("戲院") -> ExpenseSymbol.MOVIES

            // Pets / 寵物
            normalized in listOf("pets", "pet", "dog", "cat") ||
                raw.contains("寵物") || raw.contains("貓") || raw.contains("狗") -> ExpenseSymbol.PETS

            // Work / 工作
            normalized in listOf("work", "job", "business", "office") ||
                raw.contains("工作") || raw.contains("商務") || raw.contains("辦公") -> ExpenseSymbol.WORK

            else -> null
        }
    }
}
