package com.moneymanagement.spendwise.utils

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Centralizes every date computation the app needs. Transactions store [date] as epoch
 * millis normalized to local midnight, so grouping/range comparisons are plain integer
 * comparisons - all the java.time work happens only at the UI/query boundary, here.
 */
object DateTimeUtils {

    private val zone: ZoneId = ZoneId.systemDefault()
    private val displayDateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    private val dayHeaderFormatter = DateTimeFormatter.ofPattern("EEEE, MMM dd")
    private val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    private val monthShortFormatter = DateTimeFormatter.ofPattern("MMM")
    private val time24Formatter = DateTimeFormatter.ofPattern("HH:mm")
    private val time12Formatter = DateTimeFormatter.ofPattern("hh:mm a")

    fun todayMillis(): Long = toEpochMillis(LocalDate.now(zone))

    fun toEpochMillis(date: LocalDate): Long = date.atStartOfDay(zone).toInstant().toEpochMilli()

    fun toLocalDate(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    fun currentMonth(): Int = LocalDate.now(zone).monthValue

    fun currentYear(): Int = LocalDate.now(zone).year

    fun formatDate(millis: Long): String = toLocalDate(millis).format(displayDateFormatter)

    /** "Today" / "Yesterday" for recent dates, otherwise a full weekday + date string. */
    fun formatDateHeader(millis: Long): String {
        val date = toLocalDate(millis)
        val today = LocalDate.now(zone)
        return when (date) {
            today -> "Today"
            today.minusDays(1) -> "Yesterday"
            else -> date.format(dayHeaderFormatter)
        }
    }

    fun formatMonthYear(month: Int, year: Int): String = LocalDate.of(year, month, 1).format(monthYearFormatter)

    fun formatMonthShort(yearMonth: String): String {
        // yearMonth is "yyyy-MM" as produced by SQLite's strftime('%Y-%m', ...)
        val parts = yearMonth.split("-")
        if (parts.size != 2) return yearMonth
        val year = parts[0].toIntOrNull() ?: return yearMonth
        val month = parts[1].toIntOrNull() ?: return yearMonth
        return LocalDate.of(year, month, 1).format(monthShortFormatter)
    }

    fun formatTime12h(time24: String): String = try {
        LocalTime.parse(time24, time24Formatter).format(time12Formatter)
    } catch (e: Exception) {
        time24
    }

    fun currentTime24h(): String = LocalTime.now().format(time24Formatter)

    /** Inclusive [start, end] range covering the current calendar month. */
    fun currentMonthRange(): Pair<Long, Long> = monthRange(currentMonth(), currentYear())

    fun monthRange(month: Int, year: Int): Pair<Long, Long> {
        val start = LocalDate.of(year, month, 1)
        val end = start.withDayOfMonth(start.lengthOfMonth())
        return toEpochMillis(start) to toEpochMillis(end)
    }

    fun lastMonthRange(): Pair<Long, Long> {
        val lastMonth = LocalDate.now(zone).minusMonths(1)
        return monthRange(lastMonth.monthValue, lastMonth.year)
    }

    fun currentYearRange(): Pair<Long, Long> {
        val now = LocalDate.now(zone)
        return toEpochMillis(now.withDayOfYear(1)) to toEpochMillis(now.withDayOfYear(now.lengthOfYear()))
    }

    /** Monday-to-Sunday range containing today. */
    fun currentWeekRange(): Pair<Long, Long> {
        val now = LocalDate.now(zone)
        val start = now.with(DayOfWeek.MONDAY)
        return toEpochMillis(start) to toEpochMillis(start.plusDays(6))
    }

    fun lastSevenDaysRange(): Pair<Long, Long> {
        val now = LocalDate.now(zone)
        return toEpochMillis(now.minusDays(6)) to toEpochMillis(now)
    }

    /** Start-of-day millis for the first day of the month [monthsBack] months before now. */
    fun monthsAgoStartMillis(monthsBack: Int): Long {
        val start = LocalDate.now(zone).minusMonths(monthsBack.toLong()).withDayOfMonth(1)
        return toEpochMillis(start)
    }
}
