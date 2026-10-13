package com.moneymanagement.spendwise.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneymanagement.spendwise.domain.model.CategoryTotal
import com.moneymanagement.spendwise.domain.model.DailyTotal
import com.moneymanagement.spendwise.domain.model.MonthlyTotal
import com.moneymanagement.spendwise.domain.model.TransactionType
import com.moneymanagement.spendwise.domain.repository.SettingsRepository
import com.moneymanagement.spendwise.domain.repository.TransactionRepository
import com.moneymanagement.spendwise.utils.Constants
import com.moneymanagement.spendwise.utils.DateTimeUtils
import com.moneymanagement.spendwise.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.concurrent.TimeUnit
import javax.inject.Inject

enum class AnalyticsRange(val label: String) {
    WEEK("This Week"),
    MONTH("This Month"),
    LAST_MONTH("Last Month"),
    YEAR("This Year"),
    CUSTOM("Custom Range")
}

data class AnalyticsData(
    val totalIncome: Double,
    val totalExpense: Double,
    val savings: Double,
    val avgDailySpend: Double,
    val topCategory: CategoryTotal?,
    val categoryBreakdown: List<CategoryTotal>,
    val dailySpending: List<DailyTotal>,
    val monthlyExpenses: List<MonthlyTotal>,
    val currencyCode: String,
    val rangeLabel: String
)

private data class ResolvedRange(val start: Long, val end: Long, val label: String)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _selectedRange = MutableStateFlow(AnalyticsRange.MONTH)
    val selectedRange: StateFlow<AnalyticsRange> = _selectedRange.asStateFlow()

    private val _customRange = MutableStateFlow<Pair<Long, Long>?>(null)

    private val monthlyExpenses: StateFlow<List<MonthlyTotal>> = transactionRepository
        .getMonthlyTotals(TransactionType.EXPENSE, Constants.ANALYTICS_MONTHLY_CHART_MONTHS)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<UiState<AnalyticsData>> =
        combine(_selectedRange, _customRange) { range, custom -> resolveRange(range, custom) }
            .flatMapLatest { resolved ->
                combine(
                    transactionRepository.getTotalByTypeBetween(TransactionType.INCOME, resolved.start, resolved.end),
                    transactionRepository.getTotalByTypeBetween(TransactionType.EXPENSE, resolved.start, resolved.end),
                    transactionRepository.getCategoryTotals(TransactionType.EXPENSE, resolved.start, resolved.end),
                    transactionRepository.getDailyTotals(TransactionType.EXPENSE, resolved.start, resolved.end),
                    monthlyExpenses
                ) { income, expense, breakdown, daily, monthly ->
                    buildAnalyticsData(income, expense, breakdown, daily, monthly, resolved)
                }
            }
            .combine(settingsRepository.userPreferences) { data, prefs -> data.copy(currencyCode = prefs.currencyCode) }
            .map { UiState.Success(it) as UiState<AnalyticsData> }
            .catch { emit(UiState.Error(it.message ?: "Couldn't load analytics")) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    private fun resolveRange(range: AnalyticsRange, custom: Pair<Long, Long>?): ResolvedRange = when (range) {
        AnalyticsRange.WEEK -> DateTimeUtils.currentWeekRange().let { ResolvedRange(it.first, it.second, range.label) }
        AnalyticsRange.MONTH -> DateTimeUtils.currentMonthRange().let { ResolvedRange(it.first, it.second, range.label) }
        AnalyticsRange.LAST_MONTH -> DateTimeUtils.lastMonthRange().let { ResolvedRange(it.first, it.second, range.label) }
        AnalyticsRange.YEAR -> DateTimeUtils.currentYearRange().let { ResolvedRange(it.first, it.second, range.label) }
        AnalyticsRange.CUSTOM -> {
            val c = custom ?: DateTimeUtils.currentMonthRange()
            ResolvedRange(c.first, c.second, range.label)
        }
    }

    private fun buildAnalyticsData(
        income: Double,
        expense: Double,
        breakdown: List<CategoryTotal>,
        daily: List<DailyTotal>,
        monthly: List<MonthlyTotal>,
        resolved: ResolvedRange
    ): AnalyticsData {
        val spanDays = TimeUnit.MILLISECONDS.toDays(resolved.end - resolved.start).toInt() + 1
        val avgDaily = if (spanDays > 0) expense / spanDays else 0.0
        return AnalyticsData(
            totalIncome = income,
            totalExpense = expense,
            savings = income - expense,
            avgDailySpend = avgDaily,
            topCategory = breakdown.maxByOrNull { it.total },
            categoryBreakdown = breakdown.sortedByDescending { it.total },
            dailySpending = daily,
            monthlyExpenses = monthly,
            currencyCode = "INR",
            rangeLabel = resolved.label
        )
    }

    fun onRangeSelected(range: AnalyticsRange) {
        _selectedRange.value = range
        if (range != AnalyticsRange.CUSTOM) _customRange.value = null
    }

    fun onCustomRangeSelected(start: Long, end: Long) {
        _customRange.value = start to end
        _selectedRange.value = AnalyticsRange.CUSTOM
    }
}
