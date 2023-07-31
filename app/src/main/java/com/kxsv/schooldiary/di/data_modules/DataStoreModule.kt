package com.kxsv.schooldiary.di.data_modules

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import com.kxsv.schooldiary.data.local.user_preferences.UserPreferences
import com.kxsv.schooldiary.data.local.user_preferences.UserPreferencesSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataStoreModule {
	
	@Singleton
	@Provides
	fun provideDataStore(@ApplicationContext appContext: Context): DataStore<UserPreferences> {
		return DataStoreFactory.create(
			serializer = UserPreferencesSerializer,
			corruptionHandler = ReplaceFileCorruptionHandler(
				produceNewData = { UserPreferences() }
			),
			scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
			produceFile = { appContext.dataStoreFile("app-settings.json") }
		)
	}
}