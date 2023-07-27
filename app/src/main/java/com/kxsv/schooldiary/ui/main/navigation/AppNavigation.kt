package com.kxsv.schooldiary.ui.main.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.kxsv.schooldiary.ui.main.navigation.AppDestinations.COPY_DATE_RANGE_SCHEDULE_ROUTE
import com.kxsv.schooldiary.ui.main.navigation.AppDestinations.COPY_DAY_SCHEDULE_ROUTE
import com.kxsv.schooldiary.ui.main.navigation.AppDestinations.GRADES_ROUTE
import com.kxsv.schooldiary.ui.main.navigation.AppDestinations.TEACHERS_ROUTE
import com.kxsv.schooldiary.ui.main.navigation.AppDestinationsArgs.CUSTOM_PATTERN_SET_ARG
import com.kxsv.schooldiary.ui.main.navigation.AppDestinationsArgs.DATESTAMP_ARG
import com.kxsv.schooldiary.ui.main.navigation.AppDestinationsArgs.GRADE_ID_ARG
import com.kxsv.schooldiary.ui.main.navigation.AppDestinationsArgs.PATTERN_ID_ARG
import com.kxsv.schooldiary.ui.main.navigation.AppDestinationsArgs.SCHEDULE_ID_ARG
import com.kxsv.schooldiary.ui.main.navigation.AppDestinationsArgs.SELECTED_PATTERN_ARG
import com.kxsv.schooldiary.ui.main.navigation.AppDestinationsArgs.STUDY_DAY_ID_ARG
import com.kxsv.schooldiary.ui.main.navigation.AppDestinationsArgs.SUBJECT_ID_ARG
import com.kxsv.schooldiary.ui.main.navigation.AppDestinationsArgs.TASK_ID_ARG
import com.kxsv.schooldiary.ui.main.navigation.AppDestinationsArgs.TITLE_ARG
import com.kxsv.schooldiary.ui.main.navigation.AppDestinationsArgs.USER_MESSAGE_ARG
import com.kxsv.schooldiary.ui.main.navigation.AppScreens.ADD_EDIT_PATTERN_SCREEN
import com.kxsv.schooldiary.ui.main.navigation.AppScreens.ADD_EDIT_SCHEDULE_SCREEN
import com.kxsv.schooldiary.ui.main.navigation.AppScreens.ADD_EDIT_SUBJECT_SCREEN
import com.kxsv.schooldiary.ui.main.navigation.AppScreens.ADD_EDIT_TASK_SCREEN
import com.kxsv.schooldiary.ui.main.navigation.AppScreens.COPY_DATE_RANGE_SCHEDULE_SCREEN
import com.kxsv.schooldiary.ui.main.navigation.AppScreens.COPY_DAY_SCHEDULE_SCREEN
import com.kxsv.schooldiary.ui.main.navigation.AppScreens.DAY_SCHEDULE_SCREEN
import com.kxsv.schooldiary.ui.main.navigation.AppScreens.EDU_PERFORMANCE_SCREEN
import com.kxsv.schooldiary.ui.main.navigation.AppScreens.GRADES_SCREEN
import com.kxsv.schooldiary.ui.main.navigation.AppScreens.GRADE_DETAIL_SCREEN
import com.kxsv.schooldiary.ui.main.navigation.AppScreens.LOGIN_SCREEN
import com.kxsv.schooldiary.ui.main.navigation.AppScreens.PATTERNS_SCREEN
import com.kxsv.schooldiary.ui.main.navigation.AppScreens.PATTERNS_SELECTION_SCREEN
import com.kxsv.schooldiary.ui.main.navigation.AppScreens.SUBJECTS_SCREEN
import com.kxsv.schooldiary.ui.main.navigation.AppScreens.SUBJECT_DETAIL_SCREEN
import com.kxsv.schooldiary.ui.main.navigation.AppScreens.TASKS_SCREEN
import com.kxsv.schooldiary.ui.main.navigation.AppScreens.TASK_DETAIL_SCREEN
import com.kxsv.schooldiary.ui.main.navigation.AppScreens.TEACHERS_SCREEN
import java.time.LocalDate
import java.time.ZoneId

private const val TAG = "AppNavigation"

/**
 * Screens used in [AppDestinations]
 */
