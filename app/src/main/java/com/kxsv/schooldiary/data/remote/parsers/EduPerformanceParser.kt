package com.kxsv.schooldiary.data.remote.parsers

import android.util.Log
import com.kxsv.schooldiary.data.remote.dtos.EduPerformanceDto
import com.kxsv.schooldiary.data.util.EduPerformancePeriod
import com.kxsv.schooldiary.util.Mark
import org.jsoup.select.Elements

private const val TAG = "EduPerformanceParser"

class EduPerformanceParser {
	fun parseTerm(rows: Elements, period: EduPerformancePeriod): List<EduPerformanceDto> {
		val eduPerformances = mutableListOf<EduPerformanceDto>()
		rows.forEach { row ->
			val subjectAncestorName = row.firstElementChild()?.text()?.trim()
			if (subjectAncestorName == "ИТОГО" || subjectAncestorName.isNullOrBlank()) return@forEach
			
			val finalMark = row.lastElementChild()?.text()?.trim()?.let { Mark.fromInput(it) }
			// dropLast(1) to get rid of finalMark, drop(1) to get rid of subject name,
			// dropLastWhile to get rid of: averageMark, diagram text, empty fields
			val marks = row.children().dropLast(1).drop(1).dropLastWhile {
				(it.text().length > 1) || it.text().isEmpty()
			}.map { Mark.fromInput(it.text()) }
			
			val eduPerformance = EduPerformanceDto(
				subjectAncestorName = subjectAncestorName,
				marks = marks,
				finalMark = finalMark,
				period = period
			)
//			Log.d(TAG, "parseTerm: adding eduPerformance $eduPerformance")
			eduPerformances.add(eduPerformance)
		}
//		Log.d(TAG, "parseTerm: result is $eduPerformances")
		return eduPerformances
	}
	
	fun parseYear(rows: Elements): List<EduPerformanceDto> {
		val eduPerformances = mutableListOf<EduPerformanceDto>()
		rows.forEach { row ->
			val subjectAncestorName = row.firstElementChild()?.text()?.trim()
			if (subjectAncestorName == "ИТОГО" || subjectAncestorName.isNullOrBlank()) return@forEach
			
			val finalMark = row.lastElementChild()?.text()?.let { Mark.fromInput(it) }
			// dropLast(1) to get rid of finalMark, drop(1) to get rid of subject name,
			// dropLastWhile to get rid of: averageMark, diagram text, empty fields
			var marks = row.children().dropLast(1).drop(1).dropLastWhile {
				(it.text().length > 1) || it.text().isEmpty()
			}.map { Mark.fromInput(it.text()) }
			var examMark: Mark? = null
			if (marks.size > 4) { // exam mark present
				Log.w(TAG, "parseYear: marks.size > 4 $subjectAncestorName\n$marks")
				examMark = marks.last()
				marks = marks.dropLast(1)
				Log.i(TAG, "parseYear: marks dropped last $subjectAncestorName\n$marks")
			}
			val eduPerformance = EduPerformanceDto(
				subjectAncestorName = subjectAncestorName,
				marks = marks,
				finalMark = finalMark,
				examMark = examMark,
				period = EduPerformancePeriod.YEAR
			)
//			Log.d(TAG, "parseYear: adding eduPerformance $eduPerformance")
			eduPerformances.add(eduPerformance)
		}
//		Log.d(TAG, "parseYear: result is $eduPerformances")
		return eduPerformances
	}
}