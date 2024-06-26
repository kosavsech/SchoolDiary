package com.kxsv.schooldiary.ui.screens.main_screen

import androidx.annotation.StringRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.CircleNotifications
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.lesson.LessonEntity
import com.kxsv.schooldiary.data.local.features.lesson.LessonWithSubject
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.local.features.task.TaskEntity
import com.kxsv.schooldiary.data.local.features.task.TaskWithSubject
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeEntity
import com.kxsv.schooldiary.data.util.AppVersionState
import com.kxsv.schooldiary.ui.main.app_bars.topbar.MainTopAppBar
import com.kxsv.schooldiary.ui.main.navigation.nav_actions.AppUpdateNavActions
import com.kxsv.schooldiary.ui.main.navigation.nav_actions.MainScreenNavActions
import com.kxsv.schooldiary.ui.screens.destinations.AddEditLessonScreenDestination
import com.kxsv.schooldiary.ui.theme.AppTheme
import com.kxsv.schooldiary.ui.util.AppSnackbarHost
import com.kxsv.schooldiary.ui.util.DaysCounter
import com.kxsv.schooldiary.ui.util.LoadingContent
import com.kxsv.schooldiary.util.Utils
import com.kxsv.schooldiary.util.Utils.convertMillis
import com.kxsv.schooldiary.util.Utils.fromLocalTime
import com.kxsv.schooldiary.util.Utils.getCurrentLessonIndexByTime
import com.kxsv.schooldiary.util.Utils.getIndexOfClosestLessonToTime
import com.kxsv.schooldiary.util.Utils.getNextLessonsIndices
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.ResultRecipient
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.toKotlinLocalTime
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt

private const val TAG = "MainScreen"

const val secMilSec: Long = 1000
const val minMilSec = 60 * secMilSec
const val hourMilSec = 60 * minMilSec
const val dayMilSec = 24 * hourMilSec

@RootNavGraph(start = true)
@Destination
@Composable
fun MainScreen(
	destinationsNavigator: DestinationsNavigator,
	lessonAddEditResult: ResultRecipient<AddEditLessonScreenDestination, Int>,
	drawerState: DrawerState,
	coroutineScope: CoroutineScope,
	viewModel: MainScreenViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState,
) {
	val navigator = MainScreenNavActions(destinationsNavigator = destinationsNavigator)
	val onNavigate = remember<(String) -> Unit> {
		{ navigator.chipNavigate(route = it) }
	}
	lessonAddEditResult.onNavResult { result ->
		when (result) {
			is NavResult.Canceled -> {}
			is NavResult.Value -> {
				viewModel.showEditResultMessage(result.value)
			}
		}
	}
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
	Scaffold(
		snackbarHost = { AppSnackbarHost(hostState = snackbarHostState) },
		topBar = {
			MainTopAppBar(
				onNavigate = onNavigate,
				openDrawer = { coroutineScope.launch { drawerState.open() } }
			)
		},
		modifier = Modifier.fillMaxSize(),
	) { paddingValues ->
		val uiState = viewModel.uiState.collectAsState().value
		val classDialogState = rememberMaterialDialogState(false)

		val onRefresh = remember {
			{ viewModel.refresh() }
		}
		val onScheduleShowMore = remember {
			{ navigator.onScheduleShowMore() }
		}
		val onTaskChecked = remember<(String, Boolean) -> Unit> {
			{ id, isDone ->
				viewModel.completeTask(id = id, isDone = isDone)
			}
		}
		val onTaskClicked = remember<(String) -> Unit> {
			{ navigator.onTaskClicked(taskId = it) }
		}
		val onClassClick = remember<(
			LessonWithSubject?,
			LocalDate?,
			ClosedRange<LocalTime>?,
		) -> Unit> {
			{ lessonWithSubject, date, timings ->
				if (lessonWithSubject != null && date != null) {
					viewModel.selectClass(lessonWithSubject, date, timings)
					classDialogState.show()
				} else {
					viewModel.clickedCorruptedClass()
				}
			}
		}
		val onDeleteClass = remember<(Long?) -> Unit> {
			{
				if (it != null) {
					viewModel.deleteClass(it)
				} else {
					viewModel.clickedCorruptedClass()
				}
			}
		}
		val onEditClass = remember<(Long?) -> Unit> {
			{ lessonId ->
				if (lessonId != null) {
					navigator.onAddEditClass(lessonId)
				} else {
					viewModel.clickedCorruptedClass()
				}
			}
		}
		val onTasksShowMore = remember<(LocalDate) -> Unit> {
			{ navigator.onTasksShowMore(date = it) }
		}

		MainScreenContent(
			modifier = Modifier.padding(paddingValues),
			isLoading = uiState.isLoading,
			daysCounters = uiState.daysCounter,
			itemList = uiState.itemList,
			onRefresh = onRefresh,
			onScheduleShowMore = onScheduleShowMore,
			onLessonClick = onClassClick,
			onEditClass = onEditClass,
			onDeleteClick = onDeleteClass,
			onTaskChecked = onTaskChecked,
			onTaskClicked = onTaskClicked,
			onTasksShowMore = onTasksShowMore,
		)

		val onSubjectClick = remember<(String) -> Unit> {
			{ navigator.onSubjectClick(it) }
		}
		val unselectClass = remember {
			{ viewModel.unselectClass() }
		}
		LessonDialog(
			dialogState = classDialogState,
			classDetailed = uiState.classDetailed,
			selectedDate = uiState.classDetailedDate,
			classTimings = uiState.classDetailedTimings,
			onSubjectClick = onSubjectClick,
			onDeleteClass = onDeleteClass,
			onEditClass = onEditClass,
			unselectClass = unselectClass,
		)
	}
}

