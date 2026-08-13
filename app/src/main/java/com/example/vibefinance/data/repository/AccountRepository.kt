package com.example.vibefinance.data.repository

import com.example.vibefinance.data.InMemoryDatabase
import com.example.vibefinance.data.entity.AccountEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor() {
    val allAccountsFlow: Flow<List<AccountEntity>> = InMemoryDatabase.accounts

    fun getAccountByIdFlow(id: Long): Flow<AccountEntity?> {
        return InMemoryDatabase.accounts.map { list -> list.find { it.id == id } }
    }

    suspend fun getAccountByIdSync(id: Long): AccountEntity? {
        return InMemoryDatabase.accounts.value.find { it.id == id }
    }

    suspend fun insertAccount(account: AccountEntity): Long {
        return InMemoryDatabase.insertAccount(account)
    }

    suspend fun updateAccount(account: AccountEntity) {
        InMemoryDatabase.updateAccount(account)
    }

    suspend fun updateBalance(id: Long, newBalance: Double) {
        InMemoryDatabase.updateBalance(id, newBalance)
    }

    suspend fun deleteAccount(account: AccountEntity) {
        InMemoryDatabase.deleteAccount(account)
    }
}
