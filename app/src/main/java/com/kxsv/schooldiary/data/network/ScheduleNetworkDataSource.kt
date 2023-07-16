package com.kxsv.schooldiary.data.network

import com.kxsv.schooldiary.data.network.schedule.NetworkSchedule
import java.time.LocalDate

interface ScheduleNetworkDataSource {
	
	suspend fun loadScheduleForDate(localDate: LocalDate): List<NetworkSchedule>
}