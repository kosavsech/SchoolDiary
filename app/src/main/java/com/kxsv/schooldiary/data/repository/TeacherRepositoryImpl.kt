package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.teacher.TeacherDao
import com.kxsv.schooldiary.data.local.features.teacher.TeacherEntity
import com.kxsv.schooldiary.data.local.features.teacher.TeacherEntity.Companion.fullName
import com.kxsv.schooldiary.data.local.features.teacher.TeacherWithSubjects
import com.kxsv.schooldiary.data.util.DataIdGenUtils.generateTeacherId
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeacherRepositoryImpl @Inject constructor(
	private val teacherDataSource: TeacherDao,
) : TeacherRepository {
	
	override fun observeTeachers(): Flow<List<TeacherEntity>> {
		return teacherDataSource.observeAll()
	}
	
	override fun getTeacherStream(teacherId: String): Flow<TeacherEntity> {
		return teacherDataSource.observeById(teacherId)
	}
	
	override suspend fun getTeachers(): List<TeacherEntity> {
		return teacherDataSource.getAll()
	}
	
	override suspend fun getById(teacherId: String): TeacherEntity? {
		return teacherDataSource.getById(teacherId)
	}
	
	override suspend fun getByFullName(
		lastName: String,
		firstName: String,
		patronymic: String,
	): TeacherEntity? {
		return teacherDataSource.getByFullName(lastName, firstName, patronymic)
	}
	
	override suspend fun getTeacherWithSubjects(teacherId: String): TeacherWithSubjects? {
		return teacherDataSource.getByIdWithSubjects(teacherId)
	}
	
	override suspend fun createTeacher(teacher: TeacherEntity): String {
		val teacherId = generateTeacherId(teacher.fullName())
		teacherDataSource.upsert(teacher.copy(teacherId = teacherId))
		return teacherId
	}
	
	override suspend fun upsert(teacher: TeacherEntity) {
		teacherDataSource.upsert(teacher)
	}
	
	override suspend fun update(teacher: TeacherEntity) {
		teacherDataSource.update(teacher)
	}
	
	override suspend fun deleteTeachers() {
		teacherDataSource.deleteAll()
	}
	
	override suspend fun deleteTeacher(teacherId: String) {
		teacherDataSource.deleteById(teacherId)
	}
	
}