package com.kxsv.schooldiary.ui.screens.main_screen

import androidx.annotation.StringRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.CircleNotifications
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.local.features.task.TaskWithSubject
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeEntity
import com.kxsv.schooldiary.ui.main.app_bars.topbar.MainTopAppBar
import com.kxsv.schooldiary.ui.main.navigation.nav_actions.MainScreenNavActions
import com.kxsv.schooldiary.ui.screens.destinations.DayScheduleScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.EduPerformanceScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.GradesScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.TasksScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.TypedDestination
import com.kxsv.schooldiary.ui.screens.patterns.add_edit_pattern.fromLocalTime
import com.kxsv.schooldiary.util.Utils.getIndexByTime
import com.kxsv.schooldiary.util.Utils.getNextLessonsAfterIndex
import com.kxsv.schooldiary.util.ui.LoadingContent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@RootNavGraph(start = true)
@Destination
@Composable
fun MainScreen(
	destinationsNavigator: DestinationsNavigator,
	drawerState: DrawerState,
	coroutineScope: CoroutineScope,
	viewModel: MainScreenViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState,
) {
	val navigator = MainScreenNavActions(destinationsNavigator = destinationsNavigator)
	Scaffold(
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		topBar = {
			MainTopAppBar(openDrawer = { coroutineScope.launch { drawerState.open() } })
		},
		modifier = Modifier.fillMaxSize(),
	) { paddingValues ->
		val uiState = viewModel.uiState.collectAsState().value
		
		val onScheduleShowMore = remember {
			{ navigator.onScheduleShowMore() }
		}
		val onTasksShowMore = remember<(LocalDate) -> Unit> {
			{ navigator.onTasksShowMore(date = it) }
		}
		val onNavigate = remember<(String) -> Unit> {
			{ navigator.chipNavigate(route = it) }
		}
		MainScreenContent(
			modifier = Modifier.padding(paddingValues),
			isLoading = uiState.isLoading,
			itemList = uiState.itemList,
			onScheduleShowMore = onScheduleShowMore,
			onTasksShowMore = onTasksShowMore,
			onNavigate = onNavigate
		)
	}
}

data class MainScreenItem(
	val date: LocalDate,
	val classes: Map<Int, SubjectEntity>,
	val tasks: List<TaskWithSubject>,
	val pattern: List<PatternStrokeEntity>,
)

@Composable
fun MainScreenContent(
	modifier: Modifier,
	isLoading: Boolean,
	itemList: List<MainScreenItem>,
	onScheduleShowMore: () -> Unit,
	onTasksShowMore: (LocalDate) -> Unit,
	onNavigate: (String) -> Unit,
) {
	LoadingContent(
		modifier = modifier,
		loading = isLoading,
		empty = itemList.isEmpty(),
		isContentScrollable = true
	) {
		Column(
			modifier = Modifier.verticalScroll(rememberScrollState())
		) {
			ChipSection(onNavigate = onNavigate)
			
			Spacer(modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.vertical_margin)))
			
			itemList.forEach {
				if (it.date == LocalDate.now()) {
					key(it.date, it.classes, it.pattern) {
						CurrentDay(
							classes = it.classes,
							currentPattern = it.pattern,
							onScheduleShowMore = onScheduleShowMore,
						)
						Spacer(modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.vertical_margin)))
					}
				} else {
					key(it.date, it.classes, it.pattern) {
						ScheduleDay(
							date = it.date,
							classes = it.classes,
							tasks = it.tasks,
							currentPattern = it.pattern,
							onTasksShowMore = { onTasksShowMore(it.date) }
						)
						Spacer(modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.vertical_margin)))
					}
				}
			}
		}
	}
}


