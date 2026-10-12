package com.moneymanagement.spendwise.data.model.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * A category a transaction can belong to. [type] and [icon] are stored as plain strings
 * ("EXPENSE"/"INCOME" and an icon lookup key like "food") rather than enums/resource ids
 * so the schema never depends on app-internal enum ordinals or drawable ids - both are
 * mapped to their domain/UI equivalents in the data layer (see EntityMappers.kt and
 * CategoryIconProvider).
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "icon")
    val icon: String,
    @ColumnInfo(name = "color")
    val color: String,
    @ColumnInfo(name = "type")
    val type: String,
    @ColumnInfo(name = "is_default", defaultValue = "0")
    val isDefault: Boolean = false
)
