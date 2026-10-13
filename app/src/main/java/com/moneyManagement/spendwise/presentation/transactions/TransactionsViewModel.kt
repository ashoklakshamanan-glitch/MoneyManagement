package com.moneymanagement.spendwise.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneymanagement.spendwise.domain.model.Category
import com.moneymanagement.spendwise.domain.model.PaymentMethod
import com.moneymanagement.spendwise.domain.model.Transaction
import com.moneymanagement.spendwise.domain.model.TransactionType
import com.moneymanagement.spendwise.domain.repository.CategoryRepository
import com.moneymanagement.spendwise.domain.repository.TransactionRepository
import com.moneymanagement.spendwise.utils.DateTimeUtils
import com.moneymanagement.spendwise.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class TransactionsScreenData(
    val items: List<TransactionListItem>,
    val currencyCode: String
)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _filter = MutableStateFlow(TransactionsFilter())
    val filter: StateFlow<TransactionsFilter> = _filter.asStateFlow()

    val allCategories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val hasAnyTransactions: StateFlow<Boolean> = transactionRepository.getAllTransactions()
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val uiState: StateFlow<UiState<TransactionsScreenData>> = combine(
        transactionRepository.getAllTransactions(),
        _filter
    ) { transactions, filter ->
        val filtered = applyFilter(transactions, filter)
        Triple(filtered, filter,"INR")
    }
        .map { (items, _, currencyCode) ->
            if (items.isEmpty()) UiState.Empty
            else UiState.Success(TransactionsScreenData(items, currencyCode)) as UiState<TransactionsScreenData>
        }
        .catch { emit(UiState.Error(it.message ?: "Couldn't load transactions")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    private fun applyFilter(
        transactions: List<Transaction>,
        filter: TransactionsFilter
    ): List<TransactionListItem> {
        var result = transactions

        if (filter.query.isNotBlank()) {
            val query = filter.query.trim()
            result = result.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.category.name.contains(query, ignoreCase = true) ||
                        it.notes.contains(query, ignoreCase = true)
            }
        }
        filter.type?.let { type -> result = result.filter { it.type == type } }
        if (filter.categoryIds.isNotEmpty()) {
            result = result.filter { it.category.id in filter.categoryIds }
        }
        if (filter.paymentMethods.isNotEmpty()) {
            result = result.filter { it.paymentMethod in filter.paymentMethods }
        }
        if (filter.startDate != null && filter.endDate != null) {
            result = result.filter { it.date in filter.startDate..filter.endDate }
        }

        result = when (filter.sortOption) {
            SortOption.DATE_NEWEST -> result.sortedWith(compareByDescending<Transaction> { it.date }.thenByDescending { it.time })
            SortOption.DATE_OLDEST -> result.sortedWith(compareBy<Transaction> { it.date }.thenBy { it.time })
            SortOption.AMOUNT_HIGH -> result.sortedByDescending { it.amount }
            SortOption.AMOUNT_LOW -> result.sortedBy { it.amount }
        }

        val groupByDate =
            filter.sortOption == SortOption.DATE_NEWEST || filter.sortOption == SortOption.DATE_OLDEST
        return if (groupByDate) buildGroupedList(result) else result.map {
            TransactionListItem.Item(
                it
            )
        }
    }

    private fun buildGroupedList(transactions: List<Transaction>): List<TransactionListItem> {
        val items = mutableListOf<TransactionListItem>()
        var lastDate: Long? = null
        for (transaction in transactions) {
            if (transaction.date != lastDate) {
                items += TransactionListItem.Header(DateTimeUtils.formatDateHeader(transaction.date))
                lastDate = transaction.date
            }
            items += TransactionListItem.Item(transaction)
        }
        return items
    }
    fun onSearchQueryChanged(query: String) = _filter.update { it.copy(query = query) }
    /** True only when there are literally zero transactions in the whole database, so the
     *  UI can distinguish "nothing added yet" from "no results for this filter". */
    fun isTrulyEmptyDatabase(): Boolean = !hasAnyTransactions.value
    fun onSortOptionSelected(option: SortOption) = _filter.update { it.copy(sortOption = option) }

    fun clearFilters() = _filter.update { TransactionsFilter(query = it.query, sortOption = it.sortOption) }
    fun applyFilters(
        type: TransactionType?,
        categoryIds: Set<Long>,
        paymentMethods: Set<PaymentMethod>,
        startDate: Long?,
        endDate: Long?
    ) {
        _filter.update { it.copy(type = type, categoryIds = categoryIds, paymentMethods = paymentMethods, startDate = startDate, endDate = endDate) }
    }
}
