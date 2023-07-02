package com.kxsv.schooldiary.di

import android.content.Context
import androidx.room.Room
import com.kxsv.schooldiary.data.AppDatabase
import com.kxsv.schooldiary.data.features.associative_tables.subject_teacher.SubjectTeacherDao
import com.kxsv.schooldiary.data.features.associative_tables.subject_teacher.SubjectTeacherRepository
import com.kxsv.schooldiary.data.features.associative_tables.subject_teacher.SubjectTeacherRepositoryImpl
import com.kxsv.schooldiary.data.features.schedule.ScheduleDao
import com.kxsv.schooldiary.data.features.schedule.ScheduleRepository
import com.kxsv.schooldiary.data.features.schedule.ScheduleRepositoryImpl
import com.kxsv.schooldiary.data.features.subjects.SubjectDao
import com.kxsv.schooldiary.data.features.subjects.SubjectRepository
import com.kxsv.schooldiary.data.features.subjects.SubjectRepositoryImpl
import com.kxsv.schooldiary.data.features.teachers.TeacherDao
import com.kxsv.schooldiary.data.features.teachers.TeacherRepository
import com.kxsv.schooldiary.data.features.teachers.TeacherRepositoryImpl
import com.kxsv.schooldiary.data.features.time_pattern.TimePatternDao
import com.kxsv.schooldiary.data.features.time_pattern.TimePatternRepository
import com.kxsv.schooldiary.data.features.time_pattern.TimePatternRepositoryImpl
import com.kxsv.schooldiary.data.features.time_pattern.pattern_stroke.PatternStrokeDao
import com.kxsv.schooldiary.data.features.time_pattern.pattern_stroke.PatternStrokeRepository
import com.kxsv.schooldiary.data.features.time_pattern.pattern_stroke.PatternStrokeRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
    abstract fun bindSubjectTeacherRepository(repository: SubjectTeacherRepositoryImpl): SubjectTeacherRepository

    @Binds
    @Singleton
    abstract fun bindScheduleRepository(repository: ScheduleRepositoryImpl): ScheduleRepository
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
}