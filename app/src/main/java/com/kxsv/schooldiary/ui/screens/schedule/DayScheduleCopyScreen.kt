package com.kxsv.schooldiary.ui.screens.schedule

import android.os.Parcelable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.darkColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.lesson.LessonWithSubject
import com.kxsv.schooldiary.ui.main.app_bars.topbar.CopyScheduleForDayTopAppBar
import com.kxsv.schooldiary.ui.main.navigation.ScheduleNavGraph
import com.kxsv.schooldiary.ui.main.navigation.nav_actions.DayScheduleCopyScreenNavActions
import com.kxsv.schooldiary.ui.screens.attendance.CalendarHeader
import com.kxsv.schooldiary.ui.screens.attendance.DayInCalendar
import com.kxsv.schooldiary.ui.screens.attendance.MonthHeaderInCalendar
import com.kxsv.schooldiary.ui.util.AppSnackbarHost
import com.kxsv.schooldiary.ui.util.rememberFirstCompletelyVisibleMonth
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.message
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.vanpra.composematerialdialogs.title
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Parcelize
data class DayScheduleCopyResult(
	val isTimingsCopied: Boolean,
	val fromDate: LocalDate,
) : Parcelable

@ScheduleNavGraph
@Destination
@Composable
fun DayScheduleCopyScreen(
	resultBackNavigator: ResultBackNavigator<DayScheduleCopyResult>,
	destinationsNavigator: DestinationsNavigator,
	viewModel: ScheduleViewModel,
	snackbarHostState: SnackbarHostState,
) {
	val uiState = viewModel.uiState.collectAsState().value
	val dialogState = rememberMaterialDialogState()
	val navigator = DayScheduleCopyScreenNavActions(
		destinationsNavigator = destinationsNavigator,
		resultBackNavigator = resultBackNavigator
	)
	Scaffold(
		topBar = {
			CopyScheduleForDayTopAppBar(
				onBack = { navigator.popBackStack() },
				date = uiState.selectedDate.format(
					DateTimeFormatter.ISO_LOCAL_DATE.withLocale(Locale.getDefault())
				)
			)
		},
		snackbarHost = { AppSnackbarHost(hostState = snackbarHostState) },
		floatingActionButton = {
			val showButton =
				remember(key1 = uiState.selectedRefCalendarDay, key2 = uiState.classes) {
					uiState.selectedRefCalendarDay != null && uiState.classes.isNotEmpty()
				}
			if (showButton) {
				FloatingActionButton(onClick = { dialogState.show() }) {
					Icon(
						imageVector = Icons.Default.ContentCopy,
						contentDescription = "Copy lesson from selected calendar day"
					)
				}
			}
		},
		modifier = Modifier.fillMaxSize(),
	) { paddingValues ->
		
		DayScheduleCopyContent(
			selectedDate = uiState.selectedDate,
			classes = uiState.classes,
			selectedCalendarDay = uiState.selectedRefCalendarDay,
			updateSelectedCalendarDay = viewModel::onCalendarDayChangeUpdate,
			modifier = Modifier.padding(paddingValues)
		)
		
		DayScheduleCopyDialog(
			dialogState = dialogState,
			fromDate = uiState.selectedRefCalendarDay?.date,
			copyScheduleFromDay = viewModel::copySchedule,
			onScheduleCopied = { navigator.backWithResult(it) }
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
fun DayScheduleCopyDialog(
	dialogState: MaterialDialogState,
	fromDate: LocalDate?,
	copyScheduleFromDay: (Boolean) -> Unit,
	onScheduleCopied: (DayScheduleCopyResult) -> Unit,
) {
	if (fromDate != null) {
		MaterialDialog(
			dialogState = dialogState,
			buttons = {
				positiveButton(
					text = "Yes",
					onClick = {
						copyScheduleFromDay(true)
						onScheduleCopied(
							DayScheduleCopyResult(isTimingsCopied = true, fromDate = fromDate)
						)
					}
				)
				negativeButton(
					text = "No",
					onClick = {
						copyScheduleFromDay(false)
						onScheduleCopied(
							DayScheduleCopyResult(isTimingsCopied = false, fromDate = fromDate)
						)
					}
				)
			}
		) {
			title(text = "Copy time pattern too?") // TODO create string resource
			message(text = "Should we also copy bell timings from that day ?")
		}
	}
}

// TODO: get rid of selectedCalendarDay field and instead send day on copy action?
@Composable
fun DayScheduleCopyContent(
	selectedDate: LocalDate,
	classes: Map<Int, LessonWithSubject>,
	selectedCalendarDay: CalendarDay?,
	updateSelectedCalendarDay: (CalendarDay?) -> Unit,
	modifier: Modifier,
) {
	val currentMonth = remember { YearMonth.from(selectedDate) }
	val startMonth = remember { currentMonth.minusMonths(24) }
	val endMonth = remember { currentMonth.plusMonths(24) }
	val daysOfWeek = remember { daysOfWeek() }
	
	Column(
		modifier = modifier
			.fillMaxSize(),
	) {
		val state = rememberCalendarState(
			startMonth = startMonth,
			endMonth = endMonth,
			firstVisibleMonth = currentMonth,
			firstDayOfWeek = daysOfWeek.first(),
			outDateStyle = OutDateStyle.EndOfRow,
		)
		val coroutineScope = rememberCoroutineScope()
		val visibleMonth = rememberFirstCompletelyVisibleMonth(state)
		LaunchedEffect(visibleMonth) {
			// Clear selection if we scroll to a new month.
			updateSelectedCalendarDay(null)
		}
		
		// Draw light content on dark background.
		CompositionLocalProvider(LocalContentColor provides darkColors().onSurface) {
			CalendarHeader(
				modifier = Modifier
					.background(MaterialTheme.colorScheme.primaryContainer)
					.padding(horizontal = 8.dp, vertical = 12.dp),
				currentMonth = visibleMonth.yearMonth,
				goToPrevious = {
					coroutineScope.launch {
						state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.previousMonth)
					}
				},
				goToNext = {
					coroutineScope.launch {
						state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.nextMonth)
					}
				},
			)
			HorizontalCalendar(
				modifier = Modifier.wrapContentWidth(),
				state = state,
				dayContent = { day ->
					DayInCalendar(
						day = day,
						isSelected = selectedCalendarDay == day,
					) { clicked ->
						updateSelectedCalendarDay(clicked)
					}
				},
				monthHeader = {
					MonthHeaderInCalendar(
						modifier = Modifier.padding(vertical = 8.dp),
						daysOfWeek = daysOfWeek,
					)
				},
			)
			Divider(
				modifier = Modifier
					.padding(vertical = dimensionResource(R.dimen.list_item_padding))
			)
			ClassesPreview(classes, selectedCalendarDay)
		}
	}
}

@Composable
private fun ColumnScope.ClassesPreview(
	classes: Map<Int, LessonWithSubject>,
	selectedCalendarDay: CalendarDay?,
) {
	if (classes.isNotEmpty()) {
		LazyColumn(modifier = Modifier.fillMaxWidth()) {
			item {
				Row(
					modifier = Modifier
						.fillParentMaxWidth()
						.padding(horizontal = 16.dp),
					horizontalArrangement = Arrangement.SpaceBetween
				) {
					Row {
						Box(
							modifier = Modifier
								.size(25.dp)
								.aspectRatio(1f)
								.clip(CircleShape)
								.background(color = MaterialTheme.colorScheme.secondary)
						) {
							Text(
								modifier = Modifier
									.align(Alignment.Center),
								textAlign = TextAlign.Center,
								text = classes.size.toString(),
								color = MaterialTheme.colorScheme.onSecondary,
								style = MaterialTheme.typography.labelMedium,
							)
						}
						Spacer(Modifier.width(dimensionResource(R.dimen.list_item_padding)))
						Text(
							modifier = Modifier.align(Alignment.CenterVertically),
							text = "Your classes",
							color = MaterialTheme.colorScheme.onSurface,
							style = MaterialTheme.typography.labelMedium,
						)
					}
					Text(
						modifier = Modifier
							.align(Alignment.CenterVertically),
						text = selectedCalendarDay?.date.toString(),
						color = MaterialTheme.colorScheme.onSurface,
						style = MaterialTheme.typography.labelMedium,
					)
				}
			}
			val maxLines = classes.maxBy { it.key }.key + 1
			items(maxLines) { classIndex ->
				ClassInformation(classes[classIndex])
				if ((classIndex + 1) != maxLines) {
					Divider(color = MaterialTheme.colorScheme.onBackground)
				}
			}
		}
	} else {
		Box(
			modifier = Modifier
				.padding(4.dp)
				.aspectRatio(1f)
				.align(CenterHorizontally),
			contentAlignment = Alignment.Center
		) {
			Text(
				modifier = Modifier
					.align(Alignment.Center),
				textAlign = TextAlign.Center,
				text = "No classes",
				color = MaterialTheme.colorScheme.onSurface,
				style = MaterialTheme.typography.bodyMedium,
			)
		}
	}
}

@Composable
private fun LazyItemScope.ClassInformation(lesson: LessonWithSubject?) {
	Row(
		modifier = Modifier
			.fillParentMaxWidth()
			.padding(
				vertical = dimensionResource(R.dimen.list_item_padding),
				horizontal = dimensionResource(R.dimen.horizontal_margin)
			),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		val lessonName = lesson?.subject?.getName() ?: ""
		Text(
			modifier = Modifier.weight(0.75f),
			text = lessonName,
			color = MaterialTheme.colorScheme.onSurface
		)
	}
}

@Composable
private fun CalendarNavigationIcon(
	imageVector: ImageVector,
	contentDescription: String,
	onClick: () -> Unit,
) = Box(
	modifier = Modifier
		.fillMaxHeight()
		.aspectRatio(1f)
		.clip(shape = CircleShape)
		.clickable(role = Role.Button, onClick = onClick),
) {
	Icon(
		modifier = Modifier
			.fillMaxSize()
			.padding(4.dp)
			.align(Alignment.Center),
		imageVector = imageVector,
		contentDescription = contentDescription,
	)
}

// The default dark them ripple is too bright so we tone it down.
private object Example3RippleTheme : RippleTheme {
	@Composable
	override fun defaultColor() =
		RippleTheme.defaultRippleColor(Color.Gray, lightTheme = false)
	
	@Composable
	override fun rippleAlpha() =
		RippleTheme.defaultRippleAlpha(Color.Gray, lightTheme = false)
}