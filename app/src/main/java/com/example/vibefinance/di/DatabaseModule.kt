package com.example.vibefinance.di

import android.content.Context
import androidx.room.Room
import com.example.vibefinance.data.VibeFinanceDatabase
import com.example.vibefinance.data.dao.AccountDao
import com.example.vibefinance.data.dao.BudgetDao
import com.example.vibefinance.data.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): VibeFinanceDatabase {
        return Room.databaseBuilder(
            context,
            VibeFinanceDatabase::class.java,
            "vibefinance_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideAccountDao(db: VibeFinanceDatabase): AccountDao {
        return db.accountDao()
    }

    @Provides
    fun provideTransactionDao(db: VibeFinanceDatabase): TransactionDao {
        return db.transactionDao()
    }

    @Provides
    fun provideBudgetDao(db: VibeFinanceDatabase): BudgetDao {
        return db.budgetDao()
    }
}
