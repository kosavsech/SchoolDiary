package com.kxsv.schooldiary.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object WorkManagerModule {
	
	@Provides
	fun provideWorkManager(@ApplicationContext context: Context) = WorkManager.getInstance(context)
}