@Composable
private fun ChipSection(
	onNavigate: (String) -> Unit,
) {
	data class NavButton(
		@StringRes val res: Int,
		val destination: TypedDestination<out Any>,
	)
	
	val buttons = listOf(
		NavButton(res = R.string.timetable, destination = DayScheduleScreenDestination),
		NavButton(res = R.string.tasks_title, destination = TasksScreenDestination),
		NavButton(res = R.string.grades_title, destination = GradesScreenDestination),
		NavButton(res = R.string.report_card_title, destination = EduPerformanceScreenDestination),
	)
	
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.horizontalScroll(rememberScrollState())
			.padding(vertical = dimensionResource(R.dimen.list_item_padding)),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween
	) {
		buttons.forEach {
			key(it.res) {
				OutlinedButton(
					onClick = { onNavigate(it.destination.route) },
					modifier = Modifier.padding(
						horizontal = dimensionResource(R.dimen.list_item_padding)
					),
				) {
					Text(
						text = stringResource(it.res),
						style = MaterialTheme.typography.labelMedium
					)
				}
			}
		}
	}
}

@Composable
private fun CurrentDay(
	classes: Map<Int, SubjectEntity>,
	currentPattern: List<PatternStrokeEntity>,
	onScheduleShowMore: () -> Unit,
) {
	Column {
		DayHeader(date = LocalDate.now())
		val currentTime = LocalTime.now()
		
		val currentLessonIndex = currentPattern.getIndexByTime(currentTime)
		if (currentLessonIndex != null) {
			val currentLesson = classes[currentLessonIndex]
			if (currentLesson != null) {
				LessonInfo(
					isDetailed = true,
					label = R.string.right_now_label,
					labelIcon = Icons.Outlined.CircleNotifications,
					subjectEntity = currentLesson,
					startTime = currentPattern[currentLessonIndex].startTime,
					endTime = currentPattern[currentLessonIndex].endTime,
				)
				Divider(modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.vertical_margin)))
			}
		}
		
		val nextFourLessonsIndices =
			classes.getNextLessonsAfterIndex(n = 4, index = currentLessonIndex)
		
		nextFourLessonsIndices?.forEach { lessonIndex ->
			LessonInfo(
				isDetailed = (lessonIndex == nextFourLessonsIndices.first()),
				label = R.string.next_lessons_label,
				labelIcon = Icons.Default.ViewDay,
				subjectEntity = classes[lessonIndex]!!,
				startTime = currentPattern[lessonIndex].startTime,
				endTime = currentPattern[lessonIndex].endTime,
			)
			Spacer(modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.list_item_padding)))
		}
		
		Divider(modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.vertical_margin)))
		Button(onClick = onScheduleShowMore) {
			Text(
				text = stringResource(id = R.string.btn_show_more),
				style = MaterialTheme.typography.labelMedium
			)
		}
	}
}

@Composable
fun ScheduleDay(
	date: LocalDate,
	classes: Map<Int, SubjectEntity>,
	tasks: List<TaskWithSubject>,
	currentPattern: List<PatternStrokeEntity>,
	onTasksShowMore: () -> Unit,
) {
	Column {
		DayHeader(date = date)
		
		TasksGrid(tasks = tasks)
		
		Divider(modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.vertical_margin)))
		Button(onClick = onTasksShowMore) {
			Text(
				text = stringResource(id = R.string.btn_show_more),
				style = MaterialTheme.typography.labelMedium
			)
		}
		
		classes.forEach {
			key(it.value, it.key) {
				LessonShort(
					subjectEntity = it.value,
					startTime = currentPattern.getOrNull(it.key)?.startTime,
					endTime = currentPattern.getOrNull(it.key)?.endTime,
				)
			}
		}
	}
}

@Composable
fun TasksGrid(tasks: List<TaskWithSubject>) {
	LazyHorizontalGrid(
		modifier = Modifier.heightIn(max = 200.dp),
		rows = GridCells.Adaptive(
			minSize = 60.dp
		)
	) {
		tasks.forEachIndexed { index, taskEntity ->
			TasksGridItem(index, taskEntity)
		}
	}
}

