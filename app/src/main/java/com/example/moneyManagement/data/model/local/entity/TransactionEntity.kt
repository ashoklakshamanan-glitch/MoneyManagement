package com.example.moneyManagement.data.model.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
/**
 * A single expense or income entry.
 *
 * Dates are intentionally split into [date] (epoch millis normalized to local midnight -
 * used for grouping/range queries) and [time] ("HH:mm" - used only for display), matching
 * the two separate fields the product spec calls for and avoiding any need for a Room
 * TypeConverter: every column here is already a type Room supports natively.
 *
 * The FK uses [ForeignKey.RESTRICT] as a database-level safety net; the app layer
 * ([com.eample.app.data.repository.CategoryRepositoryImpl]) proactively checks for
 * in-use categories and offers to reassign transactions *before* a delete would ever
 * reach this constraint.
 */
@Entity(tableName = "transactions")
data class TransactionEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "categoryId")
    val categoryId: Long,

    @ColumnInfo(name = "date")
    val date: Long,

    @ColumnInfo(name = "time")
    val time: String,

    @ColumnInfo(name = "notes", defaultValue = "")
    val notes: String = "",

    @ColumnInfo(name = "paymentMethod")
    val paymentMethod: String,

    @ColumnInfo(name = "createdAt")
    val createdAt: Long,

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Long
)