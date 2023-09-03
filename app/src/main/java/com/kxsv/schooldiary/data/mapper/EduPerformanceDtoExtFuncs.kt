package com.kxsv.schooldiary.data.mapper

import com.kxsv.schooldiary.data.local.features.edu_performance.EduPerformanceEntity
import com.kxsv.schooldiary.data.local.features.subject.SubjectDao
import com.kxsv.schooldiary.data.remote.dtos.EduPerformanceDto
import com.kxsv.schooldiary.data.util.DataIdGenUtils.generateEduPerformanceId

suspend fun EduPerformanceDto.toEduPerformanceEntity(
	subjectDataSource: SubjectDao,
): EduPerformanceEntity {
	try {
		val subject = subjectDataSource.getByName(subjectAncestorName)
			?: throw NoSuchElementException("There is no subject with name $subjectAncestorName")
		
		
		return EduPerformanceEntity(
			subjectMasterId = subject.subjectId,
			marks = marks,
			finalMark = finalMark,
			examMark = examMark,
			period = period,
			eduPerformanceId = generateEduPerformanceId()
		)
		
	} catch (e: NoSuchElementException) {
		throw RuntimeException("Failed to convert network to local", e)
	}
}

suspend fun List<EduPerformanceDto>.toEduPerformanceEntities(
	subjectDataSource: SubjectDao,
) = map { it.toEduPerformanceEntity(subjectDataSource = subjectDataSource) }
