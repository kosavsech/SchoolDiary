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
import androidx.compose.material.ContentAlpha
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeEntity
import com.kxsv.schooldiary.ui.main.app_bars.topbar.AddEditPatternTopAppBar
import com.kxsv.schooldiary.ui.main.navigation.nav_actions.AddEditPatternScreenNavActions
import com.kxsv.schooldiary.ui.screens.navArgs
import com.kxsv.schooldiary.ui.util.LoadingContent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.input
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.vanpra.composematerialdialogs.title
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class AddEditPatternScreenNavArgs(
	val patternId: Long?,
	@StringRes val topBarTitle: Int,
)

@Destination(
	navArgsDelegate = AddEditPatternScreenNavArgs::class
)
@Composable
fun AddEditPatternScreen(
	resultBackNavigator: ResultBackNavigator<Int>,
	destinationsNavigator: DestinationsNavigator,
	viewModel: AddEditPatternViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState,
	navBackStackEntry: NavBackStackEntry,
) {
	val navigator = AddEditPatternScreenNavActions(
		destinationsNavigator = destinationsNavigator, resultBackNavigator = resultBackNavigator
	)
	val navArgs: AddEditPatternScreenNavArgs = navBackStackEntry.navArgs()
	val topBarTitle = navArgs.topBarTitle
	val addEditStrokeDialogState = rememberMaterialDialogState(false)
	Scaffold(
		modifier = Modifier.fillMaxSize(),
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		topBar = { AddEditPatternTopAppBar(title = topBarTitle) { navigator.popBackStack() } },
		floatingActionButton = {
			Row {
				FloatingActionButton(onClick = { addEditStrokeDialogState.show() }) {
					Icon(Icons.Filled.Add, stringResource(R.string.add_pattern_stroke))
				}
				Spacer(Modifier.padding(horizontal = dimensionResource(R.dimen.horizontal_margin)))
				FloatingActionButton(
					onClick = {
						val result = viewModel.savePattern()
						if (result != null) navigator.navigateBackWithResult(result)
					}
				) {
					Icon(Icons.Filled.Done, stringResource(R.string.save_pattern))
				}
			}
		}
	) { paddingValues ->
		val uiState = viewModel.uiState.collectAsState().value
		
		val updateName = remember<(String) -> Unit> {
			{ viewModel.updateName(it) }
		}
		val onStrokeClick = remember<(PatternStrokeEntity) -> Unit> {
			{
				viewModel.onStrokeClick(it)
				addEditStrokeDialogState.show()
			}
		}
		val deleteStroke = remember<(PatternStrokeEntity) -> Unit> {
			{
				viewModel.deleteStroke(it)
			}
		}
		AddEditPatternContent(
			loading = uiState.isLoading,
			name = uiState.name,
			strokes = uiState.strokes,
			onNameChanged = updateName,
			onStrokeClick = onStrokeClick,
			onStrokeDelete = deleteStroke,
			modifier = Modifier.padding(paddingValues)
		)
		
		val changeIndex = remember<(Int) -> Unit> {
			{ viewModel.updateIndex(it) }
		}
		val saveStroke = remember {
			{ viewModel.saveStroke() }
		}
		val updateStartTime = remember<(LocalTime) -> Unit> {
			{ viewModel.updateStartTime(it) }
		}
		val updateEndTime = remember<(LocalTime) -> Unit> {
			{ viewModel.updateEndTime(it) }
		}
		AddEditStrokeDialog(
			dialogState = addEditStrokeDialogState,
			startTime = uiState.startTime,
			endTime = uiState.endTime,
			index = uiState.index,
			errorMessage = uiState.errorMessage,
			strokeToUpdate = uiState.strokeToUpdate,
			saveStroke = saveStroke,
			updateStartTime = updateStartTime,
			updateEndTime = updateEndTime,
			changeIndex = changeIndex
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditPatternContent(
	modifier: Modifier = Modifier,
	loading: Boolean,
	name: String,
	strokes: List<PatternStrokeEntity>,
	onNameChanged: (String) -> Unit,
	onStrokeClick: (PatternStrokeEntity) -> Unit,
	onStrokeDelete: (PatternStrokeEntity) -> Unit,
) {
	LoadingContent(
		modifier = modifier,
		loading = loading,
		empty = false,
		onRefresh = null
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(dimensionResource(id = R.dimen.horizontal_margin))
		) {
			val textFieldColors = OutlinedTextFieldDefaults.colors(
				cursorColor = MaterialTheme.colorScheme.secondary.copy(alpha = ContentAlpha.high),
				focusedBorderColor = Color.Transparent,
				unfocusedBorderColor = Color.Transparent,
			)
			OutlinedTextField(
				value = name,
				modifier = Modifier.fillMaxWidth(),
				onValueChange = onNameChanged,
				placeholder = {
					Text(
						text = stringResource(id = R.string.full_name_hint),
						style = MaterialTheme.typography.headlineSmall
					)
				},
				textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
				maxLines = 1,
				colors = textFieldColors
			)
			strokes.forEach { stroke ->
				key(strokes) {
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.clickable { onStrokeClick(stroke) },
						horizontalArrangement = Arrangement.Center,
						verticalAlignment = Alignment.CenterVertically
					) {
						Text(
							text = fromLocalTime(stroke.startTime) + " - " + fromLocalTime(stroke.endTime),
							style = MaterialTheme.typography.bodySmall
						)
						IconButton(onClick = { onStrokeDelete(stroke) }) {
							Icon(
								Icons.Default.Delete,
								stringResource(R.string.delete_pattern_stroke)
							)
						}
					}
				}
			}
		}
	}
}

// LocalTime -> String
fun fromLocalTime(localTime: LocalTime): String {
	return localTime.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH))
}

