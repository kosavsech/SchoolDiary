package com.kxsv.schooldiary.ui.screens.schedule

import android.os.Parcelable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.darkColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.lesson.LessonWithSubject
import com.kxsv.schooldiary.ui.main.app_bars.topbar.CopyScheduleForDayTopAppBar
import com.kxsv.schooldiary.ui.main.navigation.ScheduleNavGraph
import com.kxsv.schooldiary.ui.main.navigation.nav_actions.DayScheduleCopyScreenNavActions
import com.kxsv.schooldiary.util.ui.displayText
import com.kxsv.schooldiary.util.ui.rememberFirstCompletelyVisibleMonth
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
import java.time.DayOfWeek
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
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
			.fillMaxSize()
			.background(Color.DarkGray),
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
					.background(Color.Magenta.copy(red = 0.56F, alpha = 0.85f))
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
					CompositionLocalProvider(LocalRippleTheme provides Example3RippleTheme) {
						Day(
							day = day,
							isSelected = selectedCalendarDay == day,
						) { clicked ->
							updateSelectedCalendarDay(clicked)
						}
					}
				},
				monthHeader = {
					MonthHeader(
						modifier = Modifier.padding(vertical = 8.dp),
						daysOfWeek = daysOfWeek,
					)
				},
			)
			Divider(color = Color.White)
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
										.padding(6.dp)
										.size(25.dp)
										.aspectRatio(1f)
										.clip(CircleShape)
										.background(color = Color.Blue)
								) {
									Text(
										modifier = Modifier
											.align(Alignment.Center),
										textAlign = TextAlign.Center,
										text = classes.size.toString(),
										color = Color.White,
										fontSize = 12.sp,
									)
								}
								Text(
									modifier = Modifier
										.align(Alignment.CenterVertically),
									text = "Your classes",
									color = Color.White,
									fontSize = 12.sp,
								)
							}
							Text(
								modifier = Modifier
									.align(Alignment.CenterVertically),
								text = selectedCalendarDay?.date.toString(),
								color = Color.White,
								fontSize = 12.sp,
							)
						}
					}
					val maxLines = classes.maxBy { it.key }.key + 1
					items(maxLines) {
						ClassInformation(classes[it])
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
						color = Color.White,
						fontSize = 20.sp,
					)
				}
			}
		}
	}
}


@Composable
private fun MonthHeader(
	modifier: Modifier = Modifier,
	daysOfWeek: List<DayOfWeek> = emptyList(),
) {
	Row(modifier.fillMaxWidth()) {
		for (dayOfWeek in daysOfWeek) {
			Text(
				modifier = Modifier.weight(1f),
				textAlign = TextAlign.Center,
				fontSize = 12.sp,
				color = Color.White,
				text = dayOfWeek.displayText(uppercase = true),
				fontWeight = FontWeight.Light,
			)
		}
	}
}

@Composable
private fun LazyItemScope.ClassInformation(lesson: LessonWithSubject?) {
	Row(
		modifier = Modifier
			.fillParentMaxWidth()
			.height(IntrinsicSize.Max)
			.padding(vertical = dimensionResource(R.dimen.list_item_padding)),
		horizontalArrangement = Arrangement.Center,
	) {
		val lessonName = lesson?.subject?.getName() ?: ""
		Text(text = lessonName)
	}
	Divider(color = Color.White, thickness = 2.dp)
}

@Composable
private fun Day(
	day: CalendarDay,
	isSelected: Boolean = false,
	onClick: (CalendarDay) -> Unit = {},
) {
	Box(
		modifier = Modifier
			.aspectRatio(1f) // This is important for square-sizing!
			.border(
				width = if (isSelected) 1.dp else 0.dp,
				color = if (isSelected) Color.Gray else Color.Transparent,
			)
			.padding(1.dp)
			.background(color = Color.DarkGray)
			// Disable clicks on inDates/outDates
			.clickable(
				enabled = day.position == DayPosition.MonthDate,
				onClick = { onClick(day) },
			),
	) {
		val textColor = when (day.position) {
			DayPosition.MonthDate -> Color.Unspecified
			DayPosition.InDate, DayPosition.OutDate -> Color.Transparent
		}
		Text(
			modifier = Modifier
				.align(Alignment.Center)
				.padding(top = 3.dp, end = 3.dp),
			text = day.date.dayOfMonth.toString(),
			color = textColor,
			fontSize = 12.sp,
		)
	}
}

@Composable
private fun CalendarHeader(
	modifier: Modifier,
	currentMonth: YearMonth,
	goToPrevious: () -> Unit,
	goToNext: () -> Unit,
) {
	Row(
		modifier = modifier.height(35.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		CalendarNavigationIcon(
			imageVector = Icons.Default.ArrowLeft,
			contentDescription = "Previous",
			onClick = goToPrevious,
		)
		Text(
			modifier = Modifier
				.weight(1f)
				.testTag("MonthTitle"),
			text = currentMonth.displayText(),
			fontSize = 22.sp,
			textAlign = TextAlign.Center,
			fontWeight = FontWeight.Medium,
		)
		CalendarNavigationIcon(
			imageVector = Icons.Default.ArrowRight,
			contentDescription = "Next",
			onClick = goToNext,
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