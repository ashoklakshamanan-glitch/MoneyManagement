package com.moneymanagement.spendwise.domain.repository

import com.moneymanagement.spendwise.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {

    /** Per-category budgets for [month]/[year], with [Budget.spent] already computed. */
    fun getCategoryBudgetsForMonth(month: Int, year: Int): Flow<List<Budget>>

    /** The single overall budget for [month]/[year], if one has been set. */
    fun getOverallBudgetForMonth(month: Int, year: Int): Flow<Budget?>

    suspend fun setOverallBudget(amount: Double, month: Int, year: Int): Result<Unit>

    suspend fun setCategoryBudget(categoryId: Long, amount: Double, month: Int, year: Int): Result<Unit>

    suspend fun deleteBudget(budgetId: Long): Result<Unit>

    suspend fun clearAllBudgets(): Result<Unit>
}
