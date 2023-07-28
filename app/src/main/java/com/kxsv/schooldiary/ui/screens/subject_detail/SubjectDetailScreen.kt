package com.kxsv.schooldiary.ui.screens.subject_detail

import android.graphics.Typeface
import android.text.TextUtils
import androidx.annotation.StringRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.edu_performance.EduPerformanceEntity
import com.kxsv.schooldiary.data.local.features.grade.GradeEntity
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.local.features.teacher.TeacherEntity
import com.kxsv.schooldiary.ui.main.app_bars.topbar.SubjectDetailTopAppBar
import com.kxsv.schooldiary.util.Mark
import com.kxsv.schooldiary.util.Mark.Companion.getStringValueFrom
import com.kxsv.schooldiary.util.Utils
import com.kxsv.schooldiary.util.Utils.calculateMarkPrediction
import com.kxsv.schooldiary.util.Utils.fullNameOf
import com.kxsv.schooldiary.util.Utils.roundTo
import com.kxsv.schooldiary.util.Utils.stringRoundTo
import com.kxsv.schooldiary.util.ui.EduPerformancePeriod
import com.kxsv.schooldiary.util.ui.LoadingContent
import com.kxsv.ychart_mod.common.components.Legends
import com.kxsv.ychart_mod.common.model.PlotType
import com.kxsv.ychart_mod.common.utils.DataUtils
import com.kxsv.ychart_mod.ui.piechart.charts.PieChart
import com.kxsv.ychart_mod.ui.piechart.models.PieChartConfig
import com.kxsv.ychart_mod.ui.piechart.models.PieChartData
import com.stephenvinouze.segmentedprogressbar.SegmentedProgressBar
import com.stephenvinouze.segmentedprogressbar.models.SegmentColor
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.input
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.vanpra.composematerialdialogs.title
import java.time.format.DateTimeFormatter

@Composable
fun SubjectDetailScreen(
	@StringRes userMessage: Int?,
	onGradeClick: (String) -> Unit,
	onEditSubject: (Long) -> Unit,
	onBack: () -> Unit,
	onDeleteSubject: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: SubjectDetailViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
	val uiState = viewModel.uiState.collectAsState().value
	Scaffold(
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		modifier = modifier.fillMaxSize(),
		topBar = {
			SubjectDetailTopAppBar(
				title = uiState.subjectWithTeachers?.subject?.getName(),
				onBack = onBack,
				onDelete = viewModel::deleteSubject
			)
		},
	) { paddingValues ->
		val targetMarkDialogState = rememberMaterialDialogState(false)
		SubjectContent(
			loading = uiState.isLoading,
			empty = (uiState.subjectWithTeachers == null && uiState.grades.isEmpty()) && !uiState.isLoading,
			subject = uiState.subjectWithTeachers?.subject,
			targetMark = uiState.targetMark,
			dialogState = targetMarkDialogState,
			teachers = uiState.subjectWithTeachers?.teachers?.toList(),
			grades = uiState.grades,
			currentEduPerformancePeriod = uiState.period,
			eduPerformance = uiState.eduPerformance,
			onPeriodChange = { viewModel.changePeriod(it) },
			onGradeClick = onGradeClick,
			onEditSubject = onEditSubject,
			modifier = Modifier.padding(paddingValues)
		)
		
		TargetGradeDialog(
			dialogState = targetMarkDialogState,
			targetMark = uiState.targetMark,
			changeTargetMark = { viewModel.changeTargetMark(it) }
		)
		
		uiState.userMessage?.let { userMessage ->
			val snackbarText = stringResource(userMessage)
			LaunchedEffect(snackbarHostState, viewModel, userMessage, snackbarText) {
				snackbarHostState.showSnackbar(snackbarText)
				viewModel.snackbarMessageShown()
			}
		}
		
		LaunchedEffect(uiState.isSubjectDeleted) {
			if (uiState.isSubjectDeleted) {
				onDeleteSubject()
			}
		}
		
		if (userMessage != null) {
			LaunchedEffect(userMessage != 0) {
				viewModel.showEditResultMessage(userMessage)
			}
		}
	}
}

