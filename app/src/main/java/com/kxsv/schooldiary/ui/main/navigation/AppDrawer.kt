package com.kxsv.schooldiary.ui.main.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.ui.screens.NavGraphs
import com.kxsv.schooldiary.ui.screens.appCurrentDestinationAsState
import com.kxsv.schooldiary.ui.screens.destinations.DayScheduleScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.EduPerformanceScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.GradesScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.PatternsScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.SubjectsScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.TasksScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.TeachersScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.TypedDestination
import com.kxsv.schooldiary.ui.screens.startAppDestination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

enum class NavigationDrawerDestination(
	val destination: TypedDestination<out Any?>,
//	val icon: ImageVector,
	@StringRes val label: Int,
) {
	Timetable(DayScheduleScreenDestination, R.string.timetable),
	ReportCard(EduPerformanceScreenDestination, R.string.report_card_title),
	Agenda(TasksScreenDestination, R.string.tasks_title),
	Grades(GradesScreenDestination, R.string.grades_title),
	Subjects(SubjectsScreenDestination, R.string.subjects_title),
	TimePatterns(PatternsScreenDestination, R.string.patterns_list),
	Teachers(TeachersScreenDestination, R.string.teachers_title)
}

@Composable
fun AppModalDrawer(
	drawerState: DrawerState,
	navController: NavController,
	coroutineScope: CoroutineScope = rememberCoroutineScope(),
	content: @Composable () -> Unit,
) {
	val currentDestination = navController.appCurrentDestinationAsState().value
		?: NavGraphs.root.startAppDestination
	
	ModalNavigationDrawer(
		drawerState = drawerState,
		drawerContent = {
			ModalDrawerSheet {
				Column(modifier = Modifier.fillMaxSize()) {
					NavigationDrawerDestination.values().forEach { item ->
						NavigationDrawerItem(
							label = {
								Text(
									text = stringResource(id = item.label),
									style = MaterialTheme.typography.bodyMedium,
								)
							},
							selected = currentDestination == item.destination,
							onClick = {
								navController.navigate(item.destination.route) {
									launchSingleTop = true
								}
								coroutineScope.launch { drawerState.close() }
							}
						)
					}
				}
			}
		}
	) {
		content()
	}
}