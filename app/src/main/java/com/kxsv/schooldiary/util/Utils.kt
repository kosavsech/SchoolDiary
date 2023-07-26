package com.kxsv.schooldiary.util

import com.kxsv.schooldiary.data.local.features.teacher.TeacherEntity
import com.kxsv.schooldiary.util.ui.EduPerformancePeriod
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale


object Utils {
	fun timestampToLocalDate(value: Long?): LocalDate? = value?.let {
		Instant.ofEpochSecond(it).atZone(ZoneId.of("Europe/Moscow")).toLocalDate()
	}
	
	fun localDateToTimestamp(date: LocalDate?): Long? =
		date?.atStartOfDay(ZoneId.of("Europe/Moscow"))?.toEpochSecond()
	
	fun Double.stringRoundTo(n: Int): String {
		return String.format("%.${n}f", this, Locale.ENGLISH)
	}
	
	fun Double.roundTo(n: Int): Double {
		return String.format("%.${n}f", this, Locale.ENGLISH).toDouble()
	}
	
	/**
	 * Return teacher full name. Variants:
	 * 1) try to get L.N. Patronymic
	 * 2) if Patronymic isNotEmpty N. Patronymic / L. Patronymic / Patronymic
	 * 3) if firstName isNotEmpty  N. Lastname / Name
	 * 4) Lastname
	 *
	 * @param teacher
	 * @return [teacher full name][String]
	 */
	fun fullNameOf(teacher: TeacherEntity): String {
		return if (teacher.lastName.isNotEmpty() && teacher.firstName.isNotEmpty() && teacher.patronymic.isNotEmpty()) {
			teacher.firstName[0] + "." + teacher.lastName[0] + ". " + teacher.patronymic
		} else if (teacher.patronymic.isNotEmpty()) {
			if (teacher.firstName.isNotEmpty()) {
				teacher.firstName[0] + "." + teacher.patronymic
			} else if (teacher.lastName.isNotEmpty()) {
				teacher.lastName[0] + "." + teacher.patronymic
			} else {
				teacher.patronymic
			}
		} else if (teacher.firstName.isNotEmpty()) {
			if (teacher.lastName.isNotEmpty()) {
				teacher.firstName[0] + "." + teacher.lastName
			} else {
				teacher.firstName
			}
		} else {
			teacher.lastName
		}
	}
	
	fun calculateMarkPrediction(
		target: Double,
		avgMark: Double,
		sum: Int,
		valueSum: Double,
	): EstimatesGrades {
		fun calculateGrades(grade: Int): Int? {
			var processAvg = avgMark
			var gradeCount: Int = if (processAvg >= grade) return null else 0
			while (processAvg < target && gradeCount < 81) {
				gradeCount++
				processAvg = (valueSum + grade * gradeCount) / (sum + gradeCount)
			}
			return gradeCount
		}
		
		val fiveCount = calculateGrades(grade = 5)
		val fourCount = calculateGrades(grade = 4)
		val threeCount = calculateGrades(grade = 3)
		
		return EstimatesGrades(fiveCount, fourCount, threeCount)
	}
	
	data class EstimatesGrades(
		val fiveCount: Int? = null,
		val fourCount: Int? = null,
		val threeCount: Int? = null,
	)
	
	data class PeriodButton(
		val text: String,
		val callbackPeriod: EduPerformancePeriod,
	)
}