object AppScreens {
	const val PATTERNS_SCREEN = "patterns"
	const val PATTERNS_SELECTION_SCREEN = "patternsSelection"
	const val ADD_EDIT_PATTERN_SCREEN = "addEditPattern"
	const val SUBJECTS_SCREEN = "subjects"
	const val SUBJECT_DETAIL_SCREEN = "subjectDetail"
	const val ADD_EDIT_SUBJECT_SCREEN = "addEditSubject"
	const val TEACHERS_SCREEN = "teachers"
	const val DAY_SCHEDULE_SCREEN = "daySchedule"
	const val ADD_EDIT_SCHEDULE_SCREEN = "addEditSchedule"
	const val COPY_DAY_SCHEDULE_SCREEN = "copyDaySchedule"
	const val COPY_DATE_RANGE_SCHEDULE_SCREEN = "copyDateRangeSchedule"
	const val GRADES_SCREEN = "grades"
	const val GRADE_DETAIL_SCREEN = "gradeDetail"
	const val LOGIN_SCREEN = "login"
	const val EDU_PERFORMANCE_SCREEN = "eduPerformance"
	const val TASKS_SCREEN = "tasks"
	const val TASK_DETAIL_SCREEN = "taskDetail"
	const val ADD_EDIT_TASK_SCREEN = "addEditTask"
	
}

/**
 * Arguments used in [AppDestinations] routes
 */
object AppDestinationsArgs {
	const val USER_MESSAGE_ARG = "userMessage"
	const val TITLE_ARG = "title"
	const val PATTERN_ID_ARG = "patternId"
	const val SUBJECT_ID_ARG = "subjectId"
	const val GRADE_ID_ARG = "gradeId"
	const val DATESTAMP_ARG = "dateStamp"
	const val SCHEDULE_ID_ARG = "scheduleId"
	const val STUDY_DAY_ID_ARG = "studyDayId"
	const val SELECTED_PATTERN_ARG = "selectedPattern"
	const val CUSTOM_PATTERN_SET_ARG = "customPatternSet"
	const val TASK_ID_ARG = "taskId"
}

/**
 * Destinations used in the [MainActivity][com.kxsv.schooldiary.ui.main.MainActivity]
 */
object AppDestinations {
	const val PATTERNS_ROUTE = "$PATTERNS_SCREEN?$USER_MESSAGE_ARG={$USER_MESSAGE_ARG}"
	const val PATTERNS_SELECTION_ROUTE =
		"$PATTERNS_SELECTION_SCREEN/{$STUDY_DAY_ID_ARG}?$USER_MESSAGE_ARG={$USER_MESSAGE_ARG}"
	const val ADD_EDIT_PATTERN_ROUTE =
		"$ADD_EDIT_PATTERN_SCREEN/{$TITLE_ARG}?$PATTERN_ID_ARG={$PATTERN_ID_ARG}"
	const val SUBJECTS_ROUTE = "$SUBJECTS_SCREEN?$USER_MESSAGE_ARG={$USER_MESSAGE_ARG}"
	const val SUBJECT_DETAIL_ROUTE =
		"$SUBJECT_DETAIL_SCREEN/{$SUBJECT_ID_ARG}?$USER_MESSAGE_ARG={$USER_MESSAGE_ARG}"
	const val ADD_EDIT_SUBJECT_ROUTE = "$ADD_EDIT_SUBJECT_SCREEN?$SUBJECT_ID_ARG={$SUBJECT_ID_ARG}"
	const val TEACHERS_ROUTE = TEACHERS_SCREEN
	const val SCHEDULE_ROUTE = "lesson"
	const val ADD_EDIT_SCHEDULE_ROUTE =
		"$ADD_EDIT_SCHEDULE_SCREEN?$SCHEDULE_ID_ARG={$SCHEDULE_ID_ARG}?$DATESTAMP_ARG={$DATESTAMP_ARG}"
	const val DAY_SCHEDULE_ROUTE =
		"$DAY_SCHEDULE_SCREEN/{$DATESTAMP_ARG}?$USER_MESSAGE_ARG={$USER_MESSAGE_ARG}?$SELECTED_PATTERN_ARG={$SELECTED_PATTERN_ARG}"
	const val COPY_DAY_SCHEDULE_ROUTE = COPY_DAY_SCHEDULE_SCREEN
	const val COPY_DATE_RANGE_SCHEDULE_ROUTE = COPY_DATE_RANGE_SCHEDULE_SCREEN
	const val GRADES_ROUTE = "$GRADES_SCREEN?$USER_MESSAGE_ARG={$USER_MESSAGE_ARG}"
	const val GRADE_DETAIL_ROUTE = "$GRADE_DETAIL_SCREEN?$GRADE_ID_ARG={$GRADE_ID_ARG}"
	const val LOGIN_ROUTE = LOGIN_SCREEN
	const val EDU_PERFORMANCE_ROUTE = EDU_PERFORMANCE_SCREEN
	const val TASKS_ROUTE = TASKS_SCREEN
	const val TASK_DETAIL_ROUTE = "$TASK_DETAIL_SCREEN/{$TASK_ID_ARG}"
	const val ADD_EDIT_TASK_ROUTE = "$ADD_EDIT_TASK_SCREEN?$TASK_ID_ARG={$TASK_ID_ARG}"
	
}

