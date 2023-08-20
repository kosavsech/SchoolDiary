package com.kxsv.schooldiary.di.data_modules

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kxsv.schooldiary.data.local.AppDatabase
import com.kxsv.schooldiary.data.local.features.associative_tables.subject_teacher.SubjectTeacherDao
import com.kxsv.schooldiary.data.local.features.edu_performance.EduPerformanceDao
import com.kxsv.schooldiary.data.local.features.grade.GradeDao
import com.kxsv.schooldiary.data.local.features.lesson.LessonDao
import com.kxsv.schooldiary.data.local.features.study_day.StudyDayDao
import com.kxsv.schooldiary.data.local.features.subject.SubjectDao
import com.kxsv.schooldiary.data.local.features.task.TaskDao
import com.kxsv.schooldiary.data.local.features.teacher.TeacherDao
import com.kxsv.schooldiary.data.local.features.time_pattern.TimePatternDao
import com.kxsv.schooldiary.data.local.features.time_pattern.TimePatternEntity
import com.kxsv.schooldiary.data.local.features.time_pattern.TimePatternWithStrokes
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeDao
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeEntity
import com.kxsv.schooldiary.data.local.user_preferences.UserPreferences
import com.kxsv.schooldiary.di.util.AppDispatchers
import com.kxsv.schooldiary.di.util.ApplicationScope
import com.kxsv.schooldiary.di.util.Dispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
	
	@Provides
	@Singleton
	fun provideDatabase(
		@ApplicationContext context: Context,
		@ApplicationScope applicationScope: CoroutineScope,
		@Dispatcher(AppDispatchers.IO) ioDispatcher: CoroutineDispatcher,
		patternDaoProvider: Provider<TimePatternDao>,
		strokeDaoProvider: Provider<PatternStrokeDao>,
		dataStore: Provider<DataStore<UserPreferences>>,
	): AppDatabase {
		return Room.databaseBuilder(
			context.applicationContext,
			AppDatabase::class.java,
			"school_diary.db"
		)
			.addCallback(
				TimePatternCallBack(
					applicationScope = applicationScope,
					ioDispatcher = ioDispatcher,
					patternDaoProvider = patternDaoProvider,
					strokeDaoProvider = strokeDaoProvider,
					dataStore = dataStore
				)
			)
			.fallbackToDestructiveMigration()
			.build()
	}
	
	
	class TimePatternCallBack(
		@ApplicationScope private val applicationScope: CoroutineScope,
		@Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
		private val patternDaoProvider: Provider<TimePatternDao>,
		private val strokeDaoProvider: Provider<PatternStrokeDao>,
		private val dataStore: Provider<DataStore<UserPreferences>>,
	) : RoomDatabase.Callback() {
		override fun onCreate(db: SupportSQLiteDatabase) {
			super.onCreate(db)
			applicationScope.launch(ioDispatcher) {
				populateDatabase()
			}
		}
		
		private suspend fun populateDatabase() {
			val timePatternEntities = listOf(
				TimePatternWithStrokes(
					timePattern = TimePatternEntity("Default", 1),
					strokes = listOf(
						PatternStrokeEntity(
							index = 0,
							startTime = LocalTime.of(8, 30),
							endTime = LocalTime.of(9, 15),
						),
						PatternStrokeEntity(
							index = 1,
							startTime = LocalTime.of(9, 30),
							endTime = LocalTime.of(10, 15)
						),
						PatternStrokeEntity(
							index = 2,
							startTime = LocalTime.of(10, 30),
							endTime = LocalTime.of(11, 15)
						),
						PatternStrokeEntity(
							index = 3,
							startTime = LocalTime.of(11, 25),
							endTime = LocalTime.of(12, 10)
						),
						PatternStrokeEntity(
							index = 4,
							startTime = LocalTime.of(12, 30),
							endTime = LocalTime.of(13, 15)
						),
						PatternStrokeEntity(
							index = 5,
							startTime = LocalTime.of(13, 30),
							endTime = LocalTime.of(14, 20)
						),
						PatternStrokeEntity(
							index = 6,
							startTime = LocalTime.of(14, 30),
							endTime = LocalTime.of(15, 15)
						),
					)
				),
				TimePatternWithStrokes(
					timePattern = TimePatternEntity("Monday"),
					strokes = listOf(
						PatternStrokeEntity(
							index = 0,
							startTime = LocalTime.of(8, 30),
							endTime = LocalTime.of(9, 10),
						),
						PatternStrokeEntity(
							index = 1,
							startTime = LocalTime.of(9, 25),
							endTime = LocalTime.of(10, 5)
						),
						PatternStrokeEntity(
							index = 2,
							startTime = LocalTime.of(10, 15),
							endTime = LocalTime.of(10, 55)
						),
						PatternStrokeEntity(
							index = 3,
							startTime = LocalTime.of(11, 5),
							endTime = LocalTime.of(11, 45)
						),
						PatternStrokeEntity(
							index = 4,
							startTime = LocalTime.of(12, 5),
							endTime = LocalTime.of(12, 45)
						),
						PatternStrokeEntity(
							index = 5,
							startTime = LocalTime.of(13, 0),
							endTime = LocalTime.of(13, 40)
						),
						PatternStrokeEntity(
							index = 6,
							startTime = LocalTime.of(13, 50),
							endTime = LocalTime.of(14, 30)
						),
					)
				),
				TimePatternWithStrokes(
					timePattern = TimePatternEntity("Saturday"),
					strokes = listOf(
						PatternStrokeEntity(
							index = 0,
							startTime = LocalTime.of(8, 30),
							endTime = LocalTime.of(9, 10),
						),
						PatternStrokeEntity(
							index = 1,
							startTime = LocalTime.of(9, 20),
							endTime = LocalTime.of(10, 0)
						),
						PatternStrokeEntity(
							index = 2,
							startTime = LocalTime.of(10, 15),
							endTime = LocalTime.of(10, 55)
						),
						PatternStrokeEntity(
							index = 3,
							startTime = LocalTime.of(11, 10),
							endTime = LocalTime.of(11, 50)
						),
						PatternStrokeEntity(
							index = 4,
							startTime = LocalTime.of(12, 5),
							endTime = LocalTime.of(12, 45)
						),
						PatternStrokeEntity(
							index = 5,
							startTime = LocalTime.of(12, 55),
							endTime = LocalTime.of(13, 35)
						),
						PatternStrokeEntity(
							index = 6,
							startTime = LocalTime.of(13, 40),
							endTime = LocalTime.of(14, 20)
						),
					)
				),
			)
			timePatternEntities.forEach { patternWithStrokes ->
				val masterId = patternDaoProvider.get().upsert(patternWithStrokes.timePattern)
				if (patternWithStrokes.timePattern.name == "Default") {
					dataStore.get().updateData { it.copy(defaultPatternId = masterId) }
				}
				strokeDaoProvider.get().upsertAll(
					patternWithStrokes.strokes.map { strokes ->
						strokes.copy(patternMasterId = masterId)
					}
				)
			}
		}
	}
	
	@Provides
	fun provideTeacherDao(db: AppDatabase): TeacherDao = db.teacherDao()
	
	@Provides
	fun provideTimePatternDao(db: AppDatabase): TimePatternDao = db.timePatternDao()
	
	@Provides
	fun provideTimePatternStrokeDao(db: AppDatabase): PatternStrokeDao =
		db.timePatternStrokeDao()
	
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