package com.moneymanagement.spendwise.data.model.local.database
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.moneymanagement.spendwise.data.model.local.dao.BudgetDao
import com.moneymanagement.spendwise.data.model.local.dao.CategoryDao
import com.moneymanagement.spendwise.data.model.local.dao.TransactionDao
import com.moneymanagement.spendwise.data.model.local.entity.BudgetEntity
import com.moneymanagement.spendwise.data.model.local.entity.CategoryEntity
import com.moneymanagement.spendwise.data.model.local.entity.TransactionEntity
import com.moneymanagement.spendwise.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [TransactionEntity::class, CategoryEntity::class, BudgetEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao

    abstract fun budgetDao(): BudgetDao
}
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `budgets` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `categoryId` INTEGER,
                `amount` REAL NOT NULL,
                `month` INTEGER NOT NULL,
                `year` INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS
            `index_budgets_categoryId_month_year`
            ON `budgets` (`categoryId`, `month`, `year`)
            """.trimIndent()
        )
    }
}
class DatabaseCallback @Inject constructor(
    private val categoryDaoProvider: Provider<CategoryDao>,
    @param:ApplicationScope private val applicationScope: CoroutineScope
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        applicationScope.launch {
            categoryDaoProvider.get().insertAll(DefaultCategories.list)
        }
    }
}