@Composable
private fun LessonDialog(
	dialogState: MaterialDialogState,
	classDetailed: LessonWithSubject?,
	selectedDate: LocalDate?,
	classTimings: ClosedRange<LocalTime>?,
	onSubjectClick: (String) -> Unit,
	onDeleteClass: (Long) -> Unit,
	onEditClass: (Long) -> Unit,
	unselectClass: () -> Unit,
) {
	MaterialDialog(
		dialogState = dialogState,
		onCloseRequest = { unselectClass(); it.hide() },
	) {
		if (classDetailed != null && selectedDate != null) {
			Column(
				modifier = Modifier
					.padding(dimensionResource(R.dimen.horizontal_margin))

			) {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.clip(MaterialTheme.shapes.extraLarge)
						.clickable {
							onSubjectClick(classDetailed.subject.subjectId)
							dialogState.hide()
						}
						.padding(dimensionResource(R.dimen.list_item_padding)),
					verticalArrangement = Arrangement.Center,
				) {
					Text(
						classDetailed.subject.getName(),
						style = MaterialTheme.typography.titleMedium
					)
					Spacer(modifier = Modifier.padding(vertical = 4.dp))
					Text(
						selectedDate.dayOfWeek.name,
						style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal)
					)
					Spacer(modifier = Modifier.padding(vertical = 2.dp))

					val text: String =
						if (classTimings != null) {
							classTimings.start.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)) +
									" - " +
									classTimings.endInclusive.format(
										DateTimeFormatter.ofLocalizedTime(
											FormatStyle.SHORT
										)
									)
						} else stringResource(
							R.string.class_out_of_strokes_bounds_message,
							classDetailed.lesson.index + 1
						)
					Text(
						text,
						style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal)
					)
				}
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.Center
				) {
					FilledTonalButton(
						onClick = { onDeleteClass(classDetailed.lesson.lessonId) },
						modifier = Modifier.weight(0.45f)
					) {
						Text(text = stringResource(R.string.btn_delete))
					}
					Spacer(modifier = Modifier.weight(0.1f))
					FilledTonalButton(
						onClick = {
							onEditClass(classDetailed.lesson.lessonId)
							dialogState.hide()
						},
						modifier = Modifier.weight(0.45f)
					) {
						Text(text = stringResource(R.string.btn_edit))
					}
				}
				val cabinetText = remember(classDetailed) {
					classDetailed.lesson.cabinet ?: classDetailed.subject.cabinet
				}
				if (cabinetText != null) {
					Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding)))
					Row(
						modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.list_item_padding)),
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.SpaceBetween
					) {
						Icon(
							imageVector = Icons.Default.LocationOn,
							contentDescription = stringResource(R.string.lesson_room),
						)
						Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.list_item_padding)))
						Column {
							Text(
								text = cabinetText,
								style = MaterialTheme.typography.labelLarge
							)
							Text(
								text = stringResource(R.string.cabinet_hint),
								style = MaterialTheme.typography.labelMedium
							)
						}
					}
				}
			}
		}
	}
}


