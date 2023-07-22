package com.kxsv.schooldiary.data.mapper

import com.kxsv.schooldiary.data.local.features.grade.GradeEntity
import com.kxsv.schooldiary.data.remote.grade.DayGradeDto
import com.kxsv.schooldiary.data.repository.SubjectRepository

suspend fun DayGradeDto.toLocal(subjectRepository: SubjectRepository): GradeEntity {
	try {
		val subjectMasterId =
			subjectRepository.getSubjectIdByName(subjectAncestorName)
				?: throw NoSuchElementException("Not found subject with name $subjectAncestorName")
		// TODO: add prompt to create subject with such name, or create it forcibly
		
		return GradeEntity(
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

suspend fun List<DayGradeDto>.toLocal(subjectRepository: SubjectRepository) =
	map { it.toLocal(subjectRepository) }