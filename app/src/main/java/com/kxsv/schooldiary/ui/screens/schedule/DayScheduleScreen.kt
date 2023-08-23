package com.kxsv.schooldiary.ui.screens.schedule

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.darkColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.lesson.LessonEntity
import com.kxsv.schooldiary.data.local.features.lesson.LessonWithSubject
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeEntity
import com.kxsv.schooldiary.data.util.AppVersionState
import com.kxsv.schooldiary.ui.main.app_bars.topbar.ScheduleTopAppBar
import com.kxsv.schooldiary.ui.main.navigation.ScheduleNavGraph
import com.kxsv.schooldiary.ui.main.navigation.nav_actions.AppUpdateNavActions
import com.kxsv.schooldiary.ui.main.navigation.nav_actions.DayScheduleScreenNavActions
import com.kxsv.schooldiary.ui.screens.destinations.AddEditLessonScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.DateRangeScheduleCopyScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.DayScheduleCopyScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.PatternsScreenDestination
import com.kxsv.schooldiary.ui.screens.grade_list.MY_URI
import com.kxsv.schooldiary.ui.screens.patterns.PatternSelectionResult
import com.kxsv.schooldiary.ui.util.LoadingContent
import com.kxsv.schooldiary.ui.util.displayText
import com.kxsv.schooldiary.util.Utils
import com.kxsv.schooldiary.util.Utils.localDateToTimestamp
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.FULL_ROUTE_PLACEHOLDER
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.ResultRecipient
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

data class DayScheduleScreenNavArgs(
	val datestamp: Long?,
	val showComparison: Boolean?,
)

private const val TAG = "DayScheduleScreen"

