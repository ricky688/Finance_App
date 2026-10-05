package com.example.vibefinance.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.vibefinance.data.dao.AccountDao
import com.example.vibefinance.data.dao.BudgetDao
import com.example.vibefinance.data.dao.TransactionDao
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.BudgetEntity
import com.example.vibefinance.data.entity.TransactionEntity

@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        BudgetEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class VibeFinanceDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
}
