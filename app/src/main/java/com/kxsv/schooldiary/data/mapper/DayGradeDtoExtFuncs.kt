package com.kxsv.schooldiary.data.mapper

import com.kxsv.schooldiary.data.DataUtils
import com.kxsv.schooldiary.data.local.features.grade.GradeEntity
import com.kxsv.schooldiary.data.local.features.grade.GradeWithSubject
import com.kxsv.schooldiary.data.local.features.subject.SubjectDao
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.remote.grade.DayGradeDto
import java.time.LocalDateTime

suspend fun DayGradeDto.toGradeEntity(
	subjectDataSource: SubjectDao,
): GradeEntity {
	try {
		val existedSubject = subjectDataSource.getByName(subjectAncestorName)
		
		val subject = if (existedSubject != null) {
			existedSubject
		} else {
			val subject = SubjectEntity(fullName = subjectAncestorName)
			val subjectId = subjectDataSource.upsert(subject)
			
			subject.copy(subjectId = subjectId)
		}
		
		return GradeEntity(
			mark = mark,
			date = date,
			fetchDateTime = LocalDateTime.now(),
			subjectMasterId = subject.subjectId,
			typeOfWork = typeOfWork,
			index = index,
			lessonIndex = lessonIndex,
			gradeId = DataUtils.generateGradeId(
				date = date,
				index = index,
				lessonIndex = lessonIndex
			)
		)
		
	} catch (e: NoSuchElementException) {
		throw RuntimeException("Failed to convert network to local", e)
	}
}

suspend fun List<DayGradeDto>.toGradeEntities(
	subjectDataSource: SubjectDao,
) = map { it.toGradeEntity(subjectDataSource = subjectDataSource) }

suspend fun DayGradeDto.toGradeWithSubject(
	subjectDataSource: SubjectDao,
): GradeWithSubject {
	try {
		val existedSubject = subjectDataSource.getByName(subjectAncestorName)
		
		val subject = if (existedSubject != null) {
			existedSubject
		} else {
			val subject = SubjectEntity(fullName = subjectAncestorName)
			val subjectId = subjectDataSource.upsert(subject)
			
			subject.copy(subjectId = subjectId)
		}
		
		// TODO: add prompt to create subject with such name, or create it forcibly
		return GradeWithSubject(
			grade = GradeEntity(
				mark = mark,
				date = date,
				fetchDateTime = LocalDateTime.now(),
				subjectMasterId = subject.subjectId,
				typeOfWork = typeOfWork,
				index = index,
				lessonIndex = lessonIndex,
				gradeId = DataUtils.generateGradeId(
					date = date,
					index = index,
					lessonIndex = lessonIndex
				)
			),
			subject = subject
		)
		
	} catch (e: NoSuchElementException) {
		throw RuntimeException("Failed to convert network to local", e)
	}
}

suspend fun List<DayGradeDto>.toGradesWithSubject(
	subjectDataSource: SubjectDao,
) = map { it.toGradeWithSubject(subjectDataSource = subjectDataSource) }