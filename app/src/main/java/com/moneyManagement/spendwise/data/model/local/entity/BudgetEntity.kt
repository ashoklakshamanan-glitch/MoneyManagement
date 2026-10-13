package com.moneymanagement.spendwise.data.model.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A budget cap for a given [month]/[year]. [categoryId] is null for the single overall
 * monthly budget and non-null for a per-category budget.
 *
 * The unique index only meaningfully enforces "one budget per category per month" since
 * SQLite treats NULL as distinct across rows in a unique index, so it does not by itself
 * prevent duplicate *overall* budgets; [com.spendwise.app.data.repository.BudgetRepositoryImpl]
 * upserts the overall budget explicitly (query-then-insert-or-update) rather than relying
 * on this constraint for that case.
 */
@Entity(
    tableName = "budgets",
    indices = [Index(value = ["categoryId", "month", "year"], unique = true)]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "categoryId")
    val categoryId: Long?,
    @ColumnInfo(name = "amount")
    val amount: Double,
    @ColumnInfo(name = "month")
    val month: Int,
    @ColumnInfo(name = "year")
    val year: Int
)
