package com.example.vibefinance.ui.main

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver

/** One draft survives mode changes and recreation; selectors resolve IDs against current accounts. */
internal class AddTransactionDraft(
    initialMode: TransactionMode,
    sourceId: Long? = null,
    destinationId: Long? = null
) {
    val mode = mutableStateOf(initialMode)
    val amount = mutableStateOf("0")
    val evaluated = mutableStateOf(false)
    val description = mutableStateOf("")
    val currency = mutableStateOf("HKD")
    val sourceAccountId = mutableStateOf(sourceId)
    val destinationAccountId = mutableStateOf(destinationId)

    fun switchMode(next: TransactionMode): Boolean {
        if (mode.value == next) return false
        mode.value = next
        return true
    }

    fun hasValidAccounts(sourceId: Long?, destinationId: Long?): Boolean = sourceId != null &&
        (mode.value != TransactionMode.TRANSFER || (destinationId != null && destinationId != sourceId))

    companion object {
        val Saver = listSaver<AddTransactionDraft, Any>(
            save = { listOf(it.mode.value.name, it.amount.value, it.evaluated.value, it.description.value,
                it.currency.value, it.sourceAccountId.value ?: -1L, it.destinationAccountId.value ?: -1L) },
            restore = { values ->
                AddTransactionDraft(TransactionMode.valueOf(values[0] as String),
                    (values[5] as Long).takeIf { it > 0 }, (values[6] as Long).takeIf { it > 0 }).apply {
                    amount.value = values[1] as String
                    evaluated.value = values[2] as Boolean
                    description.value = values[3] as String
                    currency.value = values[4] as String
                }
            }
        )
    }
}
