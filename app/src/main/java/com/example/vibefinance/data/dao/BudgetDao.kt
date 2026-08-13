package com.example.vibefinance.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.vibefinance.data.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE id = :id")
    fun getBudgetFlow(id: String): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getBudgetSync(id: String): BudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)
}
