package com.kxsv.schooldiary.data.features.grade

import com.kxsv.schooldiary.domain.GradeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GradeRepositoryImpl @Inject constructor(
	private val gradeDataSource: GradeDao,
) : GradeRepository {
	
	override fun getGradesStream(): Flow<List<Grade>> {
		return gradeDataSource.observeAll()
	}
	
	override fun getGradesBySubjectIdStream(subjectId: Long): Flow<List<Grade>> {
		return gradeDataSource.observeAllBySubjectId(subjectId)
	}
	
	override fun getGradeStream(gradeId: Long): Flow<Grade> {
		return gradeDataSource.observeById(gradeId)
	}
	
	override suspend fun getGrades(): List<Grade> {
		return gradeDataSource.getAll()
	}
	
	override suspend fun getGrade(gradeId: Long): Grade? {
		return gradeDataSource.getById(gradeId)
	}
	
	override suspend fun createGrade(grade: Grade): Long {
		return gradeDataSource.upsert(grade)
	}
	
	override suspend fun updateGrade(grade: Grade) {
		gradeDataSource.upsert(grade)
	}
	
	override suspend fun deleteAllGrades() {
		gradeDataSource.deleteAll()
	}
	
	override suspend fun deleteGrade(gradeId: Long) {
		gradeDataSource.deleteById(gradeId)
	}
	
}