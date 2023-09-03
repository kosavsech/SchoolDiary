package com.kxsv.schooldiary.ui.screens.task_list


import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.local.features.task.TaskEntity
import com.kxsv.schooldiary.data.local.features.task.TaskWithSubject
import com.kxsv.schooldiary.data.util.AppVersionState
import com.kxsv.schooldiary.ui.main.app_bars.bottombar.TasksBottomAppBar
import com.kxsv.schooldiary.ui.main.app_bars.topbar.TasksTopAppBar
import com.kxsv.schooldiary.ui.main.navigation.nav_actions.AppUpdateNavActions
import com.kxsv.schooldiary.ui.main.navigation.nav_actions.TasksScreenNavActions
import com.kxsv.schooldiary.ui.screens.destinations.TaskDetailScreenDestination
import com.kxsv.schooldiary.ui.screens.grade_list.MY_URI
import com.kxsv.schooldiary.ui.theme.AppTheme
import com.kxsv.schooldiary.ui.util.LoadingContent
import com.kxsv.schooldiary.util.Utils
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.FULL_ROUTE_PLACEHOLDER
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.ResultRecipient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class TasksScreenNavArgs(
	val dateStamp: Long?,
)

@Destination(
	navArgsDelegate = TasksScreenNavArgs::class,
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
	taskDeleteResult: ResultRecipient<TaskDetailScreenDestination, Int>,
	drawerState: DrawerState,
	coroutineScope: CoroutineScope,
	viewModel: TasksViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState,
) {
	val uiState = viewModel.uiState.collectAsState().value
	val formattedDate =
		Utils.datestampToLocalDate(viewModel.dateStamp)
			?.format(DateTimeFormatter.ofPattern("MMM-dd"))
	
	val navigator = TasksScreenNavActions(destinationsNavigator = destinationsNavigator)
	val updateDialog = AppUpdateNavActions(destinationsNavigator = destinationsNavigator)
	val toShowUpdateDialog = viewModel.toShowUpdateDialog.collectAsState().value
	LaunchedEffect(toShowUpdateDialog) {
		when (toShowUpdateDialog) {
			is AppVersionState.MustUpdate -> {
				updateDialog.onMandatoryUpdate(toShowUpdateDialog.update)
			}
			
			is AppVersionState.ShouldUpdate -> {
				updateDialog.onAvailableUpdate(toShowUpdateDialog.update)
				viewModel.onUpdateDialogShown()
			}
			
			else -> Unit
		}
	}
	taskDeleteResult.onNavResult { result ->
		when (result) {
			is NavResult.Canceled -> {}
			is NavResult.Value -> {
				viewModel.showEditResultMessage(result.value)
			}
		}
	}
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
				selectedDataFilterText = stringResource(
					uiState.dateFilterType.getLocalisedStringId(),
					formattedDate ?: stringResource(R.string.something_went_wrong)
				),
				isSpecificDatePresent = formattedDate != null,
				onAddTask = { navigator.onAddTask(viewModel.dateStamp) }
			) { viewModel.changeDataFilter(it) }
		},
		modifier = Modifier.fillMaxSize(),
	) { paddingValues ->
		
		val onTaskClick = remember<(String) -> Unit> {
			{ taskId -> navigator.onTaskClick(taskId) }
		}
		val onRefresh = remember<() -> Unit> {
			{ viewModel.refresh() }
		}
		TasksContent(
			modifier = Modifier.padding(paddingValues),
			isLoading = uiState.isLoading,
			tasksGroups = uiState.tasks,
			onTaskClick = onTaskClick,
			onRefresh = onRefresh
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
	modifier: Modifier,
	isLoading: Boolean,
	tasksGroups: Map<LocalDate, List<TaskWithSubject>>,
	onTaskClick: (String) -> Unit,
	onRefresh: () -> Unit,
) {
	LoadingContent(
		modifier = modifier,
		loading = isLoading,
		empty = tasksGroups.isEmpty(),
		emptyContent = { Text(text = "No tasks yet") },
		isContentScrollable = true,
		onRefresh = onRefresh
	) {
		LazyColumn(
			contentPadding = PaddingValues(vertical = dimensionResource(R.dimen.list_item_padding)),
		) {
			tasksGroups.keys.toList().forEach { dateKey ->
				item {
					Row(
						modifier = Modifier.padding(
							vertical = dimensionResource(R.dimen.list_item_padding),
							horizontal = dimensionResource(R.dimen.horizontal_margin),
						),
						verticalAlignment = CenterVertically,
						horizontalArrangement = Arrangement.Start
					) {
						val titleText =
							if (dateKey == Utils.currentDate) {
								"Today"
							} else if (ChronoUnit.DAYS.between(Utils.currentDate, dateKey) == 1L) {
								"Tomorrow"
							} else if (ChronoUnit.DAYS.between(Utils.currentDate, dateKey) == -1L) {
								"Yesterday"
							} else if (dateKey.isAfter(Utils.currentDate)) {
								ChronoUnit.DAYS.between(Utils.currentDate, dateKey)
									.toString() + " days"
							} else {
								ChronoUnit.DAYS.between(dateKey, Utils.currentDate)
									.toString() + " days ago"
							}
						Text(
							text = titleText,
							style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
							color = MaterialTheme.colorScheme.primary
						)
						Spacer(modifier = Modifier.padding(horizontal = 8.dp))
						Text(
							text = dateKey.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
							style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
							color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
						)
					}
				}
				items(tasksGroups.getOrDefault(dateKey, emptyList())) { taskWithSubject ->
					Column {
						TaskItem(
							taskWithSubject = taskWithSubject,
							onTaskClick = onTaskClick,
						)
						
						Divider(
							modifier = Modifier
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
	onTaskClick: (String) -> Unit,
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
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
			)
		}
		Spacer(modifier = Modifier.padding(vertical = 4.dp))
		Row(
			verticalAlignment = Alignment.CenterVertically,
		) {
			Icon(
				imageVector = Icons.Outlined.Circle,
				contentDescription = "Subject",
				modifier = Modifier.size(14.dp)
			)
			Spacer(modifier = Modifier.padding(horizontal = 4.dp))
			Text(
				text = taskWithSubject.subject.getName(),
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
			)
		}
	}
}


val previewTasks = listOf(
	TaskWithSubject(
		subject = SubjectEntity(fullName = "Физическая культура"),
		taskEntity = TaskEntity(
			title = "Tomorrow",
			dueDate = Utils.currentDate.plusDays(1),
			subjectMasterId = ""
		)
	),
	TaskWithSubject(
		subject = SubjectEntity(fullName = "Алгебра"),
		taskEntity = TaskEntity(
			title = "задания №20",
			dueDate = Utils.currentDate.plusDays(1),
			subjectMasterId = ""
		)
	),
	TaskWithSubject(
		subject = SubjectEntity(fullName = "Русский язык"),
		taskEntity = TaskEntity(
			title = "Параграф 36, вар.18",
			dueDate = Utils.currentDate.plusDays(1),
			subjectMasterId = ""
		)
	),
	TaskWithSubject(
		subject = SubjectEntity(fullName = "Обществознание"),
		taskEntity = TaskEntity(
			title = "п. 25 читать пересказывать",
			dueDate = Utils.currentDate.plusDays(1),
			subjectMasterId = ""
		)
	),
	TaskWithSubject(
		subject = SubjectEntity(fullName = "Русский язык"),
		taskEntity = TaskEntity(
			title = "Параграф 36, вар.19",
			dueDate = Utils.currentDate.plusDays(2),
			subjectMasterId = ""
		)
	),
	TaskWithSubject(
		subject = SubjectEntity(fullName = "Физика"),
		taskEntity = TaskEntity(
			title = "стр 52-53 выучить, записи в тетради-подготовка к СР по астрономии, параграф 30+ конспекты тренировочных заданий уроков 46,47,49,50 на РЭШ",
			dueDate = Utils.currentDate.plusDays(2),
			subjectMasterId = ""
		)
	),
	TaskWithSubject(
		subject = SubjectEntity(fullName = "Литература"),
		taskEntity = TaskEntity(
			title = "Подготовить анализ пьесы.",
			dueDate = Utils.currentDate.plusDays(2),
			subjectMasterId = ""
		)
	),
	TaskWithSubject(
		subject = SubjectEntity(fullName = "Химия"),
		taskEntity = TaskEntity(
			title = "лекция в тетради",
			dueDate = Utils.currentDate.plusDays(2),
			subjectMasterId = ""
		)
	),
)
val previewTaskGroups = previewTasks.groupBy { it.taskEntity.dueDate }.toSortedMap()

@Preview
@Composable
fun TasksScreenContentPreview() {
	AppTheme(darkTheme = true) {
		Surface {
			TasksContent(
				modifier = Modifier,
				isLoading = false,
				tasksGroups = previewTaskGroups,
				onTaskClick = {},
				onRefresh = {}
			)
		}
	}
}