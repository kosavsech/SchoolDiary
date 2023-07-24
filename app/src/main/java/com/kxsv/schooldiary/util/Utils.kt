package com.kxsv.schooldiary.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

private const val TAG = "Utils"

object Utils {
	fun timestampToLocalDate(value: Long?): LocalDate? = value?.let {
		Instant.ofEpochSecond(it).atZone(ZoneId.of("Europe/Moscow")).toLocalDate()
	}
	
	fun localDateToTimestamp(date: LocalDate?): Long? =
		date?.atStartOfDay(ZoneId.of("Europe/Moscow"))?.toEpochSecond()
	
	fun Double.stringRoundTo(n: Int): String {
		return String.format("%.${n}f", this, Locale.ENGLISH)
	}
}