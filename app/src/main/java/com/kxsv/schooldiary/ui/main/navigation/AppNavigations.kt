package com.kxsv.schooldiary.ui.main.navigation

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.kxsv.schooldiary.ui.screens.NavGraph
import com.kxsv.schooldiary.ui.screens.NavGraphs
import com.kxsv.schooldiary.ui.screens.login.SplashViewModel
import com.kxsv.schooldiary.ui.screens.schedule.ScheduleViewModel
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.dependency
import kotlinx.coroutines.CoroutineScope

@Composable
fun NavGraph(
	navController: NavHostController = rememberNavController(),
	coroutineScope: CoroutineScope = rememberCoroutineScope(),
	drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
	snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
	activity: ComponentActivity,
	startDestination: NavGraph,
	splashViewModel: SplashViewModel,
) {
	AppModalDrawer(
		drawerState = drawerState,
		navController = navController
	) {
		DestinationsNavHost(
			navGraph = NavGraphs.root,
			startRoute = startDestination,
			navController = navController,
			dependenciesContainerBuilder = {
				dependency(NavGraphs.schedule) {
					val entry = remember(navBackStackEntry) {
						navController.getBackStackEntry(NavGraphs.schedule.route)
					}
					hiltViewModel<ScheduleViewModel>(entry)
				}
				dependency(snackbarHostState)
				dependency(drawerState)
				dependency(coroutineScope)
			}
		)
	}
}


// Keys for navigation
const val ADD_RESULT_OK = Activity.RESULT_FIRST_USER + 1
const val DELETE_RESULT_OK = Activity.RESULT_FIRST_USER + 2
const val EDIT_RESULT_OK = Activity.RESULT_FIRST_USER + 3
const val SELECTED_DEFAULT_PATTERN_OK = Activity.RESULT_FIRST_USER + 4
const val LOGIN_RESULT_OK = Activity.RESULT_FIRST_USER + 5