package com.kxsv.schooldiary.data.app_defaults

import androidx.datastore.core.DataStore
import com.kxsv.schooldiary.domain.AppDefaultsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppDefaultsRepositoryImpl @Inject constructor(
	private val dataStore: DataStore<AppDefaults>,
) : AppDefaultsRepository {
	
	override fun getPatternIdStream(): Flow<Long> {
		return dataStore.data.map {
			it.defaultPatternId
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
}