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
import com.kxsv.schooldiary.AppDestinations.ADD_EDIT_SUBJECT_ROUTE
import com.kxsv.schooldiary.AppDestinations.PATTERNS_ROUTE
import com.kxsv.schooldiary.AppDestinations.SUBJECTS_ROUTE
import com.kxsv.schooldiary.AppDestinations.SUBJECT_DETAIL_ROUTE
import com.kxsv.schooldiary.AppDestinations.TEACHERS_ROUTE
import com.kxsv.schooldiary.AppDestinationsArgs.PATTERN_ID_ARG
import com.kxsv.schooldiary.AppDestinationsArgs.SUBJECT_ID_ARG
import com.kxsv.schooldiary.AppDestinationsArgs.TITLE_ARG
import com.kxsv.schooldiary.AppDestinationsArgs.USER_MESSAGE_ARG
import com.kxsv.schooldiary.ui.screens.patterns_list.PatternsScreen
import com.kxsv.schooldiary.ui.screens.patterns_list.add_edit_pattern.AddEditTimePatternScreen
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
                    onAddSubject = { navActions.navigateToAddEditSubject(0L) },
                    onSubjectClick = { subject -> navActions.navigateToSubjectDetail(subject.subjectId) },
                    onUserMessageDisplayed = { entry.arguments?.putInt(USER_MESSAGE_ARG, 0) },
                    openDrawer = { coroutineScope.launch { drawerState.open() } }
                )
            }

        }
        composable(
            PATTERNS_ROUTE,
            arguments = listOf(
                navArgument(USER_MESSAGE_ARG) { type = NavType.IntType; defaultValue = 0 }
            )
        ) { entry ->
            AppModalDrawer(drawerState, currentRoute, navActions) {
                PatternsScreen(
                    userMessage = entry.arguments?.getInt(USER_MESSAGE_ARG)!!,
                    onUserMessageDisplayed = { entry.arguments?.putInt(USER_MESSAGE_ARG, 0) },
                    onAddPattern = {
                        navActions.navigateToAddEditPattern(
                            R.string.add_pattern, null
                        )
                    },
                    onPatternClick = { pattern ->
                        navActions.navigateToAddEditPattern(
                            R.string.edit_pattern, pattern.timePattern.patternId
                        )
                    },
                    onDeletePattern = { navActions.navigateToPatterns(DELETE_RESULT_OK) },
                    openDrawer = { coroutineScope.launch { drawerState.open() } }
                )
            }
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
                    navActions.navigateToPatterns(
                        if (patternId == null) ADD_EDIT_RESULT_OK else EDIT_RESULT_OK
                    )
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
                // TODO: need to go subjectDetail onUpdate and to subjectsScreen onCreate
                onSubjectUpdate = {
                    navActions.navigateToSubjects(
                        if (subjectId == 0L) ADD_EDIT_RESULT_OK else EDIT_RESULT_OK
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
