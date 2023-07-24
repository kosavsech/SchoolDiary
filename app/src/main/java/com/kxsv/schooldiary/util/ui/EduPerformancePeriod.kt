package com.kxsv.schooldiary.util.ui

enum class EduPerformancePeriod {
	FIRST_TERM,
	SECOND_TERM,
	THIRD_TERM,
	FOURTH_TERM,
	YEAR_PERIOD;
	
	companion object {
		fun fromInput(input: String) = when (input) {
			"1" -> FIRST_TERM
			"2" -> SECOND_TERM
			"3" -> THIRD_TERM
			"4" -> FOURTH_TERM
			"year" -> YEAR_PERIOD
			else -> throw IllegalArgumentException("Wrong period input value=($input)")
		}
	}
}