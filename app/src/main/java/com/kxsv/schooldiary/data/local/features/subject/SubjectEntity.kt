package com.kxsv.schooldiary.data.local.features.subject

import android.util.Log
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kxsv.schooldiary.data.local.features.DatabaseConstants
import com.kxsv.schooldiary.data.local.features.study_day.StudyDayWithSchedulesAndSubjects
import com.kxsv.schooldiary.util.Utils
import com.kxsv.schooldiary.util.Utils.isHoliday
import java.time.DayOfWeek
import java.time.LocalDate

@Entity(
	tableName = DatabaseConstants.SUBJECT_TABLE_NAME,
	indices = [Index(value = ["fullName"], unique = true)],
)
data class SubjectEntity(
	val fullName: String,
	val cabinet: String? = null,
	val displayName: String? = null,
	val targetMark: Double? = null,
	val lowerBoundMark: Double? = null,
//    val tags: List<Tag>,
	@PrimaryKey
	val subjectId: String = "",
) {
	fun getName(): String {
		return if (!this.displayName.isNullOrBlank()) {
			this.displayName
		} else {
			this.fullName
		}
	}
	
	fun getCabinetString(): String {
		return if (!this.cabinet.isNullOrBlank()) {
			this.cabinet
		} else {
			""
		}
	}
	
	fun getDisplayNameString(): String {
		return if (!this.displayName.isNullOrBlank()) {
			this.displayName
		} else {
			""
		}
	}
	
	fun getDayOfWeekForSubject(
		weekSample: List<StudyDayWithSchedulesAndSubjects>?,
	): List<DayOfWeek> {
		val result = mutableListOf<DayOfWeek>()
		if (weekSample.isNullOrEmpty()) return result
		weekSample.forEach { dayWithSchedulesAndSubjects ->
			if (dayWithSchedulesAndSubjects.classes.any { it.subject.subjectId == this.subjectId }) {
				result.add(dayWithSchedulesAndSubjects.studyDay.date.dayOfWeek)
			}
		}
		Log.d("SubjectEntity", "getDayOfWeekForSubject() returned: $result")
		return result
	}
	
	fun getDayOfWeekAndClassAmountForSubject(
		weekSample: List<StudyDayWithSchedulesAndSubjects>?,
	): Map<DayOfWeek, Int> {
		val result = mutableMapOf<DayOfWeek, Int>()
		if (weekSample.isNullOrEmpty()) return result
		weekSample.forEach { dayWithSchedulesAndSubjects ->
			val subjectOccurrence =
				dayWithSchedulesAndSubjects.classes.count { it.subject.subjectId == this.subjectId }
			if (subjectOccurrence > 0) {
				result[dayWithSchedulesAndSubjects.studyDay.date.dayOfWeek] = subjectOccurrence
			}
		}
		Log.d("SubjectEntity", "getDayOfWeekForSubject() returned: $result")
		return result
	}
	
	fun calculateLessonsLeftInThisPeriod(
		daysUntilPeriodEnd: Int,
		weekSample: List<StudyDayWithSchedulesAndSubjects>?,
		termsPeriodRanges: List<ClosedRange<LocalDate>>,
	): Int {
		val dayOfWeekAndClassesAmount = getDayOfWeekAndClassAmountForSubject(weekSample)
		var counter = 0
		for (i in 0 until daysUntilPeriodEnd) {
			val date = Utils.currentDate.plusDays(i.toLong())
			if (!isHoliday(date, termsPeriodRanges)) {
				val valueOfThisDayOfWeek = dayOfWeekAndClassesAmount[date.dayOfWeek]
				if (valueOfThisDayOfWeek != null) {
					counter += valueOfThisDayOfWeek
				}
			}
		}
		return counter
	}
	
	fun calculateDaysWithLessonLeftInThisPeriod(
		daysUntilPeriodEnd: Int,
		weekSample: List<StudyDayWithSchedulesAndSubjects>?,
		termsPeriodRanges: List<ClosedRange<LocalDate>>,
	): Int {
		val dayOfWeekAndClassesAmount = getDayOfWeekForSubject(weekSample)
		var counter = 0
		for (i in 0 until daysUntilPeriodEnd) {
			val date = Utils.currentDate.plusDays(i.toLong())
			if (!isHoliday(date, termsPeriodRanges))
				if (dayOfWeekAndClassesAmount.contains(date.dayOfWeek)) counter += 1
		}
		return counter
	}
}