package com.kxsv.schooldiary.di.data_modules

import com.kxsv.schooldiary.data.remote.WebService
import com.kxsv.schooldiary.data.remote.WebServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {
	
	@Singleton
	@Binds
	abstract fun bindScheduleNetworkDataSource(dataSource: WebServiceImpl): WebService
}