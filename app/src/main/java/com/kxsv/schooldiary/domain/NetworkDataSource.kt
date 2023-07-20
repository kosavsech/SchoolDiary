package com.kxsv.schooldiary.domain

import com.kxsv.schooldiary.data.network.grade.NetworkGrade
import com.kxsv.schooldiary.data.network.schedule.NetworkSchedule
import java.time.LocalDate

interface NetworkDataSource {
	
	suspend fun eduTatarAuth(login: String, password: String)
	
	suspend fun loadScheduleForDate(localDate: LocalDate): List<NetworkSchedule>
	
	suspend fun loadGradesForDate(localDate: LocalDate): List<NetworkGrade>
	
}