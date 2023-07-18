package com.kxsv.schooldiary.domain

import com.kxsv.schooldiary.data.network.schedule.NetworkSchedule
import java.time.LocalDate

interface ScheduleNetworkDataSource {
	
	suspend fun eduTatarAuth(login: String, password: String)
	
	suspend fun loadScheduleForDate(localDate: LocalDate): List<NetworkSchedule>
	
}