package com.example.vibefinance.data.repository

import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.data.entity.AccountEntity
import com.example.vibefinance.data.entity.AccountType
import com.example.vibefinance.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor() {
    val allTransactionsFlow: Flow<List<TransactionEntity>> = InMemoryDatabase.transactions

    fun getTransactionsForMonthFlow(startTime: Long, endTime: Long): Flow<List<TransactionEntity>> {
        return InMemoryDatabase.transactions.map { list ->
            list.filter { it.timestamp in startTime..endTime }
        }
    }

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        val txId = InMemoryDatabase.insertTransaction(transaction)
        
        // Update account balances if the transaction timestamp is in the past or present
        if (transaction.timestamp <= System.currentTimeMillis()) {
            updateAccountBalancesForTransaction(transaction)
        }
        
        return txId
    }

    suspend fun insertInstallmentTransaction(
        baseTransaction: TransactionEntity,
        installments: Int
    ) {
        val groupId = UUID.randomUUID().toString()
        val installmentAmount = baseTransaction.amount / installments
        val now = System.currentTimeMillis()

        for (i in 1..installments) {
            val futureTimestamp = addMonthsToTimestamp(baseTransaction.timestamp, i - 1)
            val installmentTx = baseTransaction.copy(
                amount = installmentAmount,
                timestamp = futureTimestamp,
                installmentNumber = i,
                totalInstallments = installments,
                groupId = groupId
            )
            
            InMemoryDatabase.insertTransaction(installmentTx)
            
            // Only update current account balance if the installment is due (timestamp is past/present)
            if (futureTimestamp <= now) {
                updateAccountBalancesForTransaction(installmentTx)
            }
        }
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        // Reverse account balance effect
        if (transaction.timestamp <= System.currentTimeMillis()) {
            reverseAccountBalancesForTransaction(transaction)
        }
        InMemoryDatabase.deleteTransaction(transaction)
    }

    suspend fun updateTransaction(newTx: TransactionEntity, oldTx: TransactionEntity) {
        // 1. Reverse the effect of the old transaction on account balances.
        if (oldTx.timestamp <= System.currentTimeMillis()) {
            reverseAccountBalancesForTransaction(oldTx)
        }
        // 2. Update in database.
        InMemoryDatabase.updateTransaction(newTx)
        // 3. Apply the effect of the new transaction on account balances.
        if (newTx.timestamp <= System.currentTimeMillis()) {
            updateAccountBalancesForTransaction(newTx)
        }
    }

    suspend fun deleteInstallmentGroup(groupId: String, accountId: Long) {
        InMemoryDatabase.deleteTransactionsByGroupId(groupId)
    }

    private suspend fun updateAccountBalancesForTransaction(tx: TransactionEntity) {
        if (tx.toAccountId != null) {
            // It's a transfer!
            adjustAccountBalance(tx.accountId, -tx.amount) // decrease source
            
            // For destination: if it is CC, we pay off debt (which decreases outstanding balance).
            // If it is BANK/CASH, we increase asset balance.
            val toAccount = getAccountByIdSync(tx.toAccountId)
            if (toAccount != null) {
                if (toAccount.type == AccountType.CC) {
                    adjustAccountBalance(tx.toAccountId, -tx.amount) // pay off CC debt
                } else {
                    adjustAccountBalance(tx.toAccountId, tx.amount) // increase asset balance
                }
            }
        } else {
            // Regular income or expense
            // If amount > 0, it's an expense (outflow). If amount < 0, it's an income (inflow).
            val account = getAccountByIdSync(tx.accountId) ?: return
            if (account.type == AccountType.CC) {
                // For CC: expense increases debt (balance). Income decreases debt.
                adjustAccountBalance(tx.accountId, tx.amount)
            } else {
                // For Cash/Bank: expense decreases balance. Income increases balance.
                adjustAccountBalance(tx.accountId, -tx.amount)
            }
        }
    }

    private suspend fun reverseAccountBalancesForTransaction(tx: TransactionEntity) {
        // Reversing balance changes is the exact opposite of applying them
        if (tx.toAccountId != null) {
            adjustAccountBalance(tx.accountId, tx.amount)
            val toAccount = getAccountByIdSync(tx.toAccountId)
            if (toAccount != null) {
                if (toAccount.type == AccountType.CC) {
                    adjustAccountBalance(tx.toAccountId, tx.amount)
                } else {
                    adjustAccountBalance(tx.toAccountId, -tx.amount)
                }
            }
        } else {
            val account = getAccountByIdSync(tx.accountId) ?: return
            if (account.type == AccountType.CC) {
                adjustAccountBalance(tx.accountId, -tx.amount)
            } else {
                adjustAccountBalance(tx.accountId, tx.amount)
            }
        }
    }

    private suspend fun adjustAccountBalance(accountId: Long, amount: Double) {
        val account = getAccountByIdSync(accountId) ?: return
        val newBalance = account.balance + amount
        InMemoryDatabase.updateBalance(accountId, newBalance)
    }

    private suspend fun getAccountByIdSync(id: Long): AccountEntity? {
        return InMemoryDatabase.accounts.value.find { it.id == id }
    }

    private fun addMonthsToTimestamp(timestamp: Long, monthsToAdd: Int): Long {
        val instant = Instant.ofEpochMilli(timestamp)
        val localDate = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate()
        val futureDate = localDate.plusMonths(monthsToAdd.toLong())
        return futureDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