@ScheduleNavGraph(start = true)
@Destination(
	navArgsDelegate = DayScheduleScreenNavArgs::class,
	deepLinks = [
		DeepLink(
			action = Intent.ACTION_VIEW,
			uriPattern = "$MY_URI/$FULL_ROUTE_PLACEHOLDER"
		)
	]
)
@Composable
fun DayScheduleScreen(
	lessonAddEditResult: ResultRecipient<AddEditLessonScreenDestination, Int>,
	patternPickResult: ResultRecipient<PatternsScreenDestination, PatternSelectionResult>,
	dayScheduleCopyResult: ResultRecipient<DayScheduleCopyScreenDestination, DayScheduleCopyResult>,
	dateRangeScheduleCopyResult: ResultRecipient<DateRangeScheduleCopyScreenDestination, DateRangeScheduleCopyResult>,
	destinationsNavigator: DestinationsNavigator,
	drawerState: DrawerState,
	coroutineScope: CoroutineScope,
	snackbarHostState: SnackbarHostState,
	viewModel: ScheduleViewModel,
) {
	val navigator = DayScheduleScreenNavActions(destinationsNavigator = destinationsNavigator)
	val uiState = viewModel.uiState.collectAsState().value
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
	lessonAddEditResult.onNavResult { result ->
		when (result) {
			is NavResult.Canceled -> {}
			is NavResult.Value -> {
				viewModel.showEditResultMessage(result.value)
				viewModel.loadLocalScheduleOnDate(uiState.selectedDate)
			}
		}
	}
	patternPickResult.onNavResult { result ->
		when (result) {
			is NavResult.Canceled -> {}
			is NavResult.Value -> {
				viewModel.selectCustomPattern(result.value)
			}
		}
	}
	dayScheduleCopyResult.onNavResult { result ->
		when (result) {
			is NavResult.Canceled -> {}
			is NavResult.Value -> {
				viewModel.showDayScheduleCopyResult(result.value)
			}
		}
	}
	dateRangeScheduleCopyResult.onNavResult { result ->
		when (result) {
			is NavResult.Canceled -> {}
			is NavResult.Value -> {
				viewModel.showDateRangeScheduleCopyResult(result.value)
			}
		}
	}
	Scaffold(
		topBar = {
			ScheduleTopAppBar(
				onChangePattern = { navigator.onChangePattern() },
				onCopyDaySchedule = { navigator.onCopyDaySchedule() },
				onCopyDateRangeSchedule = { navigator.onCopyDateRangeSchedule() },
				onFetchSchedule = {
					coroutineScope.launch {
						if (viewModel.isScheduleRemote()) {
							viewModel.localiseCachedNetClasses()
						} else {
							viewModel.fetchSchedule()
						}
					}
				},
				openDrawer = { coroutineScope.launch { drawerState.open() } }
			)
		},
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		modifier = Modifier.fillMaxSize(),
		floatingActionButton = {
			FloatingActionButton(
				onClick = {
					coroutineScope.launch {
						if (viewModel.isScheduleRemote()) viewModel.localiseCachedNetClasses()
						navigator.onAddEditClass(
							datestamp = localDateToTimestamp(uiState.selectedDate)!!,
							lessonId = null
						)
					}
				}
			) {
				Icon(Icons.Default.Add, stringResource(R.string.add_schedule_item))
			}
		},
		containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 1f),
		contentColor = MaterialTheme.colorScheme.onBackground
	) { paddingValues ->
		val dialogState: MaterialDialogState = rememberMaterialDialogState()
		
		val onRefresh = remember {
			{ viewModel.onDayChangeUpdate(null) }
		}
		val changeDate = remember<(LocalDate) -> Unit> {
			{ viewModel.onDayChangeUpdate(it) }
		}
		val scheduleChoose = remember<(Int) -> Unit> {
			{ viewModel.scheduleChoose(it) }
		}
		val onClassClick = remember<(LessonWithSubject) -> Unit> {
			{ viewModel.selectClass(it); dialogState.show() }
		}
		DayScheduleContent(
			modifier = Modifier.padding(paddingValues),
			loading = uiState.isLoading,
			classes = uiState.classes,
			fetchedClasses = uiState.fetchedClasses,
			selectedDate = uiState.selectedDate,
			currentPattern = uiState.currentTimings,
			onRefresh = onRefresh,
			changeDate = changeDate,
			scheduleChoose = scheduleChoose,
			onClassClick = onClassClick
		)
		
		val onDeleteClass = remember<(LessonWithSubject) -> Unit> {
			{ viewModel.deleteClass(it) }
		}
		val onEditClass = remember<(Long) -> Unit> {
			{ lessonId -> navigator.onAddEditClass(0, lessonId) }
		}
		val unselectClass = remember {
			{ viewModel.unselectClass() }
		}
		val getIdForClassFromNet = remember<suspend () -> Long?> {
			{ viewModel.getIdForClassFromNet() }
		}
		LessonDialog(
			dialogState = dialogState,
			classDetailed = uiState.classDetailed,
			selectedDate = uiState.selectedDate,
			currentPattern = uiState.currentTimings,
			onDeleteClass = onDeleteClass,
			onEditClass = onEditClass,
			unselectClass = unselectClass,
			getIdForClassFromNet = getIdForClassFromNet
		)
		
		uiState.userMessage?.let { userMessage ->
			val snackbarText = if (uiState.userMessageArgs != null) {
				stringResource(userMessage, *uiState.userMessageArgs)
			} else {
				stringResource(userMessage)
			}
			LaunchedEffect(snackbarHostState, viewModel, userMessage, snackbarText) {
				snackbarHostState.showSnackbar(snackbarText)
				viewModel.snackbarMessageShown()
			}
		}
	}
}

