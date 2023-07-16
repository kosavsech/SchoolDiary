package com.kxsv.schooldiary.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kxsv.schooldiary.data.local.features.associative_tables.subject_teacher.SubjectTeacher
import com.kxsv.schooldiary.data.local.features.associative_tables.subject_teacher.SubjectTeacherDao
import com.kxsv.schooldiary.data.local.features.grade.Grade
import com.kxsv.schooldiary.data.local.features.grade.GradeDao
import com.kxsv.schooldiary.data.local.features.schedule.Schedule
import com.kxsv.schooldiary.data.local.features.schedule.ScheduleDao
import com.kxsv.schooldiary.data.local.features.study_day.StudyDay
import com.kxsv.schooldiary.data.local.features.study_day.StudyDayDao
import com.kxsv.schooldiary.data.local.features.subject.Subject
import com.kxsv.schooldiary.data.local.features.subject.SubjectDao
import com.kxsv.schooldiary.data.local.features.teacher.Teacher
import com.kxsv.schooldiary.data.local.features.teacher.TeacherDao
import com.kxsv.schooldiary.data.local.features.time_pattern.TimePattern
import com.kxsv.schooldiary.data.local.features.time_pattern.TimePatternDao
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStroke
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeDao
import com.kxsv.schooldiary.util.Converters

@Database(
    entities = [
        Teacher::class, TimePattern::class, PatternStroke::class,
        Subject::class, SubjectTeacher::class, Schedule::class,
        StudyDay::class, Grade::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun teacherDao(): TeacherDao
    abstract fun timePatternDao(): TimePatternDao
    abstract fun timePatternStrokeDao(): PatternStrokeDao
    abstract fun subjectDao(): SubjectDao
    abstract fun subjectTeacherDao(): SubjectTeacherDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun studyDayDao(): StudyDayDao
    abstract fun gradeDao(): GradeDao
    
}

