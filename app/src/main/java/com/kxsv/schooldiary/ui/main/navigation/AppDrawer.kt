package com.kxsv.schooldiary.ui.main.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kxsv.schooldiary.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AppModalDrawer(
	drawerState: DrawerState,
	currentRoute: String,
	navigationActions: AppNavigationActions,
	coroutineScope: CoroutineScope = rememberCoroutineScope(),
	content: @Composable () -> Unit,
) {
	ModalNavigationDrawer(
		drawerState = drawerState,
		drawerContent = {
			ModalDrawerSheet {
				AppDrawer(
					currentRoute = currentRoute,
					navigateToPatterns = { navigationActions.navigateToPatterns() },
					navigateToTeachers = { navigationActions.navigateToTeachers() },
					navigateToSubjects = { navigationActions.navigateToSubjects() },
					navigateToSchedule = { navigationActions.navigateToDaySchedule() },
					navigateToGrades = { navigationActions.navigateToGrades() },
					navigateToEduPerformance = { navigationActions.navigateToEduPerformance() },
					closeDrawer = { coroutineScope.launch { drawerState.close() } }
				)
			}
		}
	) {
		content()
	}
}

@Composable
private fun AppDrawer(
	currentRoute: String,
	navigateToPatterns: () -> Unit,
	navigateToTeachers: () -> Unit,
	navigateToSubjects: () -> Unit,
	navigateToSchedule: () -> Unit,
	navigateToGrades: () -> Unit,
	navigateToEduPerformance: () -> Unit,
	closeDrawer: () -> Unit,
	modifier: Modifier = Modifier,
) {
	Column(modifier = modifier.fillMaxSize()) {
		NavigationDrawerItem(
			label = {
				Text(
					text = stringResource(R.string.patterns_list),
					style = MaterialTheme.typography.body2,
					//color = tintColor,
				)
			},
			selected = currentRoute == AppDestinations.PATTERNS_ROUTE,
			onClick = {
				navigateToPatterns()
				closeDrawer()
			}
		)
		NavigationDrawerItem(
			label = {
				Text(
					text = stringResource(R.string.teachers_title),
					style = MaterialTheme.typography.body2,
					//color = tintColor,
				)
			},
			selected = currentRoute == AppDestinations.TEACHERS_ROUTE,
			onClick = {
				navigateToTeachers()
				closeDrawer()
			}
		)
		NavigationDrawerItem(
			label = {
				Text(
					text = stringResource(R.string.subjects_title),
					style = MaterialTheme.typography.body2,
					//color = tintColor,
				)
			},
			selected = currentRoute == AppDestinations.SUBJECTS_ROUTE,
			onClick = {
				navigateToSubjects()
				closeDrawer()
			}
		)
		NavigationDrawerItem(
			label = {
				Text(
					text = stringResource(R.string.timetable),
					style = MaterialTheme.typography.body2,
					//color = tintColor,
				)
			},
			selected = currentRoute == AppDestinations.DAY_SCHEDULE_ROUTE,
			onClick = {
				navigateToSchedule()
				closeDrawer()
			}
		)
		NavigationDrawerItem(
			label = {
				Text(
					text = stringResource(R.string.grades_title),
					style = MaterialTheme.typography.body2,
					//color = tintColor,
				)
			},
			selected = currentRoute == AppDestinations.GRADES_ROUTE,
			onClick = {
				navigateToGrades()
				closeDrawer()
			}
		)
		NavigationDrawerItem(
			label = {
				Text(
					text = stringResource(R.string.report_card_title),
					style = MaterialTheme.typography.body2,
					//color = tintColor,
				)
			},
			selected = currentRoute == AppDestinations.EDU_PERFORMANCE_ROUTE,
			onClick = {
				navigateToEduPerformance()
				closeDrawer()
			}
		)
	}
}


@Preview("Drawer contents")
@Composable
fun PreviewAppDrawer() {
	Surface {
		AppDrawer(
			currentRoute = AppDestinations.DAY_SCHEDULE_ROUTE,
			navigateToPatterns = {},
			navigateToTeachers = {},
			navigateToSubjects = {},
			navigateToSchedule = {},
			navigateToGrades = {},
			navigateToEduPerformance = {},
			closeDrawer = {}
		)
	}
}