@Composable
private fun MainScreenContent(
	modifier: Modifier,
	isLoading: Boolean,
	daysCounters: List<DaysCounter>,
	itemList: List<MainScreenItem>,
	onRefresh: () -> Unit,
	onScheduleShowMore: () -> Unit,
	onLessonClick: (LessonWithSubject?, LocalDate?, ClosedRange<LocalTime>?) -> Unit,
	onEditClass: (Long?) -> Unit,
	onDeleteClick: (Long?) -> Unit,
	onTaskClicked: (String) -> Unit,
	onTaskChecked: (String, Boolean) -> Unit,
	onTasksShowMore: (LocalDate) -> Unit,
) {
	LoadingContent(
		modifier = modifier,
		isLoading = isLoading,
		empty = (itemList.isEmpty() && daysCounters.isEmpty()),
		isContentScrollable = true,
		onRefresh = onRefresh
	) {
		Column {
			Column(
				modifier = Modifier
					.verticalScroll(rememberScrollState())
					.fillMaxWidth()
					.padding(horizontal = dimensionResource(id = R.dimen.horizontal_margin))
			) {
				DaysLeftBoard(counters = daysCounters)
				if (itemList.isEmpty()) {
					Box(
						contentAlignment = Alignment.Center,
						modifier = Modifier.fillMaxSize()
					) {
						Text(
							text = stringResource(id = R.string.holidays),
							style = MaterialTheme.typography.displayMedium,
							modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding))
						)
					}
				}
				itemList.forEach {
					if (it.date == Utils.currentDate) {
						key(it.date, it.classes, it.pattern) {
							CurrentDay(
								classes = it.classes,
								currentPattern = it.pattern,
								onScheduleShowMore = onScheduleShowMore,
								onLessonClick = onLessonClick,
								onEditClass = onEditClass,
								onDeleteClick = onDeleteClick
							)
						}
					} else {
						key(it.date, it.classes, it.pattern, it.tasks) {
							ScheduleDay(
								date = it.date,
								classes = it.classes,
								tasks = it.tasks,
								currentPattern = it.pattern,
								onTasksShowMore = { onTasksShowMore(it.date) },
								onTaskClicked = onTaskClicked,
								onTaskChecked = onTaskChecked
							)
						}
					}
					Spacer(modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.vertical_margin)))
				}
			}
		}
	}
}

@Composable
private fun DaysLeftBoard(
	counters: List<DaysCounter>,
) {
	Column {
		counters.forEach {
			if (it.value != null) {
				key(it) {
					DaysLeftItem(title = it.textRes, value = it.value)
				}
			}
		}
	}
}

@Composable
private fun DaysLeftItem(
	@StringRes title: Int,
	value: Int,
) {
	Text(
		text = stringResource(id = title) + ": " + value,
		style = MaterialTheme.typography.bodyLarge,
		modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding))
	)
}

