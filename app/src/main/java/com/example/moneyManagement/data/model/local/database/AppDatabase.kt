package com.example.moneyManagement.data.model.local.database
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.moneyManagement.data.model.local.dao.TransactionDao
import com.example.moneyManagement.data.model.local.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
}