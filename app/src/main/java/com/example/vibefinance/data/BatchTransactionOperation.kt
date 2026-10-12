package com.example.vibefinance.data

import com.example.vibefinance.data.entity.TransactionEntity

sealed interface BatchTransactionOperation {
    data object Delete : BatchTransactionOperation
    data class Category(val category: String) : BatchTransactionOperation
    data class Account(val accountId: Long) : BatchTransactionOperation
}
data class DeletedTransactionBatch(val records: List<TransactionEntity>, val appliedThrough: Long)
