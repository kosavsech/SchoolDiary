package com.kxsv.schooldiary.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import androidx.room.Room
import com.kxsv.schooldiary.data.AppDatabase
import com.kxsv.schooldiary.data.local.features.associative_tables.subject_teacher.SubjectTeacherDao
import com.kxsv.schooldiary.data.local.features.edu_performance.EduPerformanceDao
import com.kxsv.schooldiary.data.local.features.grade.GradeDao
import com.kxsv.schooldiary.data.local.features.lesson.LessonDao
import com.kxsv.schooldiary.data.local.features.study_day.StudyDayDao
import com.kxsv.schooldiary.data.local.features.subject.SubjectDao
import com.kxsv.schooldiary.data.local.features.task.TaskDao
import com.kxsv.schooldiary.data.local.features.teacher.TeacherDao
import com.kxsv.schooldiary.data.local.features.time_pattern.TimePatternDao
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeDao
import com.kxsv.schooldiary.data.local.user_preferences.UserPreferences
import com.kxsv.schooldiary.data.local.user_preferences.UserPreferencesSerializer
import com.kxsv.schooldiary.data.remote.WebService
import com.kxsv.schooldiary.data.remote.WebServiceImpl
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
import com.kxsv.schooldiary.data.repository.TaskRepository
import com.kxsv.schooldiary.data.repository.TaskRepositoryImpl
import com.kxsv.schooldiary.data.repository.TeacherRepository
import com.kxsv.schooldiary.data.repository.TeacherRepositoryImpl
import com.kxsv.schooldiary.data.repository.TimePatternRepository
import com.kxsv.schooldiary.data.repository.TimePatternRepositoryImpl
import com.kxsv.schooldiary.data.repository.UserPreferencesRepository
import com.kxsv.schooldiary.data.repository.UserPreferencesRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
	
	@Provides
	@Singleton
	fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
		return Room.databaseBuilder(
			context.applicationContext,
			AppDatabase::class.java,
			"app_db"
		).fallbackToDestructiveMigration().build()
	}
	
	@Provides
	fun provideTeacherDao(db: AppDatabase): TeacherDao = db.teacherDao()
	
	@Provides
	fun provideTimePatternDao(db: AppDatabase): TimePatternDao = db.timePatternDao()
	
	@Provides
	fun provideTimePatternStrokeDao(db: AppDatabase): PatternStrokeDao = db.timePatternStrokeDao()
	
	@Provides
	fun provideSubjectDao(db: AppDatabase): SubjectDao = db.subjectDao()
	
	@Provides
	fun provideSubjectTeacherDao(db: AppDatabase): SubjectTeacherDao = db.subjectTeacherDao()
	
	@Provides
	fun provideScheduleDao(db: AppDatabase): LessonDao = db.scheduleDao()
	
	@Provides
	fun provideStudyDayDao(db: AppDatabase): StudyDayDao = db.studyDayDao()
	
	@Provides
	fun provideGradeDao(db: AppDatabase): GradeDao = db.gradeDao()
	
	@Provides
	fun provideEduPerformanceDao(db: AppDatabase): EduPerformanceDao = db.eduPerformanceDao()
	
	@Provides
	fun provideTaskDao(db: AppDatabase): TaskDao = db.taskDao()
}

@InstallIn(SingletonComponent::class)
@Module
class DataStoreModule {
	
	@Singleton
	@Provides
	fun provideDataStore(@ApplicationContext appContext: Context): DataStore<UserPreferences> {
		return DataStoreFactory.create(
			serializer = UserPreferencesSerializer,
			corruptionHandler = ReplaceFileCorruptionHandler(
				produceNewData = { UserPreferences() }
			),
			scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
			produceFile = { appContext.dataStoreFile("app-settings.json") }
		)
	}
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
	
	@Singleton
	@Binds
	abstract fun bindScheduleNetworkDataSource(dataSource: WebServiceImpl): WebService
}