@Composable
private fun LessonDialog(
	dialogState: MaterialDialogState,
	classDetailed: LessonWithSubject?,
	selectedDate: LocalDate,
	currentPattern: List<PatternStrokeEntity>,
	onDeleteClass: (LessonWithSubject) -> Unit,
	onEditClass: (Long) -> Unit,
	unselectClass: () -> Unit,
	getIdForClassFromNet: suspend () -> Long?,
) {
	if (classDetailed != null) {
		MaterialDialog(
			dialogState = dialogState,
			onCloseRequest = { unselectClass(); it.hide() },
		) {
			Column(Modifier.padding(dimensionResource(R.dimen.horizontal_margin))) {
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
					if (classDetailed.lesson.index >= 0 && classDetailed.lesson.index <= currentPattern.lastIndex) {
						currentPattern[classDetailed.lesson.index].startTime.format(
							DateTimeFormatter.ofLocalizedTime(
								FormatStyle.SHORT
							)
						) + " - " +
								currentPattern[classDetailed.lesson.index].endTime.format(
									DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
								)
					} else stringResource(
						R.string.class_out_of_strokes_bounds_message,
						classDetailed.lesson.index + 1
					)
				Text(
					text,
					style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal)
				)
				Spacer(modifier = Modifier.padding(vertical = 4.dp))
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.Center
				) {
					val coroutineScope = rememberCoroutineScope()
					FilledTonalButton(
						onClick = {
							coroutineScope.launch {
								if (classDetailed.lesson.lessonId == 0L) {
									val lessonId = getIdForClassFromNet()
									if (lessonId != null) onEditClass(lessonId)
								} else {
									onEditClass(classDetailed.lesson.lessonId)
								}
								
								dialogState.hide()
							}
						},
						modifier = Modifier.weight(0.45f)
					) {
						Text(text = stringResource(R.string.btn_edit))
					}
					Spacer(modifier = Modifier.weight(0.1f))
					FilledTonalButton(
						onClick = { onDeleteClass(classDetailed) },
						modifier = Modifier.weight(0.45f)
					) {
						Text(text = stringResource(R.string.btn_delete))
					}
				}
				val cabinetText = remember(classDetailed) {
					classDetailed.lesson.cabinet ?: classDetailed.subject.cabinet
				}
				if (cabinetText != null) {
					Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding)))
					Row(verticalAlignment = Alignment.CenterVertically) {
						Icon(Icons.Default.LocationOn, stringResource(R.string.lesson_room))
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
private fun DayScheduleContent(
	modifier: Modifier,
	loading: Boolean,
	classes: Map<Int, LessonWithSubject>,
	fetchedClasses: Map<Int, LessonWithSubject>?,
	selectedDate: LocalDate,
	currentPattern: List<PatternStrokeEntity>,
	onRefresh: () -> Unit,
	changeDate: (LocalDate) -> Unit,
	scheduleChoose: (Int) -> Unit,
	onClassClick: (LessonWithSubject) -> Unit,
) {
	Column(modifier = modifier) {
		CalendarLine(changeDate = changeDate, selectedDate = selectedDate)
		if (fetchedClasses != null) {
			ScheduleComparisonTable(
				loading = loading,
				classes = classes,
				fetchedClasses = fetchedClasses,
				scheduleChoose = scheduleChoose,
				onRefresh = onRefresh,
			)
		} else {
			ScheduleForDay(
				loading = loading,
				classes = classes,
				selectedDate = selectedDate,
				currentPattern = currentPattern,
				onRefresh = onRefresh,
				onClassClick = onClassClick
			)
		}
	}
}

@Composable
fun ScheduleComparisonTable(
	loading: Boolean,
	classes: Map<Int, LessonWithSubject>,
	fetchedClasses: Map<Int, LessonWithSubject>,
	scheduleChoose: (Int) -> Unit,
	onRefresh: () -> Unit,
) {
	if (fetchedClasses.isNotEmpty()) {
		val maxLines = maxOf(classes.maxBy { it.key }.key, fetchedClasses.maxBy { it.key }.key) + 1
		
		LoadingContent(
			loading = loading,
			empty = false,
			emptyContent = {},
			isContentScrollable = true,
			onRefresh = onRefresh
		) {
			val cellWidth: (Int) -> Dp = { index ->
				when (index) {
					0 -> 25.dp
					else -> 168.dp
				}
			}
			val headerCellTitle: @Composable (Int) -> Unit = { index ->
				val value = when (index) {
					0 -> "#"
					1 -> "Local"
					2 -> "Network"
					else -> ""
				}
				
				Text(
					text = value,
					fontSize = 20.sp,
					textAlign = TextAlign.Center,
					modifier = Modifier.padding(8.dp),
					maxLines = 1,
				)
			}
			
			val cellText: @Composable (Int, Int) -> Unit = { index, columnIndex ->
				val value = when (columnIndex) {
					0 -> (index).toString()
					1 -> classes[index - 1]?.subject?.getName() ?: ""
					2 -> fetchedClasses[index - 1]?.subject?.getName() ?: ""
					else -> ""
				}
				
				Text(
					text = value,
					textAlign = TextAlign.Justify,
					modifier = Modifier
						.padding(8.dp)
						.height(40.dp),
					maxLines = 2,
				)
			}
			
			Table(
				columnCount = 3,
				lineCount = maxLines,
				cellWidth = cellWidth,
				scheduleChoose = scheduleChoose,
				modifier = Modifier.verticalScroll(rememberScrollState()),
				headerCellContent = headerCellTitle,
				cellContent = cellText
			)
		}
	}
}

/**
 * The horizontally scrollable table with header and content.
 * @param columnCount the count of columns in the table
 * @param cellWidth the width of column, can be configured based on index of the column.
 * @param modifier the modifier to apply to this layout node.
 * @param headerCellContent a block which describes the header cell content.
 * @param cellContent a block which describes the cell content.
 */
@Composable
private fun Table(
	columnCount: Int,
	lineCount: Int,
	cellWidth: (index: Int) -> Dp,
	modifier: Modifier = Modifier,
	scheduleChoose: (Int) -> Unit,
	headerCellContent: @Composable (columnIndex: Int) -> Unit,
	cellContent: @Composable (lineCount: Int, columnIndex: Int) -> Unit,
) {
	Surface(
		modifier = modifier
	) {
		LazyRow(
			modifier = Modifier.padding(16.dp)
		) {
			items((0 until columnCount).toList()) { columnIndex ->
				Column(modifier = Modifier.clickable {
					if (columnIndex == 1 || columnIndex == 2) {
						scheduleChoose(columnIndex)
					}
				}) {
					(0..lineCount).forEach { index ->
						Surface(
							border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
							modifier = Modifier.width(cellWidth(columnIndex))
						) {
							if (index == 0) {
								headerCellContent(columnIndex)
							} else {
								cellContent(index, columnIndex)
							}
						}
					}
				}
			}
		}
	}
}

@Composable
private fun ScheduleForDay(
	loading: Boolean,
	classes: Map<Int, LessonWithSubject>,
	selectedDate: LocalDate,
	currentPattern: List<PatternStrokeEntity>,
	onRefresh: () -> Unit,
	onClassClick: (LessonWithSubject) -> Unit,
) {
	LoadingContent(
		loading = loading,
		empty = classes.isEmpty(),
		emptyContent = {
			Column(
				modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.horizontal_margin))
			) {
				Text(text = stringResource(R.string.no_classes_label))
			}
		},
		isContentScrollable = true,
		onRefresh = onRefresh
	) {
		LazyColumn(
			contentPadding = PaddingValues(horizontal = dimensionResource(R.dimen.horizontal_margin))
		) {
			item {
				DayOfWeekHeader(date = selectedDate, lessonsAmount = classes.size)
			}
			classes.forEach { (ordinalIndex, lesson) ->
				item(contentType = { lesson }) {
					ClassItem(
						index = ordinalIndex,
						lesson = lesson,
						onClassClick = onClassClick,
						currentPattern = currentPattern
					)
					Divider(
						modifier = Modifier
							.padding(horizontal = dimensionResource(R.dimen.list_item_padding))
							.fillMaxWidth()
					)
				}
			}
		}
	}
}


