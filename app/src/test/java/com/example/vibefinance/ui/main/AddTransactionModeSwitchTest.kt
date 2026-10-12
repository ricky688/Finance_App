package com.example.vibefinance.ui.main

import androidx.compose.runtime.saveable.SaverScope
import org.junit.Assert.*
import org.junit.Test

class AddTransactionModeSwitchTest {
    @Test fun switchingEveryModeRetainsTheSharedDraft() {
        val draft = AddTransactionDraft(TransactionMode.EXPENSE, 7, 9).apply {
            amount.value = "6+4-"
            description.value = "午餐 🍜"
            currency.value = "USD"
            evaluated.value = true
        }
        for (mode in listOf(TransactionMode.INCOME, TransactionMode.TRANSFER, TransactionMode.EXPENSE)) {
            assertTrue(draft.switchMode(mode))
            assertEquals(mode, draft.mode.value)
            assertEquals("6+4-", draft.amount.value)
            assertEquals("午餐 🍜", draft.description.value)
            assertEquals("USD", draft.currency.value)
            assertEquals(7L, draft.sourceAccountId.value)
            assertEquals(9L, draft.destinationAccountId.value)
            assertTrue(draft.evaluated.value)
            assertFalse(draft.switchMode(mode))
        }
    }
    @Test fun onlyTransferRequiresADistinctDestination() {
        val draft = AddTransactionDraft(TransactionMode.EXPENSE)
        for (mode in listOf(TransactionMode.EXPENSE, TransactionMode.INCOME, TransactionMode.TRANSFER)) {
            draft.switchMode(mode)
            assertFalse(draft.hasValidAccounts(null, 2))
            assertEquals(mode != TransactionMode.TRANSFER, draft.hasValidAccounts(1, null))
            assertEquals(mode != TransactionMode.TRANSFER, draft.hasValidAccounts(1, 1))
            assertTrue(draft.hasValidAccounts(1, 2))
        }
    }
    @Test fun savedDraftRestoresModeAndAllCommonInputs() {
        val draft = AddTransactionDraft(TransactionMode.TRANSFER, 3, 8).apply {
            amount.value = "12.34+"; description.value = "帳單 💳"; currency.value = "JPY"; evaluated.value = true
        }
        val scope = object : SaverScope { override fun canBeSaved(value: Any) = true }
        val saved = with(AddTransactionDraft.Saver) { scope.save(draft) }!!
        val restored = AddTransactionDraft.Saver.restore(saved)!!
        assertEquals(TransactionMode.TRANSFER, restored.mode.value)
        assertEquals("12.34+", restored.amount.value)
        assertEquals(draft.description.value, restored.description.value)
        assertEquals("JPY", restored.currency.value)
        assertEquals(3L, restored.sourceAccountId.value)
        assertEquals(8L, restored.destinationAccountId.value)
        assertTrue(restored.evaluated.value)
    }
}
