package com.kxsv.schooldiary.ui.screens.patterns_list

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.kxsv.schooldiary.data.features.time_pattern.PatternWithStrokes
import com.kxsv.schooldiary.data.features.time_pattern.TimePattern
import com.kxsv.schooldiary.data.features.time_pattern.pattern_stroke.PatternStroke
import com.kxsv.schooldiary.util.ui.PatternsTopAppBar

@Composable
fun PatternsScreen(
    @StringRes userMessage: Int,
    onAddPattern: () -> Unit,
    onPatternClick: (PatternWithStrokes) -> Unit,
    onDeletePattern: () -> Unit,
    onUserMessageDisplayed: () -> Unit,
    openDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PatternsViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    androidx.compose.material3.Scaffold(
        topBar = { PatternsTopAppBar(openDrawer = openDrawer) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPattern) {
                Icon(Icons.Default.Add, stringResource(R.string.add_pattern))
            }
        }
    ) { paddingValues ->
        val uiState = viewModel.uiState.collectAsState().value

        PatternsContent(
            loading = uiState.isLoading,
            patterns = uiState.patterns,
            //noPatternsLabel = 0,
            onPatternClick = onPatternClick,
            deletePattern = viewModel::deletePattern,
            modifier = Modifier.padding(paddingValues),
        )

        uiState.userMessage?.let { userMessage ->
            val snackbarText = stringResource(userMessage)
            LaunchedEffect(snackbarHostState, viewModel, userMessage, snackbarText) {
                snackbarHostState.showSnackbar(snackbarText)
                viewModel.snackbarMessageShown()
            }
        }

        LaunchedEffect(uiState.isPatternDeleted) {
            if (uiState.isPatternDeleted) {
                onDeletePattern()
            }
        }

        // Check if there's a userMessage to show to the user
        val currentOnUserMessageDisplayed by rememberUpdatedState(onUserMessageDisplayed)
        LaunchedEffect(userMessage) {
            if (userMessage != 0) {
                viewModel.showEditResultMessage(userMessage)
                currentOnUserMessageDisplayed()
            }
        }
    }
}

@Composable
private fun PatternsContent(
    loading: Boolean,
    patterns: List<PatternWithStrokes>,
    //@StringRes noPatternsLabel: Int,
    //onRefresh: () -> Unit,
    onPatternClick: (PatternWithStrokes) -> Unit,
    deletePattern: (Long) -> Unit,
    modifier: Modifier,
) {
    /*LoadingContent(
        loading = loading,
        empty = patterns.isEmpty() && !loading,
        emptyContent = { PatternsEmptyContent(noPatternsLabel, modifier) },
        onRefresh = onRefresh
    ) */
    LazyRow(
        contentPadding = PaddingValues(horizontal = dimensionResource(R.dimen.horizontal_margin)),
        modifier = modifier
    ) {
        items(patterns) { pattern ->
            PatternItem(
                pattern = pattern,
                onPatternClick = onPatternClick,
                deletePattern = deletePattern
            )
        }
    }
}

@Composable
private fun PatternItem(
    pattern: PatternWithStrokes,
    onPatternClick: (PatternWithStrokes) -> Unit,
    deletePattern: (Long) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(
                horizontal = dimensionResource(R.dimen.horizontal_margin),
                vertical = dimensionResource(R.dimen.vertical_margin)
            )
            .clickable { onPatternClick(pattern) }
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
        }
        PatternStrokes(pattern.strokes)
    }
}

@Composable
private fun PatternStrokes(
    strokes: List<PatternStroke>,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .border(BorderStroke(5.dp, Color.Red))
            .padding(16.dp)
    ) {
        LazyColumn {
            items(strokes) {
                Row {
                    Text(text = it.startTime + " - " + it.endTime)
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
                PatternWithStrokes(
                    TimePattern("Default"),
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
                )
            ),
            onPatternClick = {},
            deletePattern = {},
            modifier = Modifier
        )
    }
}