package com.moneymanagement.spendwise.di

import android.content.Context
import androidx.room.Room
import com.moneymanagement.spendwise.data.model.local.dao.BudgetDao
import com.moneymanagement.spendwise.data.model.local.dao.CategoryDao
import com.moneymanagement.spendwise.data.model.local.dao.TransactionDao
import com.moneymanagement.spendwise.data.model.local.database.AppDatabase
import com.moneymanagement.spendwise.data.model.local.database.DatabaseCallback
import com.moneymanagement.spendwise.data.model.local.database.MIGRATION_1_2
import com.moneymanagement.spendwise.utils.Constants
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
    fun provideAppDatabase(
        @ApplicationContext context: Context,callback: DatabaseCallback
    ): AppDatabase = Room.databaseBuilder(
            context, AppDatabase::class.java, Constants.DATABASE_NAME
        ).addCallback(callback).addMigrations(MIGRATION_1_2).build()

    @Provides
    @Singleton
    fun provideTransactionDao(
        database: AppDatabase
    ): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(
        database: AppDatabase
    ): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    @Singleton
    fun provideBudgetDao(
        database: AppDatabase
    ): BudgetDao {
        return database.budgetDao()
    }


}
