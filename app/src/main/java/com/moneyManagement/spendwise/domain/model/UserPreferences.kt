package com.moneymanagement.spendwise.domain.model


enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

data class UserPreferences(
    val currencyCode: String = "INR",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val defaultTransactionType: TransactionType = TransactionType.EXPENSE,
    val hasCompletedFirstRun: Boolean = false
)
