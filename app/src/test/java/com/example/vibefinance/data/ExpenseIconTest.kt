package com.example.vibefinance.data

import com.example.vibefinance.data.entity.ExpenseIcon
import com.example.vibefinance.data.entity.ExpenseSymbol
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Picker paths explored using ARTEMIS on Waydroid before authoring these checks. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [24, 34])
class ExpenseIconTest {
    @Test fun joinedEmojiFlagsAndModifiersStayOneIcon() {
        listOf("☕", "🍜", "🛍️", "⭐", "🇭🇰", "👍🏽", "👨‍👩‍👧‍👦", "1️⃣", "©️").forEach { emoji ->
            assertEquals(emoji, "emoji:$emoji", ExpenseIcon.emoji(emoji))
            assertEquals("emoji:$emoji", ExpenseIcon.normalize("emoji:$emoji"))
        }
        assertEquals("emoji:☕", ExpenseIcon.emoji(" ☕ "))
    }
    @Test fun invalidOrMultipleCharactersCannotBecomeAnIcon() {
        listOf("", "coffee", "a", "☕🍜", "☕ text", "1", "\u200D", "☕\u200D", "👍🏽🏽", "🏽", "🇭", "🇭🇰🇭", "x".repeat(65)).forEach {
            assertNull(it, ExpenseIcon.emoji(it))
        }
        listOf("symbol:UNKNOWN", "symbol:coffee", "emoji:text", "coffee", "https://example.com/icon").forEach {
            assertNull(it, ExpenseIcon.normalize(it))
        }
    }
    @Test fun symbolsHaveStableLanguageIndependentIdentifiersAndNullKeepsDefault() {
        ExpenseSymbol.entries.forEach { symbol ->
            assertEquals("symbol:${symbol.name}", ExpenseIcon.normalize(ExpenseIcon.symbol(symbol)))
        }
        assertNull(ExpenseIcon.normalize(null))
    }
}
