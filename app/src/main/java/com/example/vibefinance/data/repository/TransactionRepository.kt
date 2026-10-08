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
        val recordedTransaction = withAccountTypeSnapshot(transaction)
        val txId = InMemoryDatabase.insertTransaction(recordedTransaction)
        
        // Update account balances if the transaction timestamp is in the past or present
        if (recordedTransaction.timestamp <= System.currentTimeMillis()) {
            updateAccountBalancesForTransaction(recordedTransaction)
        }
        
        return txId
    }

    suspend fun insertTransactions(transactions: List<TransactionEntity>, updateBalances: Boolean = true) {
        val recordedTransactions = transactions.map(::withAccountTypeSnapshot)
        InMemoryDatabase.insertTransactions(recordedTransactions)
        if (updateBalances) {
            val now = System.currentTimeMillis()
            val deltas = mutableMapOf<Long, Double>()
            val currentAccounts = InMemoryDatabase.accounts.value.associateBy { it.id }

            for (tx in recordedTransactions) {
                if (tx.timestamp <= now) {
                    if (tx.isBalanceAdjustment) {
                        val delta = requireNotNull(tx.balanceAdjustmentDelta) {
                            "Balance adjustment is missing its delta"
                        }
                        deltas[tx.accountId] = (deltas[tx.accountId] ?: 0.0) + delta
                    } else if (tx.toAccountId != null) {
                        deltas[tx.accountId] = (deltas[tx.accountId] ?: 0.0) +
                            transferSourceBalanceDelta(tx.amount, tx.sourceWasCreditCard)
                        val toAccount = currentAccounts[tx.toAccountId]
                        if (toAccount != null) {
                            if (tx.destinationWasCreditCard == true) {
                                deltas[tx.toAccountId] = (deltas[tx.toAccountId] ?: 0.0) - tx.amount
                            } else {
                                deltas[tx.toAccountId] = (deltas[tx.toAccountId] ?: 0.0) + tx.amount
                            }
                        }
                    } else {
                        val account = currentAccounts[tx.accountId]
                        if (account != null) {
                            if (tx.sourceWasCreditCard == true) {
                                deltas[tx.accountId] = (deltas[tx.accountId] ?: 0.0) + tx.amount
                            } else {
                                deltas[tx.accountId] = (deltas[tx.accountId] ?: 0.0) - tx.amount
                            }
                        }
                    }
                }
            }
            if (deltas.isNotEmpty()) {
                InMemoryDatabase.batchUpdateBalances(deltas)
            }
        }
    }

    suspend fun insertInstallmentTransaction(
        baseTransaction: TransactionEntity,
        installments: Int
    ) {
        val groupId = UUID.randomUUID().toString()
        val installmentAmount = baseTransaction.amount / installments
        val recordedBase = withAccountTypeSnapshot(baseTransaction)
        val now = System.currentTimeMillis()

        for (i in 1..installments) {
            val futureTimestamp = addMonthsToTimestamp(baseTransaction.timestamp, i - 1)
            val installmentTx = recordedBase.copy(
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
        InMemoryDatabase.updateTransactionWithBalances(newTx, oldTx)
    }

    suspend fun deleteInstallmentGroup(groupId: String, accountId: Long) {
        InMemoryDatabase.deleteTransactionsByGroupId(groupId)
    }

    private suspend fun updateAccountBalancesForTransaction(tx: TransactionEntity) {
        if (tx.isBalanceAdjustment) {
            adjustAccountBalance(tx.accountId, requireNotNull(tx.balanceAdjustmentDelta))
            return
        }
        if (tx.toAccountId != null) {
            // It's a transfer!
            val source = getAccountByIdSync(tx.accountId)
            // Moving money from a credit card increases its debt; moving it from
            // cash, bank, or debit decreases its available balance.
            adjustAccountBalance(tx.accountId, transferSourceBalanceDelta(tx.amount, tx.sourceWasCreditCard ?: (source?.type == AccountType.CC)))
            
            // For destination: if it is CC, we pay off debt (which decreases outstanding balance).
            // If it is BANK/CASH/DEBIT, we increase asset balance.
            val toAccount = getAccountByIdSync(tx.toAccountId)
            if (toAccount != null) {
                if (tx.destinationWasCreditCard ?: (toAccount.type == AccountType.CC)) {
                    adjustAccountBalance(tx.toAccountId, -tx.amount) // pay off CC debt
                } else {
                    adjustAccountBalance(tx.toAccountId, tx.amount) // increase asset balance
                }
            }
        } else {
            // Regular income or expense
            // If amount > 0, it's an expense (outflow). If amount < 0, it's an income (inflow).
            val account = getAccountByIdSync(tx.accountId) ?: return
            if (tx.sourceWasCreditCard ?: (account.type == AccountType.CC)) {
                // For CC: expense increases debt (balance). Income decreases debt.
                adjustAccountBalance(tx.accountId, tx.amount)
            } else {
                // For Cash/Bank/Debit: expense decreases balance. Income increases balance.
                adjustAccountBalance(tx.accountId, -tx.amount)
            }
        }
    }

    private suspend fun reverseAccountBalancesForTransaction(tx: TransactionEntity) {
        // Reversing balance changes is the exact opposite of applying them
        if (tx.isBalanceAdjustment) {
            adjustAccountBalance(tx.accountId, -requireNotNull(tx.balanceAdjustmentDelta))
            return
        }
        if (tx.toAccountId != null) {
            val source = getAccountByIdSync(tx.accountId)
            adjustAccountBalance(tx.accountId, -transferSourceBalanceDelta(tx.amount, tx.sourceWasCreditCard ?: (source?.type == AccountType.CC)))
            val toAccount = getAccountByIdSync(tx.toAccountId)
            if (toAccount != null) {
                if (tx.destinationWasCreditCard ?: (toAccount.type == AccountType.CC)) {
                    adjustAccountBalance(tx.toAccountId, tx.amount)
                } else {
                    adjustAccountBalance(tx.toAccountId, -tx.amount)
                }
            }
        } else {
            val account = getAccountByIdSync(tx.accountId) ?: return
            if (tx.sourceWasCreditCard ?: (account.type == AccountType.CC)) {
                adjustAccountBalance(tx.accountId, -tx.amount)
            } else {
                adjustAccountBalance(tx.accountId, tx.amount)
            }
        }
    }

    private suspend fun adjustAccountBalance(accountId: Long, amount: Double) {
        InMemoryDatabase.adjustAccountBalance(accountId, amount)
    }

    private suspend fun getAccountByIdSync(id: Long): AccountEntity? {
        return InMemoryDatabase.accounts.value.find { it.id == id }
    }

    private fun transferSourceBalanceDelta(amount: Double, sourceWasCreditCard: Boolean?): Double =
        if (sourceWasCreditCard == true) amount else -amount

    private fun withAccountTypeSnapshot(tx: TransactionEntity): TransactionEntity {
        val currentAccounts = InMemoryDatabase.accounts.value.associateBy { it.id }
        return tx.copy(
            sourceWasCreditCard = tx.sourceWasCreditCard
                ?: (currentAccounts[tx.accountId]?.type == AccountType.CC),
            destinationWasCreditCard = if (tx.toAccountId == null) null else (
                tx.destinationWasCreditCard ?: (currentAccounts[tx.toAccountId]?.type == AccountType.CC)
            )
        )
    }

    private fun addMonthsToTimestamp(timestamp: Long, monthsToAdd: Int): Long {
        val instant = Instant.ofEpochMilli(timestamp)
        val localDate = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate()
        val futureDate = localDate.plusMonths(monthsToAdd.toLong())
        return futureDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
