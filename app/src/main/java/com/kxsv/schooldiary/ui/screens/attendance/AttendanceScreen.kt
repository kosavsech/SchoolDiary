package com.kxsv.schooldiary.ui.screens.attendance

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.darkColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
import com.kxsv.schooldiary.data.local.features.lesson.LessonEntity
import com.kxsv.schooldiary.data.local.features.lesson.LessonWithSubject
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.util.Mark
import com.kxsv.schooldiary.data.util.Mark.Companion.getStringValueFrom
import com.kxsv.schooldiary.ui.main.navigation.nav_actions.AttendanceScreenNavActions
import com.kxsv.schooldiary.ui.theme.AppTheme
import com.kxsv.schooldiary.ui.util.AppSnackbarHost
import com.kxsv.schooldiary.ui.util.LoadingContent
import com.kxsv.schooldiary.ui.util.displayText
import com.kxsv.schooldiary.ui.util.rememberFirstCompletelyVisibleMonth
import com.kxsv.schooldiary.util.Utils
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.YearMonth

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Destination
@Composable
fun AttendanceScreen(
	destinationsNavigator: DestinationsNavigator,
	drawerState: DrawerState,
	coroutineScope: CoroutineScope,
	snackbarHostState: SnackbarHostState,
	viewModel: AttendanceViewModel,
) {
	val navigator = AttendanceScreenNavActions(destinationsNavigator = destinationsNavigator)
	val uiState = viewModel.uiState.collectAsState().value
	
	Scaffold(
		topBar = {
		
		},
		snackbarHost = { AppSnackbarHost(hostState = snackbarHostState) },
		modifier = Modifier.fillMaxSize(),
		containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 1f),
	) {
		
		/*val onRefresh = remember {
			{ viewModel.onDayChangeUpdate(null) }
		}
		val changeDate = remember<(LocalDate) -> Unit> {
			{ viewModel.onDayChangeUpdate(it) }
		}*/
		/*AttendanceContent(
			modifier = Modifier.padding(paddingValues),
			isLoading = uiState.isLoading,
			classesOnDate = uiState.classes,
			fetchedClasses = uiState.fetchedClasses,
			
		)*/
		
		
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
private fun AttendanceContent(
	modifier: Modifier,
	isLoading: Boolean,
	selectedCalendarDay: CalendarDay?,
	updateSelectedCalendarDay: (CalendarDay?) -> Unit,
	classesWithGrades: List<ClassWithGrade>,
	fetchedClassesWithGrades: List<ClassWithGrade>?,
	onRefresh: () -> Unit,
) {
	val currentMonth = remember { YearMonth.from(Utils.currentDate) }
	val startMonth = remember { currentMonth.minusMonths(24) }
	val endMonth = remember { currentMonth.plusMonths(24) }
	val daysOfWeek = remember { daysOfWeek() }
	LoadingContent(
		isLoading = isLoading,
		empty = false,
		isContentScrollable = true,
		onRefresh = onRefresh
	) {
		Column(
			modifier = modifier
				.fillMaxSize()
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
				ClassesPreview(classesWithGrades, selectedCalendarDay)
			}
		}
	}
}

@Composable
private fun ColumnScope.ClassesPreview(
	classesWithGrades: List<ClassWithGrade>,
	selectedCalendarDay: CalendarDay?,
) {
	if (classesWithGrades.isNotEmpty()) {
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
								text = classesWithGrades.size.toString(),
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
			items(classesWithGrades) { classWithGrade ->
				ClassInformation(
					lesson = classWithGrade.lessonWithSubject,
					mark = classWithGrade.grade
				)
				if (classWithGrade != classesWithGrades.last()) {
					Divider(color = MaterialTheme.colorScheme.onBackground)
				}
			}
		}
	} else {
		Box(
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
				color = MaterialTheme.colorScheme.onSurface,
				style = MaterialTheme.typography.bodyMedium,
			)
		}
	}
}

@Composable
private fun LazyItemScope.ClassInformation(lesson: LessonWithSubject?, mark: Mark) {
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
		Text(
			text = getStringValueFrom(mark),
			color = MaterialTheme.colorScheme.onSurface
		)
	}
}

@Composable
fun MonthHeaderInCalendar(
	modifier: Modifier = Modifier,
	daysOfWeek: List<DayOfWeek> = emptyList(),
) {
	Row(modifier.fillMaxWidth()) {
		for (dayOfWeek in daysOfWeek) {
			Text(
				modifier = Modifier.weight(1f),
				text = dayOfWeek.displayText(uppercase = true),
				color = MaterialTheme.colorScheme.onBackground,
				style = MaterialTheme.typography.labelMedium.copy(
					textAlign = TextAlign.Center,
					fontWeight = FontWeight.Light
				),
			)
		}
	}
}

