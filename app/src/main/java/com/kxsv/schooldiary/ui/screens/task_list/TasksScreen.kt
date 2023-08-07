package com.kxsv.schooldiary.ui.screens.task_list


import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.task.TaskWithSubject
import com.kxsv.schooldiary.ui.main.app_bars.bottombar.TasksBottomAppBar
import com.kxsv.schooldiary.ui.main.app_bars.topbar.TasksTopAppBar
import com.kxsv.schooldiary.ui.main.navigation.nav_actions.TasksScreenNavActions
import com.kxsv.schooldiary.ui.screens.grade_list.MY_URI
import com.kxsv.schooldiary.util.Utils
import com.kxsv.schooldiary.util.ui.LoadingContent
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.FULL_ROUTE_PLACEHOLDER
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Destination(
	deepLinks = [
		DeepLink(
			action = Intent.ACTION_VIEW,
			uriPattern = "$MY_URI/$FULL_ROUTE_PLACEHOLDER"
		)
	]
)
@Composable
fun TasksScreen(
	destinationsNavigator: DestinationsNavigator,
	drawerState: DrawerState,
	coroutineScope: CoroutineScope,
	viewModel: TasksViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState,
) {
	val uiState = viewModel.uiState.collectAsState().value
	val navigator = TasksScreenNavActions(destinationsNavigator = destinationsNavigator)
	Scaffold(
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		topBar = {
			TasksTopAppBar(
				openDrawer = { coroutineScope.launch { drawerState.open() } },
				onDoneFilterSet = { viewModel.changeDoneFilter(it) }
			)
		},
		bottomBar = {
			TasksBottomAppBar(
				selectedDataFilterText = stringResource(id = uiState.dateFilterType.getLocalisedStringId()),
				onAddTask = { navigator.onAddTask() },
				onDateFilterChoose = { viewModel.changeDataFilter(it) }
			)
		},
		modifier = Modifier.fillMaxSize(),
	) { paddingValues ->
		
		TasksContent(
			isLoading = uiState.isLoading,
			tasksGroups = uiState.tasks,
			onTaskClick = { taskId -> navigator.onTaskClick(taskId) },
			modifier = Modifier.padding(paddingValues)
		)
		
		// Check for user messages to display on the screen
		uiState.userMessage?.let { userMessage ->
			val snackbarText = stringResource(userMessage, uiState.userMessageArg ?: "")
			LaunchedEffect(snackbarText) {
				snackbarHostState.showSnackbar(snackbarText)
				viewModel.snackbarMessageShown()
			}
		}
	}
}

@Composable
private fun TasksContent(
	isLoading: Boolean,
	tasksGroups: Map<LocalDate, List<TaskWithSubject>>,
	onTaskClick: (Long) -> Unit,
	modifier: Modifier,
) {
	LoadingContent(
		modifier = modifier,
		loading = isLoading,
		empty = tasksGroups.isEmpty(),
		emptyContent = { Text(text = "No tasks yet") },
		isContentScrollable = true,
		onRefresh = { /*TODO*/ }
	) {
		LazyColumn(
			contentPadding = PaddingValues(
				vertical = dimensionResource(R.dimen.list_item_padding),
			),
		) {
			tasksGroups.keys.toList().forEach { key ->
				item {
					Row(
						modifier = Modifier.padding(
							vertical = dimensionResource(R.dimen.list_item_padding),
							horizontal = dimensionResource(R.dimen.horizontal_margin),
						),
						verticalAlignment = Bottom
					) {
						val titleText =
							if (key == Utils.currentDate) {
								"Today"
							} else if (ChronoUnit.DAYS.between(Utils.currentDate, key) == 1L) {
								"Tomorrow"
							} else if (ChronoUnit.DAYS.between(Utils.currentDate, key) == -1L) {
								"Yesterday"
							} else if (key.isAfter(Utils.currentDate)) {
								ChronoUnit.DAYS.between(Utils.currentDate, key)
									.toString() + " days"
							} else {
								ChronoUnit.DAYS.between(key, Utils.currentDate)
									.toString() + " days ago"
							}
						Text(
							text = titleText,
							style = MaterialTheme.typography.titleSmall,
							color = Color.Blue
						)
						Spacer(modifier = Modifier.padding(horizontal = 8.dp))
						Text(
							text = key.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
							style = MaterialTheme.typography.labelSmall,
						)
					}
				}
				items(tasksGroups[key]!!) { taskWithSubject ->
					Column {
						TaskItem(
							taskWithSubject = taskWithSubject,
							onTaskClick = onTaskClick,
						)
						
						Divider(
							Modifier
								.fillMaxWidth()
								.align(Alignment.CenterHorizontally)
						)
					}
				}
			}
		}
	}
}

@Composable
private fun TaskItem(
	taskWithSubject: TaskWithSubject,
	onTaskClick: (Long) -> Unit,
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onTaskClick(taskWithSubject.taskEntity.taskId) }
			.padding(
				vertical = dimensionResource(R.dimen.vertical_margin),
				horizontal = dimensionResource(R.dimen.horizontal_margin)
			)
	) {
		val textStyle = if (taskWithSubject.taskEntity.isDone) {
			MaterialTheme.typography.titleMedium.copy(textDecoration = TextDecoration.LineThrough)
		} else {
			MaterialTheme.typography.titleMedium
		}
		Text(
			text = taskWithSubject.taskEntity.title,
			style = textStyle,
		)
		Spacer(modifier = Modifier.padding(vertical = 4.dp))
		Row(
			verticalAlignment = Alignment.CenterVertically,
		) {
			Icon(
				imageVector = Icons.Default.Schedule,
				contentDescription = stringResource(R.string.due_date),
				modifier = Modifier.size(14.dp)
			)
			Spacer(modifier = Modifier.padding(horizontal = 4.dp))
			Text(
				text = taskWithSubject.taskEntity.dueDate.format(DateTimeFormatter.ofPattern("d MMM yyyy")),
				style = MaterialTheme.typography.bodySmall,
			)
		}
		Spacer(modifier = Modifier.padding(vertical = 4.dp))
		Row(
			verticalAlignment = Alignment.CenterVertically,
		) {
			Text(
				text = taskWithSubject.subject?.getName() ?: "Not loaded subject",
				style = MaterialTheme.typography.bodySmall,
			)
		}
	}
}