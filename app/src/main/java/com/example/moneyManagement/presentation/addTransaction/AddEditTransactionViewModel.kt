package com.example.moneyManagement.presentation.addTransaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneyManagement.domain.model.Category
import com.example.moneyManagement.domain.model.PaymentMethod
import com.example.moneyManagement.domain.model.TransactionType
import com.example.moneyManagement.domain.repository.TransactionRepository
import com.example.moneyManagement.utils.Constants
import com.example.moneyManagement.utils.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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

@HiltViewModel
class AddEditTransactionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TransactionRepository
) : ViewModel() {

    private val transactionId: Long =
        savedStateHandle.get<Long>("transactionId") ?: Constants.NEW_TRANSACTION_ID
    private val isEditMode = transactionId != Constants.NEW_TRANSACTION_ID

    private val selectedType = MutableStateFlow(TransactionType.EXPENSE)

    val categories: StateFlow<List<Category>> = MutableStateFlow(
        listOf(
            Category(1, "Food", "food", "#FF9800", TransactionType.EXPENSE, true),
            Category(2, "Transport", "transport", "#2196F3", TransactionType.EXPENSE, true),
            Category(3, "Shopping", "shopping", "#E91E63", TransactionType.EXPENSE, true),
            Category(4, "Entertainment", "entertainment", "#9C27B0", TransactionType.EXPENSE, true)
        )
    )/*selectedType
        .flatMapLatest { type -> categoryRepository.getCategoriesByType(type) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())*/


    private val _formState = MutableStateFlow(AddEditFormState(isEditMode = isEditMode))
    val formState: StateFlow<AddEditFormState> = _formState.asStateFlow()


    fun onAmountChanged(value: String) =
        _formState.update { it.copy(amount = value, amountError = null) }

    fun onTitleChanged(value: String) =
        _formState.update { it.copy(title = value, titleError = null) }

    fun onNotesChanged(value: String) = _formState.update { it.copy(notes = value) }
    fun onPaymentMethodChanged(value: PaymentMethod) =
        _formState.update { it.copy(paymentMethod = value) }

    fun onDateChanged(value: Long) = _formState.update { it.copy(dateMillis = value) }
    fun onTimeChanged(value: String) = _formState.update { it.copy(time = value) }


    fun onTypeChanged(value: TransactionType) = _formState.update { it.copy(type = value) }
    fun onCategorySelected(categoryId: Long) =
        _formState.update { it.copy(selectedCategoryId = categoryId, categoryError = null) }

}