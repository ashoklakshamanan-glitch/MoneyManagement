package com.example.moneyManagement.data

import com.example.moneyManagement.data.model.local.entity.TransactionEntity
import com.example.moneyManagement.domain.model.Transaction

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    title = title,
    amount = amount,
    type = "",
    categoryId =0,
    date = date,
    time = time,
    notes = notes,
    paymentMethod ="",
    createdAt = createdAt,
    updatedAt = updatedAt
)