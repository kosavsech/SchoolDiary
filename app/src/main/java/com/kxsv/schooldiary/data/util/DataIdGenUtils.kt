package com.kxsv.schooldiary.data.util

import com.kxsv.schooldiary.data.remote.dtos.DayGradeDto
import com.kxsv.schooldiary.data.remote.dtos.EduPerformanceDto
import com.kxsv.schooldiary.data.remote.dtos.TaskDto
import java.time.LocalDate
import java.time.ZoneId

object DataIdGenUtils {
	private val zoneForIdGen = ZoneId.of("Europe/Moscow")
	
	fun generateGradeId(date: LocalDate, index: Int, lessonIndex: Int): String {
		val dateStamp = date.atStartOfDay(zoneForIdGen).toEpochSecond().toString()
		val gradeIndex = index.toString()
		val lessonIndexString = lessonIndex.toString()
		return (dateStamp + "_" + gradeIndex + "_" + lessonIndexString)
	}
	
	fun generateTeacherId(teacherFullName: String): String {
		return teacherFullName.trim().lowercase().replace(" ", "_")
	}
	
	fun generateSubjectId(subjectFullName: String): String {
		return subjectFullName.trim().lowercase().replace(" ", "_")
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
	
	fun EduPerformanceDto.generateEduPerformanceId(): String {
		return (subjectAncestorName + "_" + period.toString())
	}
}