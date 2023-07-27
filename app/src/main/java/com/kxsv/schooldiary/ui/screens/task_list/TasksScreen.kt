package com.kxsv.schooldiary.ui.screens.task_list


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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.task.TaskEntity
import com.kxsv.schooldiary.data.local.features.task.TaskWithSubject
import com.kxsv.schooldiary.ui.main.topbar.TasksTopAppBar
import com.kxsv.schooldiary.util.ui.LoadingContent
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TasksScreen(
	onAddTask: () -> Unit,
	onTaskClick: (TaskEntity) -> Unit,
	openDrawer: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: TasksViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
	Scaffold(
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		topBar = { TasksTopAppBar(openDrawer = openDrawer) },
		modifier = modifier.fillMaxSize(),
		floatingActionButton = {
			FloatingActionButton(onClick = onAddTask) {
				Icon(Icons.Default.Add, stringResource(R.string.add_task))
			}
		}
	) { paddingValues ->
		val uiState = viewModel.uiState.collectAsState().value
		
		TasksContent(
			isLoading = uiState.isLoading,
			tasksGroups = uiState.tasks,
			onTaskClick = onTaskClick,
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
	onTaskClick: (TaskEntity) -> Unit,
	modifier: Modifier,
) {
	LoadingContent(
		modifier = modifier,
		loading = isLoading,
		isContentScrollable = true,
		empty = tasksGroups.isEmpty(),
		emptyContent = { Text(text = "No tasks yet") },
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
							if (key == LocalDate.now()) {
								"Today"
							} else if (key.dayOfMonth - LocalDate.now().dayOfMonth == 1) {
								"Tomorrow"
							} else if (key.dayOfMonth - LocalDate.now().dayOfMonth == -1) {
								"Yesterday"
							} else if (key.isAfter(LocalDate.now())) {
								key.dayOfMonth.minus(LocalDate.now().dayOfMonth)
									.toString() + " days"
							} else {
								LocalDate.now().dayOfMonth.minus(key.dayOfMonth)
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
					Column() {
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
	onTaskClick: (TaskEntity) -> Unit,
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onTaskClick(taskWithSubject.taskEntity) }
			.padding(
				vertical = dimensionResource(R.dimen.vertical_margin),
				horizontal = dimensionResource(R.dimen.horizontal_margin)
			)
	) {
		Text(
			text = taskWithSubject.taskEntity.title,
			style = MaterialTheme.typography.titleMedium,
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