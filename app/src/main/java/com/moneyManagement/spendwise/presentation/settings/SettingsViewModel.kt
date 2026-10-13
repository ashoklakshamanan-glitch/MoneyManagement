package com.moneymanagement.spendwise.presentation.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneymanagement.spendwise.domain.model.Category
import com.moneymanagement.spendwise.domain.model.ThemeMode
import com.moneymanagement.spendwise.domain.model.Transaction
import com.moneymanagement.spendwise.domain.model.TransactionType
import com.moneymanagement.spendwise.domain.model.UserPreferences
import com.moneymanagement.spendwise.domain.repository.BudgetRepository
import com.moneymanagement.spendwise.domain.repository.CategoryRepository
import com.moneymanagement.spendwise.domain.repository.SettingsRepository
import com.moneymanagement.spendwise.domain.repository.TransactionRepository
import com.moneymanagement.spendwise.data.export.DataExportManager
import com.moneymanagement.spendwise.data.export.ExportFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SettingsEvent {
    data class ExportResult(val success: Boolean, val count: Int, val errorMessage: String? = null) : SettingsEvent
    data class ImportResult(val success: Boolean, val count: Int, val errorMessage: String? = null) : SettingsEvent
    data object DataCleared : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
    private val dataExportManager: DataExportManager
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = settingsRepository.userPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun setCurrency(code: String) = viewModelScope.launch { settingsRepository.setCurrency(code) }

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { settingsRepository.setThemeMode(mode) }

    fun setDefaultTransactionType(type: TransactionType) =
        viewModelScope.launch { settingsRepository.setDefaultTransactionType(type) }

    fun exportData(uri: Uri, format: ExportFormat) {
        viewModelScope.launch {
            val transactions = transactionRepository.getAllTransactions().first()
            if (transactions.isEmpty()) {
                _events.send(SettingsEvent.ExportResult(success = false, count = 0, errorMessage = "empty"))
                return@launch
            }
            val result = runCatching {
                val stream = context.contentResolver.openOutputStream(uri) ?: error("Couldn't open destination file")
                stream.use { out ->
                    when (format) {
                        ExportFormat.CSV -> dataExportManager.exportToCsv(transactions, out)
                        ExportFormat.JSON -> dataExportManager.exportToJson(transactions, out)
                    }
                }
                transactions.size
            }
            result
                .onSuccess { count -> _events.send(SettingsEvent.ExportResult(true, count)) }
                .onFailure { _events.send(SettingsEvent.ExportResult(false, 0, it.message)) }
        }
    }

    fun importData(uri: Uri, format: ExportFormat) {
        viewModelScope.launch {
            val result = runCatching {
                val rows = context.contentResolver.openInputStream(uri)?.use { input ->
                    when (format) {
                        ExportFormat.CSV -> dataExportManager.importFromCsv(input)
                        ExportFormat.JSON -> dataExportManager.importFromJson(input)
                    }
                } ?: error("Couldn't open the selected file")

                val existing = categoryRepository.getAllCategories().first()
                val categoryCache = existing.associateByTo(
                    HashMap(),
                    keySelector = { it.type to it.name.trim().lowercase() }
                )

                val transactions = rows.mapNotNull { row ->
                    if (row.title.isBlank() && row.amount <= 0.0) return@mapNotNull null
                    val key :Pair<TransactionType, String> = row.type to row.categoryName.trim().lowercase()
                    val category = categoryCache[key] ?: run {
                        val created = Category(
                            name = row.categoryName.ifBlank { "Other" },
                            icon = "other",
                            color = "#78909C",
                            type = row.type
                        )
                        val id = categoryRepository.addCategory(created).getOrThrow()
                        (created.copy(id = id)).also { categoryCache[key] = it }
                    }
                    Transaction(
                        title = row.title.ifBlank { "Imported transaction" },
                        amount = row.amount,
                        type = row.type,
                        category = category,
                        date = row.date,
                        time = row.time,
                        notes = row.notes,
                        paymentMethod = row.paymentMethod
                    )
                }
                transactionRepository.insertAll(transactions).getOrThrow()
            }
            result
                .onSuccess { count -> _events.send(SettingsEvent.ImportResult(true, count)) }
                .onFailure { _events.send(SettingsEvent.ImportResult(false, 0, it.message)) }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            transactionRepository.clearAllTransactions()
            budgetRepository.clearAllBudgets()
            categoryRepository.clearCustomCategories()
            _events.send(SettingsEvent.DataCleared)
        }
    }
}

