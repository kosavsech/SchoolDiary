package com.kxsv.schooldiary.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kxsv.schooldiary.data.features.associative_tables.subject_teacher.SubjectTeacher
import com.kxsv.schooldiary.data.features.associative_tables.subject_teacher.SubjectTeacherDao
import com.kxsv.schooldiary.data.features.schedule.Schedule
import com.kxsv.schooldiary.data.features.schedule.ScheduleDao
import com.kxsv.schooldiary.data.features.study_day.StudyDay
import com.kxsv.schooldiary.data.features.study_day.StudyDayDao
import com.kxsv.schooldiary.data.features.subjects.Subject
import com.kxsv.schooldiary.data.features.subjects.SubjectDao
import com.kxsv.schooldiary.data.features.teachers.Teacher
import com.kxsv.schooldiary.data.features.teachers.TeacherDao
import com.kxsv.schooldiary.data.features.time_pattern.TimePattern
import com.kxsv.schooldiary.data.features.time_pattern.TimePatternDao
import com.kxsv.schooldiary.data.features.time_pattern.pattern_stroke.PatternStroke
import com.kxsv.schooldiary.data.features.time_pattern.pattern_stroke.PatternStrokeDao
import com.kxsv.schooldiary.util.Converters

@Database(
    entities = [
        Teacher::class, TimePattern::class, PatternStroke::class,
        Subject::class, SubjectTeacher::class, Schedule::class,
        StudyDay::class
    ],
    version = 3,
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
    
}

