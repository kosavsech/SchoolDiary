package com.kxsv.schooldiary.ui.screens.schedule

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
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
import com.kxsv.schooldiary.data.local.features.schedule.Schedule
import com.kxsv.schooldiary.data.local.features.schedule.ScheduleWithSubject
import com.kxsv.schooldiary.data.local.features.subject.Subject
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStroke
import com.kxsv.schooldiary.util.ui.LoadingContent
import com.kxsv.schooldiary.util.ui.ScheduleTopAppBar
import com.kxsv.schooldiary.util.ui.displayText
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private fun localDateToTimestamp(date: LocalDate): Long =
	date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()

@Composable
fun DayScheduleScreen(
	@StringRes userMessage: Int,
	onUserMessageDisplayed: () -> Unit,
	isCustomPatternWasSet: Boolean?,
	onAddClass: (Long) -> Unit,
	onEditClass: (Long) -> Unit,
	onChangePattern: (Long) -> Unit,
	onCopyDaySchedule: () -> Unit,
	onCopyDateRangeSchedule: () -> Unit,
	openDrawer: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: DayScheduleViewModel,
	snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
	val uiState = viewModel.uiState.collectAsState().value
	val coroutineScope = rememberCoroutineScope()
	
	Scaffold(
		topBar = {
			ScheduleTopAppBar(
				onChangePattern = {
					coroutineScope.launch {
						onChangePattern(viewModel.getCurrentStudyDayForced().studyDayId)
					}
				},
				onCopyDaySchedule = onCopyDaySchedule,
				onCopyDateRangeSchedule = onCopyDateRangeSchedule,
				onFetchSchedule = { viewModel.fetchSchedule() },
				openDrawer = openDrawer
			)
		},
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		modifier = modifier.fillMaxSize(),
		floatingActionButton = {
			FloatingActionButton(
				onClick = {
					coroutineScope.launch {
						if (viewModel.isScheduleRemote()) viewModel.localiseCurrentNetSchedule()
						onAddClass(localDateToTimestamp(uiState.selectedDate))
					}
				}
			) {
				Icon(Icons.Default.Add, stringResource(R.string.add_schedule_item))
			}
		},
	) { paddingValues ->
		
		val dialogState: MaterialDialogState = rememberMaterialDialogState()
		DayScheduleContent(
			loading = uiState.isLoading,
			classes = uiState.classes,
			fetchedClasses = uiState.fetchedClasses,
			selectedDate = uiState.selectedDate,
			currentPattern = uiState.currentTimings,
			changeDate = viewModel::onDayChangeUpdate,
			onClassClick = { (viewModel::selectClass)(it); dialogState.show() },
			noClassesLabel = R.string.no_classes_label,
			onRefresh = { (viewModel::onDayChangeUpdate)(uiState.selectedDate) },
			modifier = Modifier.padding(paddingValues),
		)
		
		LessonDialog(
			classDetailed = uiState.classDetailed,
			selectedDate = uiState.selectedDate,
			currentPattern = uiState.currentTimings,
			onDeleteClass = viewModel::deleteClass,
			onEditClass = onEditClass,
			unselectClass = viewModel::unselectClass,
			getIdForClassFromNet = viewModel::getIdForClassFromNet,
			dialogState = dialogState
		)
		
		uiState.userMessage?.let { userMessage ->
			val snackbarText = stringResource(userMessage)
			LaunchedEffect(snackbarHostState, viewModel, userMessage, snackbarText) {
				snackbarHostState.showSnackbar(snackbarText)
				viewModel.snackbarMessageShown()
			}
		}
		
		LaunchedEffect(isCustomPatternWasSet) {
			if (isCustomPatternWasSet == true) {
				viewModel.onDayChangeUpdate(uiState.selectedDate)
			}
		}
		
		// Check if there's a userMessage to show to the user
		val currentOnUserMessageDisplayed by rememberUpdatedState(onUserMessageDisplayed)
		LaunchedEffect(userMessage) {
			if (userMessage != 0) {
				viewModel.showEditResultMessage(userMessage)
				currentOnUserMessageDisplayed()
			}
		}
	}
}

