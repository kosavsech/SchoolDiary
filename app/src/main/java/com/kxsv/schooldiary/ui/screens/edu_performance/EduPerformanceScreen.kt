package com.kxsv.schooldiary.ui.screens.edu_performance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.edu_performance.EduPerformanceWithSubject
import com.kxsv.schooldiary.ui.main.topbar.EduPerformanceTopAppBar
import com.kxsv.schooldiary.util.Mark
import com.kxsv.schooldiary.util.Mark.Companion.getStringValueFrom
import com.kxsv.schooldiary.util.Utils.stringRoundTo
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
				Text(text = "No subjects yet")
			}
		},
		onRefresh = onRefresh
	) {
		Column {
			LazyRow(
				modifier = modifier
					.padding(vertical = dimensionResource(R.dimen.list_item_padding)),
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
	if (performanceWithSubject.eduPerformance.period != EduPerformancePeriod.YEAR_PERIOD) {
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
	grade: Mark?
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