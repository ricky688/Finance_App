package com.example.vibefinance.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Keep existing names stable: account snapshots persist the enum name as text.
enum class AccountType { CASH, BANK, DEBIT, CC }

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val nickname: String? = null, // Optional user label shown instead of the account name
    val type: AccountType,
    val balance: Double, // Assets for Cash/Bank/Debit; outstanding debt for CC
    val icon: String, // Identifies the graphic icon for the account
    val creditLimit: Double? = null, // Maximum allowed CC outstanding balance
    val billingDate: Int? = null, // Credit card statement closing day (結算日), 1–31; null when unknown
    val paymentDate: Int? = null, // Payment due day of month (e.g., 25 for 25th of every month)
    val paymentDeadline: Int? = null, // Payment due day deadline of month (e.g., 10th of next month)
    val cardTheme: String? = null, // Customizable gradient styling identifier
    val accentColorKey: String? = null, // Stable palette key for an optional identity accent
    val cardLast4: String? = null, // Custom last 4 digits of the card (e.g. 1234)
    val cardProtocol: String? = null, // Card protocol: visa, mastercard, or null
    val cardIssuer: String? = null, // Card issuer: boc, hsbc, chase, citi, or null
    val cardPattern: String? = null, // Customizable background pattern styling identifier
    val cardImageUri: String? = null, // Path to custom background image file in internal storage
    val customImageAspectRatio: String? = "CARD", // Ratio preference: CARD (1.586:1), SQUARE (1:1), WIDE (16:9), CIRCLE (1:1)
    val cardBgOffsetX: Float = 0f, // Horizontal offset / pan (-1.0f to 1.0f, 0f = center)
    val cardBgOffsetY: Float = 0f, // Vertical offset / pan (-1.0f to 1.0f, 0f = center)
    val cardBgScale: Float = 1f, // Scale / zoom factor (0.5f to 3.0f, 1.0f = default)
    val minSpendThreshold: Double? = null, // Minimum monthly spending threshold required to unlock high cashback
    val linkedAppPackage: String? = null // Explicitly linked target package for quick launch; null for auto-detect; "none" for disabled
)