@Composable
private fun LessonDialog(
	classDetailed: ScheduleWithSubject?,
	selectedDate: LocalDate,
	currentPattern: List<PatternStroke>,
	onDeleteClass: (ScheduleWithSubject) -> Unit,
	onEditClass: (Long) -> Unit,
	unselectClass: () -> Unit,
	getIdForClassFromNet: suspend () -> Long,
	dialogState: MaterialDialogState,
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
					if (classDetailed.schedule.index >= 0 && classDetailed.schedule.index <= currentPattern.lastIndex) {
						currentPattern[classDetailed.schedule.index].startTime.format(
							DateTimeFormatter.ofLocalizedTime(
								FormatStyle.SHORT
							)
						) + " - " +
								currentPattern[classDetailed.schedule.index].endTime.format(
									DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
								)
					} else stringResource(
						R.string.class_out_of_strokes_bounds_message,
						classDetailed.schedule.index + 1
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
								if (classDetailed.schedule.scheduleId == 0L) {
									onEditClass(getIdForClassFromNet())
								} else {
									onEditClass(classDetailed.schedule.scheduleId)
								}
								
								dialogState.hide()
							}
						},
						modifier = Modifier.fillMaxWidth(0.45f)
					) {
						Text(text = stringResource(R.string.btn_edit))
					}
					Spacer(modifier = Modifier.fillMaxWidth(0.18181819f))
					FilledTonalButton(
						onClick = { onDeleteClass(classDetailed) },
						modifier = Modifier.fillMaxWidth()
					) {
						Text(text = stringResource(R.string.btn_delete))
					}
				}
				Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding)))
				Row(verticalAlignment = Alignment.CenterVertically) {
					Icon(Icons.Default.LocationOn, stringResource(R.string.lesson_room))
					Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.list_item_padding)))
					Column {
						Text(
							classDetailed.subject.getCabinetString(),
							style = MaterialTheme.typography.labelLarge
						)
						Text(
							stringResource(R.string.cabinet_hint),
							style = MaterialTheme.typography.labelMedium
						)
					}
				}
			}
		}
	}
}

@Composable
private fun DayScheduleContent(
	loading: Boolean,
	classes: Map<Int, ScheduleWithSubject>,
	fetchedClasses: Map<Int, ScheduleWithSubject>?,
	selectedDate: LocalDate,
	currentPattern: List<PatternStroke>,
	changeDate: (LocalDate) -> Unit,
	@StringRes noClassesLabel: Int,
	onRefresh: () -> Unit,
	onClassClick: (ScheduleWithSubject) -> Unit,
	modifier: Modifier,
) {
	Column(modifier = modifier) {
		CalendarLine(changeDate = changeDate, selectedDate = selectedDate)
		if (fetchedClasses != null) {
			ScheduleComparisonTable(
				loading = loading,
				classes = classes,
				fetchedClasses = fetchedClasses,
				onRefresh = onRefresh,
			)
		} else {
			ScheduleForDay(
				loading = loading,
				classes = classes,
				selectedDate = selectedDate,
				currentPattern = currentPattern,
				noClassesLabel = noClassesLabel,
				onRefresh = onRefresh,
				onClassClick = onClassClick
			)
		}
	}
}

