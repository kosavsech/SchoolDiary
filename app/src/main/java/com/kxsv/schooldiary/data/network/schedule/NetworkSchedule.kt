package com.kxsv.schooldiary.data.network.schedule

import com.kxsv.schooldiary.data.local.features.schedule.Schedule
import com.kxsv.schooldiary.data.local.features.schedule.ScheduleWithSubject
import com.kxsv.schooldiary.domain.StudyDayRepository
import com.kxsv.schooldiary.domain.SubjectRepository
import java.time.LocalDate

data class NetworkSchedule(
	val index: Int,
	val date: LocalDate,
	val subjectAncestorName: String,
) {
	suspend fun toLocal(
		studyDayMasterId: Long?,
		subjectRepository: SubjectRepository,
		studyDayRepository: StudyDayRepository,
	): Schedule {
		try {
			val subjectMasterId =
				subjectRepository.getSubjectIdByName(subjectAncestorName)
					?: throw NoSuchElementException("Not found subject with name $subjectAncestorName")
			// TODO: add prompt to create subject with such name, or create it forcibly
			
			val studyDay = studyDayRepository.getByDate(date)
			val localizedClass = Schedule(
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
	
	suspend fun toLocalWithSubject(
		subjectRepository: SubjectRepository,
		studyDayRepository: StudyDayRepository,
	): ScheduleWithSubject {
		try {
			val subject =
				subjectRepository.getSubjectByName(subjectAncestorName)
					?: throw NoSuchElementException("Not found subject with name $subjectAncestorName")
			// TODO: add prompt to create subject with such name, or create it forcibly
			
			val studyDay = studyDayRepository.getByDate(date)
			val localizedClass = ScheduleWithSubject(
				schedule = Schedule(
					index = index,
					subjectAncestorId = subject.subjectId,
				),
				subject = subject,
			)
			return if (studyDay != null) {
				localizedClass.copy(schedule = localizedClass.schedule.copy(studyDayMasterId = studyDay.studyDayId))
			} else {
				localizedClass
			}
		} catch (e: NoSuchElementException) {
			throw RuntimeException("Failed to convert class to LocalWithSubject", e)
		}
	}
}