package com.kxsv.schooldiary

sealed class Screen(val route: String, val name: String) {
    object HomeScreen : Screen("home_screen", "Главное меню")
    object ScheduleScreen : Screen("schedule_screen", "Расписание")
    object TasksScreen : Screen("tasks_screen", "Задания")
    object GradesScreen : Screen("grades_screen", "Оценки")
    object TeachersScreen : Screen("teachers_screen", "Список учителей")
    object AttendanceScreen : Screen("attendance_screen", "Посещаемость")
    object NotesScreen : Screen("notes_screen", "Заметки")
    object NoteDetailedScreen : Screen("note_detailed_screen", "")
    object RecordingsScreen : Screen("recordings_screen", "Звуковые записи")
    object StudySitesScreen : Screen("study_sites_screen", "Учебные сайты")
    object HelpFeedbackScreen : Screen("help_feedback_screen", "Помощь и обратная связь")
    object SettingsScreen : Screen("settings_screen", "Настройки")

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}
