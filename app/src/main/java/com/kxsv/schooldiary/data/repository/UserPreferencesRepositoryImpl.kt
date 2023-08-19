package com.kxsv.schooldiary.data.repository

import androidx.datastore.core.DataStore
import com.kxsv.schooldiary.data.local.user_preferences.UserPreferences
import com.kxsv.schooldiary.data.util.user_preferences.PeriodType
import com.kxsv.schooldiary.data.util.user_preferences.PeriodWithRange
import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
	private val dataStore: DataStore<UserPreferences>,
) : UserPreferencesRepository {
	
	override fun observeTargetMark(): Flow<Double> {
		return dataStore.data.map {
			it.defaultTargetMark
		}
	}
	
	override fun observeEducationPeriodType(): Flow<PeriodType> {
		return dataStore.data.map {
			it.educationPeriodType
		}
	}
	
	override fun observePeriodsRanges(): Flow<PersistentList<PeriodWithRange>> {
		return dataStore.data.map {
			it.periodsRanges
		}
	}
	
	override fun observeRoundRule(): Flow<Double> {
		return dataStore.data.map {
			it.defaultRoundRule
		}
	}
	
	override fun observeLessonDuration(): Flow<Long> {
		return dataStore.data.map {
			it.defaultLessonDuration
		}
	}
	
	override fun observePatternId(): Flow<Long> {
		return dataStore.data.map {
			it.defaultPatternId
		}
	}
	
	override fun observeEduLogin(): Flow<String?> {
		return dataStore.data.map {
			it.eduLogin
		}
	}
	
	override fun observeEduPassword(): Flow<String?> {
		return dataStore.data.map {
			it.eduPassword
		}
	}
	
	override fun observeAuthCookie(): Flow<String?> {
		return dataStore.data.map {
			it.authCookie
		}
	}
	
	override fun observeInitLoginSuppression(): Flow<Boolean> {
		return dataStore.data.map {
			it.suppressInitLogin
		}
	}
	
	override suspend fun getTargetMark(): Double {
		return dataStore.data.first().defaultTargetMark
	}
	
	override suspend fun setTargetMark(targetMark: Double) {
		dataStore.updateData {
			it.copy(defaultTargetMark = targetMark)
		}
	}
	
	override suspend fun getEducationPeriodType(): PeriodType {
		return dataStore.data.first().educationPeriodType
	}
	
	override suspend fun setEducationPeriodType(educationPeriodType: PeriodType) {
		dataStore.updateData {
			it.copy(educationPeriodType = educationPeriodType)
		}
	}
	
	override suspend fun getPeriodsRanges(): PersistentList<PeriodWithRange> {
		return dataStore.data.first().periodsRanges
	}
	
	override suspend fun setPeriodsRanges(periodsRanges: PersistentList<PeriodWithRange>) {
		dataStore.updateData {
			it.copy(periodsRanges = periodsRanges)
		}
	}
	
	override suspend fun getRoundRule(): Double {
		return dataStore.data.first().defaultRoundRule
	}
	
	override suspend fun setRoundRule(roundRule: Double) {
		dataStore.updateData {
			it.copy(defaultRoundRule = roundRule)
		}
	}
	
	override suspend fun getLessonDuration(): Long {
		return dataStore.data.first().defaultLessonDuration
	}
	
	override suspend fun setLessonDuration(lessonDuration: Long) {
		dataStore.updateData {
			it.copy(defaultLessonDuration = lessonDuration)
		}
	}
	
	override suspend fun getPatternId(): Long {
		return dataStore.data.first().defaultPatternId
	}
	
	override suspend fun setPatternId(id: Long) {
		dataStore.updateData {
			it.copy(defaultPatternId = id)
		}
	}
	
	override suspend fun getEduLogin(): String? {
		return dataStore.data.first().eduLogin
	}
	
	override suspend fun setEduLogin(login: String?) {
		dataStore.updateData {
			it.copy(eduLogin = login)
		}
	}
	
	override suspend fun getEduPassword(): String? {
		return dataStore.data.first().eduPassword
	}
	
	override suspend fun setEduPassword(password: String?) {
		dataStore.updateData {
			it.copy(eduPassword = password)
		}
	}
	
	override suspend fun getAuthCookie(): String? {
		return dataStore.data.first().authCookie
	}
	
	override suspend fun setAuthCookie(cookie: String?) {
		dataStore.updateData { it.copy(authCookie = cookie) }
	}
	
	override suspend fun getInitLoginSuppression(): Boolean {
		return dataStore.data.first().suppressInitLogin
	}
	
	override suspend fun setInitLoginSuppression(value: Boolean) {
		dataStore.updateData { it.copy(suppressInitLogin = value) }
	}
}