package com.kxsv.schooldiary.data

import java.time.LocalDate
import java.time.ZoneId

object DataUtils {
	fun generateGradeId(date: LocalDate, index: Int, lessonIndex: Int): String {
		val dateStamp = date.atStartOfDay(ZoneId.of("Europe/Moscow")).toEpochSecond().toString()
		val gradeIndex = index.toString()
		val lessonIndexString = lessonIndex.toString()
		return (dateStamp + "_" + gradeIndex + "_" + lessonIndexString)
	}
}