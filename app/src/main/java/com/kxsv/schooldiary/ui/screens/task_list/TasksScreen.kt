package com.kxsv.schooldiary.ui.screens.task_list


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.task.TaskEntity
import com.kxsv.schooldiary.ui.main.topbar.TasksTopAppBar
import com.kxsv.schooldiary.util.ui.LoadingContent

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
			loading = uiState.isLoading,
			tasks = uiState.tasks,
			//noSubjectsLabel = 0,
			onTaskClick = onTaskClick,
			modifier = Modifier.padding(paddingValues),
		)
		
		// Check for user messages to display on the screen
		uiState.userMessage?.let { userMessage ->
			val snackbarText = stringResource(userMessage)
			LaunchedEffect(snackbarText) {
				snackbarHostState.showSnackbar(snackbarText)
				viewModel.snackbarMessageShown()
			}
		}
	}
}

@Composable
private fun TasksContent(
	loading: Boolean,
	tasks: List<TaskEntity>,
	onTaskClick: (TaskEntity) -> Unit,
	modifier: Modifier,
) {
	LoadingContent(
		loading = loading,
		isContentScrollable = true,
		empty = tasks.isEmpty(),
		emptyContent = { Text(text = "No tasks yet") },
		onRefresh = { /*TODO*/ }
	) {
		LazyColumn(
			modifier = modifier
				.padding(vertical = dimensionResource(R.dimen.list_item_padding)),
		) {
			items(tasks) { task ->
				TaskItem(
					task = task,
					onTaskClick = onTaskClick,
				)
			}
		}
	}
}

@Composable
private fun TaskItem(
	task: TaskEntity,
	onTaskClick: (TaskEntity) -> Unit,
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onTaskClick(task) }
			.padding(
				horizontal = dimensionResource(R.dimen.horizontal_margin),
				vertical = dimensionResource(R.dimen.vertical_margin)
			)
	) {
		Text(
			text = task.name,
			style = MaterialTheme.typography.titleMedium,
		)
	}
	
}