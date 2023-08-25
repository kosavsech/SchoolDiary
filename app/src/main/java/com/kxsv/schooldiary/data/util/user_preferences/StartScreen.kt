package com.kxsv.schooldiary.data.util.user_preferences

import androidx.annotation.StringRes
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.ui.screens.NavGraphs
import com.kxsv.schooldiary.ui.screens.destinations.EduPerformanceScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.GradesScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.MainScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.TasksScreenDestination
import com.ramcosta.composedestinations.spec.Route

enum class StartScreen(
	val route: Route,
	@StringRes var textRes: Int,
) {
	MAIN_SCREEN(MainScreenDestination, R.string.main_menu_title),
	SCHEDULE(NavGraphs.schedule, R.string.timetable),
	GRADE_FEED(GradesScreenDestination, R.string.grades_title),
	AGENDA(TasksScreenDestination, R.string.agenda_title),
	REPORT_CARD(EduPerformanceScreenDestination, R.string.report_card_title)
}
