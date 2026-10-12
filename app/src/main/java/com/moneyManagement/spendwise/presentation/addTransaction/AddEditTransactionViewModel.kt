package com.moneymanagement.spendwise.presentation.addTransaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneymanagement.spendwise.domain.model.Category
import com.moneymanagement.spendwise.domain.model.PaymentMethod
import com.moneymanagement.spendwise.domain.model.Transaction
import com.moneymanagement.spendwise.domain.model.TransactionType
import com.moneymanagement.spendwise.domain.repository.CategoryRepository
import com.moneymanagement.spendwise.domain.repository.SettingsRepository
import com.moneymanagement.spendwise.domain.repository.TransactionRepository
import com.moneymanagement.spendwise.utils.Constants
import com.moneymanagement.spendwise.utils.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditFormState(
    val isEditMode: Boolean = false,
    val type: TransactionType = TransactionType.EXPENSE,
    val amount: String = "",
    val title: String = "",
    val selectedCategoryId: Long? = null,
    val dateMillis: Long = DateTimeUtils.todayMillis(),
    val time: String = DateTimeUtils.currentTime24h(),
    val notes: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val amountError: String? = null,
    val titleError: String? = null,
    val categoryError: String? = null,
    val isSaving: Boolean = false,
    val currencyCode: String = "INR"
)

sealed interface AddEditTransactionEvent {
    data object Saved : AddEditTransactionEvent
    data class SaveFailed(val message: String) : AddEditTransactionEvent
}

@HiltViewModel
class AddEditTransactionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val transactionId: Long =
        savedStateHandle.get<Long>("transactionId") ?: Constants.NEW_TRANSACTION_ID
    private val isEditMode = transactionId != Constants.NEW_TRANSACTION_ID

    private val selectedType = MutableStateFlow(TransactionType.EXPENSE)

    val categories: StateFlow<List<Category>> = selectedType
        .flatMapLatest { type -> categoryRepository.getCategoriesByType(type) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    private val _formState = MutableStateFlow(AddEditFormState(isEditMode = isEditMode))
    val formState: StateFlow<AddEditFormState> = _formState.asStateFlow()

    private val eventChannel = Channel<AddEditTransactionEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            settingsRepository.userPreferences.collect { prefs ->
                _formState.update { it.copy(currencyCode = prefs.currencyCode) }
            }
        }

        if (isEditMode) {
            viewModelScope.launch {
                val transaction = transactionRepository.getTransactionById(transactionId).first()
                if (transaction != null) {
                    selectedType.value = transaction.type
                    _formState.update {
                        it.copy(
                            type = transaction.type,
                            amount = trimTrailingZero(transaction.amount),
                            title = transaction.title,
                            selectedCategoryId = transaction.category.id,
                            dateMillis = transaction.date,
                            time = transaction.time,
                            notes = transaction.notes,
                            paymentMethod = transaction.paymentMethod
                        )
                    }
                }
            }
        } else {
           /* viewModelScope.launch {
                val defaultType = settingsRepository.userPreferences.first().defaultTransactionType
                selectedType.value = defaultType
                _formState.update { it.copy(type = defaultType) }
            }*/
        }
    }
    fun onAmountChanged(value: String) =
        _formState.update { it.copy(amount = value, amountError = null) }

    fun onTitleChanged(value: String) =
        _formState.update { it.copy(title = value, titleError = null) }

    fun onNotesChanged(value: String) = _formState.update { it.copy(notes = value) }
    fun onPaymentMethodChanged(value: PaymentMethod) =
        _formState.update { it.copy(paymentMethod = value) }

    fun onDateChanged(value: Long) = _formState.update { it.copy(dateMillis = value) }
    fun onTimeChanged(value: String) = _formState.update { it.copy(time = value) }


    fun onTypeChanged(value: TransactionType) {
        selectedType.value = value
        _formState.update { it.copy(type = value, selectedCategoryId = null, categoryError = null) }
    }
    fun onCategorySelected(categoryId: Long) =
        _formState.update { it.copy(selectedCategoryId = categoryId, categoryError = null) }

    fun saveTransaction() {
        val state = _formState.value
        val amount = state.amount.toDoubleOrNull()
        val title = state.title.trim()
        val category = categories.value.firstOrNull { it.id == state.selectedCategoryId }

        val amountError = if (amount == null || amount < Constants.MIN_TRANSACTION_AMOUNT) "Enter an amount greater than 0" else null
        val titleError = if (title.isBlank()) "Title can't be empty" else null
        val categoryError = if (category == null) "Please select a category" else null

        if (amountError != null || titleError != null || categoryError != null) {
            _formState.update {
                it.copy(amountError = amountError, titleError = titleError, categoryError = categoryError)
            }
            return
        }

        _formState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val transaction = Transaction(
                title = title,
                amount = amount!!,
                type = state.type,
                category = category!!,
                date = state.dateMillis,
                time = state.time,
                notes = state.notes.trim(),
                paymentMethod = state.paymentMethod
            )
            val result = if (isEditMode) transactionRepository.updateTransaction(transaction) else transactionRepository.addTransaction(transaction)
            result.onSuccess { eventChannel.send(AddEditTransactionEvent.Saved) }
                .onFailure { eventChannel.send(AddEditTransactionEvent.SaveFailed(it.message?:"Couldn't save this transaction")) }
            _formState.update { it.copy(isSaving = false) }
        }
    }
    private fun trimTrailingZero(amount: Double): String =
        if (amount == amount.toLong().toDouble()) amount.toLong().toString() else amount.toString()
}
