package com.kxsv.schooldiary.ui.screens.subject_list.subject_detail

import android.graphics.Typeface
import android.text.TextUtils
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
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
import com.kxsv.schooldiary.data.local.features.subject.SubjectWithTeachers
import com.kxsv.schooldiary.data.local.features.teacher.TeacherEntity
import com.kxsv.schooldiary.data.local.features.teacher.TeacherEntity.Companion.shortName
import com.kxsv.schooldiary.data.util.EduPerformancePeriod
import com.kxsv.schooldiary.data.util.Mark
import com.kxsv.schooldiary.data.util.Mark.Companion.getStringValueFrom
import com.kxsv.schooldiary.ui.main.app_bars.topbar.SubjectDetailTopAppBar
import com.kxsv.schooldiary.ui.main.navigation.DELETE_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.nav_actions.SubjectDetailScreenNavActions
import com.kxsv.schooldiary.ui.util.LoadingContent
import com.kxsv.schooldiary.ui.util.TermSelector
import com.kxsv.schooldiary.util.Utils
import com.kxsv.schooldiary.util.Utils.calculateMarksUntilTarget
import com.kxsv.schooldiary.util.Utils.calculateRealizableBadMarks
import com.kxsv.schooldiary.util.Utils.getLowerBoundForMark
import com.kxsv.schooldiary.util.Utils.roundTo
import com.kxsv.schooldiary.util.Utils.stringRoundTo
import com.kxsv.ychart_mod.common.model.PlotType
import com.kxsv.ychart_mod.ui.piechart.charts.PieChart
import com.kxsv.ychart_mod.ui.piechart.models.PieChartConfig
import com.kxsv.ychart_mod.ui.piechart.models.PieChartData
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.stephenvinouze.segmentedprogressbar.SegmentedProgressBar
import com.stephenvinouze.segmentedprogressbar.models.SegmentColor
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.input
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.vanpra.composematerialdialogs.title
import java.time.format.DateTimeFormatter
import kotlin.math.floor

data class SubjectDetailScreenNavArgs(
	val subjectId: String,
)

private const val TAG = "SubjectDetailScreen"