@Composable
private fun CurrentDay(
	classes: Map<Int, LessonWithSubject>,
	currentPattern: List<PatternStrokeEntity>,
	onScheduleShowMore: () -> Unit,
	onLessonClick: (LessonWithSubject?, LocalDate?, ClosedRange<LocalTime>?) -> Unit,
	onEditClass: (Long?) -> Unit,
	onDeleteClick: (Long?) -> Unit,
) {
	Column {
		DayHeader(date = Utils.currentDate)
		ElevatedCard(
			elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.0.dp)
		) {
			Column {
				var timeUpdatedByTiming by remember { mutableStateOf(LocalTime.now()) }
				LaunchedEffect(timeUpdatedByTiming) {
					delay(CURRENT_DAY_INFO_UPDATE_TIMING)
					timeUpdatedByTiming = LocalTime.now()
				}

				val currentLessonIndex = remember(currentPattern, timeUpdatedByTiming) {
					currentPattern.getCurrentLessonIndexByTime(timeUpdatedByTiming)
				}
				val currentLesson = remember(currentLessonIndex, classes) {
					if (currentLessonIndex == null) return@remember null
					classes[currentLessonIndex]
				}
				if (currentLessonIndex != null && currentLesson != null) {
					key(currentLesson, currentPattern) {
						val startTime = currentPattern.getOrNull(currentLessonIndex)?.startTime
						val endTime = currentPattern.getOrNull(currentLessonIndex)?.endTime
						val timings =
							if (startTime != null && endTime != null) (startTime..endTime) else null
						LessonInfo(
							isDetailed = true,
							label = R.string.right_now_label,
							labelIcon = Icons.Outlined.CircleNotifications,
							lessonWithSubject = currentLesson,
							startTime = startTime,
							endTime = endTime,
							timeUpdatedByTiming = timeUpdatedByTiming,
							onLessonClick = {
								onLessonClick(
									currentLesson,
									Utils.currentDate,
									timings
								)
							},
							onEditClass = { onEditClass(currentLesson.lesson.lessonId) },
							onDeleteClick = { onDeleteClick(currentLesson.lesson.lessonId) },
						)
					}
				}

				val nextFourLessonsIndices =
					remember(timeUpdatedByTiming, currentPattern, classes) {
						if (currentPattern.isNotEmpty() && classes.isNotEmpty()) {
							classes.keys.getNextLessonsIndices(
								n = 4,
								startIndex = classes.keys
									.getIndexOfClosestLessonToTime(
										time = timeUpdatedByTiming,
										pattern = currentPattern
									)
							)
						} else null
					}


				nextFourLessonsIndices?.forEach { lessonIndex ->
					key(lessonIndex, currentLesson, classes, currentPattern) {
						val isDetailed = (lessonIndex == nextFourLessonsIndices.first())
						if (isDetailed && currentLesson != null) {
							Divider()
						}
						val startTime = currentPattern.getOrNull(lessonIndex)?.startTime
						val endTime = currentPattern.getOrNull(lessonIndex)?.endTime
						val timings =
							if (startTime != null && endTime != null) (startTime..endTime) else null
						val lessonWithSubject = classes[lessonIndex]
							?: LessonWithSubject(
								lesson = LessonEntity(index = lessonIndex),
								subject = SubjectEntity("Something went wrong")
							)
						LessonInfo(
							isDetailed = isDetailed,
							label = R.string.next_lessons_label,
							labelIcon = Icons.Default.ViewDay,
							lessonWithSubject = lessonWithSubject,
							startTime = startTime,
							endTime = endTime,
							timeUpdatedByTiming = timeUpdatedByTiming,
							onLessonClick = {
								onLessonClick(classes[lessonIndex], Utils.currentDate, timings)
							},
							onEditClass = { onEditClass(lessonWithSubject.lesson.lessonId) },
							onDeleteClick = { onDeleteClick(lessonWithSubject.lesson.lessonId) }
						)
						if (isDetailed && nextFourLessonsIndices.size > 1) {
							Divider()
						}
					}
				}

				val emptyLessonsTextRes =
					remember(currentPattern, classes, nextFourLessonsIndices, currentLesson) {
						when {
							currentPattern.isEmpty() -> R.string.pattern_not_set

							classes.isEmpty() -> R.string.no_schedule_for_day

							nextFourLessonsIndices.isNullOrEmpty() && (currentLesson == null) -> {
								R.string.study_day_passed
							}

							else -> null
						}
					}
				if (emptyLessonsTextRes != null) {
					Text(
						text = stringResource(emptyLessonsTextRes),
						style = MaterialTheme.typography.displayMedium
					)
				}
			}
			ShowMore(onScheduleShowMore)
		}
	}
}


@Composable
private fun ScheduleDay(
	date: LocalDate,
	classes: Map<Int, LessonWithSubject>,
	tasks: List<TaskWithSubject>,
	currentPattern: List<PatternStrokeEntity>,
	onTaskChecked: (String, Boolean) -> Unit,
	onTaskClicked: (String) -> Unit,
	onTasksShowMore: () -> Unit,
) {
	Column {
		key(date) { DayHeader(date = date) }
		ElevatedCard(
			elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.0.dp)
		) {
			Column {
				key(tasks) {
					TasksOverview(
						tasks = tasks,
						onTaskChecked = onTaskChecked,
						onTaskClicked = onTaskClicked
					)
				}
			}
			ShowMore(onTasksShowMore)
		}
		Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding)))
		classes.forEach {
			key(it.value, it.key) {
				LessonShort(
					subjectEntity = it.value.subject,
					startTime = currentPattern.getOrNull(it.key)?.startTime,
					endTime = currentPattern.getOrNull(it.key)?.endTime,
				)
			}
		}
	}
}

