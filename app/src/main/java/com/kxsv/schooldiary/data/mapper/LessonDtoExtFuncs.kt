package com.kxsv.schooldiary.data.mapper

import android.util.Log
import com.kxsv.schooldiary.data.local.features.lesson.LessonEntity
import com.kxsv.schooldiary.data.local.features.lesson.LessonWithSubject
import com.kxsv.schooldiary.data.local.features.study_day.StudyDayDao
import com.kxsv.schooldiary.data.local.features.subject.SubjectDao
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.remote.lesson.LessonDto
import com.kxsv.schooldiary.data.repository.StudyDayRepository
import com.kxsv.schooldiary.data.repository.SubjectRepository

private const val TAG = "LessonMapper"

suspend fun LessonDto.toLessonEntity(
	studyDayMasterId: Long?,
	subjectRepository: SubjectRepository,
	studyDayRepository: StudyDayRepository,
): LessonEntity {
	try {
		val subjectMasterId =
			subjectRepository.getSubjectIdByName(subjectAncestorName)
				?: throw NoSuchElementException("Not found subject with name $subjectAncestorName")
		// TODO: add prompt to create subject with such name, or create it forcibly
		
		val studyDay = studyDayRepository.getByDate(date)
		val localizedClass = LessonEntity(
			index = index,
			subjectAncestorId = subjectMasterId,
		)
		return if (studyDayMasterId != null) {
			localizedClass.copy(studyDayMasterId = studyDayMasterId)
		} else if (studyDay != null) {
			localizedClass.copy(studyDayMasterId = studyDay.studyDayId)
		} else {
			localizedClass
		}
	} catch (e: NoSuchElementException) {
		throw RuntimeException("Failed to convert class to local", e)
	}
}

/**
 * Convert [LessonDto] to local, opportunity to push the [studyDayMasterId]
 * which will be assigned to each [LessonEntity] after conversion, which is by default
 * is [current studyDay][com.kxsv.schooldiary.ui.screens.schedule.DayScheduleUiState.studyDay]
 * id or is null.
 *
 * @param studyDayMasterId value to assign to each class [studyDayMasterId][com.kxsv.schooldiary.data.local.features.lesson.LessonEntity.studyDayMasterId] field
 * @return List<[LessonEntity]>
 */
suspend fun List<LessonDto>.toLessonEntities(
	studyDayMasterId: Long? = null,
	subjectRepository: SubjectRepository,
	studyDayRepository: StudyDayRepository,
): List<LessonEntity> {
	return try {
		this.map { it.toLessonEntity(studyDayMasterId, subjectRepository, studyDayRepository) }
	} catch (e: RuntimeException) {
		Log.e(TAG, "List<LessonDto>.toGradeEntities: classes are empty because", e)
		emptyList()
	}
}

suspend fun List<LessonDto>.toSubjectEntitiesIndexed(
	subjectRepository: SubjectRepository,
	studyDayRepository: StudyDayRepository,
): Map<Int, SubjectEntity> {
	return try {
		val newMap = mutableMapOf<Int, SubjectEntity>()
		this.forEach {
			val localedClass = it.toLocalWithSubject(subjectRepository, studyDayRepository)
			newMap[it.index] = localedClass.subject
		}
		newMap
	} catch (e: RuntimeException) {
		Log.e(
			TAG, "List<LessonDto>.toSubjectEntitiesIndexed: classes are empty because", e
		)
		emptyMap()
	}
}

suspend fun LessonDto.toLocalWithSubject(
	subjectRepository: SubjectRepository,
	studyDayRepository: StudyDayRepository,
): LessonWithSubject {
	try {
		val subject =
			subjectRepository.getSubjectByName(subjectAncestorName)
				?: throw NoSuchElementException("Not found subject with name $subjectAncestorName")
		// TODO: add prompt to create subject with such name, or create it forcibly
		
		val studyDay = studyDayRepository.getByDate(date)
		val localizedClass = LessonWithSubject(
			lesson = LessonEntity(
				index = index,
				subjectAncestorId = subject.subjectId,
			),
			subject = subject,
		)
		return if (studyDay != null) {
			localizedClass.copy(lesson = localizedClass.lesson.copy(studyDayMasterId = studyDay.studyDayId))
		} else {
			localizedClass
		}
	} catch (e: NoSuchElementException) {
		throw RuntimeException("Failed to convert class to LocalWithSubject", e)
	}
}

suspend fun List<LessonDto>.toLocalWithSubject(
	subjectRepository: SubjectRepository,
	studyDayRepository: StudyDayRepository,
): Map<Int, LessonWithSubject> {
	return try {
		val newMap = mutableMapOf<Int, LessonWithSubject>()
		this.forEach {
			val localedClass = it.toLocalWithSubject(subjectRepository, studyDayRepository)
			newMap[it.index] = localedClass
		}
		newMap
	} catch (e: RuntimeException) {
		Log.e(
			TAG, "List<LessonDto>.toLocalWithSubject: classes are empty because", e
		)
		emptyMap()
	}
}

suspend fun LessonDto.toLocalWithSubject(
	subjectDataSource: SubjectDao,
	studyDayDataSource: StudyDayDao,
): LessonWithSubject {
	try {
		val subject =
			subjectDataSource.getByName(subjectAncestorName)
				?: throw NoSuchElementException("Not found subject with name $subjectAncestorName")
		// TODO: add prompt to create subject with such name, or create it forcibly
		
		val studyDay = studyDayDataSource.getByDate(date)
		val localizedClass = LessonWithSubject(
			lesson = LessonEntity(
				index = index,
				subjectAncestorId = subject.subjectId,
			),
			subject = subject,
		)
		return if (studyDay != null) {
			localizedClass.copy(lesson = localizedClass.lesson.copy(studyDayMasterId = studyDay.studyDayId))
		} else {
			localizedClass
		}
	} catch (e: NoSuchElementException) {
		throw RuntimeException("Failed to convert class to LocalWithSubject", e)
	}
}

suspend fun List<LessonDto>.toLocalWithSubject(
	subjectDataSource: SubjectDao,
	studyDayDataSource: StudyDayDao,
): Map<Int, LessonWithSubject> {
	return try {
		val newMap = mutableMapOf<Int, LessonWithSubject>()
		this.forEach {
			val localedClass = it.toLocalWithSubject(subjectDataSource, studyDayDataSource)
			newMap[it.index] = localedClass
		}
		newMap
	} catch (e: RuntimeException) {
		Log.e(
			TAG, "List<LessonDto>.toLocalWithSubject: classes are empty because", e
		)
		emptyMap()
	}
}
