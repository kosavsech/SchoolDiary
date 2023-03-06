package com.kxsv.schooldiary

sealed class Screen(val route: String){
    object HomeScreen : Screen("home_screen")
    object ScheduleScreen : Screen("schedule_screen")
    object TasksScreen : Screen("tasks_screen")
    object GradesScreen : Screen("grades_screen")
    object TeachersScreen : Screen("teachers_screen")
    object AttendanceScreen : Screen("attendance_screen")
    object NotesScreen : Screen("notes_screen")
    object RecordingsScreen : Screen("recordings_screen")
    object StudySitesScreen : Screen("study_sites_screen")
    object HelpFeedbackScreen : Screen("help_feedback_screen")
    object SettingsScreen : Screen("settings_screen")
}