@Composable
fun ShowMore(
	onClick: () -> Unit,
) {
	Divider()

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(
				interactionSource = remember { MutableInteractionSource() },
				indication = rememberRipple(
					bounded = true,
					radius = Dp.Unspecified,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				),
				onClick = onClick
			)
			.padding(dimensionResource(id = R.dimen.vertical_margin)),
		horizontalArrangement = Arrangement.Start,
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = stringResource(id = R.string.btn_show_more),
			style = MaterialTheme.typography.bodyMedium
		)
		Spacer(modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.list_item_padding)))
		Icon(
			imageVector = Icons.Default.ArrowRightAlt,
			contentDescription = stringResource(id = R.string.btn_show_more),
			tint = LocalContentColor.current
		)
	}

}

@Composable
private fun TasksOverview(
	tasks: List<TaskWithSubject>,
	onTaskChecked: (String, Boolean) -> Unit,
	onTaskClicked: (String) -> Unit,
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
	) {
		val noTasks = remember(tasks) { tasks.isEmpty() }
		val pendingTasks = remember(tasks) { tasks.filterNot { it.taskEntity.isDone } }
		val message = remember(noTasks, pendingTasks) {
			if (noTasks) {
				R.string.no_task_for_this_day
			} else if (pendingTasks.isEmpty()) {
				R.string.day_tasks_done
			} else null
		}
		if (message != null) {
			Text(
				text = stringResource(message),
				style = MaterialTheme.typography.displayMedium
			)
		}
		pendingTasks.forEachIndexed { index, taskEntity ->
			if (index >= 5) return@Column
			key(index, taskEntity) {
				TasksOverviewItem(
					index = index,
					taskEntity = taskEntity,
					onTaskChecked = onTaskChecked,
					onTaskClicked = onTaskClicked
				)
			}
		}
	}
}

@Composable
private fun TasksOverviewItem(
	index: Int,
	taskEntity: TaskWithSubject,
	onTaskChecked: (String, Boolean) -> Unit,
	onTaskClicked: (String) -> Unit,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onTaskClicked(taskEntity.taskEntity.taskId) }
			.padding(
				top = dimensionResource(R.dimen.list_item_padding),
				bottom = dimensionResource(R.dimen.list_item_padding),
				start = dimensionResource(R.dimen.horizontal_margin)
			),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		// to remove delay of check change
		var isChecked by remember { mutableStateOf(taskEntity.taskEntity.isDone) }
		Row(
			verticalAlignment = Alignment.Top,
			modifier = Modifier.weight(1f)
		) {
			Text(
				text = (index + 1).toString(),
				style = MaterialTheme.typography.titleMedium,
			)
			Spacer(modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.list_item_padding)))
			Column {
				val textDecoration = if (isChecked) TextDecoration.LineThrough else null
				Text(
					text = taskEntity.taskEntity.title,
					style = MaterialTheme.typography.titleMedium,
					textDecoration = textDecoration,
				)
				Text(
					text = taskEntity.subject.getName(),
					style = MaterialTheme.typography.labelMedium,
				)
			}
		}
		Checkbox(
			checked = isChecked,
			onCheckedChange = {
				isChecked = it
				onTaskChecked(taskEntity.taskEntity.taskId, it)
			},
		)
	}
}


@Composable
private fun LessonInfo(
	isDetailed: Boolean,
	label: Int,
	labelIcon: ImageVector,
	lessonWithSubject: LessonWithSubject,
	startTime: LocalTime?,
	endTime: LocalTime?,
	timeUpdatedByTiming: LocalTime,
	onLessonClick: () -> Unit,
	onEditClass: () -> Unit,
	onDeleteClick: () -> Unit,
) {
	if (isDetailed) {
		LessonDetailed(
			label = label,
			labelIcon = labelIcon,
			lessonWithSubject = lessonWithSubject,
			startTime = startTime,
			endTime = endTime,
			timeUpdatedByTiming = timeUpdatedByTiming,
			onLessonClick = onLessonClick,
			onEditClass = onEditClass,
			onDeleteClick = onDeleteClick
		)
	} else {
		LessonBrief(
			lessonWithSubject = lessonWithSubject,
			startTime = startTime,
			endTime = endTime,
			onLessonClick = onLessonClick,
			onEditClass = onEditClass,
			onDeleteClick = onDeleteClick
		)
	}
}

