package com.kxsv.schooldiary.data.remote.parsers

import org.jsoup.select.Elements

private const val TAG = "SubjectParser"

class SubjectParser {
	fun parse(rows: Elements): MutableList<String> {
		val subjects = mutableListOf<String>()
		rows.forEach { row ->
			val subjectAncestorName = row.firstElementChild()?.text()?.trim()
			if (subjectAncestorName == "ИТОГО" || subjectAncestorName.isNullOrBlank()) return@forEach

//			Log.d(TAG, "parse: adding subjectAncestorName $subjectAncestorName")
			subjects.add(subjectAncestorName)
		}
//		Log.d(TAG, "parse: result is $subjects")
		return subjects
	}
}