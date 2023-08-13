package com.kxsv.schooldiary.di.data_modules

import android.content.Context
import androidx.room.Room
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
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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