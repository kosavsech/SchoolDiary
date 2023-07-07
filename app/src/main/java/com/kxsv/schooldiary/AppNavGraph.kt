package com.kxsv.schooldiary

import android.app.Activity
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kxsv.schooldiary.AppDestinations.ADD_EDIT_PATTERN_ROUTE
import com.kxsv.schooldiary.AppDestinations.ADD_EDIT_SCHEDULE_ROUTE
import com.kxsv.schooldiary.AppDestinations.ADD_EDIT_SUBJECT_ROUTE
import com.kxsv.schooldiary.AppDestinations.DAY_SCHEDULE_ROUTE
import com.kxsv.schooldiary.AppDestinations.PATTERNS_ROUTE
import com.kxsv.schooldiary.AppDestinations.PATTERNS_SELECTION_ROUTE
import com.kxsv.schooldiary.AppDestinations.SUBJECTS_ROUTE
import com.kxsv.schooldiary.AppDestinations.SUBJECT_DETAIL_ROUTE
import com.kxsv.schooldiary.AppDestinations.TEACHERS_ROUTE
import com.kxsv.schooldiary.AppDestinationsArgs.CUSTOM_PATTERN_SET_ARG
import com.kxsv.schooldiary.AppDestinationsArgs.DATESTAMP_ARG
import com.kxsv.schooldiary.AppDestinationsArgs.PATTERN_ID_ARG
import com.kxsv.schooldiary.AppDestinationsArgs.SCHEDULE_ID_ARG
import com.kxsv.schooldiary.AppDestinationsArgs.SELECTED_PATTERN_ARG
import com.kxsv.schooldiary.AppDestinationsArgs.STUDY_DAY_ID_ARG
import com.kxsv.schooldiary.AppDestinationsArgs.SUBJECT_ID_ARG
import com.kxsv.schooldiary.AppDestinationsArgs.TITLE_ARG
import com.kxsv.schooldiary.AppDestinationsArgs.USER_MESSAGE_ARG
import com.kxsv.schooldiary.ui.screens.patterns.PatternSelectionScreen
import com.kxsv.schooldiary.ui.screens.patterns.PatternsScreen
import com.kxsv.schooldiary.ui.screens.patterns.add_edit_pattern.AddEditTimePatternScreen
import com.kxsv.schooldiary.ui.screens.schedule.DayScheduleScreen
import com.kxsv.schooldiary.ui.screens.schedule.add_edit.AddEditScheduleScreen
import com.kxsv.schooldiary.ui.screens.subject_detail.SubjectDetailScreen
import com.kxsv.schooldiary.ui.screens.subject_detail.add_edit.AddEditSubjectScreen
import com.kxsv.schooldiary.ui.screens.subject_list.SubjectsScreen
import com.kxsv.schooldiary.ui.screens.teacher_list.TeachersScreen
import com.kxsv.schooldiary.util.ui.AppModalDrawer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(
	modifier: Modifier = Modifier,
	navController: NavHostController = rememberNavController(),
	coroutineScope: CoroutineScope = rememberCoroutineScope(),
	drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
	startDestination: String = SUBJECTS_ROUTE,
	navActions: AppNavigationActions = remember(navController) {
		AppNavigationActions(navController)
	},
) {
	val currentNavBackStackEntry = navController.currentBackStackEntryAsState().value
	val currentRoute = currentNavBackStackEntry?.destination?.route ?: startDestination
	
	NavHost(
		navController = navController,
		startDestination = startDestination,
		modifier = modifier
	) {
		composable(TEACHERS_ROUTE) {
			AppModalDrawer(drawerState, currentRoute, navActions) {
				TeachersScreen(
					// TODO: add other stuff
					openDrawer = { coroutineScope.launch { drawerState.open() } }
				)
			}
		}
		composable(
			DAY_SCHEDULE_ROUTE,
			arguments = listOf(
				navArgument(SELECTED_PATTERN_ARG) {
					type = NavType.LongType; nullable = false; defaultValue = 0
				},
				navArgument(DATESTAMP_ARG) { type = NavType.LongType },
				navArgument(USER_MESSAGE_ARG) { type = NavType.IntType; defaultValue = 0 }
			)
		) { entry ->
			AppModalDrawer(drawerState, currentRoute, navActions) {
				DayScheduleScreen(
					userMessage = entry.arguments?.getInt(USER_MESSAGE_ARG)!!,
					isCustomPatternWasSet = entry.savedStateHandle.get<Boolean>(
						CUSTOM_PATTERN_SET_ARG
					),
					onUserMessageDisplayed = { entry.arguments?.putInt(USER_MESSAGE_ARG, 0) },
					onAddSchedule = { dateStamp ->
						navActions.navigateToAddEditSchedule(
							scheduleId = null,
							dateStamp = dateStamp
						)
					},
					onEditClass = { scheduleId ->
						navActions.navigateToAddEditSchedule(scheduleId = scheduleId, 0)
					},
					onChangePattern = { studyDayId ->
						navActions.navigateToPatternsSelection(studyDayId = studyDayId)
					},
					openDrawer = { coroutineScope.launch { drawerState.open() } }
				)
			}
		}
		composable(
			SUBJECTS_ROUTE,
			arguments = listOf(
				navArgument(USER_MESSAGE_ARG) { type = NavType.IntType; defaultValue = 0 }
			)
		) { entry ->
			AppModalDrawer(drawerState, currentRoute, navActions) {
				SubjectsScreen(
					userMessage = entry.arguments?.getInt(USER_MESSAGE_ARG)!!,
					onUserMessageDisplayed = { entry.arguments?.putInt(USER_MESSAGE_ARG, 0) },
					onAddSubject = { navActions.navigateToAddEditSubject(null) },
					onSubjectClick = { subject -> navActions.navigateToSubjectDetail(subject.subjectId) },
					openDrawer = { coroutineScope.launch { drawerState.open() } }
				)
			}
			
		}
		composable(
			PATTERNS_ROUTE,
			arguments = listOf(
				navArgument(USER_MESSAGE_ARG) { type = NavType.IntType; defaultValue = 0 },
				navArgument(STUDY_DAY_ID_ARG) { type = NavType.LongType; defaultValue = 0 }
			)
		) { entry ->
			AppModalDrawer(drawerState, currentRoute, navActions) {
				PatternsScreen(
					userMessage = entry.arguments?.getInt(USER_MESSAGE_ARG)!!,
					onAddPattern = {
						navActions.navigateToAddEditPattern(
							R.string.add_pattern,
							null
						)
					},
					onEditPattern = {
						navActions.navigateToAddEditPattern(
							R.string.edit_pattern,
							it.timePattern.patternId
						)
					},
					onDeletePattern = { navActions.navigateToPatterns(DELETE_RESULT_OK) },
					onUserMessageDisplayed = { entry.arguments?.putInt(USER_MESSAGE_ARG, 0) },
					openDrawer = { coroutineScope.launch { drawerState.open() } }
				)
			}
		}
		composable(
			PATTERNS_SELECTION_ROUTE,
			arguments = listOf(
				navArgument(STUDY_DAY_ID_ARG) { type = NavType.LongType; defaultValue = 0 }
			)
		) { entry ->
			PatternSelectionScreen(
				userMessage = entry.arguments?.getInt(USER_MESSAGE_ARG)!!,
				onAddPattern = {
					navActions.navigateToAddEditPattern(
						R.string.add_pattern,
						null
					)
				},
				onEditPattern = {
					navActions.navigateToAddEditPattern(
						R.string.edit_pattern,
						it.timePattern.patternId
					)
				},
				onUserMessageDisplayed = { entry.arguments?.putInt(USER_MESSAGE_ARG, 0) },
				onPatternSelected = { navActions.returnToDayScheduleWithPatternId() },
				onBack = { navController.popBackStack() }
			)
		}
		composable(
			route = ADD_EDIT_PATTERN_ROUTE,
			arguments = listOf(
				navArgument(TITLE_ARG) { type = NavType.IntType },
				navArgument(PATTERN_ID_ARG) {
					type = NavType.LongType; nullable = false; defaultValue = 0
				},
			)
		) { entry ->
			val patternId = entry.arguments?.getLong(PATTERN_ID_ARG)
			AddEditTimePatternScreen(
				topBarTitle = entry.arguments?.getInt(TITLE_ARG)!!,
				onPatternUpdate = {
					navController.popBackStack()
					// TODO: create separate method in navActions to pop back stack with arguments
					/*navActions.navigateToPatterns(
						if (patternId == 0L) ADD_EDIT_RESULT_OK else EDIT_RESULT_OK
					)*/
				},
				onBack = { navController.popBackStack() }
			)
		}
		composable(
			route = ADD_EDIT_SUBJECT_ROUTE,
			arguments = listOf(
				navArgument(SUBJECT_ID_ARG) {
					type = NavType.LongType; nullable = false; defaultValue = 0
				},
			)
		) { entry ->
			val subjectId = entry.arguments?.getLong(SUBJECT_ID_ARG)
			AddEditSubjectScreen(
				// TODO: need to go subjectDetailed onUpdate and to subjectsScreen onCreate
				onSubjectUpdate = {
					navActions.navigateToSubjects(
						if (subjectId == 0L) ADD_EDIT_RESULT_OK else EDIT_RESULT_OK
					)
				},
				onBack = { navController.popBackStack() }
			)
		}
		composable(
			route = ADD_EDIT_SCHEDULE_ROUTE,
			arguments = listOf(
				navArgument(SCHEDULE_ID_ARG) {
					type = NavType.LongType; nullable = false; defaultValue = 0
				},
				navArgument(DATESTAMP_ARG) {
					type = NavType.LongType; nullable = false; defaultValue = 0
				},
			)
		) { entry ->
			val scheduleId = entry.arguments?.getLong(SCHEDULE_ID_ARG)
			AddEditScheduleScreen(
				onScheduleUpdate = {
					navActions.navigateToDaySchedule(
						userMessage = if (scheduleId == 0L) ADD_EDIT_RESULT_OK else EDIT_RESULT_OK,
						dateStamp = it
					)
				},
				onBack = { navController.popBackStack() }
			)
		}
		composable(
			route = SUBJECT_DETAIL_ROUTE,
			arguments = listOf(
				navArgument(SUBJECT_ID_ARG) {
					type = NavType.LongType; nullable = false; defaultValue = 0
				},
			)
		) {
			SubjectDetailScreen(
				onEditSubject = { subjectId ->
					navActions.navigateToAddEditSubject(subjectId)
				},
				onBack = { navController.popBackStack() },
				onDeleteSubject = { navActions.navigateToSubjects(DELETE_RESULT_OK) }
			)
		}
	}
}


// Keys for navigation
const val ADD_EDIT_RESULT_OK = Activity.RESULT_FIRST_USER + 1
const val DELETE_RESULT_OK = Activity.RESULT_FIRST_USER + 2
const val EDIT_RESULT_OK = Activity.RESULT_FIRST_USER + 3
const val SELECTED_DEFAULT_PATTERN_OK = Activity.RESULT_FIRST_USER + 4
const val SELECTED_CUSTOM_PATTERN_OK = Activity.RESULT_FIRST_USER + 5