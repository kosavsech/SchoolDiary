package com.kxsv.schooldiary.data.network.grade

import com.kxsv.schooldiary.data.local.features.grade.Grade
import com.kxsv.schooldiary.domain.SubjectRepository
import com.kxsv.schooldiary.util.Mark
import java.time.LocalDate

data class NetworkGrade(
	val mark: Mark,
	val typeOfWork: String,
	val date: LocalDate,
	val subjectAncestorName: String,
	val index: Int,
	val lessonIndex: Int,
) {
	suspend fun toLocal(subjectRepository: SubjectRepository): Grade {
		try {
			val subjectMasterId =
				subjectRepository.getSubjectIdByName(subjectAncestorName)
					?: throw NoSuchElementException("Not found subject with name $subjectAncestorName")
			// TODO: add prompt to create subject with such name, or create it forcibly
			
			return Grade(
				mark = mark,
				date = date,
				subjectMasterId = subjectMasterId,
				typeOfWork = typeOfWork,
				index = index,
				lessonIndex = lessonIndex
			)
			
		} catch (e: NoSuchElementException) {
			throw RuntimeException("Failed to convert network to local", e)
		}
	}
}
