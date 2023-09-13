package com.kxsv.schooldiary.data.mapper

import android.util.Log
import com.kxsv.schooldiary.data.local.features.edu_performance.EduPerformanceEntity
import com.kxsv.schooldiary.data.remote.dtos.EduPerformanceDto
import com.kxsv.schooldiary.data.repository.SubjectRepository
import com.kxsv.schooldiary.data.util.DataIdGenUtils.generateEduPerformanceId

private const val TAG = "EduPerformanceDtoExtFun"

suspend fun EduPerformanceDto.toEduPerformanceEntity(
	subjectRepository: SubjectRepository,
): EduPerformanceEntity {
	try {
		val subject = subjectRepository.getSubjectByName(subjectAncestorName)
			?: throw NoSuchElementException("There is no subject with name $subjectAncestorName")
		
		val result = EduPerformanceEntity(
			subjectMasterId = subject.subjectId,
			marks = marks,
			finalMark = finalMark,
			examMark = examMark,
			period = period,
			eduPerformanceId = generateEduPerformanceId()
		)
		Log.d(TAG, "toEduPerformanceEntity() returned: $result")
		
		return result
	} catch (e: NoSuchElementException) {
		throw RuntimeException("Failed to convert network to local", e)
	}
}

suspend fun List<EduPerformanceDto>.toEduPerformanceEntities(
	subjectRepository: SubjectRepository,
) = map { it.toEduPerformanceEntity(subjectRepository = subjectRepository) }
