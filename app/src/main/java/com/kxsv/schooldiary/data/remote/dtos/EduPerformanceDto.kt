package com.kxsv.schooldiary.data.remote.dtos

import com.kxsv.schooldiary.data.util.EduPerformancePeriod
import com.kxsv.schooldiary.util.Mark

data class EduPerformanceDto(
	val subjectAncestorName: String,
	val marks: List<Mark?>,
	val finalMark: Mark?,
	val examMark: Mark? = null,
	val period: EduPerformancePeriod,
)
