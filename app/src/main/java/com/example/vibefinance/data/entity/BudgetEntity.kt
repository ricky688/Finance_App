package com.example.vibefinance.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RolloverMode {
    DISTRIBUTE_EVENLY, // Distribute past leftover/overspend evenly over remaining days
    ADD_TO_NEXT_DAY    // Add yesterday's leftover directly to today's daily budget
}

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val id: String = "ACTIVE_PERIOD", // Key for the active budgeting period
    val totalBudgetAmount: Double,
    val startDate: Long, // Start date of period in epoch milliseconds
    val endDate: Long,   // End date of period in epoch milliseconds
    val rolloverMode: RolloverMode = RolloverMode.DISTRIBUTE_EVENLY
)