/**
 * Models the navigation actions in the app.
 */
class AppNavigationActions(private val navController: NavHostController) {
	
	fun navigateToTeachers(userMessage: Int = 0) {
		val navigatesFromDrawer = userMessage == 0
		navController.navigate(TEACHERS_ROUTE) {
			// Pop up to the start destination of the graph to
			// avoid building up a large stack of destinations
			// on the back stack as users select items
			popUpTo(navController.graph.findStartDestination().id) {
				saveState = navigatesFromDrawer
			}
			// Avoid multiple copies of the same destination when
			// reselecting the same item
			launchSingleTop = true
			// Restore state when reselecting a previously selected item
			restoreState = navigatesFromDrawer
		}
	}
	
	fun navigateToPatterns(userMessage: Int = 0) {
		val navigatesFromDrawer = userMessage == 0
		navController.navigate(
			PATTERNS_SCREEN.let {
				if (userMessage != 0) "$it?$USER_MESSAGE_ARG=$userMessage" else it
			}
		) {
			popUpTo(navController.graph.findStartDestination().id) {
				inclusive = !navigatesFromDrawer
				saveState = navigatesFromDrawer
			}
			launchSingleTop = true
			restoreState = navigatesFromDrawer
		}
	}
	
	fun navigateToPatternsSelection(studyDayId: Long) {
		navController.navigate(
			"$PATTERNS_SELECTION_SCREEN/$studyDayId?$USER_MESSAGE_ARG=0"
		)
	}
	
	fun navigateToSubjects(userMessage: Int = 0) {
		val navigatesFromDrawer = userMessage == 0
		navController.navigate(
			SUBJECTS_SCREEN.let {
				if (navigatesFromDrawer) it else "$it?$USER_MESSAGE_ARG=$userMessage"
			}
		) {
			popUpTo(navController.graph.findStartDestination().id) {
				inclusive = !navigatesFromDrawer
				saveState = navigatesFromDrawer
			}
			launchSingleTop = true
			restoreState = navigatesFromDrawer
		}
	}
	
	fun navigateToSubjectDetail(userMessage: Int = 0, subjectId: Long) {
		navController.navigate("$SUBJECT_DETAIL_SCREEN/$subjectId?$USER_MESSAGE_ARG=$userMessage")
	}
	
	fun navigateToAddEditSubject(subjectId: Long?) {
		navController.navigate(
			ADD_EDIT_SUBJECT_SCREEN.let {
				if (subjectId != null) "$it?$SUBJECT_ID_ARG=$subjectId" else it
			}
		)
	}
	
	fun navigateToTasks(userMessage: Int = 0) {
		val navigatesFromDrawer = userMessage == 0
		navController.navigate(
			TASKS_SCREEN.let {
				it
//				if (navigatesFromDrawer) it else "$it?$USER_MESSAGE_ARG=$userMessage"
			}
		) {
			popUpTo(navController.graph.findStartDestination().id) {
				inclusive = !navigatesFromDrawer
				saveState = navigatesFromDrawer
			}
			launchSingleTop = true
			restoreState = navigatesFromDrawer
		}
	}
	
	fun navigateToTaskDetail(userMessage: Int = 0, taskId: Long) {
		navController.navigate("$TASK_DETAIL_SCREEN/$taskId")
	}
	
	fun navigateToAddEditTask(taskId: Long?) {
		navController.navigate(
			ADD_EDIT_TASK_SCREEN.let {
				if (taskId != null) "$it?$TASK_ID_ARG=$taskId" else it
			}
		)
	}
	
	private fun localDateToTimestamp(date: LocalDate): Long =
		date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
	
