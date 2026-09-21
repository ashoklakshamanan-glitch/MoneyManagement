package com.example.moneyManagement.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateTimeUtils {
    private val zone: ZoneId = ZoneId.systemDefault()
    private val time24Formatter = DateTimeFormatter.ofPattern("HH:mm")
    fun todayMillis(): Long = toEpochMillis(LocalDate.now(zone))
    fun toEpochMillis(date: LocalDate): Long = date.atStartOfDay(zone).toInstant().toEpochMilli()
    fun currentTime24h(): String = LocalTime.now().format(time24Formatter)

}