@Composable
private fun CalendarLine(
	changeDate: (LocalDate) -> Unit,
	selectedDate: LocalDate,
) {
	val currentDate = remember { Utils.currentDate }
	val startDate = remember { currentDate.minusDays(600) }
	val endDate = remember { currentDate.plusDays(600) }
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(Color.Transparent)
	) {
		val state = rememberWeekCalendarState(
			startDate = startDate,
			endDate = endDate,
			firstVisibleWeekDate = selectedDate,
		)
		CompositionLocalProvider(LocalContentColor provides darkColors().onSurface) {
			WeekCalendar(
				modifier = Modifier.padding(vertical = 4.dp),
				state = state,
				calendarScrollPaged = false,
				dayContent = { day ->
					if (day.date.dayOfWeek != DayOfWeek.SUNDAY) {
						Day(date = day.date, selected = selectedDate == day.date) {
							changeDate(it)
						}
					}
				},
			)
		}
	}
}

private val dateFormatter = DateTimeFormatter.ofPattern("dd")

@Composable
private fun Day(
	date: LocalDate,
	selected: Boolean = false,
	onClick: (LocalDate) -> Unit = {},
) {
	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp
	Box(
		modifier = Modifier
			// If paged scrolling is disabled (calendarScrollPaged = false),
			// you must set the day width on the WeekCalendar!
			.width(screenWidth / 7)
			.padding(2.dp)
			.clip(RoundedCornerShape(20.dp))
			.background(color = MaterialTheme.colorScheme.surfaceVariant)
			.border(
				shape = RoundedCornerShape(20.dp),
				width = if (selected) 2.dp else 0.dp,
				color = if (selected) MaterialTheme.colorScheme.outline else Color.Transparent,
			)
			.wrapContentHeight()
			.clickable { onClick(date) },
		contentAlignment = Alignment.Center,
	) {
		Column(
			modifier = Modifier.padding(vertical = 8.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(5.dp),
		) {
			Text(
				text = date.month.displayText(),
				style = MaterialTheme.typography.labelMedium,
			)
			Text(
				text = dateFormatter.format(date),
				style = MaterialTheme.typography.titleMedium,
				
				)
			Text(
				text = date.dayOfWeek.displayText(),
				style = MaterialTheme.typography.labelMedium,
			)
		}
	}
}

@Composable
private fun DayOfWeekHeader(
	lessonsAmount: Int,
	date: LocalDate,
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
	) {
		Text(
			date.dayOfWeek.name,
			style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold)
		)
		val text: String = if (lessonsAmount != 0)
			stringResource(R.string.lessons_quantity_label, lessonsAmount) else
			stringResource(R.string.lessons_zero_quantity_label)
		Text(
			text = text,
			style = MaterialTheme.typography.labelLarge
		)
	}
}

