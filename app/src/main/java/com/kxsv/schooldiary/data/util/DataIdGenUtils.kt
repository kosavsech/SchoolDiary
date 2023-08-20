package com.kxsv.schooldiary.data.util

import com.kxsv.schooldiary.data.remote.dtos.DayGradeDto
import com.kxsv.schooldiary.data.remote.dtos.EduPerformanceDto
import java.time.LocalDate
import java.time.ZoneId

object DataIdGenUtils {
	private val zoneForIdGen = ZoneId.of("Europe/Moscow")
	
	/**
	 * Generate based on grade date, index(e.g could be 2 grades for one lesson),
	 * lesson index(e.g could be 2 same subjects but different timings), subjectId
	 *
	 * @return gradeId
	 */
	fun generateGradeId(
		date: LocalDate,
		index: Int,
		lessonIndex: Int,
		subjectFullName: String,
	): String {
		val dateStamp = date.atStartOfDay(zoneForIdGen).toEpochSecond().toString()
		val gradeIndex = index.toString()
		val lessonIndexString = lessonIndex.toString()
		val subjectId =
			if (subjectFullName.contains("_")) subjectFullName
			else generateSubjectId(subjectFullName)
		
		return (dateStamp + "_" + gradeIndex + "_" + lessonIndexString + "_" + subjectId)
	}
	
	/**
	 * Generate based on grade date, index(e.g could be 2 grades for one lesson),
	 * lesson index(e.g could be 2 same subjects but different timings), subjectId
	 *
	 * @return gradeId
	 */
	fun DayGradeDto.generateId(): String {
		return generateGradeId(
			date = date,
			index = index,
			lessonIndex = lessonIndex,
			subjectFullName = subjectAncestorFullName
		)
	}
	
	fun generateTeacherId(teacherFullName: String): String {
		return teacherFullName.trim().lowercase().replace(" ", "_")
	}
	
	fun generateSubjectId(subjectFullName: String): String {
		return subjectFullName.trim().lowercase().replace(" ", "_")
	}
	
	fun generateTaskId(
		dueDate: LocalDate,
		subjectId: String,
		lessonIndex: Int,
	): String {
		val dueDateStamp = dueDate.atStartOfDay(zoneForIdGen).toEpochSecond()
		
		return ("${lessonIndex}_${subjectId}_${dueDateStamp}")
	}
	
	fun EduPerformanceDto.generateEduPerformanceId(): String {
		return (generateSubjectId(subjectAncestorName) + "_" + period.name)
	}
}