package com.kxsv.schooldiary.ui.screens.edu_performance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.edu_performance.EduPerformanceWithSubject
import com.kxsv.schooldiary.ui.main.topbar.EduPerformanceTopAppBar
import com.kxsv.schooldiary.util.ui.EduPerformancePeriod
import com.kxsv.schooldiary.util.ui.LoadingContent

@Composable
fun EduPerformanceScreen(
//	onSubjectClick: (Long) -> Unit,
	openDrawer: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: EduPerformanceViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
	Scaffold(
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		topBar = {
			EduPerformanceTopAppBar(openDrawer = openDrawer)
		},
		modifier = modifier.fillMaxSize(),
	) { paddingValues ->
		val uiState = viewModel.uiState.collectAsState().value
		
		EduPerformanceContent(
			loading = uiState.isLoading,
			eduPerformanceList = uiState.eduPerformanceList,
			onPeriodChange = { viewModel.changePeriod(it) },
			currentEduPerformancePeriod = uiState.period,
			onRefresh = { viewModel.refresh() },
			modifier = Modifier.padding(paddingValues)
		)
	}
}

@Composable
private fun EduPerformanceContent(
	loading: Boolean,
	eduPerformanceList: List<EduPerformanceWithSubject>,
	onPeriodChange: (EduPerformancePeriod) -> Unit,
	currentEduPerformancePeriod: EduPerformancePeriod,
	//	onSubjectClick: (Long) -> Unit,
	onRefresh: () -> Unit,
	modifier: Modifier,
) {
	data class PeriodButton(
		val text: String,
		val callbackPeriod: EduPerformancePeriod,
	)
	
	val buttons = listOf(
		PeriodButton("First term", EduPerformancePeriod.FIRST_TERM),
		PeriodButton("Second term", EduPerformancePeriod.SECOND_TERM),
		PeriodButton("Third term", EduPerformancePeriod.THIRD_TERM),
		PeriodButton("Fourth term", EduPerformancePeriod.FOURTH_TERM),
		PeriodButton("Year", EduPerformancePeriod.YEAR_PERIOD),
	)
	LoadingContent(
		loading = loading,
		isContentScrollable = true,
		empty = eduPerformanceList.isEmpty(),
		emptyContent = {
			Column {
				LazyRow(
					modifier = modifier
						.padding(vertical = dimensionResource(R.dimen.list_item_padding)),
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
				Text(text = "No subjects for yet")
			}
		},
		onRefresh = onRefresh
	) {
		Column {
			LazyRow(
				modifier = modifier
					.padding(vertical = dimensionResource(R.dimen.list_item_padding)),
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
			LazyColumn {
				items(eduPerformanceList) { performanceWithSubject ->
					PerformanceRow(performanceWithSubject)
				}
			}
		}
	}
}

@Composable
private fun PerformanceRow(
	performanceWithSubject: EduPerformanceWithSubject,
//	onGradeClick: (GradeEntity) -> Unit,
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween,
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
		Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.horizontal_margin)))
		performanceWithSubject.eduPerformance.marks.forEach {
			Text(text = it?.getValue() ?: "—")
		}
	}
}