package com.moneymanagement.spendwise.domain.repository

import com.moneymanagement.spendwise.domain.model.DailyTotal
import com.moneymanagement.spendwise.domain.model.Transaction
import com.moneymanagement.spendwise.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    fun getAllTransactions(): Flow<List<Transaction>>
  fun getRecentTransactions(limit: Int): Flow<List<Transaction>>
  /*

    fun getTransactionsBetween(startMillis: Long, endMillis: Long): Flow<List<Transaction>>
*/
    fun getTransactionById(id: Long): Flow<Transaction?>
  fun getTotalByType(type: TransactionType): Flow<Double>
  fun getTotalByTypeBetween(type: TransactionType, startMillis: Long, endMillis: Long): Flow<Double>
  fun getDailyTotals(type: TransactionType, startMillis: Long, endMillis: Long): Flow<List<DailyTotal>>
  /*



   fun getCategoryTotals(type: TransactionType, startMillis: Long, endMillis: Long): Flow<List<CategoryTotal>>



   fun getMonthlyTotals(type: TransactionType, monthsBack: Int): Flow<List<MonthlyTotal>>*/
  suspend fun addTransaction(transaction: Transaction): Result<Long>

    suspend fun updateTransaction(transaction: Transaction): Result<Unit>

    suspend fun deleteTransaction(transaction: Transaction): Result<Unit>

    suspend fun clearAllTransactions(): Result<Unit>

    suspend fun insertAll(transactions: List<Transaction>): Result<Int>
}
