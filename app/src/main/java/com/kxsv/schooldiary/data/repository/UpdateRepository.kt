package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.util.AppVersionState
import kotlinx.coroutines.flow.SharedFlow

interface UpdateRepository {
	
	val isUpdateAvailable: SharedFlow<AppVersionState>
	
	suspend fun checkUpdate()
	
	suspend fun suppressUpdateUntilNextAppStart()
	
}