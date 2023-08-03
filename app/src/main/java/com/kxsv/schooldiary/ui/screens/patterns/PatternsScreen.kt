package com.kxsv.schooldiary.ui.screens.patterns

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DrawerState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.time_pattern.TimePatternEntity
import com.kxsv.schooldiary.data.local.features.time_pattern.TimePatternWithStrokes
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeEntity
import com.kxsv.schooldiary.ui.main.app_bars.topbar.PatternsTopAppBar
import com.kxsv.schooldiary.ui.main.navigation.nav_actions.PatternsScreenNavActions
import com.kxsv.schooldiary.ui.screens.destinations.AddEditPatternScreenDestination
import com.kxsv.schooldiary.util.ui.LoadingContent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.ResultRecipient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Destination
@Composable
fun PatternsScreen(
	destinationsNavigator: DestinationsNavigator,
	patternAddEditResult: ResultRecipient<AddEditPatternScreenDestination, Int>,
	drawerState: DrawerState,
	viewModel: PatternsViewModel = hiltViewModel(),
	coroutineScope: CoroutineScope,
	snackbarHostState: SnackbarHostState,
) {
	val navigator = PatternsScreenNavActions(destinationsNavigator = destinationsNavigator)
	patternAddEditResult.onNavResult { result ->
		when (result) {
			is NavResult.Canceled -> {}
			is NavResult.Value -> {
				viewModel.showEditResultMessage(result.value)
			}
		}
	}
	Scaffold(
		topBar = { PatternsTopAppBar(openDrawer = { coroutineScope.launch { drawerState.open() } }) },
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		modifier = Modifier.fillMaxSize(),
		floatingActionButton = {
			FloatingActionButton(onClick = { navigator.onAddEditPattern(null) }) {
				Icon(Icons.Default.Add, stringResource(R.string.add_pattern))
			}
		}
	) { paddingValues ->
		val uiState = viewModel.uiState.collectAsState().value
		
		PatternsContent(
			loading = uiState.isLoading,
			patterns = uiState.patterns,
			//noPatternsLabel = 0,
			defaultPatternId = uiState.defaultPatternId,
			editPattern = { patternId -> navigator.onAddEditPattern(patternId) },
			deletePattern = viewModel::deletePattern,
			setDefaultPattern = viewModel::updateDefaultPatternId,
			modifier = Modifier.padding(paddingValues),
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
private fun PatternsContent(
	loading: Boolean,
	patterns: List<TimePatternWithStrokes>,
	defaultPatternId: Long,
//@StringRes noPatternsLabel: Int,
	//onRefresh: () -> Unit,
	editPattern: (Long) -> Unit,
	deletePattern: (Long) -> Unit,
	setDefaultPattern: (Long) -> Unit,
	modifier: Modifier,
) {
	LoadingContent(
		loading = loading,
		empty = false,
		emptyContent = { Text(text = "empty content") },
		onRefresh = { }
	) {
		LazyRow(
			contentPadding = PaddingValues(horizontal = dimensionResource(R.dimen.horizontal_margin)),
			modifier = modifier
		) {
			items(patterns) { pattern ->
				PatternItem(
					pattern = pattern,
					defaultPatternId = defaultPatternId,
					editPattern = editPattern,
					deletePattern = deletePattern,
					setDefaultPattern = setDefaultPattern
				)
			}
		}
	}
}

@Composable
private fun PatternItem(
	pattern: TimePatternWithStrokes,
	defaultPatternId: Long,
	editPattern: (Long) -> Unit,
	deletePattern: (Long) -> Unit,
	setDefaultPattern: (Long) -> Unit,
) {
	Column(
		verticalArrangement = Arrangement.SpaceBetween,
		modifier = Modifier
			.padding(
				horizontal = dimensionResource(R.dimen.horizontal_margin),
				vertical = dimensionResource(R.dimen.vertical_margin)
			)
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.Start,
			modifier = Modifier
				.fillMaxWidth()
		
		) {
			Text(
				text = pattern.timePattern.name,
				style = MaterialTheme.typography.headlineSmall,
				textAlign = TextAlign.Start,
			)
			IconButton(onClick = { deletePattern(pattern.timePattern.patternId) }) {
				Icon(Icons.Default.Delete, stringResource(R.string.delete_pattern))
			}
			IconButton(onClick = { editPattern(pattern.timePattern.patternId) }) {
				Icon(Icons.Default.Edit, stringResource(R.string.edit_pattern))
			}
			IconButton(onClick = { setDefaultPattern(pattern.timePattern.patternId) }) {
				Icon(Icons.Default.Check, stringResource(R.string.set_pattern_as_default))
			}
		}
		PatternStrokes(
			strokes = pattern.strokes,
			isPatternSetAsDefault = (pattern.timePattern.patternId == defaultPatternId)
		)
	}
}

@Composable
private fun PatternStrokes(
	modifier: Modifier = Modifier,
	strokes: List<PatternStrokeEntity>,
	isPatternSetAsDefault: Boolean = false,
) {
	val borderModifier = if (isPatternSetAsDefault) {
		Modifier.border(BorderStroke(5.dp, Color.Red))
	} else {
		Modifier
	}
	Box(
		modifier = modifier
			.then(borderModifier)
			.padding(16.dp)
	) {
		Column {
			strokes.forEachIndexed { index, patternStroke ->
				Row {
					Text(text = (index + 1).toString())
					Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.list_item_padding)))
					Text(
						text = patternStroke.startTime.format(
							DateTimeFormatter.ofLocalizedTime(
								FormatStyle.SHORT
							)
						) + " - " + patternStroke.endTime.format(
							DateTimeFormatter.ofLocalizedTime(
								FormatStyle.SHORT
							)
						)
					)
				}
			}
		}
	}
}