@Composable
private fun SubjectContent(
	loading: Boolean,
	empty: Boolean,
	subject: SubjectEntity?,
	targetMark: Double,
	dialogState: MaterialDialogState,
	teachers: List<TeacherEntity>?,
	currentEduPerformancePeriod: EduPerformancePeriod,
	grades: List<GradeEntity>,
	eduPerformance: EduPerformanceEntity?,
	onPeriodChange: (EduPerformancePeriod) -> Unit,
	onGradeClick: (String) -> Unit,
	onEditSubject: (Long) -> Unit,
	modifier: Modifier,
) {
	val screenPadding = Modifier.padding(
		horizontal = dimensionResource(id = R.dimen.horizontal_margin),
		vertical = dimensionResource(id = R.dimen.vertical_margin),
	)
	val commonModifier = modifier
		.fillMaxWidth()
		.then(screenPadding)
	
	LoadingContent(
		loading = loading,
		isContentScrollable = true,
		empty = empty,
		emptyContent = { Text(text = stringResource(R.string.no_data), modifier = commonModifier) },
		onRefresh = {}
	) {
		Column(
			modifier = modifier.verticalScroll(rememberScrollState())
		) {
			Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.vertical_margin)))
			
			SubjectInfo(
				subject = subject,
				teachers = teachers,
				onEditSubject = onEditSubject,
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = dimensionResource(R.dimen.horizontal_margin))
			)
			
			Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding)))
			
			TermSelector(
				currentEduPerformancePeriod = currentEduPerformancePeriod,
				onPeriodChange = onPeriodChange
			)
			
			Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding)))
			
			// TODO: make noContent cover
			if (eduPerformance != null) {
				TargetGradeProgress(
					eduPerformance = eduPerformance,
					subject = subject,
					targetMark = targetMark,
					dialogState = dialogState
				)
			}
			
			Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.vertical_margin)))
			
			val fivesCount = eduPerformance?.marks?.count { it == Mark.FIVE }?.toFloat() ?: 0F
			val fourthsCount = eduPerformance?.marks?.count { it == Mark.FOUR }?.toFloat() ?: 0F
			val threesCount = eduPerformance?.marks?.count { it == Mark.THREE }?.toFloat() ?: 0F
			val twosCount = eduPerformance?.marks?.count { it == Mark.TWO }?.toFloat() ?: 0F
			val pieChartData = PieChartData(
				slices = listOf(
					PieChartData.Slice("5", fivesCount, Color(0xFF5200D5)),
					PieChartData.Slice("4", fourthsCount, Color(0xFF2A7511)),
					PieChartData.Slice("3", threesCount, Color(0xFFF68300)),
					PieChartData.Slice("2", twosCount, Color(0xFFD10000)),
				), plotType = PlotType.Pie
			)
			val pieChartConfig = PieChartConfig(
				activeSliceAlpha = 0.9f,
				isEllipsizeEnabled = true,
				sliceLabelEllipsizeAt = TextUtils.TruncateAt.MIDDLE,
				sliceLabelTypeface = Typeface.defaultFromStyle(Typeface.ITALIC),
				isAnimationEnable = true,
				chartPadding = 40,
				showSliceLabels = true,
				labelVisible = true,
				labelType = PieChartConfig.LabelType.BOTH,
				sumUnit = "unit"
			)
			
			Column(
				modifier = Modifier
					.height(300.dp)
					.padding(horizontal = dimensionResource(R.dimen.horizontal_margin))
			) {
				Legends(
					legendsConfig = DataUtils.getLegendsConfigFromPieChartData(pieChartData, 4)
				)
				PieChart(
					modifier = Modifier,
					pieChartData = pieChartData,
					pieChartConfig = pieChartConfig
				)
			}
			GradesHistory(grades = grades, onGradeClick = onGradeClick)
		}
	}
}

