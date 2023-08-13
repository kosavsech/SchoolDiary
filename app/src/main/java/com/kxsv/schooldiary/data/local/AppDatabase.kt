package com.kxsv.schooldiary.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kxsv.schooldiary.data.local.features.associative_tables.subject_teacher.SubjectTeacher
import com.kxsv.schooldiary.data.local.features.associative_tables.subject_teacher.SubjectTeacherDao
import com.kxsv.schooldiary.data.local.features.edu_performance.EduPerformanceDao
import com.kxsv.schooldiary.data.local.features.edu_performance.EduPerformanceEntity
import com.kxsv.schooldiary.data.local.features.grade.GradeDao
import com.kxsv.schooldiary.data.local.features.grade.GradeEntity
import com.kxsv.schooldiary.data.local.features.lesson.LessonDao
import com.kxsv.schooldiary.data.local.features.lesson.LessonEntity
import com.kxsv.schooldiary.data.local.features.study_day.StudyDayDao
import com.kxsv.schooldiary.data.local.features.study_day.StudyDayEntity
import com.kxsv.schooldiary.data.local.features.subject.SubjectDao
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.local.features.task.TaskDao
import com.kxsv.schooldiary.data.local.features.task.TaskEntity
import com.kxsv.schooldiary.data.local.features.teacher.TeacherDao
import com.kxsv.schooldiary.data.local.features.teacher.TeacherEntity
import com.kxsv.schooldiary.data.local.features.time_pattern.TimePatternDao
import com.kxsv.schooldiary.data.local.features.time_pattern.TimePatternEntity
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeDao
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeEntity

@Database(
	entities = [
		TimePatternEntity::class, PatternStrokeEntity::class,
		SubjectEntity::class, TeacherEntity::class, SubjectTeacher::class,
		StudyDayEntity::class, LessonEntity::class,
		GradeEntity::class, EduPerformanceEntity::class,
		TaskEntity::class
	],
	version = 2,
	exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
	
	abstract fun teacherDao(): TeacherDao
	abstract fun timePatternDao(): TimePatternDao
	abstract fun timePatternStrokeDao(): PatternStrokeDao
	abstract fun subjectDao(): SubjectDao
	abstract fun subjectTeacherDao(): SubjectTeacherDao
	abstract fun scheduleDao(): LessonDao
	abstract fun studyDayDao(): StudyDayDao
	abstract fun gradeDao(): GradeDao
	abstract fun eduPerformanceDao(): EduPerformanceDao
	abstract fun taskDao(): TaskDao
	
}

