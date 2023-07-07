package com.kxsv.schooldiary.ui.screens.patterns

import androidx.annotation.StringRes
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.features.time_pattern.PatternWithStrokes
import com.kxsv.schooldiary.data.features.time_pattern.TimePattern
import com.kxsv.schooldiary.data.features.time_pattern.pattern_stroke.PatternStroke
import com.kxsv.schooldiary.util.ui.LoadingContent
import com.kxsv.schooldiary.util.ui.PatternSelectionTopAppBar

@Composable
fun PatternSelectionScreen(
	modifier: Modifier = Modifier,
	@StringRes userMessage: Int,
	onAddPattern: () -> Unit,
	onEditPattern: (PatternWithStrokes) -> Unit,
	onUserMessageDisplayed: () -> Unit,
	onPatternSelected: () -> Unit,
	onBack: () -> Unit,
	viewModel: PatternsViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
	Scaffold(
		topBar = { PatternSelectionTopAppBar(onBack = onBack) },
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		modifier = modifier.fillMaxSize(),
		floatingActionButton = {
			FloatingActionButton(onClick = onAddPattern) {
				Icon(Icons.Default.Add, stringResource(R.string.add_pattern))
			}
		}
	) { paddingValues ->
		val uiState = viewModel.uiState.collectAsState().value
		
		PatternsSelectionContent(
			loading = uiState.isLoading,
			patterns = uiState.patterns,
			//noPatternsLabel = 0,
			defaultPatternId = uiState.defaultPatternId,
			editPattern = onEditPattern,
			deletePattern = viewModel::deletePattern,
			setDefaultPattern = viewModel::updateDefaultPatternId,
			selectCustomPattern = { (viewModel::selectCustomPattern)(it); onPatternSelected() },
			modifier = Modifier.padding(paddingValues),
		)
		
		uiState.userMessage?.let { userMessage ->
			val snackbarText = stringResource(userMessage)
			LaunchedEffect(snackbarHostState, viewModel, userMessage, snackbarText) {
				snackbarHostState.showSnackbar(snackbarText)
				viewModel.snackbarMessageShown()
			}
		}
		
		/*		LaunchedEffect(uiState.isPatternDeleted) {
					if (uiState.isPatternDeleted) {
						viewModel.showEditResultMessage(DELETE_RESULT_OK)
					}
				}*/
		
		// Check if there's a userMessage to show to the user
		val currentOnUserMessageDisplayed by rememberUpdatedState(onUserMessageDisplayed)
		LaunchedEffect(userMessage != 0) {
			viewModel.showEditResultMessage(userMessage)
			currentOnUserMessageDisplayed()
		}
	}
}

@Composable
private fun PatternsSelectionContent(
	loading: Boolean,
	patterns: List<PatternWithStrokes>,
	defaultPatternId: Long,
	//@StringRes noPatternsLabel: Int,
	//onRefresh: () -> Unit,
	editPattern: (PatternWithStrokes) -> Unit,
	deletePattern: (Long) -> Unit,
	setDefaultPattern: (Long) -> Unit,
	selectCustomPattern: (Long) -> Unit,
	modifier: Modifier,
) {
	LoadingContent(
		loading = loading,
		isContentScrollable = true,
		empty = false,
		emptyContent = { Text(text = "empty content") },
		onRefresh = { }
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
	pattern: PatternWithStrokes,
	defaultPatternId: Long,
	editPattern: (PatternWithStrokes) -> Unit,
	deletePattern: (Long) -> Unit,
	setDefaultPattern: (Long) -> Unit,
	selectCustomPattern: (Long) -> Unit,
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
			modifier = Modifier.clickable { selectCustomPattern(pattern.timePattern.patternId) }
		)
	}
}

