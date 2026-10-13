package com.moneymanagement.spendwise.domain.model

enum class TransactionType {
    EXPENSE, INCOME
}

/*TODO will move this to db so user can add more*/
enum class PaymentMethod(val displayName: String) {
    CASH("Cash"),
    UPI("UPI"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    BANK_TRANSFER("Bank Transfer"),
    OTHER("Other");

    companion object {
        fun fromDisplayName(name: String): PaymentMethod =
            PaymentMethod.entries.find { it.displayName.equals(name, ignoreCase = true) }
                ?: PaymentMethod.OTHER
    }
}
