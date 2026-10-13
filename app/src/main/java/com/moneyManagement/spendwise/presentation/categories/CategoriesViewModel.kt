package com.moneymanagement.spendwise.presentation.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneymanagement.spendwise.domain.model.Category
import com.moneymanagement.spendwise.domain.model.TransactionType
import com.moneymanagement.spendwise.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoriesScreenData(
    val expenseCategories: List<Category>,
    val incomeCategories: List<Category>
)

sealed interface CategoriesEvent {
    data object Saved : CategoriesEvent
    data object Deleted : CategoriesEvent
    data class ShowError(val message: String) : CategoriesEvent
    data object CannotDeleteDefault : CategoriesEvent
    data class ConfirmSimpleDelete(val category: Category) : CategoriesEvent
    data class ConfirmReassignDelete(val category: Category, val usageCount: Int) : CategoriesEvent
}

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val screenData: StateFlow<CategoriesScreenData> = combine(
        categoryRepository.getCategoriesByType(TransactionType.EXPENSE),
        categoryRepository.getCategoriesByType(TransactionType.INCOME)
    ) { expense, income -> CategoriesScreenData(expense, income) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CategoriesScreenData(emptyList(), emptyList()))

    private val _events = Channel<CategoriesEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun addCategory(name: String, icon: String, color: String, type: TransactionType) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            viewModelScope.launch { _events.send(CategoriesEvent.ShowError("Category name can't be empty")) }
            return
        }
        viewModelScope.launch {
            categoryRepository.addCategory(Category(name = trimmed, icon = icon, color = color, type = type))
                .onSuccess { _events.send(CategoriesEvent.Saved) }
                .onFailure { _events.send(CategoriesEvent.ShowError(it.message ?: "Couldn't save this category")) }
        }
    }

    fun updateCategory(category: Category, name: String, icon: String, color: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            viewModelScope.launch { _events.send(CategoriesEvent.ShowError("Category name can't be empty")) }
            return
        }
        viewModelScope.launch {
            categoryRepository.updateCategory(category.copy(name = trimmed, icon = icon, color = color))
                .onSuccess { _events.send(CategoriesEvent.Saved) }
                .onFailure { _events.send(CategoriesEvent.ShowError(it.message ?: "Couldn't update this category")) }
        }
    }

    fun requestDelete(category: Category) {
        if (category.isDefault) {
            viewModelScope.launch { _events.send(CategoriesEvent.CannotDeleteDefault) }
            return
        }
        viewModelScope.launch {
            val usageCount = categoryRepository.getTransactionCountForCategory(category.id)
            if (usageCount == 0) {
                _events.send(CategoriesEvent.ConfirmSimpleDelete(category))
            } else {
                _events.send(CategoriesEvent.ConfirmReassignDelete(category, usageCount))
            }
        }
    }

    fun performSimpleDelete(category: Category) {
        viewModelScope.launch { performDelete(category, reassignTo = null) }
    }

    fun confirmReassignAndDelete(category: Category) {
        viewModelScope.launch {
            val fallback = findFallbackOther(category.type)
            performDelete(category, reassignTo = fallback?.id)
        }
    }

    private fun findFallbackOther(type: TransactionType): Category? {
        val pool = if (type == TransactionType.EXPENSE) screenData.value.expenseCategories else screenData.value.incomeCategories
        return pool.firstOrNull { it.isDefault && it.icon == "other" }
    }

    private suspend fun performDelete(category: Category, reassignTo: Long?) {
        categoryRepository.deleteCategory(category, reassignTo)
            .onSuccess { _events.send(CategoriesEvent.Deleted) }
            .onFailure { _events.send(CategoriesEvent.ShowError(it.message ?: "Couldn't delete this category")) }
    }
}
