package com.kxsv.schooldiary.data.mapper

import com.kxsv.schooldiary.data.local.features.grade.GradeEntity
import com.kxsv.schooldiary.data.local.features.grade.GradeWithSubject
import com.kxsv.schooldiary.data.local.features.subject.SubjectDao
import com.kxsv.schooldiary.data.remote.dtos.DayGradeDto
import com.kxsv.schooldiary.data.repository.SubjectRepository
import com.kxsv.schooldiary.data.util.DataIdGenUtils.generateGradeId
import java.time.LocalDateTime

suspend fun DayGradeDto.toGradeEntity(
	subjectDataSource: SubjectDao,
): GradeEntity {
	try {
		val subject = subjectDataSource.getByName(subjectAncestorName)
			?: throw NoSuchElementException("There is no subject with name $subjectAncestorName")
		
		
		return GradeEntity(
			mark = mark,
			date = date,
			fetchDateTime = LocalDateTime.now(),
			subjectMasterId = subject.subjectId,
			typeOfWork = typeOfWork,
			index = index,
			lessonIndex = lessonIndex,
			gradeId = generateGradeId()
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
	val subject = subjectDataSource.getByName(subjectAncestorName)
		?: throw NoSuchElementException("There is no subject with name $subjectAncestorName")
	
	return GradeWithSubject(
		grade = GradeEntity(
			mark = mark,
			date = date,
			fetchDateTime = LocalDateTime.now(),
			subjectMasterId = subject.subjectId,
			typeOfWork = typeOfWork,
			index = index,
			lessonIndex = lessonIndex,
			gradeId = generateGradeId()
		),
		subject = subject
	)
}

suspend fun DayGradeDto.toGradeWithSubject(
	subjectRepository: SubjectRepository,
): GradeWithSubject {
	val subject = subjectRepository.getSubjectByName(subjectAncestorName)
		?: throw NoSuchElementException("There is no subject with name $subjectAncestorName")
	
	return GradeWithSubject(
		grade = GradeEntity(
			mark = mark,
			date = date,
			fetchDateTime = LocalDateTime.now(),
			subjectMasterId = subject.subjectId,
			typeOfWork = typeOfWork,
			index = index,
			lessonIndex = lessonIndex,
			gradeId = generateGradeId()
		),
		subject = subject
	)
}

suspend fun List<DayGradeDto>.toGradesWithSubject(
	subjectDataSource: SubjectDao,
) = map { it.toGradeWithSubject(subjectDataSource = subjectDataSource) }