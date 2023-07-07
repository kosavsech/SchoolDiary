package com.kxsv.schooldiary.data.features.subjects

import com.kxsv.schooldiary.data.features.associative_tables.subject_teacher.SubjectTeacher
import com.kxsv.schooldiary.data.features.associative_tables.subject_teacher.SubjectTeacherDao
import com.kxsv.schooldiary.data.features.teachers.Teacher
import com.kxsv.schooldiary.domain.SubjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubjectRepositoryImpl @Inject constructor(
    private val subjectDataSource: SubjectDao,
    private val subjectTeacherDataSource: SubjectTeacherDao,
    //@DefaultDispatcher private val dispatcher: CoroutineDispatcher,
) : SubjectRepository {

    override fun getSubjectsStream(): Flow<List<Subject>> {
        return subjectDataSource.observeAll()
    }

    override fun getSubjectStream(subjectId: Long): Flow<Subject> {
        return subjectDataSource.observeById(subjectId)
    }

    override suspend fun getSubjects(): List<Subject> {
        return subjectDataSource.getAll()
    }

    override suspend fun getSubject(subjectId: Long): Subject? {
        return subjectDataSource.getById(subjectId)
    }

    override suspend fun getSubjectWithTeachers(subjectId: Long): SubjectWithTeachers? {
        return subjectDataSource.getByIdWithTeachers(subjectId)
    }

    override suspend fun createSubject(subject: Subject, teachers: Set<Teacher>) {
        val subjectId = subjectDataSource.upsert(subject)

        teachers.forEach {
            subjectTeacherDataSource.upsert(
                SubjectTeacher(
                    subjectId = subjectId,
                    teacherId = it.teacherId
                )
            )
        }
    }

    override suspend fun updateSubject(subject: Subject, teachers: Set<Teacher>) {
        subjectDataSource.upsert(subject)

        subjectTeacherDataSource.deleteBySubjectId(subject.subjectId)
        teachers.forEach { teacher ->
            subjectTeacherDataSource.upsert(
                SubjectTeacher(subject.subjectId, teacher.teacherId)
            )
        }
    }

    override suspend fun deleteAllSubjects() {
        subjectDataSource.deleteAll()
    }

    override suspend fun deleteSubject(subjectId: Long) {
        subjectDataSource.deleteById(subjectId)
    }

}