package com.kxsv.schooldiary.ui.main.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.ManageHistory
import androidx.compose.material.icons.rounded.PermContactCalendar
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Summarize
import androidx.compose.material.icons.rounded.Task
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.ui.screens.NavGraphs
import com.kxsv.schooldiary.ui.screens.appCurrentDestinationAsState
import com.kxsv.schooldiary.ui.screens.destinations.DayScheduleScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.EduPerformanceScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.GradesScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.MainScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.PatternsScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.SettingsScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.SubjectsScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.TasksScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.TeachersScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.TypedDestination
import com.kxsv.schooldiary.ui.screens.startAppDestination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

enum class NavigationDrawerDestination(
	val destination: TypedDestination<out Any?>? = null,
	val icon: ImageVector? = null,
	@StringRes val label: Int? = null,
) {
	Main(MainScreenDestination, Icons.Rounded.Home, R.string.main_menu_title),
	Timetable(DayScheduleScreenDestination, Icons.Rounded.Schedule, R.string.timetable),
	Grades(GradesScreenDestination, Icons.Rounded.EmojiEvents, R.string.grades_title),
	Agenda(TasksScreenDestination, Icons.Rounded.Task, R.string.tasks_title),
	Spacer,
	ReportCard(
		EduPerformanceScreenDestination, Icons.Rounded.Summarize, R.string.report_card_title
	),
	TimePatterns(PatternsScreenDestination, Icons.Rounded.ManageHistory, R.string.patterns_list),
	Spacer2,
	Subjects(SubjectsScreenDestination, Icons.Rounded.School, R.string.subjects_title),
	Teachers(TeachersScreenDestination, Icons.Rounded.PermContactCalendar, R.string.teachers_title),
	Spacer3,
	Settings(SettingsScreenDestination, Icons.Rounded.Settings, R.string.settings_title)
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
				Column(modifier = Modifier.fillMaxSize(0.80f)) {
					Text(
						text = stringResource(id = R.string.app_name),
						style = MaterialTheme.typography.displaySmall,
						modifier = Modifier.padding(
							vertical = dimensionResource(id = R.dimen.horizontal_margin),
							horizontal = dimensionResource(id = R.dimen.list_item_padding)
						)
					)
					Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.vertical_margin)))
					NavigationDrawerDestination.values().forEach { item ->
						if (item.destination != null && item.label != null && item.icon != null) {
							NavigationDrawerItem(
								label = {
									Text(
										text = stringResource(id = item.label),
										style = MaterialTheme.typography.bodyMedium,
									)
								},
								icon = {
									Icon(
										imageVector = item.icon,
										contentDescription = null
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
						} else {
							Divider(
								modifier = Modifier
									.padding(vertical = dimensionResource(id = R.dimen.list_item_padding)),
								thickness = 0.5.dp,
								color = Color.LightGray
							)
						}
					}
				}
			}
		}
	) {
		content()
	}
}