@Composable
private fun DayHeader(
	date: LocalDate,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.wrapContentHeight()
			.padding(vertical = dimensionResource(R.dimen.vertical_margin)),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween,
	) {
		val context = LocalContext.current
		val dayOfWeekText = remember(date) {
			when (date) {
				Utils.currentDate -> context.getString(R.string.today_filter)

				Utils.currentDate.plusDays(1) -> context.getString(R.string.tomorrow_filter)

				else -> date.dayOfWeek.getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH)
			}
		}
		Text(
			text = dayOfWeekText,
			style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
		)
		Text(
			text = date.format(DateTimeFormatter.ofPattern("dd MMMM")),
			style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
		)
	}
}

@Composable
fun Countdown(targetTime: Int, delay: Long, content: @Composable (remainingTime: Int) -> Unit) {
	val currentTimeInMilli = LocalTime.now().toKotlinLocalTime().toMillisecondOfDay()
	var remainingTime by remember(targetTime) {
		mutableIntStateOf(targetTime.minus(currentTimeInMilli))
	}

	content.invoke(remainingTime)

	LaunchedEffect(remainingTime) {
		val diff = remainingTime - (targetTime - currentTimeInMilli)
		delay(delay - diff)
		remainingTime = targetTime - currentTimeInMilli
	}
}

@Composable
private fun LessonDetailed(
	label: Int,
	labelIcon: ImageVector,
	lessonWithSubject: LessonWithSubject,
	startTime: LocalTime?,
	endTime: LocalTime?,
	timeUpdatedByTiming: LocalTime,
	onLessonClick: () -> Unit,
	onEditClass: () -> Unit,
	onDeleteClick: () -> Unit,
) {
	var delay by remember { mutableStateOf(LessonDetailedUpdateTiming.VERY_SHORT) }

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clip(MaterialTheme.shapes.medium)
			.clickable { onLessonClick() }
			.padding(
				vertical = dimensionResource(R.dimen.horizontal_margin),
				horizontal = dimensionResource(R.dimen.horizontal_margin),
			),
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				imageVector = labelIcon,
				contentDescription = "Label icon"
			)
			Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.list_item_padding)))
			Text(
				text = stringResource(label),
				style = MaterialTheme.typography.titleSmall
			)
		}
		Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding) / 3))
		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.weight(1f)
			) {
				Icon(
					imageVector = Icons.Outlined.Circle,
					contentDescription = "Lesson name"
				)
				Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.list_item_padding)))
				val subjectNameText = remember(lessonWithSubject) {
					lessonWithSubject.subject.getName()
				}
				Text(
					text = subjectNameText,
					style = MaterialTheme.typography.headlineSmall,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
			}
			Row(
				verticalAlignment = Alignment.CenterVertically,
			) {
				Icon(
					imageVector = Icons.Outlined.AccessTime,
					contentDescription = "Time until lesson start/end"
				)
				Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.list_item_padding) / 2))
				val (countdownTargetTime, isCountingToEnd) = remember(
					timeUpdatedByTiming,
					startTime,
					endTime
				) {
					if (startTime == null || endTime == null) return@remember Pair(null, true)
					if (timeUpdatedByTiming in startTime..endTime) {
						Pair(endTime.toKotlinLocalTime().toMillisecondOfDay(), true)
					} else {
						Pair(startTime.toKotlinLocalTime().toMillisecondOfDay(), false)

					}
				}
				if (countdownTargetTime == null) {
					Text(
						text = stringResource(R.string.no_pattern_stroke),
						style = MaterialTheme.typography.bodyMedium
					)
				} else {
					Countdown(countdownTargetTime, delay.timing) { remainingTime ->
						val convertedMillis = convertMillis(remainingTime)
						val (hours, minutes, seconds, milliSeconds) = convertedMillis

						val timeUntil = if (remainingTime <= 0) {
							delay = LessonDetailedUpdateTiming.LONG
							if (isCountingToEnd) "Finished" else "Started"
						} else {
							when {
								hours < 1 && minutes < 1 -> {
									delay = LessonDetailedUpdateTiming.VERY_SHORT
									val tens = when (milliSeconds) {
										in 100..999 -> milliSeconds.div(100f).roundToInt()
										in 11..99 -> milliSeconds.div(10f).roundToInt()
										else -> milliSeconds
									}
									"$seconds.$tens s"
								}

								hours < 1 && minutes < 5 -> {
									delay = LessonDetailedUpdateTiming.SHORT
									"$minutes m ${seconds + 1} s"
								}

								hours < 1 -> {
									delay = LessonDetailedUpdateTiming.DEFAULT
									"${minutes + 1} m"
								}

								else -> {
									delay = LessonDetailedUpdateTiming.VERY_LONG
									"$hours h ${minutes + 1} m"
								}
							}
						}
						Text(
							text = timeUntil,
							style = MaterialTheme.typography.bodyMedium
						)
					}
				}
			}
		}
		Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding) / 2))
		Row(
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Row {
				val text = if (startTime != null && endTime != null) {
					startTime.format(
						DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
					) + " - " +
							endTime.format(
								DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
							)
				} else {
					stringResource(R.string.no_pattern_stroke)
				}
				Text(
					text = text,
					style = MaterialTheme.typography.bodyMedium
				)
			}
			val cabinetText = remember(lessonWithSubject) {
				lessonWithSubject.lesson.cabinet ?: lessonWithSubject.subject.cabinet
			}
			if (cabinetText != null) {
				Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.list_item_padding)))
				Row(
					verticalAlignment = Alignment.CenterVertically
				) {
					Icon(
						imageVector = Icons.Outlined.Place,
						contentDescription = "Time of lesson left",
						modifier = Modifier.size(20.dp)
					)
					Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.list_item_padding) / 2))
					Text(
						text = cabinetText,
						style = MaterialTheme.typography.bodyMedium
					)
				}
			}
		}

		val timeOfLessonPassed = remember(timeUpdatedByTiming, startTime) {
			startTime?.until(timeUpdatedByTiming, ChronoUnit.SECONDS)?.toFloat()
		}
		val lessonDuration = remember(startTime, endTime) {
			startTime?.until(endTime, ChronoUnit.SECONDS)?.toFloat()
		}
		val indicatorProgress = remember(timeOfLessonPassed, lessonDuration) {
			if (timeOfLessonPassed == null || lessonDuration == null || timeOfLessonPassed <= 0) return@remember 0F
			(timeOfLessonPassed / lessonDuration)
		}
		val progressAnimation by animateFloatAsState(
			targetValue = indicatorProgress,
			animationSpec = tween(durationMillis = 2750, easing = FastOutSlowInEasing),
			label = "Lesson time passed animation progress"
		)
		Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding) / 2))
		if (progressAnimation > 0) {
			LinearProgressIndicator(
				progress = progressAnimation,
				modifier = Modifier
					.fillMaxWidth()
					.height(dimensionResource(R.dimen.list_item_padding))
			)
		}
	}
}

