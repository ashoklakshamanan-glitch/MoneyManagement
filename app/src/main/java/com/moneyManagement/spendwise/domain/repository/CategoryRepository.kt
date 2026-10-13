package com.moneymanagement.spendwise.domain.repository

import com.moneymanagement.spendwise.domain.model.Category
import com.moneymanagement.spendwise.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

        fun getAllCategories(): Flow<List<Category>>

        fun getCategoriesByType(type: TransactionType): Flow<List<Category>>

        suspend fun getCategoryById(id: Long): Category?

        suspend fun addCategory(category: Category): Result<Long>

        suspend fun updateCategory(category: Category): Result<Unit>

        suspend fun getTransactionCountForCategory(categoryId: Long): Int

        /**
         * Deletes [category]. If [reassignToCategoryId] is provided, every transaction
         * currently pointing at [category] is repointed to it first (the "safe migration"
         * path); otherwise deletion fails fast if any transactions still reference it.
         */
        suspend fun deleteCategory(category: Category, reassignToCategoryId: Long? = null): Result<Unit>

        suspend fun clearCustomCategories(): Result<Unit>
}