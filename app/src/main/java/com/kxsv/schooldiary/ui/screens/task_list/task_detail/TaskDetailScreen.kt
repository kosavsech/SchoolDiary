package com.kxsv.schooldiary.ui.screens.task_list.task_detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Done
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.local.features.task.TaskEntity
import com.kxsv.schooldiary.ui.main.app_bars.topbar.TaskDetailTopAppBar
import com.kxsv.schooldiary.ui.main.navigation.DELETE_RESULT_OK
import com.kxsv.schooldiary.util.ui.LoadingContent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator
import java.time.format.DateTimeFormatter

data class TaskDetailScreenNavArgs(
	val taskId: Long,
)

@Destination(
	navArgsDelegate = TaskDetailScreenNavArgs::class
)
@Composable
fun TaskDetailScreen(
	destinationsNavigator: DestinationsNavigator,
	resultNavigator: ResultBackNavigator<Int>,
	viewModel: TaskDetailViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState,
) {
	val uiState = viewModel.uiState.collectAsState().value
	val navigator = TasksDetailNavActions(navigator = destinationsNavigator)
	Scaffold(
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		modifier = Modifier.fillMaxSize(),
		topBar = {
			TaskDetailTopAppBar(
				onBack = { destinationsNavigator.popBackStack() },
				onDelete = {
					viewModel.deleteTask()
					resultNavigator.navigateBack(DELETE_RESULT_OK)
				},
				onEdit = { navigator.onEditTask(viewModel.taskId) }
			)
		},
		floatingActionButton = {
			FloatingActionButton(onClick = { viewModel.completeTask() }) {
				Icon(
					imageVector = Icons.Filled.Done,
					contentDescription = stringResource(R.string.mark_task_done)
				)
			}
		}
	) { paddingValues ->
		
		TaskContent(
			isLoading = uiState.isLoading,
			taskEntity = uiState.taskWithSubject?.taskEntity,
			subjectEntity = uiState.taskWithSubject?.subject,
			modifier = Modifier.padding(paddingValues)
		)
		
		uiState.userMessage?.let { userMessage ->
			val snackbarText = stringResource(userMessage)
			LaunchedEffect(snackbarHostState, viewModel, userMessage, snackbarText) {
				snackbarHostState.showSnackbar(snackbarText)
				viewModel.snackbarMessageShown()
			}
		}
		
	}
}

@Composable
private fun TaskContent(
	isLoading: Boolean,
	taskEntity: TaskEntity?,
	subjectEntity: SubjectEntity?,
	modifier: Modifier,
) {
	val screenPadding = Modifier.padding(
		horizontal = dimensionResource(id = R.dimen.horizontal_margin),
		vertical = dimensionResource(id = R.dimen.vertical_margin),
	)
	val commonModifier = modifier
		.fillMaxWidth()
		.then(screenPadding)
	
	LoadingContent(
		modifier = commonModifier,
		loading = isLoading,
		isContentScrollable = true,
		empty = (taskEntity == null && subjectEntity == null),
		emptyContent = { Text(text = stringResource(R.string.no_data), modifier = commonModifier) },
		onRefresh = {}
	) {
		if (taskEntity != null) {
			Column(modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.horizontal_margin))) {
				Column {
					Text(
						text = taskEntity.title,
						style = MaterialTheme.typography.titleLarge,
					)
					Text(
						text = subjectEntity?.getName() ?: "Not loaded subject",
						style = MaterialTheme.typography.labelMedium,
					)
				}
				Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.vertical_margin)))
				Row(verticalAlignment = Alignment.CenterVertically) {
					Icon(
						imageVector = Icons.Default.CalendarToday,
						contentDescription = stringResource(R.string.due_date),
						modifier = Modifier.size(18.dp)
					)
					Spacer(modifier = Modifier.padding(horizontal = 8.dp))
					Column(verticalArrangement = Arrangement.Center) {
						Text(
							text = taskEntity.dueDate.format(DateTimeFormatter.ofPattern("d MMM yyyy")),
							style = MaterialTheme.typography.titleMedium,
						)
						Text(
							text = stringResource(R.string.due_date),
							style = MaterialTheme.typography.labelMedium,
						)
					}
				}
				Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.vertical_margin)))
				Row(verticalAlignment = Alignment.Top) {
					Icon(
						imageVector = Icons.Default.Description,
						contentDescription = stringResource(R.string.due_date),
						modifier = Modifier.size(18.dp)
					)
					Spacer(modifier = Modifier.padding(horizontal = 8.dp))
					Text(
						text = taskEntity.description,
						style = MaterialTheme.typography.titleMedium,
					)
				}
				
			}
		}
	}
}