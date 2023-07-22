package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.teacher.TeacherDao
import com.kxsv.schooldiary.data.local.features.teacher.TeacherEntity
import com.kxsv.schooldiary.data.local.features.teacher.TeacherWithSubjects
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeacherRepositoryImpl @Inject constructor(
	private val teacherDataSource: TeacherDao,
) : TeacherRepository {
	
	override fun getTeachersStream(): Flow<List<TeacherEntity>> {
		return teacherDataSource.observeAll()
	}
	
	override fun getTeacherStream(teacherId: Int): Flow<TeacherEntity> {
		return teacherDataSource.observeById(teacherId)
	}
	
	override suspend fun getTeachers(): List<TeacherEntity> {
		return teacherDataSource.getAll()
	}
	
	override suspend fun getTeacher(teacherId: Int): TeacherEntity? {
		return teacherDataSource.getById(teacherId)
	}

    override suspend fun getTeacherWithSubjects(teacherId: Int): TeacherWithSubjects? {
        return teacherDataSource.getByIdWithSubjects(teacherId)
    }
	
	override suspend fun createTeacher(teacher: TeacherEntity) {
		teacherDataSource.upsert(teacher)
	}
	
	override suspend fun updateTeacher(teacher: TeacherEntity) {
		teacherDataSource.upsert(teacher)
	}

    override suspend fun deleteTeachers() {
        teacherDataSource.deleteAll()
    }

    override suspend fun deleteTeacher(teacherId: Int) {
        teacherDataSource.deleteById(teacherId)
    }

}