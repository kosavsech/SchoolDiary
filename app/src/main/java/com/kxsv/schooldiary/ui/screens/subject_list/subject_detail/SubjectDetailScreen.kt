package com.kxsv.schooldiary.ui.screens.subject_list.subject_detail

import android.graphics.Typeface
import android.text.TextUtils
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
import com.kxsv.schooldiary.data.util.user_preferences.PeriodType
import com.kxsv.schooldiary.ui.main.app_bars.topbar.SubjectDetailTopAppBar
import com.kxsv.schooldiary.ui.main.navigation.nav_actions.SubjectDetailScreenNavActions
import com.kxsv.schooldiary.ui.screens.destinations.AddEditSubjectScreenDestination
import com.kxsv.schooldiary.ui.util.AppSnackbarHost
import com.kxsv.schooldiary.ui.util.LoadingContent
import com.kxsv.schooldiary.ui.util.TermSelector
import com.kxsv.schooldiary.ui.util.displayText
import com.kxsv.schooldiary.ui.util.getPeriodButtons
import com.kxsv.schooldiary.util.Extensions.roundTo
import com.kxsv.schooldiary.util.Extensions.stringRoundTo
import com.kxsv.schooldiary.util.Utils.calculateMarksUntilTarget
import com.kxsv.schooldiary.util.Utils.calculateRealizableBadMarks
import com.kxsv.schooldiary.util.Utils.getRuinBoundForMark
import com.kxsv.ychart_mod.common.extensions.isNotNull
import com.kxsv.ychart_mod.common.model.PlotType
import com.kxsv.ychart_mod.ui.piechart.charts.PieChart
import com.kxsv.ychart_mod.ui.piechart.models.PieChartConfig
import com.kxsv.ychart_mod.ui.piechart.models.PieChartData
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.ramcosta.composedestinations.result.ResultRecipient
import com.stephenvinouze.segmentedprogressbar.SegmentedProgressBar
import com.stephenvinouze.segmentedprogressbar.models.SegmentColor
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.input
import com.vanpra.composematerialdialogs.message
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.vanpra.composematerialdialogs.title
import java.time.DayOfWeek
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
	subjectAddEditResult: ResultRecipient<AddEditSubjectScreenDestination, Int>,
	destinationsNavigator: DestinationsNavigator,
	viewModel: SubjectDetailViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState,
) {
	val navigator = SubjectDetailScreenNavActions(
		destinationsNavigator = destinationsNavigator,
		resultBackNavigator = resultNavigator
	)
	val uiState = viewModel.uiState.collectAsState().value
	subjectAddEditResult.onNavResult { result ->
		when (result) {
			is NavResult.Canceled -> {}
			is NavResult.Value -> {
				viewModel.showEditResultMessage(result.value)
			}
		}
	}
	
	Scaffold(
		snackbarHost = { AppSnackbarHost(hostState = snackbarHostState) },
		modifier = Modifier.fillMaxSize(),
		topBar = {
			SubjectDetailTopAppBar(
				title = uiState.subjectWithTeachers?.subject?.getName() ?: "",
				onBack = { navigator.popBackStack() }
			) {
				viewModel.deleteSubject()
//				navigator.backWithResult(DELETE_RESULT_OK)
			}
		},
	) { paddingValues ->
		val targetMarkDialogState = rememberMaterialDialogState(false)
		val lowerBoundMarkDialogState = rememberMaterialDialogState(false)
		
		val changeBadMarkCalcType = remember<(Boolean) -> Unit> {
			{ viewModel.changeBadMarkCalcType(it) }
		}
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
			lowerBoundMarkDialogState = lowerBoundMarkDialogState,
			isLoading = uiState.isLoading,
			currentPeriod = uiState.period,
			periodType = viewModel.periodType.collectAsState().value,
			subjectWithTeachers = uiState.subjectWithTeachers,
			subjectsDaysOfWeek = uiState.subjectsDaysOfWeek,
			classesOnDayOfWeek = uiState.classesOnDayOfWeek,
			lessonsLeft = uiState.lessonsLeft,
			targetMark = uiState.targetMark,
			lowerBoundMark = uiState.lowerBoundMark,
			roundRule = uiState.roundRule,
			eduPerformance = uiState.eduPerformance,
			grades = uiState.grades,
			isCalculatingUntilLowerBound = uiState.isCalculatingUntilLowerBound,
			changeBadMarkCalcType = changeBadMarkCalcType,
			onPeriodChange = changePeriod,
			onGradeClick = showGradeDetails,
			onEditSubject = editSubject,
			onRefresh = refresh
		)
		
		val saveTargetMark = remember<(Double) -> Unit> {
			{ viewModel.saveTargetMark(it) }
		}
		MarkDialog(
			markDialogState = targetMarkDialogState,
			prefillMark = uiState.targetMark,
			onInputSave = saveTargetMark,
			inputLabelRes = R.string.label_target_mark,
			dialogTitleRes = R.string.enter_target_mark_dialog_title,
		)
		
		val saveLowerBoundMark = remember<(Double) -> Unit> {
			{ viewModel.saveLowerBoundMark(it) }
		}
		// complete this shit, add new button to trigger this shit, add switcher
		// to change calculation mode, calculate basing on mode and lower or not lower bound
		MarkDialog(
			markDialogState = lowerBoundMarkDialogState,
			prefillMark = uiState.lowerBoundMark,
			onInputSave = saveLowerBoundMark,
			inputLabelRes = R.string.label_lower_bound_mark,
			dialogTitleRes = R.string.enter_lower_bound_mark_dialog_title,
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
	lowerBoundMarkDialogState: MaterialDialogState,
	isLoading: Boolean,
	currentPeriod: EduPerformancePeriod,
	periodType: PeriodType,
	subjectWithTeachers: SubjectWithTeachers?,
	subjectsDaysOfWeek: List<DayOfWeek>?,
	classesOnDayOfWeek: Map<DayOfWeek, Int>?,
	lessonsLeft: Int?,
	targetMark: Double,
	lowerBoundMark: Double,
	roundRule: Double,
	eduPerformance: EduPerformanceEntity?,
	grades: List<GradeEntity>,
	isCalculatingUntilLowerBound: Boolean,
	changeBadMarkCalcType: (Boolean) -> Unit,
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
		isLoading = isLoading,
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
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = dimensionResource(R.dimen.horizontal_margin)),
					subject = subjectWithTeachers.subject,
					teachers = subjectWithTeachers.teachers.toList(),
					subjectsDaysOfWeek = subjectsDaysOfWeek ?: emptyList(),
					classesOnDayOfWeek = classesOnDayOfWeek ?: emptyMap(),
					lessonsLeft = lessonsLeft,
					onEditSubject = onEditSubject
				)
				Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding)))
			}
			
			TermSelector(
				currentPeriod = currentPeriod,
				onPeriodChange = onPeriodChange,
				buttons = getPeriodButtons(periodType = periodType, withYear = false)
			)
			Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding)))
			
			// TODO: make noContent cover
			if (eduPerformance != null) {
				if (eduPerformance.marks.isNotEmpty()) {
					TargetGradeProgress(
						targetMarkDialogState = targetMarkDialogState,
						lowerBoundMarkDialogState = lowerBoundMarkDialogState,
						performanceEntity = eduPerformance,
						targetMark = targetMark,
						lowerBoundMark = lowerBoundMark,
						roundRule = roundRule,
						isCalculatingUntilLowerBound = isCalculatingUntilLowerBound,
						changeBadMarkCalcType = changeBadMarkCalcType
					)
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
						sumUnit = "unit",
						backgroundColor = MaterialTheme.colorScheme.background,
						isClickOnSliceEnabled = false
					)
					
					Column(
						modifier = Modifier
							.height(300.dp)
//						.padding(horizontal = dimensionResource(R.dimen.horizontal_margin))
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
}

@Composable
private fun TargetGradeProgress(
	targetMarkDialogState: MaterialDialogState,
	lowerBoundMarkDialogState: MaterialDialogState,
	performanceEntity: EduPerformanceEntity,
	targetMark: Double,
	lowerBoundMark: Double,
	roundRule: Double,
	isCalculatingUntilLowerBound: Boolean,
	changeBadMarkCalcType: (Boolean) -> Unit,
) {
	ElevatedCard(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = dimensionResource(R.dimen.horizontal_margin)),
	) {
		val valueSum = remember(performanceEntity.marks) {
			performanceEntity.marks.sumOf { it?.value ?: 0 }.toDouble()
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
					roundRule = roundRule,
					target = targetMark,
					avgMark = avgMark,
					sum = performanceEntity.marks.size,
					valueSum = valueSum
				)
			}
		val lowerBound =
			remember(avgMark, roundRule, isCalculatingUntilLowerBound, lowerBoundMark) {
				return@remember if (isCalculatingUntilLowerBound) {
					lowerBoundMark
				} else {
					getRuinBoundForMark(avgMark, roundRule)
				}
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
					style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
					modifier = Modifier.fillMaxWidth(),
					textAlign = TextAlign.Center
				)
				Spacer(modifier = Modifier.height(dimensionResource(R.dimen.vertical_margin)))
			} else {
				Text(
					text = "To achieve target:",
					style = MaterialTheme.typography.titleMedium,
				)
				marksUntilTarget.forEach { (strategy, count, outcome) ->
					key(strategy, count, outcome) {
						val (mark1, mark2) = strategy
						val text = "$mark1 x ${count.component1()} times" +
								if (mark2 != null) {
									" with $mark2 x ${count.component2()} times"
								} else {
									""
								}
						Text(
							text = "$text ($outcome)",
							style = MaterialTheme.typography.titleMedium,
							modifier = Modifier.padding(
								vertical = dimensionResource(R.dimen.list_item_padding),
								horizontal = dimensionResource(R.dimen.horizontal_margin)
							)
						)
					}
				}
			}
			val badMarkCalculationText = if (isCalculatingUntilLowerBound) {
				"To not go below ${lowerBound.stringRoundTo(2)}:"
			} else {
				"To not ruin current mark no more than:"
			}
			Text(
				text = badMarkCalculationText,
				style = MaterialTheme.typography.titleMedium,
				modifier = Modifier.padding(top = dimensionResource(R.dimen.list_item_padding))
			)
			
			if (realizableBadMarks.isNotEmpty()) {
				realizableBadMarks.forEach { (strategy, count, outcome) ->
					key(strategy, count, outcome) {
						val (mark1, mark2) = strategy
						val text = "$mark1 x ${count.component1()} times" +
								if (mark2 != null) {
									" with $mark2 x ${count.component2()} times"
								} else {
									""
								}
						Text(
							text = "$text ($outcome)",
							style = MaterialTheme.typography.titleMedium,
							modifier = Modifier.padding(
								vertical = dimensionResource(R.dimen.list_item_padding),
								horizontal = dimensionResource(R.dimen.horizontal_margin)
							)
						)
					}
				}
			} else {
				Text(
					text = "You cannot afford any others marks",
					style = MaterialTheme.typography.titleMedium,
					modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding))
				)
			}
			Row(
				horizontalArrangement = Arrangement.SpaceBetween,
				modifier = Modifier.fillMaxWidth()
			) {
				Button(onClick = { targetMarkDialogState.show() }) {
					Text(
						text = stringResource(R.string.edit_target_mark),
						style = MaterialTheme.typography.labelMedium
					)
				}
				Button(onClick = { lowerBoundMarkDialogState.show() }) {
					Text(
						text = stringResource(R.string.edit_lower_bound_mark),
						style = MaterialTheme.typography.labelMedium
					)
				}
			}
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.height(56.dp)
					.clip(MaterialTheme.shapes.extraLarge)
					.clickable(
						interactionSource = MutableInteractionSource(),
						indication = rememberRipple(
							bounded = false,
							radius = Dp.Unspecified,
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					) {
						changeBadMarkCalcType.invoke(!(isCalculatingUntilLowerBound))
					},
				verticalAlignment = Alignment.CenterVertically,
			) {
				Row(
					modifier = Modifier
						.fillMaxWidth(),
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.SpaceBetween
				) {
					Text(
						text = stringResource(R.string.current_mark_save),
						style = MaterialTheme.typography.titleMedium,
						modifier = Modifier.weight(0.4f, false)
					)
//					Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.list_item_padding)))
					Switch(
						checked = isCalculatingUntilLowerBound,
						onCheckedChange = changeBadMarkCalcType,
						modifier = Modifier.sizeIn(maxHeight = (30.6).dp)
					)
					Text(
						text = stringResource(R.string.label_lower_bound_mark),
						style = MaterialTheme.typography.titleMedium,
						modifier = Modifier.weight(0.4f)
					)
				}
			}
		}
	}
}

