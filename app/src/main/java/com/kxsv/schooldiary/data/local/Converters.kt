package com.kxsv.schooldiary.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kxsv.schooldiary.data.util.Mark
import java.lang.reflect.Type
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Collections


class Converters {
	// LocalDate <-> Long
	@TypeConverter
	fun fromTimestampToLocalDate(value: Long?): LocalDate? = value?.let {
		Instant.ofEpochSecond(it).atZone(ZoneId.systemDefault()).toLocalDate()
	}
	
	@TypeConverter
	fun localDateToTimestamp(date: LocalDate?): Long? =
		date?.atStartOfDay(ZoneId.systemDefault())?.toEpochSecond()
	
	// LocalTime <-> Int
	@TypeConverter
	fun fromTimestampToLocalTime(value: Int?): LocalTime? = value?.let {
		LocalTime.ofSecondOfDay(value.toLong())
	}
	
	@TypeConverter
	fun localTimeToTimestamp(time: LocalTime?): Int? =
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
	
	// List<Mark> <-> String
	@TypeConverter
	fun listMarkToString(markList: List<Mark?>?): String? {
		return Gson().toJson(markList)
	}
	
	@TypeConverter
	fun stringToListMark(data: String?): List<Mark?>? {
		if (data == null) return Collections.emptyList()
		val listType: Type = object : TypeToken<List<Mark>?>() {}.type
		return Gson().fromJson<List<Mark>>(data, listType)
	}
	
	
	// Calendar <-> Long
	/*@TypeConverter
	fun calendarToDatestamp(calendar: Calendar): Long = calendar.timeInMillis

	@TypeConverter
	fun datestampToCalendar(value: Long): Calendar =
		Calendar.getInstance().apply { timeInMillis = value }*/
}