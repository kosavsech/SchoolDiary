package com.kxsv.schooldiary.ui.screens.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.kxsv.schooldiary.util.ui.ContinuousSelectionHelper.getSelection
import com.kxsv.schooldiary.util.ui.CopyScheduleForDayTopAppBar
import com.kxsv.schooldiary.util.ui.DateSelection
import com.kxsv.schooldiary.util.ui.backgroundHighlight
import com.kxsv.schooldiary.util.ui.displayText
import com.kxsv.schooldiary.util.ui.rememberFirstCompletelyVisibleMonth
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val TAG = "DateRangeScheduleCopy"

@Composable
fun DateRangeScheduleCopyScreen(
	onBack: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: DayScheduleViewModel,
	snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
	val uiState = viewModel.uiState.collectAsState().value
	
	Scaffold(
		topBar = {
			CopyScheduleForDayTopAppBar(
				onBack = onBack,
				date = uiState.selectedDate.format(
					DateTimeFormatter.ISO_LOCAL_DATE.withLocale(Locale.getDefault())
				)
			)
		},
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		modifier = modifier.fillMaxSize(),
	) { paddingValues ->
		
		DateRangeScheduleCopyContent(
			modifier = Modifier.padding(paddingValues),
			rangeSelected = { startDate, endDate ->
				if (uiState.refRange == null) {
					viewModel.selectRefRange(startDate, endDate)
				} else {
					viewModel.copyScheduleToRange(startDate, endDate)
					onBack()
				}
			},
			isSelectingReference = uiState.refRange == null,
			selectedDay = LocalDate.now()
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
fun DateRangeScheduleCopyContent(
	modifier: Modifier,
	rangeSelected: (startDate: LocalDate, endDate: LocalDate) -> Unit = { _, _ -> },
	selectedDay: LocalDate,
	isSelectingReference: Boolean,
) {
	val currentMonth = remember(selectedDay) { YearMonth.from(selectedDay) }
	val startMonth = remember { currentMonth.minusMonths(24) }
	val endMonth = remember { currentMonth.plusMonths(24) }
	val today = remember { LocalDate.now() }
	val daysOfWeek = remember { daysOfWeek() }
	var selection by remember { mutableStateOf(DateSelection()) }
	
	MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(primary = primaryColor)) {
		Box(
			modifier = modifier
				.fillMaxSize()
				.background(Color.White),
		) {
			Column {
				val state = rememberCalendarState(
					startMonth = startMonth,
					endMonth = endMonth,
					firstVisibleMonth = currentMonth,
					firstDayOfWeek = daysOfWeek.first(),
					outDateStyle = OutDateStyle.EndOfRow
				)
				val coroutineScope = rememberCoroutineScope()
				val visibleMonth = rememberFirstCompletelyVisibleMonth(state)
				
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
						Day(
							day = day,
							today = today,
							selection = selection,
						) { clicked ->
							if (clicked.position == DayPosition.MonthDate) {
								selection = getSelection(
									clickedDate = clicked.date,
									dateSelection = selection,
								)
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
				Divider(color = Color.Black)
				// TODO: lessons preview
				/*Box(
					modifier = Modifier
						.padding(4.dp)
						.aspectRatio(1f)
						.align(Alignment.CenterHorizontally),
					contentAlignment = Alignment.Center
				) {
					Text(
						modifier = Modifier
							.align(Alignment.Center),
						textAlign = TextAlign.Center,
						text = "No classes",
						color = Color.Black,
						fontSize = 20.sp,
					)
				}*/
			}
			CalendarBottom(
				modifier = Modifier
					.wrapContentHeight()
					.fillMaxWidth()
					.background(Color.White)
					.align(Alignment.BottomCenter),
				selection = selection,
				isSelectingReference = isSelectingReference,
				save = {
					val (startDate, endDate) = selection
					if (startDate != null && endDate != null) {
						rangeSelected(startDate, endDate)
						selection = DateSelection()
					}
				},
			)
		}
	}
}


private val primaryColor = Color.Black.copy(alpha = 0.9f)
private val selectionColor = primaryColor
private val continuousSelectionColor = Color.LightGray.copy(alpha = 0.3f)

@Composable
private fun CalendarBottom(
	modifier: Modifier = Modifier,
	selection: DateSelection,
	isSelectingReference: Boolean,
	save: () -> Unit,
) {
	Column(modifier.fillMaxWidth()) {
		Divider()
		Row(
			modifier = Modifier.padding(16.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			val text = if (isSelectingReference) {
				"Select days to copy from" // todo string resources
			} else {
				"Select days to copy to"
			}
			Text(
				text = text,
				fontWeight = FontWeight.Bold,
			)
			Spacer(modifier = Modifier.weight(1f))
			Button(
				modifier = Modifier
					.height(40.dp)
					.width(100.dp),
				onClick = save,
				enabled = selection.daysBetween != null,
			) {
				Text(text = "Select")
			}
		}
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
				.weight(1f),
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


@Composable
private fun Day(
	day: CalendarDay,
	today: LocalDate,
	selection: DateSelection,
	onClick: (CalendarDay) -> Unit,
) {
	var textColor = Color.Transparent
	Box(
		modifier = Modifier
			.aspectRatio(1f) // This is important for square-sizing!
			.clickable(
				enabled = day.position == DayPosition.MonthDate && day.date.dayOfWeek != DayOfWeek.SUNDAY,
				onClick = { onClick(day) },
			)
			.backgroundHighlight(
				day = day,
				today = today,
				selection = selection,
				selectionColor = selectionColor,
				continuousSelectionColor = continuousSelectionColor,
			) { textColor = it },
		contentAlignment = Alignment.Center,
	) {
		Text(
			text = day.date.dayOfMonth.toString(),
			color = textColor,
			fontSize = 16.sp,
			fontWeight = FontWeight.Medium,
		)
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