@Composable
private fun MarkDialog(
	markDialogState: MaterialDialogState,
	prefillMark: Double,
	onInputSave: (Double) -> Unit,
	@StringRes inputLabelRes: Int,
	@StringRes dialogTitleRes: Int,
	@StringRes placeholderRes: Int = R.string.place_holder_mark,
	@StringRes errorRes: Int = R.string.error_msg_mark_input,
	@StringRes descriptionRes: Int? = null,
) {
	val focusManager = LocalFocusManager.current
	MaterialDialog(
		dialogState = markDialogState,
		buttons = {
			positiveButton(res = R.string.btn_save)
			negativeButton(res = R.string.btn_cancel)
		},
	) {
		title(res = dialogTitleRes)
		if (descriptionRes.isNotNull()) {
			message(res = descriptionRes)
		}
		input(
			label = stringResource(id = inputLabelRes),
			prefill = prefillMark.stringRoundTo(2),
			placeholder = stringResource(id = placeholderRes),
			isTextValid = {
				it.toDoubleOrNull() != null && (it.toDouble() > 2.00 && it.toDouble() < 5.00)
			},
			errorMessage = stringResource(id = errorRes),
			onInput = { onInputSave(it.toDouble().roundTo(2)) },
			waitForPositiveButton = true,
			keyboardOptions = KeyboardOptions(
				imeAction = ImeAction.Done,
				autoCorrect = false,
				capitalization = KeyboardCapitalization.None,
				keyboardType = KeyboardType.Decimal
			),
			keyboardActions = KeyboardActions(
				onDone = { focusManager.clearFocus() }
			)
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
	
	if (grades.isNotEmpty()) {
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
	} else {
		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.fillMaxSize()
				.padding(
					horizontal = dimensionResource(R.dimen.horizontal_margin),
					vertical = dimensionResource(R.dimen.vertical_margin)
				)
		) {
			Text(
				text = stringResource(id = R.string.no_grades_yet),
				style = MaterialTheme.typography.displaySmall,
				modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding))
			)
		}
	}
	Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.vertical_margin)))
	
}