@Composable
fun ScheduleComparisonTable(
	loading: Boolean,
	classes: Map<Int, ScheduleWithSubject>,
	fetchedClasses: Map<Int, ScheduleWithSubject>,
	onRefresh: () -> Unit,
) {
	val maxLines = maxOf(classes.maxBy { it.key }.key, fetchedClasses.maxBy { it.key }.key)
	
	LoadingContent(
		loading = loading,
		isContentScrollable = true,
		empty = false,
		emptyContent = {},
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
				fontWeight = FontWeight.Black,
			)
		}
		
		val cellText: @Composable (Int, Int) -> Unit = { index, columnIndex ->
			val value = when (columnIndex) {
				0 -> (index).toString()
				1 -> classes[index]?.subject?.getName() ?: ""
				2 -> fetchedClasses[index]?.subject?.getName() ?: ""
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
			modifier = Modifier.verticalScroll(rememberScrollState()),
			headerCellContent = headerCellTitle,
			cellContent = cellText
		)
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
				Column {
					(0..lineCount).forEach { index ->
						Surface(
							border = BorderStroke(1.dp, Color.Gray),
							contentColor = Color.Black,
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
	classes: Map<Int, ScheduleWithSubject>,
	selectedDate: LocalDate,
	currentPattern: List<PatternStroke>,
	@StringRes noClassesLabel: Int,
	onRefresh: () -> Unit,
	onClassClick: (ScheduleWithSubject) -> Unit,
) {
	LoadingContent(
		loading = loading,
		isContentScrollable = true,
		empty = classes.isEmpty(),
		emptyContent = {
			Column(
				modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.horizontal_margin))
			) {
				Text(text = stringResource(noClassesLabel))
			}
		},
		onRefresh = onRefresh
	) {
		LazyColumn(
			Modifier.padding(horizontal = dimensionResource(R.dimen.horizontal_margin))
		) {
			val isFirst = mutableStateOf(true)
			item {
				DayOfWeekHeader(date = selectedDate, lessonsAmount = classes.size)
			}
			classes.forEach { (ordinalIndex, lesson) ->
				item(contentType = { lesson }) {
					if (!isFirst.value) {
						Divider(
							modifier = Modifier
								.padding(horizontal = dimensionResource(R.dimen.list_item_padding))
								.fillMaxWidth(),
						)
					} else isFirst.value = false
					ClassItem(
						index = (ordinalIndex - 1),
						lesson = lesson,
						onClassClick = onClassClick,
						currentPattern = currentPattern
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
	val currentDate = remember { LocalDate.now() }
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
		// Draw light content on dark background.
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
			.background(color = Color.DarkGray)
			.border(
				shape = RoundedCornerShape(20.dp),
				width = if (selected) 2.dp else 0.dp,
				color = if (selected) Color.Cyan else Color.Transparent,
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
	lesson: ScheduleWithSubject,
	onClassClick: (ScheduleWithSubject) -> Unit,
	currentPattern: List<PatternStroke>,
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

@Preview(device = "id:pixel_4", showSystemUi = true, showBackground = true)
@Composable
private fun DayScheduleContentPreview() {
	Surface {
		DayScheduleContent(
			loading = false,
			classes = mapOf(
				Pair(
					1, ScheduleWithSubject(
						Schedule(1, 0, 0),
						Subject("Русский язык", "210")
					)
				),
				Pair(
					3, ScheduleWithSubject(
						Schedule(3, 0, 0),
						Subject("Английский языкАнглийский язык", "316")
					)
				),
			),
//			fetchedClasses = null,
			fetchedClasses = mapOf(
				Pair(
					2, ScheduleWithSubject(
						Schedule(0, 0, 0),
						Subject("Английский языкАнглийский язык", "316")
					)
				),
				Pair(
					3, ScheduleWithSubject(
						Schedule(1, 0, 0),
						Subject("Английский язык", "316")
					)
				),
				Pair(
					4, ScheduleWithSubject(
						Schedule(2, 0, 0),
						Subject("Немецкий язык", "316")
					)
				),
			),
			selectedDate = LocalDate.now(),
			currentPattern = listOf(
				PatternStroke(startTime = LocalTime.of(8, 30), endTime = LocalTime.of(9, 15)),
				PatternStroke(startTime = LocalTime.of(9, 30), endTime = LocalTime.of(10, 15)),
			),
			changeDate = {},
			onClassClick = {},
			noClassesLabel = R.string.no_classes_label,
			onRefresh = {},
			modifier = Modifier
		)
	}
}

@Preview(device = "id:pixel_4", showSystemUi = true, showBackground = true)
@Composable
private fun LessonDialogPreview() {
	Surface {
		LessonDialog(
			classDetailed = ScheduleWithSubject(
				subject = Subject(fullName = "Английский язык", cabinet = "316"),
				schedule = Schedule(index = 0)
			),
			selectedDate = LocalDate.now(),
			currentPattern = listOf(
				PatternStroke(startTime = LocalTime.of(8, 30), endTime = LocalTime.of(9, 15)),
				PatternStroke(startTime = LocalTime.of(9, 30), endTime = LocalTime.of(10, 15)),
			),
			onDeleteClass = {},
			onEditClass = {},
			unselectClass = {},
			getIdForClassFromNet = { 0 },
			dialogState = rememberMaterialDialogState(true)
		)
	}
}