	fun navigateToDaySchedule(
		userMessage: Int = 0,
		dateStamp: Long = localDateToTimestamp(LocalDate.now()),
		selectedPatternId: Long = 0,
	) {
		val navigatesFromDrawer = userMessage == 0
		navController.navigate(
			"$DAY_SCHEDULE_SCREEN/$dateStamp".let {
				if (navigatesFromDrawer) it else {
					"$it?$USER_MESSAGE_ARG=$userMessage?$SELECTED_PATTERN_ARG=$selectedPatternId"
				}
			}
		) {
			// Pop up to the start destination of the graph to
			// avoid building up a large stack of destinations
			// on the back stack as users select items
			popUpTo(navController.graph.findStartDestination().id) {
				saveState = navigatesFromDrawer
			}
			// Avoid multiple copies of the same destination when
			// reselecting the same item
			launchSingleTop = true
			// Restore state when reselecting a previously selected item
			restoreState = navigatesFromDrawer
		}
	}
	
	fun navigateToAddEditPattern(title: Int, timePatternId: Long?) {
		navController.navigate(
			"$ADD_EDIT_PATTERN_SCREEN/$title".let {
				if (timePatternId != null) "$it?$PATTERN_ID_ARG=$timePatternId" else it
			}
		)
	}
	
	fun navigateToGradeDetail(gradeId: String?) {
		navController.navigate(
			GRADE_DETAIL_SCREEN.let {
				if (gradeId != null) "$it?$GRADE_ID_ARG=$gradeId" else it
			}
		)
	}
	
	fun navigateToAddEditSchedule(scheduleId: Long?, dateStamp: Long) {
		navController.navigate(
			ADD_EDIT_SCHEDULE_SCREEN.let {
				if (scheduleId != null) {
					"$it?$SCHEDULE_ID_ARG=$scheduleId?$DATESTAMP_ARG=0"
				} else {
					"$it?$SCHEDULE_ID_ARG=0?$DATESTAMP_ARG=$dateStamp"
				}
			}
		)
	}
	
	fun returnToDayScheduleWithPatternId() {
		navController.previousBackStackEntry?.savedStateHandle?.set(CUSTOM_PATTERN_SET_ARG, true)
		navController.popBackStack()
	}
	
	fun navigateToCopyOfDaySchedule() {
		navController.navigate(
			COPY_DAY_SCHEDULE_ROUTE
		)
	}
	
	fun navigateToCopyOfDateRangeSchedule() {
		navController.navigate(
			COPY_DATE_RANGE_SCHEDULE_ROUTE
		)
	}
	
	fun navigateToLogin(userMessage: Int = 0) {
//		val navigatesFromDrawer = userMessage == 0
		navController.navigate(
			LOGIN_SCREEN.let {
				if (userMessage != 0) "$it?$USER_MESSAGE_ARG=$userMessage" else it
			}
		)
	}
	
	fun navigateToGrades(userMessage: Int = 0) {
		val navigatesFromDrawer = userMessage == 0
		navController.navigate(
			GRADES_SCREEN.let {
				if (userMessage != 0) "$it?$USER_MESSAGE_ARG=$userMessage" else it
			}
		) {
			popUpTo(navController.graph.findStartDestination().id) {
				inclusive = !navigatesFromDrawer
				saveState = navigatesFromDrawer
			}
			launchSingleTop = true
			restoreState = navigatesFromDrawer
		}
	}
	
	fun returnAfterGradeEditing(userMessage: Int) {
		val navigatedFromGradesScreen =
			navController.previousBackStackEntry?.destination?.route == GRADES_ROUTE
		if (navigatedFromGradesScreen) {
			navigateToGrades(userMessage)
		} else {
			navController.previousBackStackEntry?.arguments?.let {
//				navController.previousBackStackEntry?.savedStateHandle?.set(CUSTOM_PATTERN_SET_ARG, true)
				navController.previousBackStackEntry?.savedStateHandle?.set(
					USER_MESSAGE_ARG,
					userMessage
				)
//				navController.previousBackStackEntry?.arguments?.putInt(SUBJECT_ID_ARG, userMessage)
				navController.popBackStack()
//				navigateToSubjectDetail(userMessage = userMessage, subjectId = it.getLong(SUBJECT_ID_ARG))
			}
		}
	}
	
	fun navigateToEduPerformance(userMessage: Int = 0) {
		val navigatesFromDrawer = userMessage == 0
		navController.navigate(
			EDU_PERFORMANCE_SCREEN
		) {
			popUpTo(navController.graph.findStartDestination().id) {
				inclusive = !navigatesFromDrawer
				saveState = navigatesFromDrawer
			}
			launchSingleTop = true
			restoreState = navigatesFromDrawer
		}
	}
	
	
}
