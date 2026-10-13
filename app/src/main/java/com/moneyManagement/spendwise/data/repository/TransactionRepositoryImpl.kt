package com.moneymanagement.spendwise.data.repository

import com.moneymanagement.spendwise.data.model.local.dao.TransactionDao
import com.moneymanagement.spendwise.data.toDomain
import com.moneymanagement.spendwise.data.toEntity
import com.moneymanagement.spendwise.di.IoDispatcher
import com.moneymanagement.spendwise.domain.model.CategoryTotal
import com.moneymanagement.spendwise.domain.model.DailyTotal
import com.moneymanagement.spendwise.domain.model.MonthlyTotal
import com.moneymanagement.spendwise.domain.model.Transaction
import com.moneymanagement.spendwise.domain.model.TransactionType
import com.moneymanagement.spendwise.domain.repository.TransactionRepository
import com.moneymanagement.spendwise.utils.DateTimeUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : TransactionRepository {
    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { list -> list.map { it.toDomain() } }
    }

    override fun getRecentTransactions(limit: Int) =
        transactionDao.getRecentTransactions(limit).map { list -> list.map { it.toDomain() } }



    override fun getTotalByType(type: TransactionType) = transactionDao.getTotalByType(type.name)

    override fun getTotalByTypeBetween(
        type: TransactionType,
        startMillis: Long,
        endMillis: Long
    ) = transactionDao.getTotalByTypeBetween(type.name, startMillis, endMillis)

    override fun getDailyTotals(type: TransactionType, startMillis: Long, endMillis: Long): Flow<List<DailyTotal>> =
        transactionDao.getDailyTotals(type.name, startMillis, endMillis).map { list -> list.map { it.toDomain() } }

    override suspend fun addTransaction(transaction: Transaction): Result<Long> =
        withContext(ioDispatcher) {
            runCatching { transactionDao.insert(transaction.toEntity()) }
        }


    override suspend fun updateTransaction(transaction: Transaction): Result<Unit> = withContext(
        Dispatchers.IO
    ) {
        runCatching { transactionDao.update(transaction.toEntity()) }
    }

    override suspend fun deleteTransaction(transaction: Transaction): Result<Unit> = withContext(
        Dispatchers.IO
    ) {
        runCatching { transactionDao.delete(transaction.toEntity()) }
    }

    override suspend fun clearAllTransactions(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching { transactionDao.deleteAll() }
    }

    override suspend fun insertAll(transactions: List<Transaction>): Result<Int> =
        withContext(Dispatchers.IO) {
            runCatching { transactionDao.insertAll(transactions.map { it.toEntity() }).size }
        }


      fun getTransactionsBetween(startMillis: Long, endMillis: Long): Flow<List<Transaction>> =
         transactionDao.getTransactionsBetween(startMillis, endMillis).map { list -> list.map { it.toDomain() } }

     override fun getTransactionById(id: Long): Flow<Transaction?> =
         transactionDao.getTransactionById(id).map { it?.toDomain() }



     override fun getCategoryTotals(type: TransactionType, startMillis: Long, endMillis: Long): Flow<List<CategoryTotal>> =
         transactionDao.getCategoryTotals(type.name, startMillis, endMillis).map { list -> list.map { it.toDomain() } }

     override fun getMonthlyTotals(type: TransactionType, monthsBack: Int): Flow<List<MonthlyTotal>> {
         val sinceMillis = DateTimeUtils.monthsAgoStartMillis(monthsBack)
         return transactionDao.getMonthlyTotals(type.name, sinceMillis).map { list -> list.map { it.toDomain() } }
     }

}