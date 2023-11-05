package com.kxsv.schooldiary.ui.screens.main_screen

const val CURRENT_DAY_INFO_UPDATE_TIMING = 7_000L

enum class LessonDetailedUpdateTiming(val timing: Long) {
	VERY_LONG(30_000L),
	LONG(15_000L),
	DEFAULT(5_000L),
	SHORT(1_000L),
	VERY_SHORT(100L)
}