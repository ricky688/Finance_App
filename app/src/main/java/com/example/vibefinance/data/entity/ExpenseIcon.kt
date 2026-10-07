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
}
