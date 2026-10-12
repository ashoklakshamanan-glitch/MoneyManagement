package com.moneymanagement.spendwise.utils

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateTimeUtils {
    private val zone: ZoneId = ZoneId.systemDefault()
    private val time24Formatter = DateTimeFormatter.ofPattern("HH:mm")
    private val time12Formatter = DateTimeFormatter.ofPattern("hh:mm a")
    private val dayHeaderFormatter = DateTimeFormatter.ofPattern("EEEE, MMM dd")
    private val displayDateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    fun todayMillis(): Long = toEpochMillis(LocalDate.now(zone))
    fun toEpochMillis(date: LocalDate): Long = date.atStartOfDay(zone).toInstant().toEpochMilli()
    fun toLocalDate(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    fun currentTime24h(): String = LocalTime.now().format(time24Formatter)
    fun formatTime12h(time24: String): String = try {
        LocalTime.parse(time24, time24Formatter).format(time12Formatter)
    } catch (e: Exception) {
        time24
    }

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

    fun formatDate(millis: Long): String = toLocalDate(millis)
        .format(displayDateFormatter)


    fun currentMonth(): Int = LocalDate.now(zone).monthValue

    /** Inclusive [start, end] range covering the current calendar month. */
    fun currentMonthRange(): Pair<Long, Long> = monthRange(currentMonth(), currentYear())

    fun currentYear(): Int = LocalDate.now(zone).year
    fun monthRange(month: Int, year: Int): Pair<Long, Long> {
        val start = LocalDate.of(year, month, 1)
        val end = start.withDayOfMonth(start.lengthOfMonth())
        return toEpochMillis(start) to toEpochMillis(end)
    }
    fun lastSevenDaysRange(): Pair<Long, Long> {
        val now = LocalDate.now(zone)
        return toEpochMillis(now.minusDays(6)) to toEpochMillis(
            now
        )
    }
}