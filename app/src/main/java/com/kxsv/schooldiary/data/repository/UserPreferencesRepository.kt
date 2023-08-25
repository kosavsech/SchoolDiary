package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.util.user_preferences.PeriodType
import com.kxsv.schooldiary.data.util.user_preferences.PeriodWithRange
import com.kxsv.schooldiary.data.util.user_preferences.StartScreen
import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
	
	fun observeStartScreen(): Flow<StartScreen>
	
	fun observeTargetMark(): Flow<Double>
	
	fun observeEducationPeriodType(): Flow<PeriodType>
	
	fun observePeriodsRanges(): Flow<PersistentList<PeriodWithRange>>
	
	fun observeRoundRule(): Flow<Double>
	
	fun observeLessonDuration(): Flow<Long>
	
	fun observePatternId(): Flow<Long>
	
	fun observeEduLogin(): Flow<String?>
	
	fun observeEduPassword(): Flow<String?>
	
	fun observeAuthCookie(): Flow<String?>
	
	fun observeInitLoginSuppression(): Flow<Boolean>
	
	suspend fun getTargetMark(): Double
	
	suspend fun setTargetMark(targetMark: Double)
	
	suspend fun getEducationPeriodType(): PeriodType
	
	suspend fun setEducationPeriodType(educationPeriodType: PeriodType)
	
	suspend fun getStartScreen(): StartScreen
	
	suspend fun setStartScreen(startScreen: StartScreen)
	
	suspend fun getPeriodsRanges(): PersistentList<PeriodWithRange>
	
	suspend fun setPeriodsRanges(periodsRanges: PersistentList<PeriodWithRange>)
	
	suspend fun getRoundRule(): Double
	
	suspend fun setRoundRule(roundRule: Double)
	
	suspend fun getLessonDuration(): Long
	
	suspend fun setLessonDuration(lessonDuration: Long)
	
	suspend fun getPatternId(): Long
	
	suspend fun setPatternId(id: Long)
	
	suspend fun getEduLogin(): String?
	
	suspend fun setEduLogin(login: String?)
	
	suspend fun getEduPassword(): String?
	
	suspend fun setEduPassword(password: String?)
	
	suspend fun getAuthCookie(): String?
	
	suspend fun setAuthCookie(cookie: String?)
	
	suspend fun getInitLoginSuppression(): Boolean
	
	suspend fun setInitLoginSuppression(value: Boolean)
}