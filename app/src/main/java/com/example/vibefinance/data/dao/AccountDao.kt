package com.example.vibefinance.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.vibefinance.data.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE id = :id")
    fun getAccountById(id: Long): Flow<AccountEntity?>

    @Query("SELECT * FROM accounts WHERE id = :id")
    fun getAccountByIdSync(id: Long): AccountEntity?

    @Query("SELECT * FROM accounts ORDER BY name ASC")
    fun getAllAccountsFlow(): Flow<List<AccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("UPDATE accounts SET balance = :newBalance WHERE id = :id")
    suspend fun updateBalance(id: Long, newBalance: Double)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)
}
