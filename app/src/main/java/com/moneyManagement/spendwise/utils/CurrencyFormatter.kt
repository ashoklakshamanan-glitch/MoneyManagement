package com.moneymanagement.spendwise.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val symbols = mapOf(
        "INR" to "\u20B9",
        "USD" to "$",
        "EUR" to "\u20AC",
        "GBP" to "\u00A3",
        "JPY" to "\u00A5"
    )

    fun symbolFor(currencyCode: String): String = symbols[currencyCode] ?: currencyCode
    /** e.g. format(1250.0, "INR") -> "₹1,250.00" (with Indian lakh/crore grouping). */
    fun format(amount: Double, currencyCode: String): String {
        val formattedNumber = if (currencyCode == "INR") {
            formatInr(amount)
        } else {
            val locale = Locale.US
            val numberFormat = NumberFormat.getNumberInstance(locale).apply {
                minimumFractionDigits = if (currencyCode == "JPY") 0 else 2
                maximumFractionDigits = if (currencyCode == "JPY") 0 else 2
            }
            numberFormat.format(amount)
        }
        return "${symbolFor(currencyCode)}$formattedNumber"
    }

    private fun formatInr(amount: Double): String {
        val formatted = String.format(Locale.US, "%.2f", amount)
        val parts = formatted.split(".")
        val integerPart = parts[0]
        val decimalPart = parts[1]

        if (integerPart.length <= 3) {
            return "$integerPart.$decimalPart"
        }

        val lastThree = integerPart.substring(integerPart.length - 3)
        val remaining = integerPart.substring(0, integerPart.length - 3)

        val sb = StringBuilder()
        var count = 0
        for (i in remaining.length - 1 downTo 0) {
            sb.append(remaining[i])
            count++
            if (count % 2 == 0 && i != 0) {
                sb.append(',')
            }
        }
        return "${sb.reverse()},$lastThree.$decimalPart"
    }
    fun formatSigned(amount: Double, currencyCode: String, isIncome: Boolean): String {
        val sign = if (isIncome) "+" else "-"
        return "$sign${format(amount, currencyCode)}"
    }
}