@Composable
private fun SubjectInfo(
	modifier: Modifier = Modifier,
	subject: SubjectEntity,
	teachers: List<TeacherEntity>,
	subjectsDaysOfWeek: List<DayOfWeek>,
	classesOnDayOfWeek: Map<DayOfWeek, Int>,
	lessonsLeft: Int?,
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
			if (teachersText.isNotBlank()) {
				Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding)))
				Text(
					text = teachersText,
					style = MaterialTheme.typography.titleMedium,
				)
			}
			if (lessonsLeft != null) {
				Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding)))
				Text(
					text = stringResource(R.string.lessons_left, lessonsLeft),
					style = MaterialTheme.typography.titleMedium,
				)
				Text(
					text = stringResource(R.string.lessons_left_desc),
					style = MaterialTheme.typography.labelMedium,
					color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
				)
			}
			if (subjectsDaysOfWeek.isNotEmpty()) {
				Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding)))
				Row {
					DayOfWeek.values().forEach { dayOfWeek ->
						if (dayOfWeek == DayOfWeek.SUNDAY) return@forEach
						
						Day(
							dayOfWeek = dayOfWeek,
							isSelected = dayOfWeek in subjectsDaysOfWeek,
							classesAmount = classesOnDayOfWeek.getOrDefault(dayOfWeek, 0)
						)
					}
					
				}
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

