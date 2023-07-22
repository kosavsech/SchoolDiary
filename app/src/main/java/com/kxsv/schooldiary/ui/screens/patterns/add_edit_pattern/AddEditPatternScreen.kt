package com.kxsv.schooldiary.ui.screens.patterns.add_edit_pattern

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeEntity
import com.kxsv.schooldiary.ui.main.topbar.AddEditPatternTopAppBar
import com.kxsv.schooldiary.util.ui.LoadingContent
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.Job
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun AddEditTimePatternScreen(
	@StringRes topBarTitle: Int,
	onPatternUpdate: () -> Unit,
	onBack: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: AddEditPatternViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
	Scaffold(
		modifier = modifier.fillMaxSize(),
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		topBar = { AddEditPatternTopAppBar(topBarTitle, onBack) },
		floatingActionButton = {
			Row {
				FloatingActionButton(onClick = viewModel::showStrokeDialog) {
					Icon(Icons.Filled.Add, stringResource(R.string.add_pattern_stroke))
				}
				Spacer(Modifier.padding(horizontal = dimensionResource(R.dimen.horizontal_margin)))
				FloatingActionButton(onClick = viewModel::savePattern) {
					Icon(Icons.Filled.Done, stringResource(R.string.save_pattern))
				}
			}
		}
	) { paddingValues ->
		val uiState = viewModel.uiState.collectAsState().value
		
		AddEditPatternContent(
			loading = uiState.isLoading,
			name = uiState.name,
			strokes = uiState.strokes,
			onNameChanged = viewModel::updateName,
			onStrokeClick = viewModel::onStrokeClick,
			onStrokeDelete = viewModel::deleteStroke,
			modifier = Modifier.padding(paddingValues)
		)
		
		if (uiState.isStrokeDialogShown) {
			AddEditStrokeDialog(
				startTime = uiState.startTime,
				endTime = uiState.endTime,
				hideStrokeDialog = viewModel::hideStrokeDialog,
				saveStroke = viewModel::saveStroke,
				updateStartTime = viewModel::updateStartTime,
				updateEndTime = viewModel::updateEndTime,
			)
		}
		// Check if the pattern is saved and call onPatternUpdate event
		LaunchedEffect(uiState.isPatternSaved) {
			if (uiState.isPatternSaved) {
				onPatternUpdate()
			}
		}
		
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
private fun AddEditPatternContent(
	loading: Boolean,
	name: String,
	strokes: List<PatternStrokeEntity>,
	onNameChanged: (String) -> Unit,
	onStrokeClick: (PatternStrokeEntity) -> Unit,
	onStrokeDelete: (PatternStrokeEntity) -> Job,
	modifier: Modifier = Modifier,
) {
	LoadingContent(
		loading,
		empty = false,
		emptyContent = { Text(text = "Empty") },
		onRefresh = {}) {
		Column(
			modifier
				.fillMaxWidth()
				.padding(dimensionResource(id = R.dimen.horizontal_margin))
		) {
			val textFieldColors = TextFieldDefaults.outlinedTextFieldColors(
				focusedBorderColor = Color.Transparent,
				unfocusedBorderColor = Color.Transparent,
				cursorColor = MaterialTheme.colors.secondary.copy(alpha = ContentAlpha.high)
			)
			OutlinedTextField(
				value = name,
				modifier = Modifier.fillMaxWidth(),
				onValueChange = onNameChanged,
				placeholder = {
					Text(
						text = stringResource(id = R.string.full_name_hint),
						style = MaterialTheme.typography.h6
					)
				},
				textStyle = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
				maxLines = 1,
				colors = textFieldColors
			)
			strokes.forEach { stroke ->
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.clickable { onStrokeClick(stroke) },
					horizontalArrangement = Arrangement.Center,
					verticalAlignment = Alignment.CenterVertically
				) {
					Text(
						text = stroke.startTime
							.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)) +
								" - "
								+ stroke.endTime.format(
							DateTimeFormatter.ofLocalizedTime(
								FormatStyle.SHORT
							)
						),
						fontSize = 20.sp
					)
					IconButton(onClick = { onStrokeDelete(stroke) }) {
						Icon(Icons.Default.Delete, stringResource(R.string.delete_pattern_stroke))
					}
				}
			}
		}
	}
}

// LocalTime -> String
private fun fromLocalTime(localTime: LocalTime): String {
	return localTime.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH))
}

@Composable
private fun AddEditStrokeDialog(
	startTime: LocalTime,
	endTime: LocalTime,
	hideStrokeDialog: () -> Unit,
	saveStroke: () -> Unit,
	updateStartTime: (LocalTime) -> Unit,
	updateEndTime: (LocalTime) -> Unit,
	dialogState: MaterialDialogState = rememberMaterialDialogState(true),
	startTimeDialogState: MaterialDialogState = rememberMaterialDialogState(false),
	endTimeDialogState: MaterialDialogState = rememberMaterialDialogState(false),
) {
	MaterialDialog(
		dialogState = dialogState,
		onCloseRequest = { hideStrokeDialog() },
		buttons = {
			positiveButton(text = stringResource(R.string.btn_save), onClick = { saveStroke() })
			negativeButton(
				text = stringResource(R.string.btn_cancel),
				onClick = { hideStrokeDialog() })
		}
	) {
		Column(
			modifier = Modifier.padding(dimensionResource(R.dimen.vertical_margin)),
			verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_item_padding))
		) {
			Row {
				Icon(Icons.Default.Schedule, stringResource(R.string.set_start_time))
				Spacer(modifier = Modifier.width(dimensionResource(R.dimen.horizontal_margin)))
				Text(
					text = fromLocalTime(startTime),
					modifier = Modifier.clickable { startTimeDialogState.show() })
			}
			Row {
				Icon(Icons.Default.Schedule, stringResource(R.string.set_end_time))
				Spacer(modifier = Modifier.width(dimensionResource(R.dimen.horizontal_margin)))
				Text(
					text = fromLocalTime(endTime),
					modifier = Modifier.clickable { endTimeDialogState.show() })
			}
		}
	}
	TimePickerDialog(
		initialTime = startTime,
		dialogState = startTimeDialogState,
		onTimeChange = updateStartTime,
		title = R.string.starttime_picker_hint
	)
	
	TimePickerDialog(
		initialTime = endTime,
		dialogState = endTimeDialogState,
		onTimeChange = updateEndTime,
		title = R.string.endtime_picker_hint
	)
}

@Composable
private fun TimePickerDialog(
	initialTime: LocalTime,
	dialogState: MaterialDialogState,
	onTimeChange: (LocalTime) -> Unit,
	@StringRes title: Int,
	is24HourClock: Boolean = false, // TODO: add option in settings to configure
) {
	MaterialDialog(
		dialogState = dialogState,
		onCloseRequest = { dialogState.hide() },
		buttons = {
			positiveButton(
				text = stringResource(R.string.btn_save),
				onClick = { dialogState.hide() })
			negativeButton(
				text = stringResource(R.string.btn_cancel),
				onClick = { dialogState.hide() })
		}
	) {
		timepicker(
			initialTime = initialTime,
			title = stringResource(title),
			is24HourClock = is24HourClock,
			waitForPositiveButton = true,
			onTimeChange = onTimeChange
		)
	}
}