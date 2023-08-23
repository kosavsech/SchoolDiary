package com.kxsv.schooldiary.di.data_modules

import com.kxsv.schooldiary.data.repository.EduPerformanceRepository
import com.kxsv.schooldiary.data.repository.EduPerformanceRepositoryImpl
import com.kxsv.schooldiary.data.repository.GradeRepository
import com.kxsv.schooldiary.data.repository.GradeRepositoryImpl
import com.kxsv.schooldiary.data.repository.LessonRepository
import com.kxsv.schooldiary.data.repository.LessonRepositoryImpl
import com.kxsv.schooldiary.data.repository.PatternStrokeRepository
import com.kxsv.schooldiary.data.repository.PatternStrokeRepositoryImpl
import com.kxsv.schooldiary.data.repository.StudyDayRepository
import com.kxsv.schooldiary.data.repository.StudyDayRepositoryImpl
import com.kxsv.schooldiary.data.repository.SubjectRepository
import com.kxsv.schooldiary.data.repository.SubjectRepositoryImpl
import com.kxsv.schooldiary.data.repository.SubjectTeacherRepository
import com.kxsv.schooldiary.data.repository.SubjectTeacherRepositoryImpl
import com.kxsv.schooldiary.data.repository.TaskRepository
import com.kxsv.schooldiary.data.repository.TaskRepositoryImpl
import com.kxsv.schooldiary.data.repository.TeacherRepository
import com.kxsv.schooldiary.data.repository.TeacherRepositoryImpl
import com.kxsv.schooldiary.data.repository.TimePatternRepository
import com.kxsv.schooldiary.data.repository.TimePatternRepositoryImpl
import com.kxsv.schooldiary.data.repository.UpdateRepository
import com.kxsv.schooldiary.data.repository.UpdateRepositoryImpl
import com.kxsv.schooldiary.data.repository.UserPreferencesRepository
import com.kxsv.schooldiary.data.repository.UserPreferencesRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
	
	@Binds
	@Singleton
	abstract fun bindTeacherRepository(repository: TeacherRepositoryImpl): TeacherRepository
	
	@Binds
	@Singleton
	abstract fun bindTimePatternRepository(repository: TimePatternRepositoryImpl): TimePatternRepository
	
	@Binds
	@Singleton
	abstract fun bindTimePatternStrokeRepository(repository: PatternStrokeRepositoryImpl): PatternStrokeRepository
	
	@Binds
	@Singleton
	abstract fun bindSubjectRepository(repository: SubjectRepositoryImpl): SubjectRepository
	
	@Binds
	@Singleton
	abstract fun bindScheduleRepository(repository: LessonRepositoryImpl): LessonRepository
	
	@Binds
	@Singleton
	abstract fun bindAppDefaultsRepository(repository: UserPreferencesRepositoryImpl): UserPreferencesRepository
	
	@Binds
	@Singleton
	abstract fun bindStudyDayRepository(repository: StudyDayRepositoryImpl): StudyDayRepository
	
	@Binds
	@Singleton
	abstract fun bindGradeRepository(repository: GradeRepositoryImpl): GradeRepository
	
	@Binds
	@Singleton
	abstract fun bindEduPerformanceRepository(repository: EduPerformanceRepositoryImpl): EduPerformanceRepository
	
	@Binds
	@Singleton
	abstract fun bindTaskRepository(repository: TaskRepositoryImpl): TaskRepository
	
	@Binds
	@Singleton
	abstract fun bindSubjectTeacherRepository(repository: SubjectTeacherRepositoryImpl): SubjectTeacherRepository
	
	@Binds
	@Singleton
	abstract fun bindUpdateRepository(repository: UpdateRepositoryImpl): UpdateRepository
}