@Composable
private fun AddEditStrokeDialog(
	dialogState: MaterialDialogState,
	startTimeDialogState: MaterialDialogState = rememberMaterialDialogState(false),
	endTimeDialogState: MaterialDialogState = rememberMaterialDialogState(false),
	startTime: LocalTime,
	endTime: LocalTime,
	index: Int,
	errorMessage: Int?,
	strokeToUpdate: PatternStrokeEntity?,
	saveStroke: () -> Boolean,
	updateStartTime: (LocalTime) -> Unit,
	updateEndTime: (LocalTime) -> Unit,
	changeIndex: (Int) -> Unit,
) {
	MaterialDialog(
		dialogState = dialogState,
		onCloseRequest = { dialogState.hide() },
		buttons = {
			positiveButton(
				text = stringResource(R.string.btn_save),
				onClick = {
					val isSaved = saveStroke()
					if (isSaved) dialogState.hide()
				}
			)
			negativeButton(
				text = stringResource(R.string.btn_cancel),
				onClick = { dialogState.hide() })
		},
		autoDismiss = false,
		backgroundColor = MaterialTheme.colorScheme.surface
	) {
		val res = if (strokeToUpdate == null) {
			R.string.create_pattern_stroke
		} else {
			R.string.edit_pattern_stroke
		}
		title(
			res = res,
			color = MaterialTheme.colorScheme.onSurface
		)
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.clickable { startTimeDialogState.show() }
				.padding(
					vertical = dimensionResource(R.dimen.vertical_margin),
					horizontal = 24.dp,
				)
		) {
			Text(
				text = stringResource(id = R.string.start_time_label),
				color = MaterialTheme.colorScheme.onSurface,
				style = MaterialTheme.typography.bodyLarge,
				modifier = Modifier.align(Alignment.Start)
			)
			Row(
				verticalAlignment = Alignment.CenterVertically
			) {
				Icon(
					Icons.Default.Schedule,
					""
				)
				Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.horizontal_margin)))
				Text(
					text = fromLocalTime(startTime),
					style = MaterialTheme.typography.bodyLarge
				)
			}
		}
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.clickable { endTimeDialogState.show() }
				.padding(
					vertical = dimensionResource(R.dimen.vertical_margin),
					horizontal = 24.dp,
				)
		) {
			Text(
				text = stringResource(id = R.string.start_time_label),
				color = MaterialTheme.colorScheme.onSurface,
				style = MaterialTheme.typography.bodyLarge,
				modifier = Modifier.align(Alignment.Start)
			)
			Row(
				verticalAlignment = Alignment.CenterVertically
			) {
				Icon(
					Icons.Default.Schedule,
					""
				)
				Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.horizontal_margin)))
				Text(
					text = fromLocalTime(endTime),
					style = MaterialTheme.typography.bodyLarge
				)
			}
		}
		
		input(
			label = "Index",
			prefill = index.toString(),
			placeholder = "1",
			isTextValid = {
				it.toIntOrNull() != null && (it.toInt() in 1..9)
			},
			errorMessage = "Follow the format.\nAlso ensure that index is at least 1 and is less than 10",
			onInput = { changeIndex(it.toInt()) },
			waitForPositiveButton = true,
		)
		if (errorMessage != null) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(
						horizontal = 24.dp,
						vertical = dimensionResource(R.dimen.list_item_padding)
					)
			) {
				Text(
					text = stringResource(id = errorMessage),
					color = androidx.compose.material.MaterialTheme.colors.error,
					style = MaterialTheme.typography.bodyLarge,
					modifier = Modifier.align(Alignment.End)
				)
			}
		}
	}
	TimePickerDialog(
		dialogState = startTimeDialogState,
		title = R.string.starttime_picker_hint,
		initialTime = startTime,
		onTimeChange = updateStartTime
	)
	
	TimePickerDialog(
		dialogState = endTimeDialogState,
		title = R.string.endtime_picker_hint,
		initialTime = endTime,
		onTimeChange = updateEndTime
	)
}

@Composable
private fun TimePickerDialog(
	dialogState: MaterialDialogState,
	@StringRes title: Int,
	initialTime: LocalTime,
	onTimeChange: (LocalTime) -> Unit,
	is24HourClock: Boolean = false, // TODO: add option in settings to configure
) {
	MaterialDialog(
		dialogState = dialogState,
		buttons = {
			positiveButton(
				text = stringResource(R.string.btn_save),
			)
			negativeButton(
				text = stringResource(R.string.btn_cancel),
			)
		},
		autoDismiss = true,
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