@Composable
fun DayInCalendar(
	day: CalendarDay,
	isSelected: Boolean = false,
	isToday: Boolean = day.date == Utils.currentDate,
	onClick: (CalendarDay) -> Unit = {},
) {
	Box(
		modifier = Modifier
			.size(60.dp)
			.aspectRatio(1f)
			.clip(CircleShape)
			.clickable(
				enabled = day.position == DayPosition.MonthDate,
				onClick = { onClick(day) },
			),
		contentAlignment = Alignment.Center
	) {
		val textColor = when (day.position) {
			DayPosition.MonthDate -> {
				if (isSelected) {
					MaterialTheme.colorScheme.onPrimary
				} else if (isToday) {
					MaterialTheme.colorScheme.primary
				} else {
					MaterialTheme.colorScheme.onBackground
				}
			}
			
			DayPosition.InDate, DayPosition.OutDate -> Color.Transparent
		}
		val backgroundColor = when (day.position) {
			DayPosition.MonthDate -> {
				if (isSelected) {
					MaterialTheme.colorScheme.primary
				} else {
					Color.Transparent
				}
			}
			
			DayPosition.InDate, DayPosition.OutDate -> Color.Transparent
		}
		Text(
			modifier = Modifier
				.size(52.dp)
				.clip(CircleShape)
				.border(
					width = 1.dp,
					shape = CircleShape,
					color = if (isToday && day.position == DayPosition.MonthDate) {
						MaterialTheme.colorScheme.primary
					} else {
						Color.Transparent
					},
				)
				.background(backgroundColor)
				.wrapContentSize(Alignment.Center),
			text = day.date.dayOfMonth.toString(),
			color = textColor,
			style = MaterialTheme.typography.labelMedium,
		)
	}
}


@Composable
fun CalendarHeader(
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
			tint = MaterialTheme.colorScheme.onPrimaryContainer
		)
		Text(
			modifier = Modifier
				.weight(1f)
				.testTag("MonthTitle"),
			text = currentMonth.displayText(),
			fontSize = 22.sp,
			textAlign = TextAlign.Center,
			color = MaterialTheme.colorScheme.onPrimaryContainer,
			fontWeight = FontWeight.Medium,
		)
		CalendarNavigationIcon(
			imageVector = Icons.Default.ArrowRight,
			contentDescription = "Next",
			onClick = goToNext,
			tint = MaterialTheme.colorScheme.onPrimaryContainer
		)
	}
}

@Composable
private fun CalendarNavigationIcon(
	imageVector: ImageVector,
	contentDescription: String,
	onClick: () -> Unit,
	tint: Color = LocalContentColor.current,
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
		tint = tint,
	)
}

private val previewClassesWithGrades = listOf(
	ClassWithGrade(
		LessonWithSubject(LessonEntity(0), SubjectEntity("Татарский язык", "210")),
		Mark.ABSENT
	),
	ClassWithGrade(
		LessonWithSubject(LessonEntity(0), SubjectEntity("Русский язык", "210")),
		Mark.ABSENT
	),
	ClassWithGrade(
		LessonWithSubject(
			LessonEntity(0),
			SubjectEntity("Иностранный язык (Английский)", "316")
		), Mark.ABSENT
	),
	ClassWithGrade(
		LessonWithSubject(
			LessonEntity(0),
			SubjectEntity("Основы безопасности жизнедеятельности", "316")
		), Mark.ABSENT
	),
	ClassWithGrade(
		LessonWithSubject(
			LessonEntity(0),
			SubjectEntity("Алгебра и начала математического анализа", "310")
		), Mark.ABSENT
	),
	ClassWithGrade(
		LessonWithSubject(LessonEntity(0), SubjectEntity("Литература", "210")),
		Mark.ABSENT
	),
)

@Preview
@Composable
fun AttendanceContentPreview() {
	AppTheme(darkTheme = true) {
		Surface {
			AttendanceContent(
				modifier = Modifier,
				isLoading = false,
				selectedCalendarDay = CalendarDay(
					Utils.currentDate.minusDays(1),
					DayPosition.MonthDate
				),
				updateSelectedCalendarDay = {},
				classesWithGrades = previewClassesWithGrades,
				fetchedClassesWithGrades = null,
				onRefresh = {}
			)
		}
	}
}