@Destination(
	navArgsDelegate = SubjectDetailScreenNavArgs::class
)
@Composable
fun SubjectDetailScreen(
	resultNavigator: ResultBackNavigator<Int>,
	destinationsNavigator: DestinationsNavigator,
	viewModel: SubjectDetailViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState,
) {
	val navigator = SubjectDetailScreenNavActions(
		destinationsNavigator = destinationsNavigator,
		resultBackNavigator = resultNavigator
	)
	val uiState = viewModel.uiState.collectAsState().value
	Scaffold(
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		modifier = Modifier.fillMaxSize(),
		topBar = {
			SubjectDetailTopAppBar(
				title = uiState.subjectWithTeachers?.subject?.getName() ?: "",
				onBack = { navigator.popBackStack() },
				onDelete = {
					viewModel.deleteSubject()
					navigator.backWithResult(DELETE_RESULT_OK)
				}
			)
		},
	) { paddingValues ->
		val targetMarkDialogState = rememberMaterialDialogState(false)
		
		val changePeriod = remember<(EduPerformancePeriod) -> Unit> {
			{ viewModel.changePeriod(it) }
		}
		val editSubject = remember<(String) -> Unit> {
			{ navigator.onEditSubject(it) }
		}
		val showGradeDetails = remember<(String) -> Unit> {
			{ /*TODO pop-up with fetched date of grade */ }
		}
		val refresh = remember { { viewModel.refresh() } }
		SubjectContent(
			modifier = Modifier.padding(paddingValues),
			targetMarkDialogState = targetMarkDialogState,
			isLoading = uiState.isLoading,
			currentPeriod = uiState.period,
			subjectWithTeachers = uiState.subjectWithTeachers,
			targetMark = uiState.targetMark,
			roundRule = uiState.roundRule,
			eduPerformance = uiState.eduPerformance,
			grades = uiState.grades,
			onPeriodChange = changePeriod,
			onGradeClick = showGradeDetails,
			onEditSubject = editSubject,
			onRefresh = refresh
		)
		
		val saveTargetMark = remember<() -> Unit> {
			{ viewModel.saveTargetMark() }
		}
		val changeDialogTargetMark = remember<(Double?) -> Unit> {
			{ viewModel.changeTargetMark(it) }
		}
		TargetGradeDialog(
			dialogState = targetMarkDialogState,
			targetMark = uiState.targetMark,
			saveTargetMark = saveTargetMark,
			changeTargetMark = changeDialogTargetMark
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
private fun SubjectContent(
	modifier: Modifier,
	targetMarkDialogState: MaterialDialogState,
	isLoading: Boolean,
	currentPeriod: EduPerformancePeriod,
	subjectWithTeachers: SubjectWithTeachers?,
	targetMark: Double,
	roundRule: Double,
	eduPerformance: EduPerformanceEntity?,
	grades: List<GradeEntity>,
	onPeriodChange: (EduPerformancePeriod) -> Unit,
	onGradeClick: (String) -> Unit,
	onEditSubject: (String) -> Unit,
	onRefresh: () -> Unit,
) {
	val screenPadding = Modifier.padding(
		horizontal = dimensionResource(id = R.dimen.horizontal_margin),
		vertical = dimensionResource(id = R.dimen.vertical_margin),
	)
	val commonModifier = modifier
		.fillMaxWidth()
		.then(screenPadding)
	
	LoadingContent(
		modifier = modifier,
		loading = isLoading,
		empty = (subjectWithTeachers == null) && grades.isEmpty(),
		emptyContent = { Text(text = stringResource(R.string.no_data), modifier = commonModifier) },
		isContentScrollable = true,
		onRefresh = onRefresh
	) {
		Column(
			modifier = Modifier
				.verticalScroll(rememberScrollState())
				.padding(vertical = dimensionResource(R.dimen.vertical_margin)),
		) {
			if (subjectWithTeachers != null) {
				SubjectInfo(
					subject = subjectWithTeachers.subject,
					teachers = subjectWithTeachers.teachers.toList(),
					onEditSubject = onEditSubject,
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = dimensionResource(R.dimen.horizontal_margin))
				)
				Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding)))
			}
			
			TermSelector(
				currentPeriod = currentPeriod,
				onPeriodChange = onPeriodChange,
				buttons = remember { Utils.PeriodButton.allTerms }
			)
			Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding)))
			
			// TODO: make noContent cover
			if (eduPerformance != null) {
				TargetGradeProgress(
					targetMarkDialogState = targetMarkDialogState,
					performanceEntity = eduPerformance,
					targetMark = targetMark,
					roundRule = roundRule
				)
				Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.vertical_margin)))
				
				val fivesCount = remember(eduPerformance.marks) {
					eduPerformance.marks.count { it == Mark.FIVE }.toFloat()
				}
				val fourthsCount = remember(eduPerformance.marks) {
					eduPerformance.marks.count { it == Mark.FOUR }.toFloat()
				}
				val threesCount = remember(eduPerformance.marks) {
					eduPerformance.marks.count { it == Mark.THREE }.toFloat()
				}
				val twosCount = remember(eduPerformance.marks) {
					eduPerformance.marks.count { it == Mark.TWO }.toFloat()
				}
				
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
					PieChart(
						modifier = Modifier,
						pieChartData = pieChartData,
						pieChartConfig = pieChartConfig
					)
				}
			}
			GradesHistory(grades = grades, onGradeClick = onGradeClick)
		}
	}
}

