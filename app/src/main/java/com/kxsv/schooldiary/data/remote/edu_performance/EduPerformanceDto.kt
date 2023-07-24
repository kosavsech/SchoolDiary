package com.kxsv.schooldiary.data.remote.edu_performance

import com.kxsv.schooldiary.util.Mark
import com.kxsv.schooldiary.util.ui.EduPerformancePeriod

data class EduPerformanceDto(
	val subjectAncestorName: String,
	val marks: List<Mark?>,
	val finalMark: Mark?,
	val examMark: Mark? = null,
	val period: EduPerformancePeriod,
)
