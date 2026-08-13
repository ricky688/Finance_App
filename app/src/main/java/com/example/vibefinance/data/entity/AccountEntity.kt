package com.example.vibefinance.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AccountType { CASH, BANK, CC }

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: AccountType,
    val balance: Double, // Assets for Cash/Bank; Outstanding balance (debt) for CC
    val icon: String, // Identifies the graphic icon for the account
    val creditLimit: Double? = null, // Maximum allowed CC outstanding balance
    val billingDate: Int? = null, // Billing day of month (e.g., 10 for 10th of every month)
    val paymentDate: Int? = null, // Payment due day of month (e.g., 25 for 25th of every month)
    val paymentDeadline: Int? = null, // Payment due day deadline of month (e.g., 10th of next month)
    val cardTheme: String? = null, // Customizable gradient styling identifier
    val cardLast4: String? = null, // Custom last 4 digits of the card (e.g. 1234)
    val cardProtocol: String? = null, // Card protocol: visa, mastercard, or null
    val cardIssuer: String? = null, // Card issuer: boc, hsbc, chase, citi, or null
    val cardPattern: String? = null, // Customizable background pattern styling identifier
    val cardImageUri: String? = null, // Path to custom background image file in internal storage
    val customImageAspectRatio: String? = "CARD", // Ratio preference: CARD (1.586:1), SQUARE (1:1), WIDE (16:9), CIRCLE (1:1)
    val minSpendThreshold: Double? = null // Minimum monthly spending threshold required to unlock high cashback
)

