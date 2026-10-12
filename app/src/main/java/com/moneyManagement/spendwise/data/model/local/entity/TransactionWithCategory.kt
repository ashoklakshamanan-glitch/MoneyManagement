package com.moneymanagement.spendwise.data.model.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Relation

/**
 * The result of joining a transaction with its category, via Room's @Relation. Every
 * transaction-listing query returns this rather than a bare [TransactionEntity] so the
 * UI always has the category's name/icon/color on hand without a second query.
 */
data class TransactionWithCategory(
    @Embedded
    val transaction: TransactionEntity,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val category: CategoryEntity
)
/** Row shape for "total spent per day" queries. */
data class DailyTotalRow(
    @ColumnInfo(name = "date")
    val date: Long,
    @ColumnInfo(name = "total")
    val total: Double
)

/** Row shape for "total spent per yyyy-MM month" queries. */
data class MonthlyTotalRow(
    @ColumnInfo(name = "yearMonth")
    val yearMonth: String,
    @ColumnInfo(name = "total")
    val total: Double
)

/** Row shape for "total spent per category" queries. */
data class CategoryTotalRow(
    @Embedded
    val category: CategoryEntity,
    @ColumnInfo(name = "total")
    val total: Double
)
