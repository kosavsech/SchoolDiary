package com.kxsv.schooldiary.data.mapper

import android.util.Log
import com.kxsv.schooldiary.data.local.features.lesson.LessonDao
import com.kxsv.schooldiary.data.local.features.lesson.LessonEntity
import com.kxsv.schooldiary.data.local.features.lesson.LessonWithSubject
import com.kxsv.schooldiary.data.local.features.study_day.StudyDayDao
import com.kxsv.schooldiary.data.local.features.study_day.StudyDayEntity
import com.kxsv.schooldiary.data.local.features.subject.SubjectDao
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.remote.dtos.LessonDto
import com.kxsv.schooldiary.data.repository.StudyDayRepository
import com.kxsv.schooldiary.data.repository.SubjectRepository

private const val TAG = "LessonMapper"

suspend fun LessonDto.toLessonEntity(
	overrideStudyDayMasterId: Long?,
	subjectRepository: SubjectRepository,
	studyDayRepository: StudyDayRepository,
): LessonEntity {
	try {
		val subject = subjectRepository.getSubjectByName(subjectAncestorName)
			?: throw NoSuchElementException("There is no subject with name $subjectAncestorName")
		
		
		val studyDayMasterId = if (overrideStudyDayMasterId != null) {
			overrideStudyDayMasterId
		} else {
			val existedStudyDay = studyDayRepository.getByDate(date)
			val studyDayId = existedStudyDay?.studyDayId
				?: studyDayRepository.create(StudyDayEntity(date = date))
			studyDayId
		}
		
		return LessonEntity(
			index = index,
			subjectAncestorId = subject.subjectId,
			studyDayMasterId = studyDayMasterId
		)
	} catch (e: NoSuchElementException) {
		throw RuntimeException("Failed to convert class to local", e)
	}
}

/**
 * Convert [LessonDto] to local, opportunity to push the [overrideStudyDayMasterId]
 * which will be assigned to each [LessonEntity] after conversion, which is by default
 * is [current studyDay][com.kxsv.schooldiary.ui.screens.schedule.DayScheduleUiState.studyDay]
 * id or is null.
 *
 * @param overrideStudyDayMasterId value to override to each class [studyDayMasterId][com.kxsv.schooldiary.data.local.features.lesson.LessonEntity.studyDayMasterId] field
 * @return List<[LessonEntity]>
 */
suspend fun List<LessonDto>.toLessonEntities(
	overrideStudyDayMasterId: Long? = null,
	subjectRepository: SubjectRepository,
	studyDayRepository: StudyDayRepository,
): List<LessonEntity> {
	return try {
		this.map {
			it.toLessonEntity(
				overrideStudyDayMasterId = overrideStudyDayMasterId,
				subjectRepository = subjectRepository,
				studyDayRepository = studyDayRepository
			)
		}
	} catch (e: RuntimeException) {
		Log.e(TAG, "List<LessonDto>.toGradeEntities: classes are empty because", e)
		emptyList()
	}
}

suspend fun LessonDto.toLessonWithSubject(
	overrideStudyDayMasterId: Long? = null,
	subjectRepository: SubjectRepository,
	studyDayRepository: StudyDayRepository,
): LessonWithSubject {
	try {
		val subject = subjectRepository.getSubjectByName(subjectAncestorName)
			?: throw NoSuchElementException("There is no subject with name $subjectAncestorName")
		
		
		val studyDayMasterId = if (overrideStudyDayMasterId != null) {
			overrideStudyDayMasterId
		} else {
			val existedStudyDay = studyDayRepository.getByDate(date)
			val studyDayId = existedStudyDay?.studyDayId
				?: studyDayRepository.create(StudyDayEntity(date = date))
			studyDayId
		}
		return LessonWithSubject(
			lesson = LessonEntity(
				index = index,
				subjectAncestorId = subject.subjectId,
				studyDayMasterId = studyDayMasterId
			),
			subject = subject,
		)
	} catch (e: NoSuchElementException) {
		throw RuntimeException("Failed to convert class to LocalWithSubject", e)
	}
}

suspend fun LessonDto.toLessonWithSubject(
	overrideStudyDayMasterId: Long? = null,
	subjectDataSource: SubjectDao,
	studyDayDataSource: StudyDayDao,
): LessonWithSubject {
	try {
		val subject = subjectDataSource.getByName(subjectAncestorName)
			?: throw NoSuchElementException("There is no subject with name $subjectAncestorName")
		
		
		val studyDayMasterId = if (overrideStudyDayMasterId != null) {
			overrideStudyDayMasterId
		} else {
			val existedStudyDay = studyDayDataSource.getByDate(date)
			val studyDayId = existedStudyDay?.studyDayId
				?: studyDayDataSource.upsert(StudyDayEntity(date = date))
			studyDayId
		}
		return LessonWithSubject(
			lesson = LessonEntity(
				index = index,
				subjectAncestorId = subject.subjectId,
				studyDayMasterId = studyDayMasterId
			),
			subject = subject,
		)
	} catch (e: NoSuchElementException) {
		throw RuntimeException("Failed to convert class to LocalWithSubject", e)
	}
}

