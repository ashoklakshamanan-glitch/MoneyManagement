package com.moneymanagement.spendwise.domain.model

/**
 * A single expense or income entry. Carries the full [Category] (not just an id) since
 * almost every screen that lists transactions needs the category's name/icon/color
 * immediately - see [com.spendwise.app.data.local.entity.TransactionWithCategory] for
 * how the join is performed at the Room layer.
 */
data class Transaction(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val date: Long,
    val time: String,
    val notes: String,
    val paymentMethod: PaymentMethod,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * A spending or income bucket. [icon] is a lookup key resolved via
 * [com.spendwise.app.utils.CategoryIconProvider], not a resource id directly, so the
 * database never stores values that could change across app versions.
 */
data class Category(
    val id: Long = 0,
    val name: String,
    val icon: String,
    val color: String,
    val type: TransactionType,
    val isDefault: Boolean = false
)

/**
 * A budget for a given [month]/[year]. [categoryId] is null for the single overall
 * monthly budget, and set for a per-category budget. [spent] is populated by the
 * repository (joined against actual transaction totals) - it is never persisted itself.
 */
data class Budget(
    val id: Long = 0,
    val categoryId: Long?,
    val categoryName: String? = null,
    val categoryColor: String? = null,
    val categoryIcon: String? = null,
    val amount: Double,
    val month: Int,
    val year: Int,
    val spent: Double = 0.0
) {
    val remaining: Double get() = amount - spent

    val percentageUsed: Int get() = if (amount <= 0) 0 else ((spent / amount) * 100).toInt().coerceAtLeast(0)

    val isOverBudget: Boolean get() = spent > amount

    val isApproachingLimit: Boolean get() = !isOverBudget && percentageUsed >= BUDGET_WARNING_THRESHOLD_PERCENT

    companion object {
        const val BUDGET_WARNING_THRESHOLD_PERCENT = 90
    }
}
/** Aggregate row: total spent on a single calendar day. */
data class DailyTotal(
    val dateMillis: Long,
    val total: Double
)


/** Aggregate row: total spent in a given "yyyy-MM" month bucket. */
data class MonthlyTotal(
    val yearMonth: String,
    val total: Double
)

/** Aggregate row: total spent per category over some date range. */
data class CategoryTotal(
    val category: Category,
    val total: Double
)
