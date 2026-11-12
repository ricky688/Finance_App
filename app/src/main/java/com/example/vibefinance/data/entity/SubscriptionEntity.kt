package com.example.vibefinance.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Double,
    val category: String,
    val frequency: String, // Daily, Weekly, Monthly or Yearly
    val nextPaymentDate: Long, // epoch millis
    val accountId: Long, // associated account/card to charge from
    val interval: Int = 1, // Every N frequency units; old records retain their original schedule.
    val isPaid: Boolean = false, // Whether the user has paid for the current cycle
    val lastPaidDate: Long? = null, // Epoch millis when user marked it as paid
    val isAutoPaid: Boolean = true // Whether it is auto-debit (auto-deposit) or manual payment
)