suspend fun List<LessonDto>.toLessonsWithSubjectIndexed(
	subjectRepository: SubjectRepository,
	studyDayRepository: StudyDayRepository,
): Map<Int, LessonWithSubject> {
	return try {
		val newMap = mutableMapOf<Int, LessonWithSubject>()
		this.forEach {
			val localedClass = it.toLessonWithSubject(
				subjectRepository = subjectRepository,
				studyDayRepository = studyDayRepository
			)
			newMap[it.index] = localedClass
		}
		newMap
	} catch (e: RuntimeException) {
		Log.e(
			TAG, "List<LessonDto>.toLessonsWithSubjectIndexed: classes are empty because", e
		)
		emptyMap()
	}
}

suspend fun List<LessonDto>.toLessonsWithSubjectIndexed(
	subjectDataSource: SubjectDao,
	studyDayDataSource: StudyDayDao,
): Map<Int, LessonWithSubject> {
	return try {
		val newMap = mutableMapOf<Int, LessonWithSubject>()
		this.forEach {
			val localedClass = it.toLessonWithSubject(
				subjectDataSource = subjectDataSource,
				studyDayDataSource = studyDayDataSource
			)
			newMap[it.index] = localedClass
		}
		newMap
	} catch (e: RuntimeException) {
		Log.e(
			TAG, "List<LessonDto>.toLessonsWithSubjectIndexed: classes are empty because", e
		)
		emptyMap()
	}
}

suspend fun List<LessonDto>.toSubjectEntitiesIndexed(
	subjectRepository: SubjectRepository,
	studyDayRepository: StudyDayRepository,
): Map<Int, SubjectEntity?> {
	return try {
		val newMap = mutableMapOf<Int, SubjectEntity?>()
		this.forEach {
			val localedClass = it.toLessonWithSubject(
				subjectRepository = subjectRepository,
				studyDayRepository = studyDayRepository
			)
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

suspend fun List<LessonDto>.toSubjectEntitiesIndexed(
	subjectDataSource: SubjectDao,
	studyDayDataSource: StudyDayDao,
): Map<Int, SubjectEntity> {
	return try {
		val newMap = mutableMapOf<Int, SubjectEntity>()
		this.forEach {
			val localedClass = it.toLessonWithSubject(
				subjectDataSource = subjectDataSource,
				studyDayDataSource = studyDayDataSource
			)
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

suspend fun LessonDto.saveAsLessonWithSubject(
	overrideStudyDayMasterId: Long? = null,
	lessonsDataSource: LessonDao,
	subjectDataSource: SubjectDao,
	studyDayDataSource: StudyDayDao,
): LessonWithSubject {
	try {
		val subject = subjectDataSource.getByName(subjectAncestorName)
			?: throw NoSuchElementException("There is no subject with name $subjectAncestorName")
		
		val studyDayMasterId = if (overrideStudyDayMasterId != null) {
			overrideStudyDayMasterId
		} else {
			val existedStudyDay = studyDayDataSource.getByDate(date)
			val studyDayId = existedStudyDay?.studyDayId
				?: studyDayDataSource.upsert(StudyDayEntity(date = date))
			studyDayId
		}
		val localizedClass = LessonWithSubject(
			lesson = LessonEntity(
				index = index,
				subjectAncestorId = subject.subjectId,
				studyDayMasterId = studyDayMasterId
			),
			subject = subject,
		)
		lessonsDataSource.upsert(localizedClass.lesson)
		return localizedClass
	} catch (e: Exception) {
		throw RuntimeException("Failed to saveAsLessonWithSubject due to", e)
	}
}

/**
 * Saves given [LessonsDtos][this] to room db
 *
 * @param subjectDataSource
 * @param studyDayDataSource
 * @return
 */
suspend fun List<LessonDto>.save(
	lessonsDataSource: LessonDao,
	subjectDataSource: SubjectDao,
	studyDayDataSource: StudyDayDao,
): Map<Int, LessonWithSubject> {
	return try {
		val newMap = mutableMapOf<Int, LessonWithSubject>()
		this.forEach {
			val localedClass =
				it.saveAsLessonWithSubject(
					lessonsDataSource = lessonsDataSource,
					subjectDataSource = subjectDataSource,
					studyDayDataSource = studyDayDataSource
				)
			newMap[it.index] = localedClass
		}
		newMap
	} catch (e: RuntimeException) {
		Log.e(
			TAG, "List<LessonDto>.toLessonsWithSubjectIndexed: classes are empty because", e
		)
		emptyMap()
	}
}