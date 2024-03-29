package com.kxsv.schooldiary.data.local.user_preferences

import com.kxsv.schooldiary.data.util.user_preferences.Period
import com.kxsv.schooldiary.data.util.user_preferences.PeriodDateRange
import com.kxsv.schooldiary.data.util.user_preferences.PeriodType
import com.kxsv.schooldiary.data.util.user_preferences.PeriodWithRange
import com.kxsv.schooldiary.data.util.user_preferences.PersistentListSerializer
import com.kxsv.schooldiary.data.util.user_preferences.StartScreen
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
	val defaultTargetMark: Double = 4.6,
	val defaultLowerBoundMark: Double = 2.6,
	val defaultRoundRule: Double = 0.6,
	val defaultLessonDuration: Long = 45,
	val suppressInitLogin: Boolean = false,
	val startScreen: StartScreen = StartScreen.MAIN_SCREEN,
	val educationPeriodType: PeriodType = PeriodType.SEMESTERS,
	@Serializable(PersistentListSerializer::class)
	val periodsRanges: PersistentList<PeriodWithRange> = persistentListOf(
		PeriodWithRange(
			Period.FIRST_TERM,
			PeriodDateRange(start = "9_1", end = "10_27")
		),
		PeriodWithRange(
			Period.SECOND_TERM,
			PeriodDateRange(start = "11_7", end = "12_29")
		),
		PeriodWithRange(
			Period.THIRD_TERM,
			PeriodDateRange(start = "1_9", end = "3_22")
		),
		PeriodWithRange(
			Period.FOURTH_TERM,
			PeriodDateRange(start = "4_1", end = "5_25")
		),
		PeriodWithRange(
			Period.FIRST_SEMESTER,
			PeriodDateRange(start = "9_1", end = "12_29")
		),
		PeriodWithRange(
			Period.SECOND_SEMESTER,
			PeriodDateRange(start = "1_9", end = "5_25")
		)
	),
	val defaultPatternId: Long = 1L,
	val calendarScrollPaged: Boolean = true,
	
	val eduLogin: String? = null,
	val eduPassword: String? = null,
	val authCookie: String? = null,
)