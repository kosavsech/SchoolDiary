package com.kxsv.schooldiary.data.remote

import org.jsoup.select.Elements
import java.time.LocalDate

interface WebService {
	
	suspend fun eduTatarAuth(login: String, password: String)
	
	suspend fun getScheduleForDate(localDate: LocalDate): Elements
	
	suspend fun getTermEduPerformance(term: String): Elements
}