package com.kxsv.schooldiary.data

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId


class Converters {
	// LocalDate <-> Long
	@TypeConverter
	fun timestampToLocalDate(value: Long?): LocalDate? = value?.let {
		Instant.ofEpochSecond(it).atZone(ZoneId.systemDefault()).toLocalDate()
	}
	
	@TypeConverter
	fun localDateToTimestamp(date: LocalDate?): Long? =
		date?.atStartOfDay(ZoneId.systemDefault())?.toEpochSecond()
	
	// LocalTime <-> Int
	@TypeConverter
	fun secondOfDayToLocalTime(value: Int?): LocalTime? = value?.let {
		LocalTime.ofSecondOfDay(value.toLong())
	}
	
	@TypeConverter
	fun localTimeToSecondOfDay(time: LocalTime?): Int? =
		time?.toSecondOfDay()
	
	// Calendar <-> Long
	/*@TypeConverter
	fun calendarToDatestamp(calendar: Calendar): Long = calendar.timeInMillis

	@TypeConverter
	fun datestampToCalendar(value: Long): Calendar =
		Calendar.getInstance().apply { timeInMillis = value }*/
}