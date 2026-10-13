package com.moneymanagement.spendwise.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneymanagement.spendwise.domain.model.Budget
import com.moneymanagement.spendwise.domain.model.TransactionType
import com.moneymanagement.spendwise.domain.repository.BudgetRepository
import com.moneymanagement.spendwise.domain.repository.CategoryRepository
import com.moneymanagement.spendwise.domain.repository.SettingsRepository
import com.moneymanagement.spendwise.utils.DateTimeUtils
import com.moneymanagement.spendwise.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BudgetScreenData(
    val overallBudget: Budget?,
    val categoryBudgets: List<Budget>,
    val currencyCode: String
)

sealed interface BudgetEvent {
    data object Saved : BudgetEvent
    data class ShowError(val message: String) : BudgetEvent
}

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val month = DateTimeUtils.currentMonth()
    private val year = DateTimeUtils.currentYear()

    val expenseCategories = categoryRepository.getCategoriesByType(TransactionType.EXPENSE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _events = Channel<BudgetEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    val uiState: StateFlow<UiState<BudgetScreenData>> = combine(
        budgetRepository.getOverallBudgetForMonth(month, year),
        budgetRepository.getCategoryBudgetsForMonth(month, year),
        settingsRepository.userPreferences
    ) { overall, categoryBudgets, prefs ->
        UiState.Success(BudgetScreenData(overall, categoryBudgets, prefs.currencyCode)) as UiState<BudgetScreenData>
    }
        .catch { emit(UiState.Error(it.message ?: "Couldn't load your budgets")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    fun currentMonthYear(): Pair<Int, Int> = month to year

    fun setOverallBudget(amount: Double) {
        viewModelScope.launch {
            budgetRepository.setOverallBudget(amount, month, year)
                .onSuccess { _events.send(BudgetEvent.Saved) }
                .onFailure { _events.send(BudgetEvent.ShowError(it.message ?: "Couldn't save this budget")) }
        }
    }

    fun setCategoryBudget(categoryId: Long, amount: Double) {
        viewModelScope.launch {
            budgetRepository.setCategoryBudget(categoryId, amount, month, year)
                .onSuccess { _events.send(BudgetEvent.Saved) }
                .onFailure { _events.send(BudgetEvent.ShowError(it.message ?: "Couldn't save this budget")) }
        }
    }

    fun deleteBudget(budgetId: Long) {
        viewModelScope.launch {
            budgetRepository.deleteBudget(budgetId)
                .onFailure { _events.send(BudgetEvent.ShowError(it.message ?: "Couldn't remove this budget")) }
        }
    }
}
