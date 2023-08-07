package com.kxsv.schooldiary.data.local.features.lesson

import androidx.room.*
import com.kxsv.schooldiary.data.local.features.DatabaseConstants
import com.kxsv.schooldiary.data.local.features.DatabaseConstants.LESSON_TABLE_NAME
import com.kxsv.schooldiary.data.local.features.DatabaseConstants.STUDY_DAY_TABLE_NAME
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface LessonDao {
	
	@Query("SELECT * FROM $LESSON_TABLE_NAME ")
	fun observeAll(): Flow<List<LessonEntity>>
	
	@Transaction
	@Query("SELECT * FROM $LESSON_TABLE_NAME WHERE studyDayMasterId = :studyDayId ORDER BY `index` ASC")
	fun observeAllWithSubjectByDate(studyDayId: Long): Flow<List<LessonWithSubject>>
	
	@MapInfo(keyColumn = "date")
	@Transaction
	@RewriteQueriesToDropUnusedColumns
	@Query(
		"SELECT * FROM $LESSON_TABLE_NAME " +
				"JOIN $STUDY_DAY_TABLE_NAME ON $STUDY_DAY_TABLE_NAME.studyDayId = $LESSON_TABLE_NAME.studyDayMasterId " +
				"WHERE $STUDY_DAY_TABLE_NAME.date >= :startRange AND $STUDY_DAY_TABLE_NAME.date <= :endRange " +
				"ORDER BY $STUDY_DAY_TABLE_NAME.date ASC"
	)
	fun observeDayAndLessonsWithSubjectByDateRange(
		startRange: LocalDate,
		endRange: LocalDate,
	): Flow<Map<LocalDate, List<LessonWithSubject>>>
	
	@Query("SELECT * FROM $LESSON_TABLE_NAME WHERE lessonId = :scheduleId")
	fun observeById(scheduleId: Long): Flow<LessonEntity>
	
	/*    @Transaction
		@Query("SELECT * FROM ${DatabaseConstants.LESSON_TABLE_NAME} WHERE scheduleId = :scheduleId")
		suspend fun observeByIdWithSubject(scheduleId: Int): LessonWithSubject?*/
	
	@Query("SELECT * FROM $LESSON_TABLE_NAME")
	suspend fun getAll(): List<LessonEntity>
	
	@Query("SELECT * FROM $LESSON_TABLE_NAME WHERE studyDayMasterId = :studyDayId ORDER BY `index` ASC")
	suspend fun getAllByMasterId(studyDayId: Long): List<LessonEntity>
	
	@MapInfo(keyColumn = "date")
	@Transaction
	@RewriteQueriesToDropUnusedColumns
	@Query(
		"SELECT * FROM $LESSON_TABLE_NAME " +
				"JOIN $STUDY_DAY_TABLE_NAME ON $STUDY_DAY_TABLE_NAME.studyDayId = $LESSON_TABLE_NAME.studyDayMasterId " +
				"WHERE $STUDY_DAY_TABLE_NAME.date >= :startRange AND $STUDY_DAY_TABLE_NAME.date <= :endRange " +
				"ORDER BY $STUDY_DAY_TABLE_NAME.date ASC"
	)
	suspend fun getDayAndLessonsWithSubjectByDateRange(
		startRange: LocalDate,
		endRange: LocalDate,
	): Map<LocalDate, List<LessonWithSubject>>
	
	@Transaction
	@Query("SELECT * FROM ${DatabaseConstants.LESSON_TABLE_NAME} WHERE studyDayMasterId = :studyDayId ORDER BY `index` ASC")
	suspend fun getAllWithSubjectByDate(studyDayId: Long): List<LessonWithSubject>
	
	@Query("SELECT * FROM $LESSON_TABLE_NAME WHERE lessonId = :scheduleId")
	suspend fun getById(scheduleId: Long): LessonEntity?
	
	@Transaction
	@Query("SELECT * FROM $LESSON_TABLE_NAME WHERE lessonId = :scheduleId")
	suspend fun getByIdWithSubject(scheduleId: Long): LessonWithSubject?
	
	@Transaction
	@Query("SELECT * FROM $LESSON_TABLE_NAME WHERE studyDayMasterId = :studyDayMasterId AND `index` = :index")
	suspend fun getByIdAndIndex(studyDayMasterId: Long, index: Int): LessonEntity?
	
	@Transaction
	@Query("SELECT * FROM $LESSON_TABLE_NAME WHERE lessonId = :scheduleId")
	suspend fun getByIdWithStudyDay(scheduleId: Long): LessonWithStudyDay?
	
	@Upsert
	suspend fun upsertAll(lessons: List<LessonEntity>)
	
	@Upsert
	suspend fun upsert(lesson: LessonEntity)
	
	@Query("DELETE FROM $LESSON_TABLE_NAME")
	suspend fun deleteAll()
	
	@Query("DELETE FROM $LESSON_TABLE_NAME WHERE studyDayMasterId = :studyDayMasterId")
	suspend fun deleteAllByDayId(studyDayMasterId: Long)
	
	@Query("DELETE FROM $LESSON_TABLE_NAME WHERE lessonId = :scheduleId")
	suspend fun deleteById(scheduleId: Long)
}