package com.moneymanagement.spendwise.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.moneymanagement.spendwise.domain.model.ThemeMode
import com.moneymanagement.spendwise.domain.model.TransactionType
import com.moneymanagement.spendwise.domain.model.UserPreferences
import com.moneymanagement.spendwise.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private object PrefKeys {
    val CURRENCY_CODE = stringPreferencesKey("currency_code")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val DEFAULT_TRANSACTION_TYPE = stringPreferencesKey("default_transaction_type")
    val FIRST_RUN_COMPLETED = booleanPreferencesKey("first_run_completed")
}

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    override val userPreferences: Flow<UserPreferences> = dataStore.data.map { prefs ->
        UserPreferences(
            currencyCode = prefs[PrefKeys.CURRENCY_CODE] ?: "INR",
            themeMode = prefs[PrefKeys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            defaultTransactionType = prefs[PrefKeys.DEFAULT_TRANSACTION_TYPE]
                ?.let { runCatching { TransactionType.valueOf(it) }.getOrNull() }
                ?: TransactionType.EXPENSE,
            hasCompletedFirstRun = prefs[PrefKeys.FIRST_RUN_COMPLETED] ?: false
        )
    }

    override suspend fun setCurrency(currencyCode: String) {
        dataStore.edit { it[PrefKeys.CURRENCY_CODE] = currencyCode }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[PrefKeys.THEME_MODE] = mode.name }
    }

    override suspend fun setDefaultTransactionType(type: TransactionType) {
        dataStore.edit { it[PrefKeys.DEFAULT_TRANSACTION_TYPE] = type.name }
    }

    override suspend fun setFirstRunCompleted() {
        dataStore.edit { it[PrefKeys.FIRST_RUN_COMPLETED] = true }
    }

    override suspend fun clearAllPreferences() {
        dataStore.edit { it.clear() }
    }
}