fun LazyGridScope.TasksGridItem(
	index: Int,
	taskEntity: TaskWithSubject,
) {
	item {
		key(taskEntity) {
			Row {
				Text(text = index.toString())
				Column {
					Text(text = taskEntity.taskEntity.title)
					Text(text = taskEntity.subject.getName())
				}
				Checkbox(
					checked = taskEntity.taskEntity.isDone,
					onCheckedChange = null
				)
			}
		}
	}
}

@Composable
private fun LessonInfo(
	isDetailed: Boolean,
	label: Int,
	labelIcon: ImageVector,
	subjectEntity: SubjectEntity,
	startTime: LocalTime,
	endTime: LocalTime,
) {
	if (isDetailed) {
		LessonDetailed(
			label = label,
			labelIcon = labelIcon,
			subjectEntity = subjectEntity,
			startTime = startTime,
			endTime = endTime,
		)
		Divider()
	} else {
		LessonBrief(
			subjectEntity = subjectEntity,
			startTime = startTime,
			endTime = endTime,
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
			.padding(
				horizontal = dimensionResource(R.dimen.horizontal_margin),
				vertical = dimensionResource(R.dimen.vertical_margin)
			),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween,
	) {
		val dayOfWeekText = when (date) {
			LocalDate.now() -> {
				stringResource(id = R.string.today_filter)
			}
			
			LocalDate.now().plusDays(1) -> {
				stringResource(id = R.string.tomorrow_filter)
			}
			
			else -> {
				date.dayOfWeek.getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH)
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
private fun LessonDetailed(
	label: Int,
	labelIcon: ImageVector,
	subjectEntity: SubjectEntity,
	startTime: LocalTime,
	endTime: LocalTime,
) {
	Column(
		modifier = Modifier.padding(
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
				Text(
					text = subjectEntity.getName(),
					style = MaterialTheme.typography.headlineSmall
				)
			}
			Row(
				verticalAlignment = Alignment.CenterVertically,
			) {
				Icon(
					imageVector = Icons.Outlined.AccessTime,
					contentDescription = "Time until lesson start/end"
				)
				val currentTime = LocalTime.now()
				val untilLesson = if (currentTime.isAfter(startTime))
					currentTime.until(endTime, ChronoUnit.MINUTES)
				else
					currentTime.until(startTime, ChronoUnit.MINUTES)
				Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.list_item_padding) / 2))
				Text(
					text = untilLesson.toString() + "m",
					style = MaterialTheme.typography.bodyMedium
				)
			}
		}
		Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding) / 2))
		Row(
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Row {
				val text = startTime.format(
					DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
				) + " - " +
						endTime.format(
							DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
						)
				
				Text(
					text = text,
					style = MaterialTheme.typography.bodyMedium
				)
			}
			val cabinet = subjectEntity.getCabinetString()
			if (cabinet.isNotBlank()) {
				Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.list_item_padding)))
				Row(
					verticalAlignment = Alignment.CenterVertically
				) {
					Icon(
						imageVector = Icons.Outlined.Place,
						contentDescription = "Time of lesson left"
					)
					Text(
						text = subjectEntity.getCabinetString(),
						style = MaterialTheme.typography.bodyMedium
					)
				}
			}
		}
		val timeOfLessonPassed = startTime.until(LocalTime.now(), ChronoUnit.MINUTES).toFloat()
		if (timeOfLessonPassed > 0) {
			Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding) / 2))
			val lessonDuration = startTime.until(endTime, ChronoUnit.MINUTES)
			LinearProgressIndicator(
				progress = (timeOfLessonPassed / lessonDuration),
				modifier = Modifier
					.fillMaxWidth()
					.height(dimensionResource(R.dimen.list_item_padding) / 1.4f)
			)
		}
	}
}

