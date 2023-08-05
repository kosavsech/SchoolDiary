package com.kxsv.schooldiary.ui.screens.patterns

import android.os.Parcelable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.runtime.key
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
import com.kxsv.schooldiary.ui.main.app_bars.topbar.PatternSelectionTopAppBar
import com.kxsv.schooldiary.ui.main.navigation.nav_actions.PatternsScreenNavActions
import com.kxsv.schooldiary.ui.screens.destinations.AddEditPatternScreenDestination
import com.kxsv.schooldiary.ui.screens.patterns.add_edit_pattern.fromLocalTime
import com.kxsv.schooldiary.util.ui.LoadingContent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.ramcosta.composedestinations.result.ResultRecipient
import kotlinx.parcelize.Parcelize
import java.time.LocalTime


@Parcelize
data class PatternSelectionResult(
	val patternId: Long,
	val patternName: String,
) : Parcelable

@Destination
@Composable
fun PatternSelectionScreen(
	destinationsNavigator: DestinationsNavigator,
	resultNavigator: ResultBackNavigator<PatternSelectionResult>,
	patternAddEditResult: ResultRecipient<AddEditPatternScreenDestination, Int>,
	viewModel: PatternsViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState,
) {
	val navigator = PatternsScreenNavActions(
		destinationsNavigator = destinationsNavigator,
		resultNavigator = resultNavigator
	)
	patternAddEditResult.onNavResult { result ->
		when (result) {
			is NavResult.Canceled -> {}
			is NavResult.Value -> {
				viewModel.showEditResultMessage(result.value)
			}
		}
	}
	Scaffold(
		topBar = { PatternSelectionTopAppBar(onBack = { navigator.popBackStack() }) },
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		modifier = Modifier.fillMaxSize(),
		floatingActionButton = {
			FloatingActionButton(onClick = { navigator.onAddEditPattern(null) }) {
				Icon(Icons.Default.Add, stringResource(R.string.add_pattern))
			}
		}
	) { paddingValues ->
		val uiState = viewModel.uiState.collectAsState().value
		
		PatternsSelectionContent(
			loading = uiState.isLoading,
			patterns = uiState.patterns,
			defaultPatternId = uiState.defaultPatternId,
			editPattern = { navigator.onAddEditPattern(it.timePattern.patternId) },
			deletePattern = { viewModel.deletePattern(it) },
			setDefaultPattern = { viewModel.updateDefaultPatternId(it) },
			selectCustomPattern = {
				navigator.navigateBackWithResult(PatternSelectionResult(it.patternId, it.name))
			},
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
private fun PatternsSelectionContent(
	loading: Boolean,
	patterns: List<TimePatternWithStrokes>,
	defaultPatternId: Long,
	editPattern: (TimePatternWithStrokes) -> Unit,
	deletePattern: (Long) -> Unit,
	setDefaultPattern: (Long) -> Unit,
	selectCustomPattern: (TimePatternEntity) -> Unit,
	modifier: Modifier,
) {
	LoadingContent(
		loading = loading,
		empty = false,
		emptyContent = { Text(text = "empty content") },
		isContentScrollable = true,
	) {
		LazyVerticalGrid(
			columns = GridCells.Adaptive(minSize = 128.dp),
			contentPadding = PaddingValues(horizontal = dimensionResource(R.dimen.horizontal_margin)),
			modifier = modifier
		) {
			items(patterns) { pattern ->
				PatternItem(
					pattern = pattern,
					defaultPatternId = defaultPatternId,
					editPattern = editPattern,
					deletePattern = deletePattern,
					setDefaultPattern = setDefaultPattern,
					selectCustomPattern = selectCustomPattern
				)
			}
		}
	}
}

@Composable
private fun PatternItem(
	pattern: TimePatternWithStrokes,
	defaultPatternId: Long,
	editPattern: (TimePatternWithStrokes) -> Unit,
	deletePattern: (Long) -> Unit,
	setDefaultPattern: (Long) -> Unit,
	selectCustomPattern: (TimePatternEntity) -> Unit,
) {
	
	Column(
		verticalArrangement = Arrangement.SpaceBetween,
		modifier = Modifier
			.padding(
				vertical = dimensionResource(R.dimen.vertical_margin)
			)
	) {
		Row {
			IconButton(onClick = { deletePattern(pattern.timePattern.patternId) }) {
				Icon(Icons.Default.Delete, stringResource(R.string.delete_pattern))
			}
			IconButton(onClick = { editPattern(pattern) }) {
				Icon(Icons.Default.Edit, stringResource(R.string.edit_pattern))
			}
			IconButton(onClick = { setDefaultPattern(pattern.timePattern.patternId) }) {
				Icon(Icons.Default.Check, stringResource(R.string.set_pattern_as_default))
			}
		}
		Text(
			text = pattern.timePattern.name,
			style = MaterialTheme.typography.headlineSmall,
			textAlign = TextAlign.Start,
		)
		
		TimeStrokes(
			strokes = pattern.strokes,
			isPatternSetAsDefault = (pattern.timePattern.patternId == defaultPatternId),
			modifier = Modifier.clickable { selectCustomPattern(pattern.timePattern) }
		)
	}
}

@Composable
private fun TimeStrokes(
	modifier: Modifier = Modifier,
	strokes: List<PatternStrokeEntity>,
	isPatternSetAsDefault: Boolean = false,
) {
	val borderModifier = if (isPatternSetAsDefault) {
		Modifier.border(BorderStroke(2.dp, Color.Red))
	} else {
		Modifier
	}
	Box(
		modifier = modifier
			.then(borderModifier)
			.padding(4.dp)
	) {
		Column {
			strokes.forEach { stroke ->
				key(stroke) {
					Row {
						Text(text = (stroke.index + 1).toString())
						Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.list_item_padding)))
						Text(
							text = fromLocalTime(stroke.startTime) + " - " + fromLocalTime(stroke.endTime),
						)
					}
				}
			}
		}
	}
}


@Preview
@Composable
private fun PatternsSelectionPreview() {
	Surface {
		PatternsSelectionContent(
			loading = false,
			patterns = listOf(
				TimePatternWithStrokes(
					TimePatternEntity("Default", patternId = 5),
					listOf(
						PatternStrokeEntity(
							startTime = LocalTime.of(8, 30),
							endTime = LocalTime.of(9, 15),
							index = 80
						),
						PatternStrokeEntity(
							startTime = LocalTime.of(9, 30),
							endTime = LocalTime.of(10, 15),
							index = 1
						),
						PatternStrokeEntity(
							startTime = LocalTime.of(10, 30),
							endTime = LocalTime.of(11, 15),
							index = 2
						),
						PatternStrokeEntity(
							startTime = LocalTime.of(11, 25),
							endTime = LocalTime.of(12, 10),
							index = 3
						),
						PatternStrokeEntity(
							startTime = LocalTime.of(12, 30),
							endTime = LocalTime.of(13, 15),
							index = 4
						),
						PatternStrokeEntity(
							startTime = LocalTime.of(13, 30),
							endTime = LocalTime.of(14, 20),
							index = 5
						),
						PatternStrokeEntity(
							startTime = LocalTime.of(14, 30),
							endTime = LocalTime.of(15, 15),
							index = 6
						),
					)
				),
			),
			defaultPatternId = 5,
			editPattern = {},
			deletePattern = {},
			setDefaultPattern = {},
			selectCustomPattern = {},
			modifier = Modifier
		)
	}
}