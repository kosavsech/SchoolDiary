package com.kxsv.schooldiary.data.remote

import com.kxsv.schooldiary.data.remote.dtos.UpdateDto
import com.kxsv.schooldiary.data.remote.util.NetworkException
import com.kxsv.schooldiary.data.util.EduPerformancePeriod
import org.jsoup.select.Elements
import java.time.LocalDate

interface WebService {
	
	/**
	 * @throws NetworkException.PageNotFound
	 * @throws java.io.IOException if couldn't parseTermRows document
	 */
	suspend fun getLatestAppVersion(): UpdateDto?
	
	/**
	 * Edu tatar auth
	 *
	 * @throws NetworkException.IncorrectAuthDataException
	 * @throws NetworkException.NotLoggedInException
	 * @throws NetworkException.AccessTemporarilyBlockedException
	 * @throws NetworkException.BlankInputException
	 * @throws java.io.IOException if couldn't parseTermRows document
	 */
	suspend fun eduTatarAuth(login: String, password: String)
	
	/**
	 * Get schedule from [day page][getDayPage]
	 *
	 * @param localDate
	 * @return
	 * @throws NetworkException.NotLoggedInException
	 * @throws NetworkException.PageNotFound
	 * @throws java.io.IOException if couldn't parseTermRows document
	 */
	suspend fun getDayInfo(localDate: LocalDate): Elements
	
	/**
	 * @throws NetworkException.NotLoggedInException
	 * @throws NetworkException.PageNotFound
	 * @throws java.io.IOException if couldn't parseTermRows document
	 */
	suspend fun getTermEduPerformance(term: EduPerformancePeriod): Elements
	
}