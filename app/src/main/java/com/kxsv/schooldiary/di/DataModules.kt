package com.kxsv.schooldiary.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import androidx.room.Room
import com.kxsv.schooldiary.data.AppDatabase
import com.kxsv.schooldiary.data.app_settings.AppSettings
import com.kxsv.schooldiary.data.app_settings.AppSettingsRepositoryImpl
import com.kxsv.schooldiary.data.app_settings.AppSettingsSerializer
import com.kxsv.schooldiary.data.local.features.associative_tables.subject_teacher.SubjectTeacherDao
import com.kxsv.schooldiary.data.local.features.grade.GradeDao
import com.kxsv.schooldiary.data.local.features.grade.GradeRepositoryImpl
import com.kxsv.schooldiary.data.local.features.schedule.ScheduleDao
import com.kxsv.schooldiary.data.local.features.schedule.ScheduleRepositoryImpl
import com.kxsv.schooldiary.data.local.features.study_day.StudyDayDao
import com.kxsv.schooldiary.data.local.features.study_day.StudyDayRepositoryImpl
import com.kxsv.schooldiary.data.local.features.subject.SubjectDao
import com.kxsv.schooldiary.data.local.features.subject.SubjectRepositoryImpl
import com.kxsv.schooldiary.data.local.features.teacher.TeacherDao
import com.kxsv.schooldiary.data.local.features.teacher.TeacherRepositoryImpl
import com.kxsv.schooldiary.data.local.features.time_pattern.TimePatternDao
import com.kxsv.schooldiary.data.local.features.time_pattern.TimePatternRepositoryImpl
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeDao
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeRepositoryImpl
import com.kxsv.schooldiary.data.network.NetworkDataSourceImpl
import com.kxsv.schooldiary.domain.AppSettingsRepository
import com.kxsv.schooldiary.domain.GradeRepository
import com.kxsv.schooldiary.domain.NetworkDataSource
import com.kxsv.schooldiary.domain.PatternStrokeRepository
import com.kxsv.schooldiary.domain.ScheduleRepository
import com.kxsv.schooldiary.domain.StudyDayRepository
import com.kxsv.schooldiary.domain.SubjectRepository
import com.kxsv.schooldiary.domain.TeacherRepository
import com.kxsv.schooldiary.domain.TimePatternRepository
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
	
	/*@Binds
	@Singleton
	abstract fun bindSubjectTeacherRepository(repository: SubjectTeacherRepositoryImpl): SubjectTeacherRepository*/
	
	@Binds
	@Singleton
	abstract fun bindScheduleRepository(repository: ScheduleRepositoryImpl): ScheduleRepository
	
	@Binds
	@Singleton
	abstract fun bindAppDefaultsRepository(repository: AppSettingsRepositoryImpl): AppSettingsRepository
	
	@Binds
	@Singleton
	abstract fun bindStudyDayRepository(repository: StudyDayRepositoryImpl): StudyDayRepository
	
	@Binds
	@Singleton
	abstract fun bindGradeRepository(repository: GradeRepositoryImpl): GradeRepository
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
	fun provideScheduleDao(db: AppDatabase): ScheduleDao = db.scheduleDao()
	
	@Provides
	fun provideStudyDayDao(db: AppDatabase): StudyDayDao = db.studyDayDao()
	
	@Provides
	fun provideGradeDao(db: AppDatabase): GradeDao = db.gradeDao()
}

@InstallIn(SingletonComponent::class)
@Module
class DataStoreModule {
	
	@Singleton
	@Provides
	fun provideDataStore(@ApplicationContext appContext: Context): DataStore<AppSettings> {
		return DataStoreFactory.create(
			serializer = AppSettingsSerializer,
			corruptionHandler = ReplaceFileCorruptionHandler(
				produceNewData = { AppSettings() }
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
	abstract fun bindScheduleNetworkDataSource(dataSource: NetworkDataSourceImpl): NetworkDataSource
}