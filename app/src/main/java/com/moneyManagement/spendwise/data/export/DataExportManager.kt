package com.moneymanagement.spendwise.data.export

import com.moneymanagement.spendwise.domain.model.PaymentMethod
import com.moneymanagement.spendwise.domain.model.Transaction
import com.moneymanagement.spendwise.domain.model.TransactionType
import com.moneymanagement.spendwise.utils.DateTimeUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** A row read back from an export file, before it has been matched/created against the
 *  user's current categories - see SettingsViewModel for how these become real Transactions. */
data class ImportedTransactionRow(
    val date: Long,
    val time: String,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val categoryName: String,
    val paymentMethod: PaymentMethod,
    val notes: String
)

enum class ExportFormat { CSV, JSON }

@Singleton
class DataExportManager @Inject constructor() {

    private val csvHeader = listOf("Date", "Title", "Amount", "Type", "Category", "Payment Method", "Notes")

    // ---------------------------------------------------------------- export

    fun exportToCsv(transactions: List<Transaction>, outputStream: OutputStream) {
        outputStream.bufferedWriter().use { writer ->
            writer.write(csvHeader.joinToString(",") { csvEscape(it) })
            writer.newLine()
            for (t in transactions) {
                val row = listOf(
                    DateTimeUtils.formatDate(t.date),
                    t.title,
                    t.amount.toString(),
                    t.type.name,
                    t.category.name,
                    t.paymentMethod.displayName,
                    t.notes
                )
                writer.write(row.joinToString(",") { csvEscape(it) })
                writer.newLine()
            }
        }
    }

    fun exportToJson(transactions: List<Transaction>, outputStream: OutputStream) {
        val array = JSONArray()
        for (t in transactions) {
            val obj = JSONObject()
            obj.put("date", DateTimeUtils.formatDate(t.date))
            obj.put("dateMillis", t.date)
            obj.put("time", t.time)
            obj.put("title", t.title)
            obj.put("amount", t.amount)
            obj.put("type", t.type.name)
            obj.put("category", t.category.name)
            obj.put("paymentMethod", t.paymentMethod.displayName)
            obj.put("notes", t.notes)
            array.put(obj)
        }
        outputStream.bufferedWriter().use { it.write(array.toString(2)) }
    }

    private fun csvEscape(field: String): String {
        val needsQuoting = field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")
        return if (needsQuoting) "\"${field.replace("\"", "\"\"")}\"" else field
    }

    // ---------------------------------------------------------------- import

    fun importFromCsv(inputStream: InputStream): List<ImportedTransactionRow> {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val rows = mutableListOf<ImportedTransactionRow>()
        var isHeader = true
        reader.forEachLine { line ->
            if (line.isBlank()) return@forEachLine
            if (isHeader) {
                isHeader = false
                return@forEachLine
            }
            val fields = parseCsvLine(line)
            if (fields.size < 7) return@forEachLine
            runCatching {
                rows += ImportedTransactionRow(
                    date = parseFlexibleDate(fields[0]),
                    title = fields[1],
                    amount = fields[2].toDouble(),
                    type = runCatching { TransactionType.valueOf(fields[3].trim().uppercase()) }
                        .getOrDefault(TransactionType.EXPENSE),
                    categoryName = fields[4],
                    paymentMethod = PaymentMethod.fromDisplayName(fields[5]),
                    notes = fields[6],
                    time = DateTimeUtils.currentTime24h()
                )
            }
        }
        return rows
    }

    fun importFromJson(inputStream: InputStream): List<ImportedTransactionRow> {
        val text = inputStream.bufferedReader().use { it.readText() }
        val array = JSONArray(text)
        val rows = mutableListOf<ImportedTransactionRow>()
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            runCatching {
                val dateMillis = if (obj.has("dateMillis")) {
                    obj.getLong("dateMillis")
                } else {
                    parseFlexibleDate(obj.optString("date"))
                }
                rows += ImportedTransactionRow(
                    date = dateMillis,
                    time = obj.optString("time").ifBlank { DateTimeUtils.currentTime24h() },
                    title = obj.optString("title"),
                    amount = obj.optDouble("amount", 0.0),
                    type = runCatching { TransactionType.valueOf(obj.optString("type").uppercase()) }
                        .getOrDefault(TransactionType.EXPENSE),
                    categoryName = obj.optString("category"),
                    paymentMethod = PaymentMethod.fromDisplayName(obj.optString("paymentMethod")),
                    notes = obj.optString("notes")
                )
            }
        }
        return rows
    }

    /** Splits a single CSV line into fields, honouring RFC4180 quoting/escaping. */
    private fun parseCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                inQuotes && c == '"' && i + 1 < line.length && line[i + 1] == '"' -> {
                    current.append('"')
                    i++
                }
                c == '"' -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> {
                    fields += current.toString()
                    current.clear()
                }
                else -> current.append(c)
            }
            i++
        }
        fields += current.toString()
        return fields
    }

    private fun parseFlexibleDate(value: String): Long = runCatching {
        val parts = value.trim().split(" ")[0].split("-", "/")
        if (parts.size == 3 && parts[0].length == 4) {
            // yyyy-MM-dd
            DateTimeUtils.toEpochMillis(LocalDate.of(parts[0].toInt(), parts[1].toInt(), parts[2].toInt()))
        } else {
            DateTimeUtils.todayMillis()
        }
    }.getOrDefault(DateTimeUtils.todayMillis())
}
