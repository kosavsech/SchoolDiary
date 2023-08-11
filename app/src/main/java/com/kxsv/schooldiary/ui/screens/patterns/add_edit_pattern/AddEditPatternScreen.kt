package com.kxsv.schooldiary.ui.screens.patterns.add_edit_pattern

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeEntity
import com.kxsv.schooldiary.ui.main.app_bars.topbar.AddEditPatternTopAppBar
import com.kxsv.schooldiary.ui.main.navigation.nav_actions.AddEditPatternScreenNavActions
import com.kxsv.schooldiary.ui.screens.navArgs
import com.kxsv.schooldiary.util.ui.LoadingContent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogScope
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.TextFieldStyle
import com.vanpra.composematerialdialogs.datetime.time.timepicker
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
			val textFieldColors = TextFieldDefaults.outlinedTextFieldColors(
				focusedBorderColor = Color.Transparent,
				unfocusedBorderColor = Color.Transparent,
				cursorColor = MaterialTheme.colorScheme.secondary.copy(alpha = ContentAlpha.high)
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
		
		input3(
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


/**
 * Adds an input field with the given parameters to the dialog
 * @param label string to be shown in the input field before selection eg. Username
 * @param placeholder hint to be shown in the input field when it is selected but empty eg. Joe
 * @param prefill string to be input into the text field by default
 * @param waitForPositiveButton if true the [onInput] callback will only be called when the
 * positive button is pressed, otherwise it will be called when the input value is changed
 * @param visualTransformation a visual transformation of the content of the text field
 * @param keyboardOptions software keyboard options which can be used to customize parts
 * of the keyboard
 * @param errorMessage a message to be shown to the user when the input is not valid
 * @param focusRequester a [FocusRequester] which can be used to control the focus state of the
 * text field
 * @param focusOnShow if set to true this will auto focus the text field when the input
 * field is shown
 * @param isTextValid a function which is called to check if the user input is valid
 * @param onInput a function which is called with the user input. The timing of this call is
 * dictated by [waitForPositiveButton]
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialDialogScope.input3(
	modifier: Modifier = Modifier,
	label: String,
	placeholder: String = "",
	prefill: String = "",
	enabled: Boolean = true,
	readOnly: Boolean = false,
	textStyle: TextStyle = LocalTextStyle.current,
	leadingIcon: @Composable (() -> Unit)? = null,
	trailingIcon: @Composable (() -> Unit)? = null,
	visualTransformation: VisualTransformation = VisualTransformation.None,
	keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
	keyboardActions: KeyboardActions = KeyboardActions(),
	singleLine: Boolean = false,
	maxLines: Int = Int.MAX_VALUE,
	interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
	colors: TextFieldColors = TextFieldDefaults.textFieldColors(),
	textFieldStyle: TextFieldStyle = TextFieldStyle.Filled,
	waitForPositiveButton: Boolean = true,
	errorMessage: String = "",
	focusRequester: FocusRequester = FocusRequester.Default,
	focusOnShow: Boolean = false,
	isTextValid: (String) -> Boolean = { true },
	onInput: (String) -> Unit = {},
	
	) {
	var text by remember { mutableStateOf(prefill) }
	val valid = remember(text) { isTextValid(text) }
	
	PositiveButtonEnabled(valid = valid, onDispose = {})
	
	if (waitForPositiveButton) {
		DialogCallback { onInput(text) }
	}
	
	Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 8.dp)) {
		TextFieldWithStyle(
			modifier = modifier
				.focusRequester(focusRequester)
				.fillMaxWidth()
				.testTag("dialog_input"),
			value = text,
			onValueChange = {
				text = it
				if (!waitForPositiveButton) {
					onInput(text)
				}
			},
			enabled = enabled,
			readOnly = readOnly,
			textStyle = textStyle,
			label = {
				Text(
					label,
					color = MaterialTheme.colorScheme.onBackground.copy(0.8f)
				)
			},
			placeholder = {
				Text(
					placeholder,
					color = MaterialTheme.colorScheme.onBackground.copy(0.5f)
				)
			},
			leadingIcon = leadingIcon,
			trailingIcon = trailingIcon,
			isError = !valid,
			visualTransformation = visualTransformation,
			keyboardOptions = keyboardOptions,
			keyboardActions = keyboardActions,
			singleLine = singleLine,
			maxLines = maxLines,
			interactionSource = interactionSource,
			colors = colors,
			style = textFieldStyle
		)
		
		if (!valid) {
			Text(
				errorMessage,
				fontSize = 14.sp,
				color = MaterialTheme.colorScheme.error,
				modifier = Modifier
					.align(Alignment.End)
					.testTag("dialog_input_error")
			)
		}
	}
	
	if (focusOnShow) {
		DisposableEffect(Unit) {
			focusRequester.requestFocus()
			onDispose { }
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TextFieldWithStyle(
	value: String,
	onValueChange: (String) -> Unit,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
	readOnly: Boolean = false,
	textStyle: TextStyle = LocalTextStyle.current,
	label: @Composable (() -> Unit)? = null,
	placeholder: @Composable (() -> Unit)? = null,
	leadingIcon: @Composable (() -> Unit)? = null,
	trailingIcon: @Composable (() -> Unit)? = null,
	isError: Boolean = false,
	visualTransformation: VisualTransformation = VisualTransformation.None,
	keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
	keyboardActions: KeyboardActions = KeyboardActions(),
	singleLine: Boolean = false,
	maxLines: Int = Int.MAX_VALUE,
	interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
	colors: TextFieldColors = TextFieldDefaults.textFieldColors(),
	style: TextFieldStyle = TextFieldStyle.Filled,
) {
	when (style) {
		TextFieldStyle.Filled -> {
			TextField(
				value = value,
				onValueChange = onValueChange,
				modifier = modifier,
				enabled = enabled,
				readOnly = readOnly,
				textStyle = textStyle,
				label = label,
				placeholder = placeholder,
				leadingIcon = leadingIcon,
				trailingIcon = trailingIcon,
				isError = isError,
				visualTransformation = visualTransformation,
				keyboardOptions = keyboardOptions,
				keyboardActions = keyboardActions,
				singleLine = singleLine,
				maxLines = maxLines,
				interactionSource = interactionSource,
				colors = colors
			)
		}
		
		TextFieldStyle.Outlined -> {
			OutlinedTextField(
				value = value,
				onValueChange = onValueChange,
				modifier = modifier,
				enabled = enabled,
				readOnly = readOnly,
				textStyle = textStyle,
				label = label,
				placeholder = placeholder,
				leadingIcon = leadingIcon,
				trailingIcon = trailingIcon,
				isError = isError,
				visualTransformation = visualTransformation,
				keyboardOptions = keyboardOptions,
				keyboardActions = keyboardActions,
				singleLine = singleLine,
				maxLines = maxLines,
				interactionSource = interactionSource,
				colors = colors
			)
		}
	}
}