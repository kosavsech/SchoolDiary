package com.kxsv.schooldiary.app

import android.app.Application
import com.kxsv.schooldiary.app.sync.initializers.GradesSync
import com.kxsv.schooldiary.app.sync.initializers.ScheduleSync
import com.kxsv.schooldiary.app.sync.initializers.SubjectsSync
import com.kxsv.schooldiary.app.sync.initializers.TaskSync
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SchoolDiaryApp : Application() {
	
	override fun onCreate() {
		super.onCreate()
		
		GradesSync.initialize(this)
		ScheduleSync.initialize(this)
		SubjectsSync.initialize(this)
		TaskSync.initialize(this)
	}
}