@Composable
private fun TargetGradeProgress(
	targetMarkDialogState: MaterialDialogState,
	performanceEntity: EduPerformanceEntity,
	targetMark: Double,
	roundRule: Double,
) {
	ElevatedCard(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = dimensionResource(R.dimen.horizontal_margin)),
	) {
		val valueSum = remember(performanceEntity.marks) {
			var temp = 0.0
			performanceEntity.marks.forEach { if (it != null) temp += it.value!! }
			return@remember temp
		}
		val avgMark = remember(performanceEntity, performanceEntity.marks, valueSum) {
			(valueSum / performanceEntity.marks.size).roundTo(2)
		}
		val progress = remember(avgMark, targetMark) {
			if (avgMark >= targetMark) {
				10f
			} else {
				(((avgMark - floor(avgMark)) / (targetMark - floor(avgMark))).toFloat()) * 10f
			}
			/*Log.d(TAG, "TargetGradeProgress: PROGRESS IS $result")
			return@remember result*/
		}
		
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
			angle = -30f,
			progress = progress,
			segmentColor = SegmentColor(color = Color.Gray, alpha = 0.3f),
			progressColor = SegmentColor(color = Color.Green, alpha = 1f),
			drawSegmentsBehindProgress = false,
		)
		val marksUntilTarget =
			remember(targetMark, performanceEntity.marks.size, avgMark, valueSum) {
				calculateMarksUntilTarget(
					target = targetMark,
					avgMark = avgMark,
					sum = performanceEntity.marks.size,
					valueSum = valueSum
				)
			}
		val lowerBound = remember(avgMark, roundRule) {
			getLowerBoundForMark(avgMark, roundRule)
		}
		val realizableBadMarks =
			remember(lowerBound, avgMark, performanceEntity.marks.size, valueSum, roundRule) {
				calculateRealizableBadMarks(
					roundRule = roundRule,
					lowerBound = lowerBound,
					avgMark = avgMark,
					sum = performanceEntity.marks.size,
					valueSum = valueSum
				)
			}
		
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
				marksUntilTarget.forEach { (mark, count) ->
					key(mark, count) {
						if (count != null) {
							Text(
								text = "Need $mark x $count times",
								style = MaterialTheme.typography.titleMedium,
								modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding))
							)
						}
					}
				}
			}
			Text(
				text = "To not ruin current mark:",
				style = MaterialTheme.typography.titleMedium,
			)
			val isRealizableBadMarksNotEmpty = remember(realizableBadMarks) {
				realizableBadMarks.map { it.value }.find { it != null } != null
			}
			if (isRealizableBadMarksNotEmpty) {
				realizableBadMarks.forEach { (mark, count) ->
					key(mark, count) {
						if (count != null) {
							val (mark1, mark2) = mark.split("_")
							Text(
								text = "No more than $mark1 x ${count[mark1]} times" +
										if (mark2.isNotEmpty() && count[mark2] != 0) {
											" with $mark2 x ${count[mark2]} times"
										} else {
											""
										},
								style = MaterialTheme.typography.titleMedium,
								modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding))
							)
						}
					}
				}
			} else {
				Text(
					text = "You cannot afford any others marks",
					style = MaterialTheme.typography.titleMedium,
					modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding))
				)
			}
			Button(onClick = { targetMarkDialogState.show() }) {
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
	saveTargetMark: () -> Unit,
	changeTargetMark: (Double?) -> Unit,
) {
	MaterialDialog(
		dialogState = dialogState,
		buttons = {
			positiveButton(res = R.string.btn_save, onClick = { saveTargetMark() })
			negativeButton(res = R.string.btn_cancel)
		},
	) {
		title(res = R.string.enter_target_mark_dialog_title)
		input(
			label = "Target mark",
			prefill = targetMark.stringRoundTo(2),
			placeholder = "2.89",
			isTextValid = {
				it.toDoubleOrNull() != null && (it.toDouble() > 2.00 && it.toDouble() < 5.00)
			},
			errorMessage = "Follow the format.\nAlso ensure that target is more than 2 and is less than 5",
			onInput = { changeTargetMark(it.toDoubleOrNull()) },
			waitForPositiveButton = false
		)
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
	subject: SubjectEntity,
	teachers: List<TeacherEntity>,
	onEditSubject: (String) -> Unit,
) {
	ElevatedCard(
		modifier = modifier
	) {
		// TODO: make noContent cover
		Column(
			modifier = Modifier.padding(dimensionResource(R.dimen.vertical_margin))
		) {
			Text(
				text = subject.fullName,
				style = MaterialTheme.typography.titleMedium,
			)
			Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding)))
			Text(
				text = subject.getCabinetString(),
				style = MaterialTheme.typography.titleMedium,
			)
			var teachersText = ""
			teachers.forEachIndexed { index, teacher ->
				teachersText += (if (index != 0) ", " else "")
				teachersText += teacher.shortName()
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
			Button(onClick = { onEditSubject(subject.subjectId) }) {
				Text(
					text = stringResource(R.string.edit_subject),
					style = MaterialTheme.typography.labelMedium
				)
			}
		}
	}
}