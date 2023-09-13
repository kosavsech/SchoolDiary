package com.kxsv.schooldiary.data.remote.parsers

import com.kxsv.schooldiary.data.remote.util.NetLessonColumn
import org.jsoup.select.Elements

private const val TAG = "SubjectParser"

class SubjectParser {
	fun parseTermRows(rows: Elements): List<String> {
		val subjects = mutableListOf<String>()
		rows.forEach { row ->
			val subjectAncestorName = row.firstElementChild()?.text()?.trim()
			if (subjectAncestorName == "ИТОГО" || subjectAncestorName.isNullOrBlank()) return@forEach

//			Log.d(TAG, "parseTermRows: adding subjectAncestorName $subjectAncestorName")
			subjects.add(subjectAncestorName)
		}
//		Log.d(TAG, "parseTermRows: result is $subjects")
		return subjects
	}
	
	fun parseDays(days: List<Elements>): MutableSet<String> {
		val subjects = mutableSetOf<String>()
		days.forEach { dayInfo ->
			dayInfo.forEach { lesson ->
				lesson.child(NetLessonColumn.SUBJECT.ordinal).text()
					.let { subjectAncestorName ->
						if (subjectAncestorName.isNotBlank()) {
							subjects.add(subjectAncestorName)
						}
					}
			}
		}
		return subjects
	}
}