package com.moneymanagement.spendwise.data

import com.moneymanagement.spendwise.data.model.local.entity.CategoryEntity
import com.moneymanagement.spendwise.data.model.local.entity.CategoryTotalRow
import com.moneymanagement.spendwise.data.model.local.entity.DailyTotalRow
import com.moneymanagement.spendwise.data.model.local.entity.MonthlyTotalRow
import com.moneymanagement.spendwise.data.model.local.entity.TransactionEntity
import com.moneymanagement.spendwise.data.model.local.entity.TransactionWithCategory
import com.moneymanagement.spendwise.domain.model.Category
import com.moneymanagement.spendwise.domain.model.CategoryTotal
import com.moneymanagement.spendwise.domain.model.DailyTotal
import com.moneymanagement.spendwise.domain.model.MonthlyTotal
import com.moneymanagement.spendwise.domain.model.PaymentMethod
import com.moneymanagement.spendwise.domain.model.Transaction
import com.moneymanagement.spendwise.domain.model.TransactionType

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    title = title,
    amount = amount,
    type = type.name,
    categoryId = category.id,
    date = date,
    time = time,
    notes = notes,
    paymentMethod = paymentMethod.name,
    createdAt = createdAt,
    updatedAt = updatedAt
)
fun TransactionWithCategory.toDomain(): Transaction = Transaction(
    id = transaction.id,
    title = transaction.title,
    amount = transaction.amount,
    type = TransactionType.valueOf(transaction.type),
    category = category.toDomain(),
    date = transaction.date,
    time = transaction.time,
    notes = transaction.notes,
    paymentMethod = PaymentMethod.entries.find { it.name == transaction.paymentMethod }
        ?: PaymentMethod.OTHER,
    createdAt = transaction.createdAt,
    updatedAt = transaction.updatedAt
)
fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    icon = icon,
    color = color,
    type = TransactionType.valueOf(type),
    isDefault = isDefault
)
fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    icon = icon,
    color = color,
    type = type.name,
    isDefault = isDefault
)
fun DailyTotalRow.toDomain(): DailyTotal = DailyTotal(dateMillis = date, total = total)
fun CategoryTotalRow.toDomain(): CategoryTotal = CategoryTotal(
    category = category.toDomain(),
    total = total
)

fun MonthlyTotalRow.toDomain(): MonthlyTotal = MonthlyTotal(yearMonth = yearMonth, total = total)
