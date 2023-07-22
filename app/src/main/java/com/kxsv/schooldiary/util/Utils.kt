package com.kxsv.schooldiary.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object Utils {
	fun timestampToLocalDate(value: Long?): LocalDate? = value?.let {
		Instant.ofEpochSecond(it).atZone(ZoneId.of("Europe/Moscow")).toLocalDate()
	}
	
	fun localDateToTimestamp(date: LocalDate?): Long? =
		date?.atStartOfDay(ZoneId.of("Europe/Moscow"))?.toEpochSecond()
}