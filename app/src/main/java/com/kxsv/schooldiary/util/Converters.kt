package com.kxsv.schooldiary.util

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar

class Converters {
    // LocalDate <-> Long
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDate? = value?.let {
        Instant.ofEpochSecond(it).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    @TypeConverter
    fun localDateToTimestamp(date: LocalDate?): Long? =
        date?.atStartOfDay(ZoneId.systemDefault())?.toEpochSecond()

    // Calendar <-> Long
    @TypeConverter
    fun calendarToDatestamp(calendar: Calendar): Long = calendar.timeInMillis

    @TypeConverter
    fun datestampToCalendar(value: Long): Calendar =
        Calendar.getInstance().apply { timeInMillis = value }
}