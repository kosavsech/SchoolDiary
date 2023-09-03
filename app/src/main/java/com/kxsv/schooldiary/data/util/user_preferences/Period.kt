package com.kxsv.schooldiary.data.util.user_preferences

import com.kxsv.schooldiary.data.util.EduPerformancePeriod

enum class Period(val index: Int) {
	FIRST_TERM(1),
	SECOND_TERM(2),
	THIRD_TERM(3),
	FOURTH_TERM(4),
	FIRST_SEMESTER(1),
	SECOND_SEMESTER(2);
	
	fun convertToEduPerformancePeriod(): EduPerformancePeriod {
		return when (this.index) {
			1 -> EduPerformancePeriod.FIRST
			2 -> EduPerformancePeriod.SECOND
			3 -> EduPerformancePeriod.THIRD
			4 -> EduPerformancePeriod.FOURTH
			else -> EduPerformancePeriod.FIRST
		}
	}
	
	companion object {
		fun getTypeByPeriod(period: Period): PeriodType {
			return if (period.ordinal in 0..3) {
				PeriodType.TERMS
			} else {
				PeriodType.SEMESTERS
			}
		}
	}
}