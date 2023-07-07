package com.kxsv.schooldiary.data.features.teachers

import com.kxsv.schooldiary.domain.TeacherRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeacherRepositoryImpl @Inject constructor(
    private val teacherDataSource: TeacherDao,
) : TeacherRepository {

    override fun getTeachersStream(): Flow<List<Teacher>> {
        return teacherDataSource.observeAll()
    }

    override fun getTeacherStream(teacherId: Int): Flow<Teacher> {
        return teacherDataSource.observeById(teacherId)
    }

    override suspend fun getTeachers(): List<Teacher> {
        return teacherDataSource.getAll()
    }

    override suspend fun getTeacher(teacherId: Int): Teacher? {
        return teacherDataSource.getById(teacherId)
    }

    override suspend fun getTeacherWithSubjects(teacherId: Int): TeacherWithSubjects? {
        return teacherDataSource.getByIdWithSubjects(teacherId)
    }

    override suspend fun createTeacher(teacher: Teacher) {
        teacherDataSource.upsert(teacher)
    }

    override suspend fun updateTeacher(teacher: Teacher) {
        teacherDataSource.upsert(teacher)
    }

    override suspend fun deleteTeachers() {
        teacherDataSource.deleteAll()
    }

    override suspend fun deleteTeacher(teacherId: Int) {
        teacherDataSource.deleteById(teacherId)
    }

}