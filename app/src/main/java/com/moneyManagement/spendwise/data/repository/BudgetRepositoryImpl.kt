package com.moneymanagement.spendwise.data.repository

import com.moneymanagement.spendwise.data.model.local.dao.BudgetDao
import com.moneymanagement.spendwise.data.model.local.dao.CategoryDao
import com.moneymanagement.spendwise.data.model.local.dao.TransactionDao
import com.moneymanagement.spendwise.data.model.local.entity.BudgetEntity
import com.moneymanagement.spendwise.di.IoDispatcher
import com.moneymanagement.spendwise.domain.model.Budget
import com.moneymanagement.spendwise.domain.model.TransactionType
import com.moneymanagement.spendwise.domain.repository.BudgetRepository
import com.moneymanagement.spendwise.utils.DateTimeUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BudgetRepository {

    override fun getCategoryBudgetsForMonth(month: Int, year: Int): Flow<List<Budget>> {
        val (start, end) = DateTimeUtils.monthRange(month, year)
        return combine(
            budgetDao.getCategoryBudgetsForMonth(month, year),
            transactionDao.getCategoryTotals(TransactionType.EXPENSE.name, start, end),
            categoryDao.getAllCategories()
        ) { budgetEntities, categoryTotalRows, allCategories ->
            val spentByCategoryId = categoryTotalRows.associate { it.category.id to it.total }
            val categoryById = allCategories.associateBy { it.id }
            budgetEntities.mapNotNull { budgetEntity ->
                val categoryId = budgetEntity.categoryId ?: return@mapNotNull null
                val category = categoryById[categoryId] ?: return@mapNotNull null
                Budget(
                    id = budgetEntity.id,
                    categoryId = categoryId,
                    categoryName = category.name,
                    categoryColor = category.color,
                    categoryIcon = category.icon,
                    amount = budgetEntity.amount,
                    month = budgetEntity.month,
                    year = budgetEntity.year,
                    spent = spentByCategoryId[categoryId] ?: 0.0
                )
            }.sortedByDescending { it.percentageUsed }
        }
    }

    override fun getOverallBudgetForMonth(month: Int, year: Int): Flow<Budget?> {
        val (start, end) = DateTimeUtils.monthRange(month, year)
        return combine(
            budgetDao.getOverallBudgetForMonth(month, year),
            transactionDao.getTotalByTypeBetween(TransactionType.EXPENSE.name, start, end)
        ) { budgetEntity, totalSpent ->
            budgetEntity?.let {
                Budget(
                    id = it.id,
                    categoryId = null,
                    amount = it.amount,
                    month = it.month,
                    year = it.year,
                    spent = totalSpent
                )
            }
        }
    }

    override suspend fun setOverallBudget(amount: Double, month: Int, year: Int): Result<Unit> =
        withContext(ioDispatcher) {
            runCatching {
                val existing = budgetDao.findOverallBudgetForMonthOnce(month, year)
                if (existing != null) {
                    budgetDao.update(existing.copy(amount = amount))
                } else {
                    budgetDao.insert(
                        BudgetEntity(
                            categoryId = null,
                            amount = amount,
                            month = month,
                            year = year
                        )
                    )
                }
                Unit
            }
        }

    override suspend fun setCategoryBudget(categoryId: Long, amount: Double, month: Int, year: Int): Result<Unit> =
        withContext(ioDispatcher) {
            runCatching {
                budgetDao.upsertCategoryBudget(
                    BudgetEntity(
                        categoryId = categoryId,
                        amount = amount,
                        month = month,
                        year = year
                    )
                )
                Unit
            }
        }

    override suspend fun deleteBudget(budgetId: Long): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            budgetDao.deleteById(budgetId)
            Unit
        }
    }

    override suspend fun clearAllBudgets(): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            budgetDao.deleteAll()
            Unit
        }
    }
}
