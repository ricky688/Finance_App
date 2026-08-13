package com.example.vibefinance.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Double,
    val category: String,
    val frequency: String, // "Weekly" or "Monthly"
    val nextPaymentDate: Long, // epoch millis
    val accountId: Long // associated account/card to charge from
)
