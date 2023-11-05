package com.kxsv.schooldiary.util

import com.kxsv.schooldiary.data.util.EduPerformancePeriod

data class PeriodButton(
	val text: String,
	val callbackPeriod: EduPerformancePeriod,
)