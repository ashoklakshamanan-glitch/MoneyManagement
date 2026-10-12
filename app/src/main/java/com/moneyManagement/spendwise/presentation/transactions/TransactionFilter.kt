package com.moneymanagement.spendwise.presentation.transactions

import com.moneymanagement.spendwise.domain.model.PaymentMethod
import com.moneymanagement.spendwise.domain.model.Transaction
import com.moneymanagement.spendwise.domain.model.TransactionType

enum class SortOption(val label: String) {
    DATE_NEWEST("Newest first"),
    DATE_OLDEST("Oldest first"),
    AMOUNT_HIGH("Amount: High to low"),
    AMOUNT_LOW("Amount: Low to high")
}

sealed interface TransactionListItem {
    data class Header(val label: String) :
        TransactionListItem

    data class Item(val transaction: Transaction) :
        TransactionListItem
}
data class TransactionsFilter(
    val query: String = "",
    val type: TransactionType? = null,
    val categoryIds: Set<Long> = emptySet(),
    val paymentMethods: Set<PaymentMethod> = emptySet(),
    val startDate: Long? = null,
    val endDate: Long? = null,
    val sortOption: SortOption = SortOption.DATE_NEWEST
) {
    val hasActiveFilters: Boolean
        get() = type != null || categoryIds.isNotEmpty() || paymentMethods.isNotEmpty() || startDate != null

    val activeFilterCount: Int
        get() = listOfNotNull(
            type?.let { 1 },
            categoryIds.takeIf { it.isNotEmpty() }?.let { 1 },
            paymentMethods.takeIf { it.isNotEmpty() }?.let { 1 },
            startDate?.let { 1 }
        ).sum()
}
