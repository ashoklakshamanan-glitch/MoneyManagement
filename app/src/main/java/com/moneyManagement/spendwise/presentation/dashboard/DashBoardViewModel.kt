package com.moneymanagement.spendwise.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneymanagement.spendwise.domain.model.Budget
import com.moneymanagement.spendwise.domain.model.DailyTotal
import com.moneymanagement.spendwise.domain.model.Transaction
import com.moneymanagement.spendwise.domain.model.TransactionType
import com.moneymanagement.spendwise.domain.repository.BudgetRepository
import com.moneymanagement.spendwise.domain.repository.SettingsRepository
import com.moneymanagement.spendwise.domain.repository.TransactionRepository
import com.moneymanagement.spendwise.utils.Constants
import com.moneymanagement.spendwise.utils.DateTimeUtils
import com.moneymanagement.spendwise.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.math.roundToInt

data class DashboardData(
    val balance: Double,
    val totalIncome: Double,
    val totalExpense: Double,
    val currentMonthExpense: Double,
    val monthlyBudget: Budget?,
    val budgetPercentUsed: Int,
    val recentTransactions: List<Transaction>,
    val weeklySpending: List<DailyTotal>,
    val currencyCode: String
)
private data class PartialTotals(
    val income: Double,
    val expense: Double,
    val monthExpense: Double,
    val budget: Budget?,
    val recent: List<Transaction>
)

@HiltViewModel
class DashBoardViewModel @Inject constructor(private val transactionRepository: TransactionRepository, private val budgetRepository: BudgetRepository, private val settingsRepository: SettingsRepository): ViewModel(){
    val uiState: StateFlow<UiState<DashboardData>> = run {
        val (monthStart, monthEnd) = DateTimeUtils.currentMonthRange()
        val (weekStart, weekEnd) = DateTimeUtils.lastSevenDaysRange()
        val month = DateTimeUtils.currentMonth()
        val year = DateTimeUtils.currentYear()

        val partial = combine(
            transactionRepository.getTotalByType(TransactionType.INCOME),
            transactionRepository.getTotalByType(TransactionType.EXPENSE),
            transactionRepository.getTotalByTypeBetween(TransactionType.EXPENSE, monthStart, monthEnd),
            budgetRepository.getOverallBudgetForMonth(month, year),
            transactionRepository.getRecentTransactions(Constants.RECENT_TRANSACTIONS_LIMIT)
        ) { income, expense, monthExpense, budget, recent ->
            PartialTotals(income, expense, monthExpense, budget, recent)
        }

        combine(
            partial,
            transactionRepository.getDailyTotals(TransactionType.EXPENSE, weekStart, weekEnd),
            settingsRepository.userPreferences
        ) { totals, weekly, prefs ->
            val budgetAmount = totals.budget?.amount ?: 0.0
            val percentUsed = if (budgetAmount > 0) {
                ((totals.monthExpense / budgetAmount) * 100).roundToInt().coerceAtLeast(0)
            } else 0

            DashboardData(
                balance = totals.income - totals.expense,
                totalIncome = totals.income,
                totalExpense = totals.expense,
                currentMonthExpense = totals.monthExpense,
                monthlyBudget = totals.budget,
                budgetPercentUsed = percentUsed,
                recentTransactions = totals.recent,
                weeklySpending = weekly,
                currencyCode = prefs.currencyCode
            )
        }
            .map { data -> UiState.Success(data) as UiState<DashboardData> }
            .catch { emit(UiState.Error(it.message ?: "Couldn't load your dashboard")) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)
    }
}