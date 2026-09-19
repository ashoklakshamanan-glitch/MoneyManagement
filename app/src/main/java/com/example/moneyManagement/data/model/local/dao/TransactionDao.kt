package com.example.moneyManagement.data.model.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.moneyManagement.data.model.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert
    suspend fun insertAll(transactions: List<TransactionEntity>): List<Long>

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

   /* @Transaction
    @Query("SELECT * FROM transactions ORDER BY date DESC, time DESC, id DESC")
    fun getAllTransactions(): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY date DESC, time DESC, id DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE date BETWEEN :startMillis AND :endMillis ORDER BY date DESC, time DESC, id DESC")
    fun getTransactionsBetween(startMillis: Long, endMillis: Long): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    fun getTransactionById(id: Long): Flow<TransactionWithCategory?>*/

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = :type")
    fun getTotalByType(type: String): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = :type AND date BETWEEN :startMillis AND :endMillis")
    fun getTotalByTypeBetween(type: String, startMillis: Long, endMillis: Long): Flow<Double>

  /*  @Query(
        """
        SELECT categories.*, SUM(transactions.amount) as total
        FROM transactions
        INNER JOIN categories ON transactions.categoryId = categories.id
        WHERE transactions.type = :type AND transactions.date BETWEEN :startMillis AND :endMillis
        GROUP BY categories.id
        ORDER BY total DESC
        """
    )
    fun getCategoryTotals(type: String, startMillis: Long, endMillis: Long): Flow<List<CategoryTotalRow>>

    @Query(
        """
        SELECT date, SUM(amount) as total
        FROM transactions
        WHERE type = :type AND date BETWEEN :startMillis AND :endMillis
        GROUP BY date
        ORDER BY date ASC
        """
    )
    fun getDailyTotals(type: String, startMillis: Long, endMillis: Long): Flow<List<DailyTotalRow>>

    @Query(
        """
        SELECT strftime('%Y-%m', date / 1000, 'unixepoch') as yearMonth, SUM(amount) as total
        FROM transactions
        WHERE type = :type AND date >= :sinceMillis
        GROUP BY yearMonth
        ORDER BY yearMonth ASC
        """
    )
    fun getMonthlyTotals(type: String, sinceMillis: Long): Flow<List<MonthlyTotalRow>>*/

    @Query("SELECT COUNT(*) FROM transactions WHERE categoryId = :categoryId")
    suspend fun getTransactionCountForCategory(categoryId: Long): Int

    @Query("UPDATE transactions SET categoryId = :newCategoryId WHERE categoryId = :oldCategoryId")
    suspend fun reassignCategory(oldCategoryId: Long, newCategoryId: Long)
}