package com.kxsv.schooldiary.data.util.user_preferences

enum class Period(val index: Int) {
	FIRST_TERM(1),
	SECOND_TERM(2),
	THIRD_TERM(3),
	FOURTH_TERM(4),
	FIRST_SEMESTER(1),
	SECOND_SEMESTER(2);
	
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