@Composable
private fun LessonBrief(
	lessonWithSubject: LessonWithSubject,
	startTime: LocalTime?,
	endTime: LocalTime?,
	onLessonClick: () -> Unit,
	onEditClass: () -> Unit,
	onDeleteClick: () -> Unit,
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clip(MaterialTheme.shapes.medium)
			.clickable { onLessonClick() }
			.padding(
				vertical = dimensionResource(id = R.dimen.list_item_padding),
				horizontal = dimensionResource(R.dimen.horizontal_margin),
			),
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically,
		) {
			Icon(
				imageVector = Icons.Outlined.Circle,
				contentDescription = "Lesson name",
				modifier = Modifier.size(18.dp)
			)
			Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.list_item_padding)))
			Text(
				text = lessonWithSubject.subject.getName(),
				style = MaterialTheme.typography.bodyLarge,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		}
		Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding) / 2))
		Row(
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Row {
				val timingsText = remember(startTime, endTime) {
					if (startTime == null || endTime == null) return@remember null
					fromLocalTime(startTime) + " - " + fromLocalTime(endTime)
				}
				Text(
					text = timingsText ?: stringResource(R.string.no_pattern_stroke),
					style = MaterialTheme.typography.bodyMedium
				)
			}
			val cabinetText = remember(lessonWithSubject) {
				lessonWithSubject.lesson.cabinet ?: lessonWithSubject.subject.cabinet
			}
			if (cabinetText != null) {
				Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.list_item_padding)))
				Row(
					verticalAlignment = Alignment.CenterVertically
				) {
					Icon(
						imageVector = Icons.Outlined.Place,
						contentDescription = "Time of lesson left"
					)
					Text(
						text = cabinetText,
						style = MaterialTheme.typography.bodyMedium
					)
				}
			}
		}
	}
}


