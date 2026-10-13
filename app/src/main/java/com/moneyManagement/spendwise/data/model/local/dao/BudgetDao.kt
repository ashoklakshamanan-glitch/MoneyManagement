package com.moneymanagement.spendwise.data.model.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.moneymanagement.spendwise.data.model.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    /** Per-category budgets only use REPLACE-on-conflict safely because the unique index
     *  (categoryId, month, year) only fires for non-null categoryId rows. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategoryBudget(budget: BudgetEntity): Long

    @Insert
    suspend fun insert(budget: BudgetEntity): Long

    @Update
    suspend fun update(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :budgetId")
    suspend fun deleteById(budgetId: Long)

    @Query("DELETE FROM budgets")
    suspend fun deleteAll()

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year AND categoryId IS NOT NULL")
    fun getCategoryBudgetsForMonth(month: Int, year: Int): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year AND categoryId IS NULL LIMIT 1")
    fun getOverallBudgetForMonth(month: Int, year: Int): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year AND categoryId IS NULL LIMIT 1")
    suspend fun findOverallBudgetForMonthOnce(month: Int, year: Int): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND month = :month AND year = :year LIMIT 1")
    suspend fun findCategoryBudget(categoryId: Long, month: Int, year: Int): BudgetEntity?
}