@Composable
private fun Day(
	dayOfWeek: DayOfWeek,
	isSelected: Boolean,
	classesAmount: Int,
) {
	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp - 64.dp
	val backgroundColor = if (isSelected) {
		MaterialTheme.colorScheme.secondary
	} else MaterialTheme.colorScheme.surfaceColorAtElevation(40.dp)
	Box(
		modifier = Modifier
			// If paged scrolling is disabled (calendarScrollPaged = false),
			// you must set the day width on the WeekCalendar!
			.width(screenWidth / 6)
			.padding(2.dp)
			.clip(RoundedCornerShape(20.dp))
			.background(backgroundColor)
			.wrapContentHeight(),
		contentAlignment = Alignment.Center,
	) {
		Column(
			modifier = Modifier.padding(vertical = 8.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(5.dp),
		) {
			val textColor = if (isSelected) {
				MaterialTheme.colorScheme.onSecondary
			} else MaterialTheme.colorScheme.onSurface
			Text(
				text = dayOfWeek.displayText(),
				style = MaterialTheme.typography.labelMedium,
				color = textColor
			)
			Text(
				text = "$classesAmount h",
				style = MaterialTheme.typography.labelMedium,
				color = if (classesAmount != 0) textColor else Color.Transparent
			)
			
		}
	}
}

@Preview
@Composable
fun TargetGradeProgressPreview() {
	TargetGradeProgress(
		targetMarkDialogState = rememberMaterialDialogState(false),
		lowerBoundMarkDialogState = rememberMaterialDialogState(false),
		performanceEntity = EduPerformanceEntity(
			marks = listOf(
				Mark.FOUR, Mark.FOUR, Mark.FOUR,
				Mark.FOUR, Mark.FOUR, Mark.FOUR,
				Mark.FIVE, Mark.THREE,
			),
			finalMark = null,
			eduPerformanceId = "",
			examMark = null,
			period = EduPerformancePeriod.FIRST
		),
		targetMark = 3.6,
		lowerBoundMark = 2.6,
		roundRule = 0.6,
		isCalculatingUntilLowerBound = false,
		changeBadMarkCalcType = {}
	)
}