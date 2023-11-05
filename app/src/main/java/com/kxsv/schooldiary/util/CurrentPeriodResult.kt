package com.kxsv.schooldiary.util


data class CurrentPeriodResult(
	val daysUntilClosestHolidays: Long?,
	val daysUntilPeriodEnd: Long,
	val daysUntilClosestPeriod: Long?,
)