@Preview
@Composable
private fun PatternsContentPreview() {
	Surface {
		PatternsContent(
			loading = false,
			patterns = listOf(
				TimePatternWithStrokes(
					TimePatternEntity("Default", patternId = 5),
					listOf(
						PatternStrokeEntity(
							startTime = LocalTime.of(8, 30),
							endTime = LocalTime.of(9, 15)
						),
						PatternStrokeEntity(
							startTime = LocalTime.of(9, 30),
							endTime = LocalTime.of(10, 15)
						),
						PatternStrokeEntity(
							startTime = LocalTime.of(10, 30),
							endTime = LocalTime.of(11, 15)
						),
						PatternStrokeEntity(
							startTime = LocalTime.of(11, 25),
							endTime = LocalTime.of(12, 10)
						),
						PatternStrokeEntity(
							startTime = LocalTime.of(12, 30),
							endTime = LocalTime.of(13, 15)
						),
						PatternStrokeEntity(
							startTime = LocalTime.of(13, 30),
							endTime = LocalTime.of(14, 20)
						),
						PatternStrokeEntity(
							startTime = LocalTime.of(14, 30),
							endTime = LocalTime.of(15, 15)
						),
					)
				),
				TimePatternWithStrokes(
					TimePatternEntity("Monday"),
					listOf(
						PatternStrokeEntity(
							startTime = LocalTime.of(8, 30),
							endTime = LocalTime.of(9, 15)
						),
						PatternStrokeEntity(
							startTime = LocalTime.of(9, 30),
							endTime = LocalTime.of(10, 15)
						),
						PatternStrokeEntity(
							startTime = LocalTime.of(10, 30),
							endTime = LocalTime.of(11, 15)
						),
						PatternStrokeEntity(
							startTime = LocalTime.of(11, 25),
							endTime = LocalTime.of(12, 10)
						),
						PatternStrokeEntity(
							startTime = LocalTime.of(12, 30),
							endTime = LocalTime.of(13, 15)
						),
						PatternStrokeEntity(
							startTime = LocalTime.of(13, 30),
							endTime = LocalTime.of(14, 20)
						),
						PatternStrokeEntity(
							startTime = LocalTime.of(14, 30),
							endTime = LocalTime.of(15, 15)
						),
					)
				),
				TimePatternWithStrokes(
					TimePatternEntity("Tuesday"),
					listOf(
						PatternStrokeEntity(
							startTime = LocalTime.of(8, 30),
							endTime = LocalTime.of(9, 15)
						),
						PatternStrokeEntity(
							startTime = LocalTime.of(9, 30),
							endTime = LocalTime.of(10, 15)
						),
						PatternStrokeEntity(
							startTime = LocalTime.of(10, 30),
							endTime = LocalTime.of(11, 15)
						),
						PatternStrokeEntity(
							startTime = LocalTime.of(11, 25),
							endTime = LocalTime.of(12, 10)
						),
						PatternStrokeEntity(
							startTime = LocalTime.of(12, 30),
							endTime = LocalTime.of(13, 15)
						),
						PatternStrokeEntity(
							startTime = LocalTime.of(13, 30),
							endTime = LocalTime.of(14, 20)
						),
						PatternStrokeEntity(
							startTime = LocalTime.of(14, 30),
							endTime = LocalTime.of(15, 15)
						),
					)
				),
				TimePatternWithStrokes(
					TimePatternEntity("Holiday"),
					listOf(
						PatternStrokeEntity(
							startTime = LocalTime.of(8, 30),
							endTime = LocalTime.of(9, 15)
						),
						PatternStrokeEntity(
							startTime = LocalTime.of(9, 30),
							endTime = LocalTime.of(10, 15)
						),
						PatternStrokeEntity(
							startTime = LocalTime.of(10, 30),
							endTime = LocalTime.of(11, 15)
						),
						PatternStrokeEntity(
							startTime = LocalTime.of(11, 25),
							endTime = LocalTime.of(12, 10)
						),
						PatternStrokeEntity(
							startTime = LocalTime.of(12, 30),
							endTime = LocalTime.of(13, 15)
						),
						PatternStrokeEntity(
							startTime = LocalTime.of(13, 30),
							endTime = LocalTime.of(14, 20)
						),
						PatternStrokeEntity(
							startTime = LocalTime.of(14, 30),
							endTime = LocalTime.of(15, 15)
						),
					)
				)
			),
			defaultPatternId = 5,
			editPattern = {},
			deletePattern = {},
			setDefaultPattern = {},
			modifier = Modifier
		)
	}
}