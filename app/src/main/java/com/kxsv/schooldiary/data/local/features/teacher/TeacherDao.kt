package com.kxsv.schooldiary.data.local.features.teacher

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.kxsv.schooldiary.data.local.features.DatabaseConstants.TEACHER_TABLE_NAME
import kotlinx.coroutines.flow.Flow

@Dao
interface TeacherDao {
	
	@Query("SELECT * FROM $TEACHER_TABLE_NAME ORDER BY patronymic ASC")
	fun observeAll(): Flow<List<TeacherEntity>>
	
	@Query("SELECT * FROM $TEACHER_TABLE_NAME WHERE teacherId = :teacherId")
	fun observeById(teacherId: String): Flow<TeacherEntity>
	
	@Query("SELECT * FROM $TEACHER_TABLE_NAME ORDER BY patronymic ASC")
	suspend fun getAll(): List<TeacherEntity>
	
	@Query("SELECT * FROM $TEACHER_TABLE_NAME WHERE teacherId = :teacherId")
	suspend fun getById(teacherId: String): TeacherEntity?
	
	@Query("SELECT * FROM $TEACHER_TABLE_NAME WHERE lastName = :lastName AND firstName = :firstName AND patronymic = :patronymic")
	suspend fun getByFullName(
		lastName: String,
		firstName: String,
		patronymic: String,
	): TeacherEntity?
	
	@Transaction
	@Query("SELECT * FROM $TEACHER_TABLE_NAME WHERE teacherId = :teacherId")
	suspend fun getByIdWithSubjects(teacherId: String): TeacherWithSubjects?
	
	@Upsert
	suspend fun upsertAll(teachers: List<TeacherEntity>)
	
	@Upsert
	suspend fun upsert(teacher: TeacherEntity)
	
	@Update
	suspend fun update(teacher: TeacherEntity)
	
	@Query("DELETE FROM $TEACHER_TABLE_NAME")
	suspend fun deleteAll()
	
	@Query("DELETE FROM $TEACHER_TABLE_NAME WHERE teacherId = :teacherId")
	suspend fun deleteById(teacherId: String)
}