@Composable
private fun ClassItem(
	index: Int,
	lesson: LessonWithSubject,
	onClassClick: (LessonWithSubject) -> Unit,
	currentPattern: List<PatternStrokeEntity>,
	modifier: Modifier = Modifier,
) {
	Column(
		modifier = modifier
			.fillMaxWidth()
			.clickable { onClassClick(lesson) }
			.padding(vertical = dimensionResource(R.dimen.list_item_padding))
	) {
		val timeText: String =
			if (index <= currentPattern.lastIndex) {
				currentPattern[index].startTime.format(
					DateTimeFormatter.ofLocalizedTime(
						FormatStyle.SHORT
					)
				) + " - " + currentPattern[index].endTime.format(
					DateTimeFormatter.ofLocalizedTime(
						FormatStyle.SHORT
					)
				)
			} else stringResource(R.string.class_out_of_strokes_bounds_message, (index + 1))
		Text(
			text = timeText,
			style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
		)
		Spacer(modifier = Modifier.padding(vertical = 2.dp))
		
		Text(
			lesson.subject.getName(),
			style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium)
		)
		Spacer(modifier = Modifier.padding(vertical = 4.dp))
		Row {
			Row(verticalAlignment = Alignment.Top) {
				Icon(
					imageVector = Icons.Default.LocationOn,
					contentDescription = stringResource(R.string.lesson_room),
					modifier = Modifier.size(18.dp)
				)
				Spacer(modifier = Modifier.padding(horizontal = 4.dp))
				Text(
					lesson.subject.getCabinetString(),
					style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
				)
			}
			Spacer(modifier = Modifier.padding(horizontal = 4.dp))
			// TODO: add tags
		}
		Spacer(modifier = Modifier.padding(vertical = 4.dp))
		
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
)

@Preview(
	device = "id:pixel_4", showSystemUi = true, showBackground = true,
	uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun DayScheduleContentPreview() {
	Surface {
		DayScheduleContent(
			modifier = Modifier,
			loading = false,
//			fetchedClasses = null,
			classes = mapOf(
				Pair(
					1, LessonWithSubject(
						LessonEntity(1, 0, "0"),
						SubjectEntity("Русский язык", "210")
					)
				),
				Pair(
					3, LessonWithSubject(
						LessonEntity(3, 0, "0"),
						SubjectEntity("Английский языкАнглийский язык", "316")
					)
				),
			),
			fetchedClasses = mapOf(
				Pair(
					2, LessonWithSubject(
						LessonEntity(0, 0, "0"),
						SubjectEntity("Английский языкАнглийский язык", "316")
					)
				),
				Pair(
					3, LessonWithSubject(
						LessonEntity(1, 0, "0"),
						SubjectEntity("Английский язык", "316")
					)
				),
				Pair(
					4, LessonWithSubject(
						LessonEntity(2, 0, "0"),
						SubjectEntity("Немецкий язык", "316")
					)
				),
			),
			selectedDate = Utils.currentDate,
			currentPattern = previewCurrentPattern,
			onRefresh = {},
			changeDate = {},
			scheduleChoose = { }
		) {}
	}
}

@Preview(
	device = "id:pixel_4", showSystemUi = true, showBackground = true,
	uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
private fun LessonDialogPreview() {
	Surface {
		LessonDialog(
			dialogState = rememberMaterialDialogState(true),
			classDetailed = LessonWithSubject(
				subject = SubjectEntity(fullName = "Английский язык", cabinet = "316"),
				lesson = LessonEntity(index = 0)
			),
			selectedDate = Utils.currentDate,
			currentPattern = previewCurrentPattern,
			onDeleteClass = {},
			onEditClass = {},
			unselectClass = {}
		) { 0 }
	}
}