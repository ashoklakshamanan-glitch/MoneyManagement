package com.moneymanagement.spendwise.presentation.transactiondetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneymanagement.spendwise.domain.model.Transaction
import com.moneymanagement.spendwise.domain.repository.SettingsRepository
import com.moneymanagement.spendwise.domain.repository.TransactionRepository
import com.moneymanagement.spendwise.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionDetailsData(val transaction: Transaction, val currencyCode: String)

sealed interface TransactionDetailsEvent {
    data object Deleted : TransactionDetailsEvent
    data class ShowError(val message: String) : TransactionDetailsEvent
}

@HiltViewModel
class TransactionDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val transactionId: Long = checkNotNull(savedStateHandle["transactionId"])

    private val _events = Channel<TransactionDetailsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    val uiState: StateFlow<UiState<TransactionDetailsData>> = combine(
        transactionRepository.getTransactionById(transactionId),
        settingsRepository.userPreferences
    ) { transaction, prefs ->
        if (transaction == null) UiState.Empty
        else UiState.Success(TransactionDetailsData(transaction, prefs.currencyCode)) as UiState<TransactionDetailsData>
    }
        .catch { emit(UiState.Error(it.message ?: "Couldn't load this transaction")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    fun deleteTransaction() {
        val transaction = (uiState.value as? UiState.Success)?.data?.transaction ?: return
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transaction)
                .onSuccess { _events.send(TransactionDetailsEvent.Deleted) }
                .onFailure { _events.send(TransactionDetailsEvent.ShowError(it.message ?: "Couldn't delete this transaction")) }
        }
    }
}