@Composable
private fun TargetGradeProgress(
	eduPerformance: EduPerformanceEntity,
	subject: SubjectEntity?,
	targetMark: Double,
//	onEditTargetMark: (Long) -> Unit,
	dialogState: MaterialDialogState,
) {
	ElevatedCard(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = dimensionResource(R.dimen.horizontal_margin)),
	) {
		var value = 0.0
		
		eduPerformance.marks.forEach { if (it != null) value += it.value!! }
		val avgMark = (value / eduPerformance.marks.size).roundTo(2)
		
		val offset = kotlin.math.floor(avgMark)
		val targetInBar = targetMark - offset
		val startInBar = avgMark - offset
		
		val progress =
			if (avgMark >= targetMark) 10f else ((startInBar / targetInBar).toFloat()) * 10f
		
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(dimensionResource(R.dimen.vertical_margin)),
			horizontalArrangement = Arrangement.SpaceEvenly
		) {
			Text(
				text = "Average mark: $avgMark",
				style = MaterialTheme.typography.titleMedium,
			)
			Text(
				text = "Target mark: $targetMark",
				style = MaterialTheme.typography.titleMedium,
			)
		}
		
		SegmentedProgressBar(
			segmentCount = 10,
			modifier = Modifier.height(25.dp),
			spacing = 8.dp,
			angle = 30f, // Can also be negative to invert the bevel side
			progress = progress,
			segmentColor = SegmentColor(color = Color.Gray, alpha = 0.3f),
			progressColor = SegmentColor(color = Color.Green, alpha = 1f),
			drawSegmentsBehindProgress = false, // See Javadoc for more explanation on this parameter
			progressAnimationSpec = tween(
				// You can give any animation spec you'd like
				durationMillis = 1000,
				easing = LinearEasing,
			),
			/*onProgressChanged = { progress: Float, progressCoordinates: SegmentCoordinates ->
				// Get notified at each recomposition cycle when a progression occurs.
				// You can use the current progression or the coordinates the progress segment currently has.
			},
			onProgressFinished = {
				// Get notified when the progression animation ends.
			}*/
		)
		val estimatesGrades = calculateMarkPrediction(
			target = targetMark,
			avgMark = avgMark,
			sum = eduPerformance.marks.size,
			valueSum = value
		)
		Column(
			modifier = Modifier.padding(dimensionResource(R.dimen.vertical_margin))
		) {
			if (avgMark >= targetMark) {
				Text(
					text = "Target reached!!!",
					style = MaterialTheme.typography.titleMedium,
					modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding))
				)
			} else {
				Text(
					text = "To achieve target:",
					style = MaterialTheme.typography.titleMedium,
				)
				if (estimatesGrades.fiveCount != null) {
					Text(
						text = "Need 5 x ${estimatesGrades.fiveCount}",
						style = MaterialTheme.typography.titleMedium,
						modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding))
					)
				}
				if (estimatesGrades.fourCount != null) {
					Text(
						text = "Need 4 x ${estimatesGrades.fourCount}",
						style = MaterialTheme.typography.titleMedium,
						modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding))
					)
				}
				if (estimatesGrades.threeCount != null) {
					Text(
						text = "Need 3 x ${estimatesGrades.threeCount} ",
						style = MaterialTheme.typography.titleMedium,
						modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding))
					)
				}
			}
			Button(onClick = { if (subject != null) dialogState.show() }) {
				Text(
					text = stringResource(R.string.edit_target_mark),
					style = MaterialTheme.typography.labelMedium
				)
			}
		}
	}
}

@Composable
private fun TargetGradeDialog(
	dialogState: MaterialDialogState,
	targetMark: Double,
	changeTargetMark: (Double) -> Unit,
) {
	MaterialDialog(
		dialogState = dialogState,
		buttons = {
			positiveButton(res = R.string.btn_save)
			negativeButton(res = R.string.btn_cancel)
		},
		
		) {
		title(res = R.string.enter_target_mark_dialog_title)
		input(
			label = "Target mark",
			prefill = targetMark.stringRoundTo(2),
			placeholder = "4.69",
			isTextValid = { it.toDoubleOrNull() != null && it.toDouble() < 4.99 }
		) { inputString ->
			changeTargetMark(inputString.toDouble())
		}
	}
}

