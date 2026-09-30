package com.example.vibefinance.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["toAccountId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("accountId"),
        Index("toAccountId")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val category: String,
    val timestamp: Long,
    val accountId: Long,
    val toAccountId: Long? = null, // Set for transfers (moves from accountId -> toAccountId)
    val isExcludedFromDailyBudget: Boolean = false,
    val description: String = "",
    val installmentNumber: Int? = null, // Current installment index (e.g. 1 for 1st installment)
    val totalInstallments: Int? = null, // Total installment count (e.g. 12)
    val groupId: String? = null, // Groups related installment transactions
    val isBalanceAdjustment: Boolean = false,
    val balanceAdjustmentDelta: Double? = null, // Exact account-balance change, independent of account type
    // A credit-card balance is debt, so its signed change differs from Cash/Bank/Debit.
    // Keep the account types used when this entry affected balances: editing an account's
    // type later must not reinterpret its earlier History entries or reversal amounts.
    val sourceWasCreditCard: Boolean? = null,
    val destinationWasCreditCard: Boolean? = null
)
