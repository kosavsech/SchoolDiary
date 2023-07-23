package com.kxsv.schooldiary.data.remote.edu_performance

import com.kxsv.schooldiary.util.Mark
import com.kxsv.schooldiary.util.ui.EduPerformancePeriod
import org.jsoup.select.Elements

private const val TAG = "EduPerformanceParser"

class EduPerformanceParser {
	fun parseTerm(rows: Elements, term: String): List<EduPerformanceDto> {
		val eduPerformances = mutableListOf<EduPerformanceDto>()
		rows.forEach { row ->
			val subjectAncestorName = row.firstElementChild()?.text()
			if (subjectAncestorName == "ИТОГО" || subjectAncestorName.isNullOrBlank()) {
//				Log.w(TAG, "parseTerm: early exit name is wrong($subjectAncestorName)")
				return@forEach
			}
			val finalMark = row.lastElementChild()?.text()?.let { Mark.fromInput(it) }
			val marks = row.children().dropLast(1).drop(1).dropLastWhile {
				(it.text().length > 1) || it.text().isEmpty()
			}.map { Mark.fromInput(it.text()) }
			
			val eduPerformance = EduPerformanceDto(
				subjectAncestorName = subjectAncestorName,
				marks = marks,
				finalMark = finalMark,
				period = EduPerformancePeriod.fromInput(term)
			)
//			Log.d(TAG, "parseTerm: adding eduPerformance $eduPerformance")
			eduPerformances.add(eduPerformance)
		}
//		Log.d(TAG, "parseTerm: result is $eduPerformances")
		return eduPerformances
	}
	
}
