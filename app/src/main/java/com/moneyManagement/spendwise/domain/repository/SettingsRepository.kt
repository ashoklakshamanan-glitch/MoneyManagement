package com.moneymanagement.spendwise.domain.repository

import com.moneymanagement.spendwise.domain.model.ThemeMode
import com.moneymanagement.spendwise.domain.model.TransactionType
import com.moneymanagement.spendwise.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    val userPreferences: Flow<UserPreferences>

    suspend fun setCurrency(currencyCode: String)

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setDefaultTransactionType(type: TransactionType)

    suspend fun setFirstRunCompleted()

    suspend fun clearAllPreferences()
}