@Composable
private fun LessonBrief(
	subjectEntity: SubjectEntity,
	startTime: LocalTime,
	endTime: LocalTime,
) {
	Column(
		modifier = Modifier.padding(
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
				text = subjectEntity.getName(),
				style = MaterialTheme.typography.titleLarge
			)
		}
		Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding) / 2))
		Row(
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Row {
				val text = fromLocalTime(startTime) + " - " + fromLocalTime(endTime)
				Text(
					text = text,
					style = MaterialTheme.typography.bodyMedium
				)
			}
			val cabinet = subjectEntity.getCabinetString()
			if (cabinet.isNotBlank()) {
				Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.list_item_padding)))
				Row(
					verticalAlignment = Alignment.CenterVertically
				) {
					Icon(
						imageVector = Icons.Outlined.Place,
						contentDescription = "Time of lesson left"
					)
					Text(
						text = subjectEntity.getCabinetString(),
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
			horizontal = dimensionResource(R.dimen.horizontal_margin),
		),
		verticalAlignment = Alignment.CenterVertically,
	) {
		val text = if (startTime != null && endTime != null) {
			fromLocalTime(startTime) + " - " + fromLocalTime(
				endTime
			)
		} else {
			"No pattern stroke"
		}
		Text(
			text = text,
			style = MaterialTheme.typography.labelSmall
		)
		Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.list_item_padding) / 2))
		Icon(
			imageVector = Icons.Outlined.Circle,
			contentDescription = "Lesson name",
			modifier = Modifier.size(12.dp)
		)
		Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.list_item_padding)))
		Text(
			text = subjectEntity.getName(),
			style = MaterialTheme.typography.bodySmall
		)
	}
}

@Composable
@Preview
fun LessonDetailedPreview() {
	Surface {
		LessonDetailed(
			label = R.string.right_now_label,
			labelIcon = Icons.Outlined.CircleNotifications,
			subjectEntity = SubjectEntity(
				fullName = "Английский язык",
				cabinet = "316"
			),
			startTime = LocalTime.of(12, 10),
			endTime = LocalTime.of(12, 55),
		)
	}
}

@Composable
@Preview
fun LessonBriefPreview() {
	Surface {
		LessonBrief(
			subjectEntity = SubjectEntity(
				fullName = "Английский язык",
				cabinet = "316"
			),
			startTime = LocalTime.of(12, 10),
			endTime = LocalTime.of(12, 55),
		)
	}
}


private val previewCurrentPattern = listOf(
	PatternStrokeEntity(
		startTime = LocalTime.of(8, 30),
		endTime = LocalTime.of(9, 15),
		index = 0,
	),
	PatternStrokeEntity(
		startTime = LocalTime.of(9, 30),
		endTime = LocalTime.of(10, 15),
		index = 1
	),
	PatternStrokeEntity(
		startTime = LocalTime.of(10, 30),
		endTime = LocalTime.of(11, 15),
		index = 2
	),
	PatternStrokeEntity(
		startTime = LocalTime.of(11, 25),
		endTime = LocalTime.of(12, 10),
		index = 3
	),
	PatternStrokeEntity(
		startTime = LocalTime.of(12, 30),
		endTime = LocalTime.of(13, 15),
		index = 4
	),
	PatternStrokeEntity(
		startTime = LocalTime.of(13, 30),
		endTime = LocalTime.of(14, 20),
		index = 5
	),
	PatternStrokeEntity(
		startTime = LocalTime.of(14, 30),
		endTime = LocalTime.of(15, 15),
		index = 6
	),
)
private val previewClasses = mapOf(
	Pair(1, SubjectEntity("Русский язык", "210")),
	Pair(2, SubjectEntity("Иностранный язык (Английский)", "316")),
	Pair(3, SubjectEntity("Иностранный язык (Немецкий)", "316")),
	Pair(4, SubjectEntity("Алгебра", "310")),
	Pair(5, SubjectEntity("Литература", "210")),
)

@Composable
@Preview
fun CurrentDayPreview() {
	Surface {
		CurrentDay(
			classes = previewClasses,
			currentPattern = previewCurrentPattern,
			onScheduleShowMore = {}
		)
	}
}