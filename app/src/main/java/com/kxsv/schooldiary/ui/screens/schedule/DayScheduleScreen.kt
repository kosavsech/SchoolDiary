package com.kxsv.schooldiary.ui.screens.schedule

import androidx.annotation.StringRes
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.features.schedule.Schedule
import com.kxsv.schooldiary.data.features.schedule.ScheduleWithSubject
import com.kxsv.schooldiary.data.features.subjects.Subject
import com.kxsv.schooldiary.data.features.time_pattern.pattern_stroke.PatternStroke
import com.kxsv.schooldiary.util.ui.LoadingContent
import com.kxsv.schooldiary.util.ui.ScheduleTopAppBar
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

private fun localDateToTimestamp(date: LocalDate): Long =
	date.atStartOfDay(ZoneId.of("UTC")).toEpochSecond()

@Composable
fun DayScheduleScreen(
	@StringRes userMessage: Int,
	onUserMessageDisplayed: () -> Unit,
	isCustomPatternWasSet: Boolean?,
	onAddSchedule: (Long) -> Unit,
	onEditClass: (Long) -> Unit,
	onChangePattern: (Long) -> Unit,
	openDrawer: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: DayScheduleViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
	val uiState = viewModel.uiState.collectAsState().value
	
	Scaffold(
		topBar = {
			// TODO add check and show message about considering creating of lessons first
			ScheduleTopAppBar(
				{ uiState.studyDay?.let { onChangePattern(it.studyDayId) } },
				openDrawer
			)
		},
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		modifier = modifier.fillMaxSize(),
		floatingActionButton = {
			FloatingActionButton(
				onClick = {
					onAddSchedule(localDateToTimestamp(uiState.date))
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
			date = uiState.date,
			currentPattern = uiState.currentPattern,
			changeDate = viewModel::updateDayWithClasses,
			onClassClick = { (viewModel::selectClass)(it); dialogState.show() },
			onRefresh = { (viewModel::updateDayWithClasses)(uiState.date) },
			modifier = Modifier.padding(paddingValues),
		)
		
		LessonDialog(
			classDetailed = uiState.classDetailed,
			date = uiState.date,
			currentPattern = uiState.currentPattern,
			onDeleteClass = viewModel::deleteClass,
			onEditClass = onEditClass,
			unselectClass = viewModel::unselectClass,
			dialogState = dialogState
		)
		
		uiState.userMessage?.let { userMessage ->
			val snackbarText = stringResource(userMessage)
			LaunchedEffect(snackbarHostState, viewModel, userMessage, snackbarText) {
				snackbarHostState.showSnackbar(snackbarText)
				viewModel.snackbarMessageShown()
			}
		}
		
		LaunchedEffect(uiState.isClassDeleted) {
			if (uiState.isClassDeleted) {
				viewModel.onDeleteClass()
			}
		}
		
		LaunchedEffect(isCustomPatternWasSet) {
			if (isCustomPatternWasSet == true) {
				viewModel.updateDayWithClasses(uiState.date)
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
	date: LocalDate,
	currentPattern: List<PatternStroke>,
	onDeleteClass: (Long) -> Unit,
	onEditClass: (Long) -> Unit,
	unselectClass: () -> Unit,
	dialogState: MaterialDialogState,
) {
	if (classDetailed != null) {
		MaterialDialog(
			dialogState = dialogState,
			onCloseRequest = { unselectClass(); it.hide() },
		) {
			Column(Modifier.padding(dimensionResource(R.dimen.horizontal_margin))) {
				Text(
					classDetailed.subject.name,
					style = MaterialTheme.typography.titleMedium
				)
				Spacer(modifier = Modifier.padding(vertical = 4.dp))
				Text(
					date.dayOfWeek.name,
					style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal)
				)
				Spacer(modifier = Modifier.padding(vertical = 2.dp))
				
				// TODO: add format for lesson.index in message
				val text: String = if (classDetailed.schedule.index <= currentPattern.lastIndex) {
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
					classDetailed.schedule.index
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
					FilledTonalButton(
						onClick = {
							onEditClass(
								classDetailed.schedule.scheduleId,
							)
							dialogState.hide()
						},
						modifier = Modifier.fillMaxWidth(0.45f)
					) {
						Text(text = stringResource(R.string.btn_edit))
					}
					Spacer(modifier = Modifier.fillMaxWidth(0.18181819f))
					FilledTonalButton(
						onClick = { onDeleteClass(classDetailed.schedule.scheduleId) },
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
							classDetailed.subject.cabinet,
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
	classes: List<ScheduleWithSubject>,
	date: LocalDate,
	currentPattern: List<PatternStroke>,
	changeDate: (LocalDate) -> Unit,
//@StringRes noPatternsLabel: Int,
	onRefresh: () -> Unit,
	onClassClick: (ScheduleWithSubject) -> Unit,
	modifier: Modifier,
) {
	Column(modifier = modifier) {
		CalendarLine(changeDate = changeDate, selectedDate = date)
		LoadingContent(
			loading = loading,
			isContentScrollable = true,
			empty = classes.isEmpty(),
			emptyContent = {
				Column(
					modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.horizontal_margin))
				) {
					DayOfWeekHeader(date = date, lessonsAmount = classes.size)
					Text(text = "No schedule for this day for now")
				}
				
			},
			onRefresh = onRefresh
		) {
			LazyColumn(
				Modifier.padding(horizontal = dimensionResource(R.dimen.horizontal_margin))
			) {
				item {
					DayOfWeekHeader(date = date, lessonsAmount = classes.size)
				}
				classes.forEachIndexed { it, lesson ->
					item(contentType = { lesson }) {
						ClassItem(lesson, onClassClick, currentPattern)
						if (it != classes.lastIndex) {
							Divider(
								modifier = Modifier
									.padding(horizontal = dimensionResource(R.dimen.list_item_padding))
									.fillMaxWidth(),
							)
						}
					}
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
	val startDate = LocalDate.now().minusMonths(2)
	val endDate = LocalDate.now().plusMonths(2)
	
	Column(
		modifier = Modifier
			.fillMaxWidth()
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
					Day(date = day.date, selected = selectedDate == day.date) {
						changeDate(it)
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

fun Month.displayText(short: Boolean = true): String {
	val style = if (short) TextStyle.SHORT else TextStyle.FULL
	return getDisplayName(style, Locale.getDefault())
}

fun DayOfWeek.displayText(uppercase: Boolean = false): String {
	return getDisplayName(TextStyle.SHORT, Locale.getDefault()).let { value ->
		if (uppercase) value.uppercase(Locale.getDefault()) else value
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
		if (lessonsAmount != 0) {
			Text(
				stringResource(R.string.lessons_quantity_label, lessonsAmount),
				style = MaterialTheme.typography.labelLarge
			)
		}
	}
}

@Composable
private fun ClassItem(
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
		val text: String = if (lesson.schedule.index <= currentPattern.lastIndex) {
			currentPattern[lesson.schedule.index].startTime.format(
				DateTimeFormatter.ofLocalizedTime(
					FormatStyle.SHORT
				)
			) + " - " +
					currentPattern[lesson.schedule.index].endTime.format(
						DateTimeFormatter.ofLocalizedTime(
							FormatStyle.SHORT
						)
					)
		} else stringResource(
			R.string.class_out_of_strokes_bounds_message,
			lesson.schedule.index
		)
		Text(
			text = text,
			style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
		)
		Spacer(modifier = Modifier.padding(vertical = 2.dp))
		
		Text(
			lesson.subject.name,
			style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium)
		)
		Spacer(modifier = Modifier.padding(vertical = 4.dp))
		Row {
			Row(verticalAlignment = Alignment.Top) {
				Icon(
					Icons.Default.LocationOn,
					stringResource(R.string.lesson_room),
					Modifier.size(18.dp)
				)
				Spacer(modifier = Modifier.padding(horizontal = 4.dp))
				Text(
					lesson.subject.cabinet,
					style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
				)
			}
			Spacer(modifier = Modifier.padding(horizontal = 4.dp))
			// TODO: add tags
//            Text("Tags: ${lessonStroke.subject..tags}", fontSize = 14.sp,)
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
			classes = listOf(
				ScheduleWithSubject(
					Schedule(0, 0, 0),
					Subject("Русский язык", "210")
				),
			),
			date = LocalDate.now(),
			currentPattern = listOf(
				PatternStroke(startTime = LocalTime.of(8, 30), endTime = LocalTime.of(9, 15)),
				PatternStroke(startTime = LocalTime.of(9, 30), endTime = LocalTime.of(10, 15)),
			),
			changeDate = {},
			onClassClick = {},
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
				subject = Subject(name = "Английский язык", cabinet = "316", subjectId = 0),
				schedule = Schedule(index = 0, studyDayMasterId = 0, subjectAncestorId = 0)
			),
			date = LocalDate.now(),
			currentPattern = listOf(
				PatternStroke(startTime = LocalTime.of(8, 30), endTime = LocalTime.of(9, 15)),
				PatternStroke(startTime = LocalTime.of(9, 30), endTime = LocalTime.of(10, 15)),
			),
			onDeleteClass = {},
			onEditClass = {},
			unselectClass = {},
			dialogState = rememberMaterialDialogState(true)
		)
	}
}