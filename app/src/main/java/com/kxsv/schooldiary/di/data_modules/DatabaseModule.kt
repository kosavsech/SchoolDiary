package com.kxsv.schooldiary.di.data_modules

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
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
		val MIGRATION_3_4 = object : Migration(3, 4) {
			override fun migrate(db: SupportSQLiteDatabase) {
				db.execSQL(
					"""
                CREATE TABLE new_grade (`mark` TEXT NOT NULL, `typeOfWork` TEXT NOT NULL,
				`date` INTEGER NOT NULL, `fetchDateTime` INTEGER NOT NULL,
				`subjectMasterId` TEXT NOT NULL, `lessonIndex` INTEGER NOT NULL,
				`index` INTEGER NOT NULL, `gradeId` TEXT NOT NULL, PRIMARY KEY(`gradeId`),
				FOREIGN KEY(`subjectMasterId`) REFERENCES `subject`(`subjectId`)
				ON UPDATE NO ACTION ON DELETE CASCADE )
                """.trimIndent()
				)
				db.execSQL(
					"""
                INSERT INTO new_grade (`mark`, `typeOfWork`, `date`,
				`fetchDateTime`, `subjectMasterId`, `lessonIndex`,
				`index`, `gradeId`)
                SELECT `mark`, `typeOfWork`, `date`, `fetchDateTime`, `subjectMasterId`,
				`lessonIndex`, `index`, `gradeId` FROM grade
                """.trimIndent()
				)
				db.execSQL("DROP TABLE grade")
				db.execSQL("ALTER TABLE new_grade RENAME TO grade")
				///
				db.execSQL(
					"""
                CREATE TABLE new_eduPerformance (`subjectMasterId` TEXT NOT NULL, `marks` TEXT NOT NULL,
				`finalMark` TEXT, `examMark` TEXT, `period` TEXT NOT NULL,
				`eduPerformanceId` TEXT NOT NULL,
				PRIMARY KEY(`eduPerformanceId`),
				FOREIGN KEY(`subjectMasterId`) REFERENCES `subject`(`subjectId`)
				ON UPDATE NO ACTION ON DELETE CASCADE )
                """.trimIndent()
				)
				db.execSQL(
					"""
                INSERT INTO new_eduPerformance (`subjectMasterId`, `marks`, `finalMark`,
				`examMark`, `period`, `eduPerformanceId`)
                SELECT `subjectMasterId`, `marks`, `finalMark`,
				`examMark`, `period`, `eduPerformanceId` FROM eduPerformance
                """.trimIndent()
				)
				db.execSQL("DROP TABLE eduPerformance")
				db.execSQL("ALTER TABLE new_eduPerformance RENAME TO eduPerformance")
			}
		}
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
//			.addMigrations(MIGRATION_3_4)
//			.fallbackToDestructiveMigration()
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
					timePattern = TimePatternEntity("Default"),
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
							endTime = LocalTime.of(14, 15)
						),
						PatternStrokeEntity(
							index = 6,
							startTime = LocalTime.of(14, 25),
							endTime = LocalTime.of(15, 10)
						),
						PatternStrokeEntity(
							index = 7,
							startTime = LocalTime.of(15, 20),
							endTime = LocalTime.of(16, 5)
						),
					)
				),
				TimePatternWithStrokes(
					timePattern = TimePatternEntity("Monday/Thursday"),
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
							startTime = LocalTime.of(10, 20),
							endTime = LocalTime.of(11, 0)
						),
						PatternStrokeEntity(
							index = 3,
							startTime = LocalTime.of(11, 10),
							endTime = LocalTime.of(11, 50)
						),
						PatternStrokeEntity(
							index = 4,
							startTime = LocalTime.of(12, 10),
							endTime = LocalTime.of(12, 50)
						),
						PatternStrokeEntity(
							index = 5,
							startTime = LocalTime.of(13, 5),
							endTime = LocalTime.of(13, 45)
						),
						PatternStrokeEntity(
							index = 6,
							startTime = LocalTime.of(13, 55),
							endTime = LocalTime.of(14, 35)
						),
						PatternStrokeEntity(
							index = 7,
							startTime = LocalTime.of(14, 45),
							endTime = LocalTime.of(15, 25)
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
							startTime = LocalTime.of(12, 0),
							endTime = LocalTime.of(12, 40)
						),
						PatternStrokeEntity(
							index = 5,
							startTime = LocalTime.of(12, 45),
							endTime = LocalTime.of(13, 25)
						),
						PatternStrokeEntity(
							index = 6,
							startTime = LocalTime.of(13, 30),
							endTime = LocalTime.of(14, 10)
						),
						PatternStrokeEntity(
							index = 7,
							startTime = LocalTime.of(14, 15),
							endTime = LocalTime.of(14, 55)
						),
					)
				),
				TimePatternWithStrokes(
					timePattern = TimePatternEntity("с 8:00 по 40"),
					strokes = listOf(
						PatternStrokeEntity(
							index = 0,
							startTime = LocalTime.of(8, 0),
							endTime = LocalTime.of(8, 40),
						),
						PatternStrokeEntity(
							index = 1,
							startTime = LocalTime.of(8, 45),
							endTime = LocalTime.of(9, 25)
						),
						PatternStrokeEntity(
							index = 2,
							startTime = LocalTime.of(9, 40),
							endTime = LocalTime.of(10, 20)
						),
						PatternStrokeEntity(
							index = 3,
							startTime = LocalTime.of(10, 35),
							endTime = LocalTime.of(11, 15)
						),
						PatternStrokeEntity(
							index = 4,
							startTime = LocalTime.of(11, 25),
							endTime = LocalTime.of(12, 5)
						),
						PatternStrokeEntity(
							index = 5,
							startTime = LocalTime.of(12, 25),
							endTime = LocalTime.of(13, 5)
						),
						PatternStrokeEntity(
							index = 6,
							startTime = LocalTime.of(13, 20),
							endTime = LocalTime.of(14, 0)
						),
						PatternStrokeEntity(
							index = 7,
							startTime = LocalTime.of(14, 10),
							endTime = LocalTime.of(14, 50)
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