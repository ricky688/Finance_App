package com.example.vibefinance.ui.main

import org.junit.Assert.*
import org.junit.Test

/** Unit tests for Add Transaction Bento 2-Card layout logic, draft persistence, and category deduplication. */
class AddTransactionBentoUnitTest {

    @Test
    fun amountValidationRegexAcceptsValidDecimalsAndRejectsInvalid() {
        val regex = Regex("""^\d*(?:\.\d{0,2})?$""")
        assertTrue(regex.matches(""))
        assertTrue(regex.matches("0"))
        assertTrue(regex.matches("12"))
        assertTrue(regex.matches("12."))
        assertTrue(regex.matches("12.3"))
        assertTrue(regex.matches("12.34"))
        assertTrue(regex.matches("9999.99"))

        assertFalse(regex.matches("12.345"))
        assertFalse(regex.matches("abc"))
        assertFalse(regex.matches("-10"))
        assertFalse(regex.matches("12..3"))
        assertFalse(regex.matches("12.3.4"))
    }

    @Test
    fun addTransactionDraftModeSwitchingAndAccountValidation() {
        val draft = AddTransactionDraft(TransactionMode.EXPENSE, sourceId = 1L)

        // Initial state
        assertEquals(TransactionMode.EXPENSE, draft.mode.value)
        assertEquals("0", draft.amount.value)
        assertTrue(draft.hasValidAccounts(sourceId = 1L, destinationId = null))

        // Same mode switch returns false
        assertFalse(draft.switchMode(TransactionMode.EXPENSE))

        // Switch to Transfer mode
        assertTrue(draft.switchMode(TransactionMode.TRANSFER))
        assertEquals(TransactionMode.TRANSFER, draft.mode.value)

        // In Transfer mode, requires distinct non-null source and destination
        assertFalse(draft.hasValidAccounts(sourceId = null, destinationId = 2L))
        assertFalse(draft.hasValidAccounts(sourceId = 1L, destinationId = null))
        assertFalse(draft.hasValidAccounts(sourceId = 1L, destinationId = 1L)) // identical account invalid
        assertTrue(draft.hasValidAccounts(sourceId = 1L, destinationId = 2L)) // distinct accounts valid

        // Switch to Income mode
        assertTrue(draft.switchMode(TransactionMode.INCOME))
        assertEquals(TransactionMode.INCOME, draft.mode.value)
        assertTrue(draft.hasValidAccounts(sourceId = 1L, destinationId = null))
    }

    @Test
    fun addTransactionDraftSaverRoundtrip() {
        val original = AddTransactionDraft(
            initialMode = TransactionMode.TRANSFER,
            sourceId = 5L,
            destinationId = 8L
        ).apply {
            amount.value = "150.50"
            evaluated.value = true
            description.value = "Dinner split 🍜"
            currency.value = "USD"
        }

        // Save
        val saved = with(AddTransactionDraft.Saver) {
            mockSaverScope().save(original)
        } as List<*>
        assertNotNull(saved)
        assertEquals(7, saved.size)
        assertEquals("TRANSFER", saved[0])
        assertEquals("150.50", saved[1])
        assertEquals(true, saved[2])
        assertEquals("Dinner split 🍜", saved[3])
        assertEquals("USD", saved[4])
        assertEquals(5L, saved[5])
        assertEquals(8L, saved[6])

        // Restore
        val restored = AddTransactionDraft.Saver.restore(saved)
        assertNotNull(restored)
        assertEquals(TransactionMode.TRANSFER, restored!!.mode.value)
        assertEquals("150.50", restored.amount.value)
        assertEquals(true, restored.evaluated.value)
        assertEquals("Dinner split 🍜", restored.description.value)
        assertEquals("USD", restored.currency.value)
        assertEquals(5L, restored.sourceAccountId.value)
        assertEquals(8L, restored.destinationAccountId.value)
    }

    @Test
    fun categoryDeduplicationPreservesOrderAndKeepsFirstSeenLabel() {
        // Simulated localization mapping
        val localizedMap = mapOf(
            "Food" to "餐飲美食",
            "Food & Drink" to "餐飲美食",
            "Dining" to "餐飲美食",
            "Transport" to "交通出行",
            "Transit" to "交通出行",
            "Shopping" to "購物消費",
            "Salary" to "薪資收入"
        )

        val rawCategories = listOf("Food", "Food & Drink", "Transport", "Transit", "Dining", "Shopping", "Salary")
        val deduplicated = rawCategories.distinctBy { localizedMap[it] ?: it }

        assertEquals(listOf("Food", "Transport", "Shopping", "Salary"), deduplicated)
    }

    private fun mockSaverScope() = object : androidx.compose.runtime.saveable.SaverScope {
        override fun canBeSaved(value: Any): Boolean = true
    }
}