@Composable
private fun LessonShort(
	subjectEntity: SubjectEntity,
	startTime: LocalTime?,
	endTime: LocalTime?,
) {
	Row(
		modifier = Modifier.padding(
			horizontal = dimensionResource(R.dimen.list_item_padding),
			vertical = dimensionResource(R.dimen.list_item_padding) / 3,
		),
		verticalAlignment = Alignment.CenterVertically,
	) {
		val timingText = if (startTime != null && endTime != null) {
			fromLocalTime(startTime) + " - " + fromLocalTime(endTime)
		} else {
			stringResource(R.string.no_pattern_stroke)
		}
		Text(
			text = timingText,
			style = MaterialTheme.typography.labelSmall,
			modifier = Modifier.weight(0.48f)
		)
		Row(
			modifier = Modifier.weight(1f),
			verticalAlignment = Alignment.CenterVertically,
		) {
			Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.list_item_padding) / 2))
			Icon(
				imageVector = Icons.Outlined.Circle,
				contentDescription = "Lesson name",
				modifier = Modifier.size(12.dp)
			)
			Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.list_item_padding)))
			Text(
				text = subjectEntity.getName(),
				style = MaterialTheme.typography.bodySmall,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		}
	}
}

private val previewCurrentPattern = listOf(
	PatternStrokeEntity(
		startTime = LocalTime.of(16, 0),
		endTime = LocalTime.of(16, 5),
		index = 0,
	),
	PatternStrokeEntity(
		startTime = LocalTime.of(16, 10),
		endTime = LocalTime.of(16, 15),
		index = 1,
	),
	PatternStrokeEntity(
		startTime = LocalTime.of(16, 0),
		endTime = LocalTime.of(16, 5),
		index = 2,
	),
)
private val previewClasses1 = mapOf(
	Pair(0, LessonWithSubject(LessonEntity(0), SubjectEntity("Татарский язык", "210"))),
	Pair(1, LessonWithSubject(LessonEntity(0), SubjectEntity("Русский язык", "210"))),
	Pair(
		2,
		LessonWithSubject(LessonEntity(0), SubjectEntity("Иностранный язык (Английский)", "316"))
	),
	Pair(
		3,
		LessonWithSubject(
			LessonEntity(0),
			SubjectEntity("Основы безопасности жизнедеятельности", "316")
		)
	),
	Pair(4, LessonWithSubject(LessonEntity(0), SubjectEntity("Алгебра", "310"))),
	Pair(5, LessonWithSubject(LessonEntity(0), SubjectEntity("Литература", "210"))),
)

private val previewTasks1 = listOf(
	TaskWithSubject(
		taskEntity = TaskEntity(
			title = "стр 52-53 выучить, записи в тетради-подготовка к СР по астрономии, параграф 30+ конспекты тренировочных заданий уроков 46,47,49,50 на РЭШ",
			description = "",
			dueDate = Utils.currentDate.plusDays(1),
			subjectMasterId = "1"
		),
		SubjectEntity("Физика", subjectId = "1")
	),
	TaskWithSubject(
		taskEntity = TaskEntity(
			title = "короче короткое задание там да",
			description = "",
			dueDate = Utils.currentDate.plusDays(1),
			subjectMasterId = "2"
		),
		SubjectEntity("Русский язык", subjectId = "2")
	)
)

private val previewItems = listOf(
	MainScreenItem(
		date = Utils.currentDate,
		classes = previewClasses1,
		tasks = previewTasks1,
		pattern = previewCurrentPattern
	),
	MainScreenItem(
		date = Utils.currentDate.plusDays(1),
		classes = previewClasses1,
		tasks = previewTasks1,
		pattern = previewCurrentPattern
	),
	/*MainScreenItem(Utils.currentDate.plusDays(1), previewClasses2, previewTasks1),
	MainScreenItem(Utils.currentDate.plusDays(2), previewClasses2),
	MainScreenItem(Utils.currentDate.plusDays(3), previewClasses1),
	MainScreenItem(Utils.currentDate.plusDays(4), previewClasses1),
	MainScreenItem(Utils.currentDate.plusDays(5), previewClasses1),
	MainScreenItem(Utils.currentDate.plusDays(6), previewClasses1),*/
)

@Preview
@Composable
private fun MainScreenContentPreview() {
	AppTheme(darkTheme = true) {
		Surface {
			MainScreenContent(
				modifier = Modifier,
				isLoading = false,
				daysCounters = emptyList(),
				itemList = previewItems,
				onRefresh = {},
				onScheduleShowMore = {},
				onLessonClick = { _, _, _ -> },
				onEditClass = {},
				onDeleteClick = {},
				onTaskClicked = {},
				onTaskChecked = { _, _ -> },
			) {}
		}
	}
}
