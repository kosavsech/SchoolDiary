package com.kxsv.schooldiary.ui.screens.edu_performance

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.edu_performance.EduPerformanceWithSubject
import com.kxsv.schooldiary.data.util.AppVersionState
import com.kxsv.schooldiary.data.util.EduPerformancePeriod
import com.kxsv.schooldiary.data.util.Mark
import com.kxsv.schooldiary.data.util.Mark.Companion.getStringValueFrom
import com.kxsv.schooldiary.data.util.user_preferences.PeriodType
import com.kxsv.schooldiary.ui.main.app_bars.topbar.EduPerformanceTopAppBar
import com.kxsv.schooldiary.ui.main.navigation.nav_actions.AppUpdateNavActions
import com.kxsv.schooldiary.ui.main.navigation.nav_actions.EduPerformanceScreenNavActions
import com.kxsv.schooldiary.ui.util.LoadingContent
import com.kxsv.schooldiary.ui.util.TermSelector
import com.kxsv.schooldiary.util.Utils
import com.kxsv.schooldiary.util.Utils.AppSnackbarHost
import com.kxsv.schooldiary.util.Utils.stringRoundTo
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Destination
@Composable
fun EduPerformanceScreen(
	destinationsNavigator: DestinationsNavigator,
	drawerState: DrawerState,
	coroutineScope: CoroutineScope,
	viewModel: EduPerformanceViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState,
) {
	val navigator = EduPerformanceScreenNavActions(destinationsNavigator = destinationsNavigator)
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
	Scaffold(
		snackbarHost = { AppSnackbarHost(hostState = snackbarHostState) },
		topBar = {
			EduPerformanceTopAppBar(openDrawer = { coroutineScope.launch { drawerState.open() } })
		},
		modifier = Modifier.fillMaxSize(),
	) { paddingValues ->
		val uiState = viewModel.uiState.collectAsState().value
		
		EduPerformanceContent(
			isLoading = uiState.isLoading,
			eduPerformanceList = uiState.eduPerformanceList,
			periodType = viewModel.periodType.collectAsState().value,
			onPeriodChange = { viewModel.changePeriod(it) },
			onEduPerformanceClick = { eduPerformanceId ->
				navigator.onEduPerformanceClick(eduPerformanceId)
			},
			currentEduPerformancePeriod = uiState.period,
			onRefresh = { viewModel.refresh() },
			modifier = Modifier.padding(paddingValues)
		)
	}
}

@Composable
private fun EduPerformanceContent(
	isLoading: Boolean,
	eduPerformanceList: List<EduPerformanceWithSubject>,
	periodType: PeriodType,
	onPeriodChange: (EduPerformancePeriod) -> Unit,
	onEduPerformanceClick: (String) -> Unit,
	currentEduPerformancePeriod: EduPerformancePeriod,
	onRefresh: () -> Unit,
	modifier: Modifier,
) {
	LoadingContent(
		modifier = modifier,
		loading = isLoading,
		empty = eduPerformanceList.isEmpty(),
		emptyContent = {
			Column {
				TermSelector(
					currentPeriod = currentEduPerformancePeriod,
					onPeriodChange = onPeriodChange,
					buttons = Utils.getPeriodButtons(periodType = periodType, withYear = true)
				)
				Text(text = "No subjects yet")
			}
		},
		isContentScrollable = true,
		onRefresh = onRefresh
	) {
		Column {
			TermSelector(
				currentPeriod = currentEduPerformancePeriod,
				onPeriodChange = onPeriodChange,
				buttons = Utils.getPeriodButtons(periodType = periodType, withYear = true)
			)
			LazyColumn {
				items(eduPerformanceList) { performanceWithSubject ->
					PerformanceRow(
						performanceWithSubject = performanceWithSubject,
						onEduPerformanceClick = onEduPerformanceClick
					)
				}
			}
		}
	}
}

@Composable
private fun PerformanceRow(
	performanceWithSubject: EduPerformanceWithSubject,
	onEduPerformanceClick: (String) -> Unit,
) {
	if (performanceWithSubject.eduPerformance.period != EduPerformancePeriod.YEAR) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween,
			modifier = Modifier
				.fillMaxWidth()
				.clickable { onEduPerformanceClick(performanceWithSubject.subject.subjectId) }
				.padding(
					horizontal = dimensionResource(R.dimen.horizontal_margin),
					vertical = dimensionResource(R.dimen.vertical_margin)
				)
		) {
			Text(
				text = performanceWithSubject.subject.getName(),
				style = MaterialTheme.typography.titleMedium,
			)
			Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.horizontal_margin)))
			val avgMark = if (performanceWithSubject.eduPerformance.finalMark == null &&
				performanceWithSubject.eduPerformance.marks.isNotEmpty()
			) {
				var sum = 0.0
				performanceWithSubject.eduPerformance.marks.forEach { if (it != null) sum += it.value!! }
				
				(sum / performanceWithSubject.eduPerformance.marks.size).stringRoundTo(2)
			} else if (performanceWithSubject.eduPerformance.finalMark != null) {
				getStringValueFrom(performanceWithSubject.eduPerformance.finalMark)
			} else {
				"—"
			}
			Text(
				text = avgMark,
				style = MaterialTheme.typography.titleMedium,
			)
		}
	} else {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(
					horizontal = dimensionResource(R.dimen.horizontal_margin),
					vertical = dimensionResource(R.dimen.vertical_margin)
				)
		) {
			Text(
				text = performanceWithSubject.subject.getName(),
				style = MaterialTheme.typography.titleMedium,
			)
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.SpaceBetween,
			) {
				for (i in 1..5) {
					if (i != 5) {
						YearPerformanceRowItem(
							index = i,
							grade = performanceWithSubject.eduPerformance.marks[i - 1]
						)
					} else {
						YearPerformanceRowItem(
							index = i,
							grade = performanceWithSubject.eduPerformance.finalMark
						)
					}
				}
			}
		}
	}
}

@Composable
fun YearPerformanceRowItem(
	index: Int,
	grade: Mark?,
) {
	val term = when (index) {
		1 -> "I"
		2 -> "II"
		3 -> "III"
		4 -> "IV"
		5 -> "Year"
		else -> "wtf?"
	}
	val configuration = LocalConfiguration.current
	val screenWidth = (configuration.screenWidthDp.dp - 32.dp)
	Box(
		modifier = Modifier
			.width(screenWidth / 5)
			.padding(2.dp)
			.clip(RoundedCornerShape(5.dp))
			.background(color = Color.LightGray)
			.border(
				shape = RoundedCornerShape(20.dp),
				width = 0.dp,
				color = Color.Transparent,
			)
			.wrapContentHeight(),
		contentAlignment = Alignment.Center,
	) {
		Column(
			modifier = Modifier.padding(vertical = 8.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(5.dp),
		) {
			Text(
				text = term,
				style = MaterialTheme.typography.titleMedium,
			)
			Text(
				text = getStringValueFrom(grade),
				style = MaterialTheme.typography.labelMedium,
			)
		}
	}
}