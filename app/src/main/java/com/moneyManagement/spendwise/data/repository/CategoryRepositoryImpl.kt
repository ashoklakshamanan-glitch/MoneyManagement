package com.moneymanagement.spendwise.data.repository

import com.moneymanagement.spendwise.data.model.local.dao.CategoryDao
import com.moneymanagement.spendwise.data.toDomain
import com.moneymanagement.spendwise.data.toEntity
import com.moneymanagement.spendwise.di.IoDispatcher
import com.moneymanagement.spendwise.domain.model.Category
import com.moneymanagement.spendwise.domain.model.TransactionType
import com.moneymanagement.spendwise.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(private val categoryDao: CategoryDao, @IoDispatcher private val ioDispatcher: CoroutineDispatcher) :
    CategoryRepository {
    override fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { list -> list.map { it.toDomain() } }
    }

    override fun getCategoriesByType(type: TransactionType): Flow<List<Category>> =
        categoryDao.getCategoriesByType(type.name).map { list -> list.map { it.toDomain() } }


    override suspend fun getCategoryById(id: Long): Category? = categoryDao.getCategoryById(id)?.toDomain()
    override suspend fun addCategory(category: Category): Result<Long> = withContext(ioDispatcher){  runCatching { categoryDao.insert(category.toEntity()) } }

    override suspend fun updateCategory(category: Category): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getTransactionCountForCategory(categoryId: Long): Int {
        TODO("Not yet implemented")
    }

    override suspend fun deleteCategory(
        category: Category,
        reassignToCategoryId: Long?
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun clearCustomCategories(): Result<Unit> {
        TODO("Not yet implemented")
    }
}
