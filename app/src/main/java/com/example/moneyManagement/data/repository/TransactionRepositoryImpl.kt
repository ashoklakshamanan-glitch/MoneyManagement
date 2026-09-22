package com.example.moneyManagement.data.repository

import com.example.moneyManagement.data.model.local.dao.TransactionDao
import com.example.moneyManagement.data.toEntity
import com.example.moneyManagement.di.IoDispatcher
import com.example.moneyManagement.domain.model.Transaction
import com.example.moneyManagement.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.text.insert

class TransactionRepositoryImpl @Inject constructor (private val transactionDao: TransactionDao, @IoDispatcher private val ioDispatcher: CoroutineDispatcher) : TransactionRepository {
    override suspend fun addTransaction(transaction: Transaction): Result<Long> = withContext(ioDispatcher) {
        runCatching { transactionDao.insert(transaction.toEntity()) }
    }


    override suspend fun updateTransaction(transaction: Transaction): Result<Unit> =withContext(
        Dispatchers.IO){
        runCatching { transactionDao.update(transaction.toEntity()) }
    }

    override suspend fun deleteTransaction(transaction: Transaction): Result<Unit> = withContext(
        Dispatchers.IO){
        runCatching { transactionDao.delete(transaction.toEntity()) }
    }

    override suspend fun clearAllTransactions(): Result<Unit> = withContext(Dispatchers.IO){
        runCatching { transactionDao.deleteAll() }
    }

    override suspend fun insertAll(transactions: List<Transaction>): Result<Int> = withContext(Dispatchers.IO){
        runCatching { transactionDao.insertAll(transactions.map { it.toEntity() }).size }
    }


   /* override fun getAllTransactions(): Flow<List<Transaction>> =
        transactionDao.getAllTransactions().map { list -> list.map { it.toDomain() } }

    override fun getRecentTransactions(limit: Int): Flow<List<Transaction>> =
        transactionDao.getRecentTransactions(limit).map { list -> list.map { it.toDomain() } }

    override fun getTransactionsBetween(startMillis: Long, endMillis: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsBetween(startMillis, endMillis).map { list -> list.map { it.toDomain() } }

    override fun getTransactionById(id: Long): Flow<Transaction?> =
        transactionDao.getTransactionById(id).map { it?.toDomain() }

    override fun getTotalByType(type: TransactionType): Flow<Double> =
        transactionDao.getTotalByType(type.name)

    override fun getTotalByTypeBetween(type: TransactionType, startMillis: Long, endMillis: Long): Flow<Double> =
        transactionDao.getTotalByTypeBetween(type.name, startMillis, endMillis)

    override fun getCategoryTotals(type: TransactionType, startMillis: Long, endMillis: Long): Flow<List<CategoryTotal>> =
        transactionDao.getCategoryTotals(type.name, startMillis, endMillis).map { list -> list.map { it.toDomain() } }

    override fun getDailyTotals(type: TransactionType, startMillis: Long, endMillis: Long): Flow<List<DailyTotal>> =
        transactionDao.getDailyTotals(type.name, startMillis, endMillis).map { list -> list.map { it.toDomain() } }

    override fun getMonthlyTotals(type: TransactionType, monthsBack: Int): Flow<List<MonthlyTotal>> {
        val sinceMillis = com.spendwise.app.utils.DateTimeUtils.monthsAgoStartMillis(monthsBack)
        return transactionDao.getMonthlyTotals(type.name, sinceMillis).map { list -> list.map { it.toDomain() } }
    }
    */

}