@Composable
private fun TimeStrokes(
	modifier: Modifier = Modifier,
	strokes: List<PatternStroke>,
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
			strokes.forEachIndexed { index, patternStroke ->
				Row {
					Text(text = (index + 1).toString())
					Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.list_item_padding)))
					Text(text = patternStroke.startTime + " - " + patternStroke.endTime)
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
				PatternWithStrokes(
					TimePattern("Default", patternId = 5),
					listOf(
						PatternStroke(startTime = "8:30", endTime = "9:15"),
						PatternStroke(startTime = "9:30", endTime = "10:15"),
						PatternStroke(startTime = "10:30", endTime = "11:15"),
						PatternStroke(startTime = "11:25", endTime = "12:10"),
						PatternStroke(startTime = "12:30", endTime = "13:15"),
						PatternStroke(startTime = "13:30", endTime = "14:20"),
						PatternStroke(startTime = "14:30", endTime = "15:15"),
					)
				),
				PatternWithStrokes(
					TimePattern("Monday"),
					listOf(
						PatternStroke(startTime = "8:30", endTime = "9:10"),
						PatternStroke(startTime = "9:25", endTime = "10:10"),
						PatternStroke(startTime = "10:20", endTime = "11:05"),
						PatternStroke(startTime = "11:25", endTime = "12:10"),
						PatternStroke(startTime = "12:30", endTime = "13:15"),
						PatternStroke(startTime = "13:30", endTime = "14:20"),
						PatternStroke(startTime = "14:30", endTime = "15:15"),
					)
				),
				PatternWithStrokes(
					TimePattern("Tuesday"),
					listOf(
						PatternStroke(startTime = "8:30", endTime = "9:10"),
						PatternStroke(startTime = "9:25", endTime = "10:10"),
						PatternStroke(startTime = "10:20", endTime = "11:05"),
						PatternStroke(startTime = "11:25", endTime = "12:10"),
						PatternStroke(startTime = "12:30", endTime = "13:15"),
						PatternStroke(startTime = "13:30", endTime = "14:20"),
						PatternStroke(startTime = "14:30", endTime = "15:15"),
					)
				),
				PatternWithStrokes(
					TimePattern("Holiday"),
					listOf(
						PatternStroke(startTime = "8:30", endTime = "9:10"),
						PatternStroke(startTime = "9:25", endTime = "10:10"),
						PatternStroke(startTime = "10:20", endTime = "11:05"),
						PatternStroke(startTime = "11:25", endTime = "12:10"),
						PatternStroke(startTime = "12:30", endTime = "13:15"),
						PatternStroke(startTime = "13:30", endTime = "14:20"),
						PatternStroke(startTime = "14:30", endTime = "15:15"),
					)
				),
				PatternWithStrokes(
					TimePattern("Monday"),
					listOf(
						PatternStroke(startTime = "8:30", endTime = "9:10"),
						PatternStroke(startTime = "9:25", endTime = "10:10"),
						PatternStroke(startTime = "10:20", endTime = "11:05"),
						PatternStroke(startTime = "11:25", endTime = "12:10"),
						PatternStroke(startTime = "12:30", endTime = "13:15"),
						PatternStroke(startTime = "13:30", endTime = "14:20"),
						PatternStroke(startTime = "14:30", endTime = "15:15"),
					)
				),
				PatternWithStrokes(
					TimePattern("Monday"),
					listOf(
						PatternStroke(startTime = "8:30", endTime = "9:10"),
						PatternStroke(startTime = "9:25", endTime = "10:10"),
						PatternStroke(startTime = "10:20", endTime = "11:05"),
						PatternStroke(startTime = "11:25", endTime = "12:10"),
						PatternStroke(startTime = "12:30", endTime = "13:15"),
						PatternStroke(startTime = "13:30", endTime = "14:20"),
						PatternStroke(startTime = "14:30", endTime = "15:15"),
					)
				),
				PatternWithStrokes(
					TimePattern("Monday"),
					listOf(
						PatternStroke(startTime = "8:30", endTime = "9:10"),
						PatternStroke(startTime = "9:25", endTime = "10:10"),
						PatternStroke(startTime = "10:20", endTime = "11:05"),
						PatternStroke(startTime = "11:25", endTime = "12:10"),
						PatternStroke(startTime = "12:30", endTime = "13:15"),
						PatternStroke(startTime = "13:30", endTime = "14:20"),
						PatternStroke(startTime = "14:30", endTime = "15:15"),
					)
				),
				PatternWithStrokes(
					TimePattern("Monday"),
					listOf(
						PatternStroke(startTime = "8:30", endTime = "9:10"),
						PatternStroke(startTime = "9:25", endTime = "10:10"),
						PatternStroke(startTime = "10:20", endTime = "11:05"),
						PatternStroke(startTime = "11:25", endTime = "12:10"),
						PatternStroke(startTime = "12:30", endTime = "13:15"),
						PatternStroke(startTime = "13:30", endTime = "14:20"),
						PatternStroke(startTime = "14:30", endTime = "15:15"),
					)
				),
				PatternWithStrokes(
					TimePattern("Tuesday"),
					listOf(
						PatternStroke(startTime = "8:30", endTime = "9:10"),
						PatternStroke(startTime = "9:25", endTime = "10:10"),
						PatternStroke(startTime = "10:20", endTime = "11:05"),
						PatternStroke(startTime = "11:25", endTime = "12:10"),
						PatternStroke(startTime = "12:30", endTime = "13:15"),
						PatternStroke(startTime = "13:30", endTime = "14:20"),
						PatternStroke(startTime = "14:30", endTime = "15:15"),
					)
				),
				PatternWithStrokes(
					TimePattern("Holiday"),
					listOf(
						PatternStroke(startTime = "8:30", endTime = "9:10"),
						PatternStroke(startTime = "9:25", endTime = "10:10"),
						PatternStroke(startTime = "10:20", endTime = "11:05"),
						PatternStroke(startTime = "11:25", endTime = "12:10"),
						PatternStroke(startTime = "12:30", endTime = "13:15"),
						PatternStroke(startTime = "13:30", endTime = "14:20"),
						PatternStroke(startTime = "14:30", endTime = "15:15"),
					)
				)
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