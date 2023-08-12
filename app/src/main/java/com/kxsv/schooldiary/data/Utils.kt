package com.kxsv.schooldiary.data

import com.kxsv.schooldiary.data.remote.grade.DayGradeDto
import com.kxsv.schooldiary.data.remote.task.TaskDto
import java.time.LocalDate
import java.time.ZoneId

object DataUtils {
	private val zoneForIdGen = ZoneId.of("Europe/Moscow")
	
	fun generateGradeId(date: LocalDate, index: Int, lessonIndex: Int): String {
		val dateStamp = date.atStartOfDay(zoneForIdGen).toEpochSecond().toString()
		val gradeIndex = index.toString()
		val lessonIndexString = lessonIndex.toString()
		return (dateStamp + "_" + gradeIndex + "_" + lessonIndexString)
	}
	
	fun DayGradeDto.generateGradeId(): String {
		return generateGradeId(date, index, lessonIndex)
	}
	
	fun TaskDto.generateUniqueTaskId(): String {
		val dateStamp: String =
			dueDate.atStartOfDay(zoneForIdGen).toEpochSecond().toString()
		val subjectId: String = subject.subjectId.toString()
		val lessonIndex: String = lessonIndex.toString()
		
		return (dateStamp + "_" + subjectId + "_" + lessonIndex)
	}
}