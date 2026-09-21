package com.example.moneyManagement.domain.model

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
