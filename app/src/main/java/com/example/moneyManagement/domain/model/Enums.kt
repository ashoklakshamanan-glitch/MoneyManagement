package com.example.moneyManagement.domain.model

enum class TransactionType {
EXPENSE,INCOME
}
/*TODO will move this to db so user can add more*/
enum class PaymentMethod(displayName: String) {
    CASH("Cash"),
    UPI("UPI"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    BANK_TRANSFER("Bank Transfer"),
    OTHER("Other");
}
