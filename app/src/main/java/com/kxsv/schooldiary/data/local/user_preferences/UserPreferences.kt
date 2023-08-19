package com.kxsv.schooldiary.data.local.user_preferences

import com.kxsv.schooldiary.data.util.user_preferences.Period
import com.kxsv.schooldiary.data.util.user_preferences.PeriodType
import com.kxsv.schooldiary.data.util.user_preferences.PeriodWithRange
import com.kxsv.schooldiary.data.util.user_preferences.PersistentListSerializer
import com.kxsv.schooldiary.data.util.user_preferences.Range
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
	val defaultTargetMark: Double = 4.6,
	val defaultRoundRule: Double = 0.6,
	val defaultLessonDuration: Long = 45,
	val suppressInitLogin: Boolean = false,
	val educationPeriodType: PeriodType = PeriodType.SEMESTERS,
	@Serializable(PersistentListSerializer::class)
	val periodsRanges: PersistentList<PeriodWithRange> = persistentListOf(
		PeriodWithRange(
			Period.FIRST_TERM,
			Range(start = "9_1", end = "10_31")
		),
		PeriodWithRange(
			Period.SECOND_TERM,
			Range(start = "11_8", end = "12_28")
		),
		PeriodWithRange(
			Period.THIRD_TERM,
			Range(start = "1_12", end = "3_27")
		),
		PeriodWithRange(
			Period.FOURTH_TERM,
			Range(start = "4_6", end = "5_31")
		),
		PeriodWithRange(
			Period.FIRST_SEMESTER,
			Range(start = "9_1", end = "12_27")
		),
		PeriodWithRange(
			Period.SECOND_SEMESTER,
			Range(start = "1_9", end = "5_31")
		)
	),
	val defaultPatternId: Long = 0L,
	val eduLogin: String? = null,
	val eduPassword: String? = null,
	val authCookie: String? = null,
)