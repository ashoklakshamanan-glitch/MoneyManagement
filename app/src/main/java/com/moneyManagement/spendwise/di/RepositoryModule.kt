package com.moneymanagement.spendwise.di

import com.moneymanagement.spendwise.data.repository.BudgetRepositoryImpl
import com.moneymanagement.spendwise.data.repository.CategoryRepositoryImpl
import com.moneymanagement.spendwise.data.repository.TransactionRepositoryImpl
import com.moneymanagement.spendwise.domain.repository.BudgetRepository
import com.moneymanagement.spendwise.domain.repository.CategoryRepository
import com.moneymanagement.spendwise.domain.repository.TransactionRepository
import com.moneymanagement.spendwise.data.repository.SettingsRepositoryImpl
import com.moneymanagement.spendwise.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}