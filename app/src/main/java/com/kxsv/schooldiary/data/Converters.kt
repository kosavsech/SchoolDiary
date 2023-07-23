package com.kxsv.schooldiary.data

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
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
	
	// LocalDateTime <-> Long
	@TypeConverter
	fun timestampToLocalDateTime(value: Long?): LocalDateTime? {
		return value?.let {
			LocalDateTime.ofInstant(
				Instant.ofEpochMilli(it), ZoneId.systemDefault()
			)
		}
	}
	
	@TypeConverter
	fun localDateTimeToTimestamp(date: LocalDateTime?): Long? =
		date?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
	
	// Calendar <-> Long
	/*@TypeConverter
	fun calendarToDatestamp(calendar: Calendar): Long = calendar.timeInMillis

	@TypeConverter
	fun datestampToCalendar(value: Long): Calendar =
		Calendar.getInstance().apply { timeInMillis = value }*/
}