@Composable
private fun TermSelector(
	currentEduPerformancePeriod: EduPerformancePeriod,
	onPeriodChange: (EduPerformancePeriod) -> Unit,
) {
	val buttons = listOf(
		Utils.PeriodButton("First term", EduPerformancePeriod.FIRST_TERM),
		Utils.PeriodButton("Second term", EduPerformancePeriod.SECOND_TERM),
		Utils.PeriodButton("Third term", EduPerformancePeriod.THIRD_TERM),
		Utils.PeriodButton("Fourth term", EduPerformancePeriod.FOURTH_TERM),
	)
	LazyRow(
		state = LazyListState(firstVisibleItemIndex = currentEduPerformancePeriod.ordinal)
	) {
		items(buttons) {
			OutlinedButton(
				onClick = { onPeriodChange(it.callbackPeriod) },
				modifier = Modifier.padding(
					horizontal = dimensionResource(R.dimen.list_item_padding)
				),
				enabled = (currentEduPerformancePeriod != it.callbackPeriod)
			) {
				Text(text = it.text)
			}
		}
	}
}

@Composable
private fun GradesHistory(
	grades: List<GradeEntity>,
	onGradeClick: (String) -> Unit,
) {
	Text(
		text = stringResource(R.string.grade_history),
		style = MaterialTheme.typography.titleLarge,
		modifier = Modifier.padding(
			horizontal = dimensionResource(R.dimen.horizontal_margin),
		)
	)
	Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding)))
	
	LazyColumn(
		modifier = Modifier
			.height(250.dp)
	) {
		items(grades) { grade ->
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.SpaceBetween,
				modifier = Modifier
					.fillMaxWidth()
					.clickable { onGradeClick(grade.gradeId) }
					.padding(
						horizontal = dimensionResource(R.dimen.horizontal_margin),
						vertical = dimensionResource(R.dimen.vertical_margin)
					)
			) {
				Text(
					text = getStringValueFrom(grade.mark),
					style = MaterialTheme.typography.titleMedium,
				)
				Text(
					text = grade.typeOfWork,
					style = MaterialTheme.typography.titleMedium,
				)
				val shortDatePattern = "LLL-dd"
				Text(
					text = grade.date.format(DateTimeFormatter.ofPattern(shortDatePattern)),
					style = MaterialTheme.typography.titleMedium,
				)
			}
		}
	}
	Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.vertical_margin)))
	
}

@Composable
private fun SubjectInfo(
	modifier: Modifier = Modifier,
	subject: SubjectEntity?,
	teachers: List<TeacherEntity>?,
	onEditSubject: (Long) -> Unit,
) {
	ElevatedCard(
		modifier = modifier
	) {
		// TODO: make noContent cover
		Column(
			modifier = Modifier.padding(dimensionResource(R.dimen.vertical_margin))
		) {
			Text(
				text = subject?.fullName ?: "",
				style = MaterialTheme.typography.titleMedium,
			)
			Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding)))
			Text(
				text = subject?.getCabinetString() ?: "",
				style = MaterialTheme.typography.titleMedium,
			)
			var teachersText = ""
			teachers?.forEachIndexed { index, teacher ->
				teachersText += (if (index != 0) ", " else "")
				teachersText += fullNameOf(teacher)
				if (index == 2) return@forEachIndexed
			}
			if (teachersText.isNotEmpty()) {
				Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding)))
				Text(
					text = teachersText,
					style = MaterialTheme.typography.titleMedium,
				)
			}
			
			Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding)))
			Button(onClick = { if (subject != null) onEditSubject(subject.subjectId) }) {
				Text(
					text = stringResource(R.string.edit_subject),
					style = MaterialTheme.typography.labelMedium
				)
			}
		}
	}
}