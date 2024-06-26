package com.kxsv.schooldiary.ui.screens.schedule.add_edit

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.ui.main.app_bars.topbar.AddEditScheduleTopAppBar
import com.kxsv.schooldiary.ui.main.navigation.ADD_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.EDIT_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.nav_actions.AddEditLessonScreenNavActions
import com.kxsv.schooldiary.ui.screens.destinations.AddEditSubjectScreenDestination
import com.kxsv.schooldiary.ui.util.AppSnackbarHost
import com.kxsv.schooldiary.ui.util.LoadingContent
import com.kxsv.schooldiary.util.Utils
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.ramcosta.composedestinations.result.ResultRecipient
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.listItemsSingleChoice
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class AddEditLessonDestinationNavArgs(
	val datestamp: Long,
	val lessonId: Long? = null,
)

@Destination(
	navArgsDelegate = AddEditLessonDestinationNavArgs::class
)
@Composable
fun AddEditLessonScreen(
	resultBackNavigator: ResultBackNavigator<Int>,
	subjectAddEditResult: ResultRecipient<AddEditSubjectScreenDestination, Int>,
	destinationsNavigator: DestinationsNavigator,
	viewModel: AddEditLessonViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState,
) {
	val uiState = viewModel.uiState.collectAsState().value
	val navigator = AddEditLessonScreenNavActions(
		destinationsNavigator = destinationsNavigator, resultBackNavigator = resultBackNavigator
	)
	subjectAddEditResult.onNavResult { result ->
		when (result) {
			is NavResult.Canceled -> {}
			is NavResult.Value -> {
				viewModel.showEditResultMessage(result.value)
				viewModel.loadAvailableSubjects()
			}
		}
	}
	val saveLesson = remember {
		{ viewModel.saveLesson() }
	}
	Scaffold(
		modifier = Modifier.fillMaxSize(),
		snackbarHost = { AppSnackbarHost(hostState = snackbarHostState) },
		topBar = { AddEditScheduleTopAppBar { navigator.popBackStack() } },
		floatingActionButton = {
			FloatingActionButton(onClick = saveLesson) {
				Icon(
					imageVector = Icons.Filled.Done,
					contentDescription = stringResource(R.string.save_schedule)
				)
			}
			
		}
	) { paddingValues ->
		
		val clearIndexErrorMessage = remember {
			{ viewModel.clearIndexErrorMessage() }
		}
		val loadAvailableSubjects = remember {
			{ viewModel.loadAvailableSubjects() }
		}
		val saveSelectedSubject = remember<(Int) -> Unit> {
			{ viewModel.saveSelectedSubject(it) }
		}
		val updateDate = remember<(LocalDate) -> Unit> {
			{ viewModel.updateDate(it) }
		}
		val updateIndex = remember<(String) -> Unit> {
			{ viewModel.updateIndex(it) }
		}
		val updateCabinet = remember<(String) -> Unit> {
			{ viewModel.updateCabinet(it) }
		}
		val onCreateSubject = remember {
			{ navigator.onCreateSubject() }
		}
		AddEditLessonContent(
			modifier = Modifier.padding(paddingValues),
			isLoading = uiState.isLoading,
			pickedSubject = uiState.pickedSubject,
			indexInPattern = uiState.indexInPattern,
			date = uiState.date,
			cabinet = uiState.cabinet,
			subjects = uiState.availableSubjects,
			initialSubjectSelection = uiState.initialSubjectSelection,
			indexErrorMessage = uiState.indexErrorMessage,
			clearIndexErrorMessage = clearIndexErrorMessage,
			onSubjectDialogShown = loadAvailableSubjects,
			onSubjectChanged = saveSelectedSubject,
			onDateChanged = updateDate,
			onIndexUpdate = updateIndex,
			onCabinetUpdate = updateCabinet,
			onCreateSubject = onCreateSubject
		)
		
		LaunchedEffect(uiState.isClassSaved) {
			if (uiState.isClassSaved) {
				navigator.navigateBackWithResult(
					if (viewModel.lessonId == null) ADD_RESULT_OK else EDIT_RESULT_OK
				)
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
private fun AddEditLessonContent(
	modifier: Modifier = Modifier,
	isLoading: Boolean,
	pickedSubject: SubjectEntity?,
	indexInPattern: String,
	date: LocalDate?,
	cabinet: String,
	subjects: List<SubjectEntity>,
	initialSubjectSelection: Int?,
	@StringRes indexErrorMessage: Int?,
	clearIndexErrorMessage: () -> Unit,
	onSubjectDialogShown: () -> Unit,
	onSubjectChanged: (Int) -> Unit,
	onDateChanged: (LocalDate) -> Unit,
	onIndexUpdate: (String) -> Unit,
	onCabinetUpdate: (String) -> Unit,
	onCreateSubject: () -> Unit,
) {
	LoadingContent(
		isLoading = isLoading,
		empty = false,
		emptyContent = { Text(text = "Empty") },
		onRefresh = {}) {
		Column(
			modifier
				.fillMaxWidth()
		) {
			SubjectRow(
				isLoading = isLoading,
				subject = pickedSubject,
				initialSubjectSelection = initialSubjectSelection,
				onSubjectChanged = onSubjectChanged,
				subjects = subjects,
				onSubjectDialogShown = onSubjectDialogShown,
				onCreateSubject = onCreateSubject
			)
			Divider(
				Modifier
					.fillMaxWidth()
					.align(Alignment.CenterHorizontally)
			)
			DateRow(
				date = date,
				onDateChanged = onDateChanged
			)
			Divider(
				Modifier
					.fillMaxWidth()
					.align(Alignment.CenterHorizontally)
			)
			val focusManager = LocalFocusManager.current
			TextInputRow(
				value = indexInPattern,
				onValueChange = onIndexUpdate,
				placeholder = R.string.pick_index_hint,
				leadingIconImageVector = Icons.Default.Schedule,
				keyboardOptions = KeyboardOptions(
					imeAction = ImeAction.Done,
					autoCorrect = false,
					capitalization = KeyboardCapitalization.None,
					keyboardType = KeyboardType.Decimal
				),
				keyboardActions = KeyboardActions(
					onDone = {
						focusManager.clearFocus()
					}
				),
				clearErrorMessage = clearIndexErrorMessage,
				errorMessage = indexErrorMessage
			)
			Divider(
				Modifier
					.fillMaxWidth()
					.align(Alignment.CenterHorizontally)
			)
			TextInputRow(
				value = cabinet,
				onValueChange = onCabinetUpdate,
				placeholder = R.string.override_lesson_cabinet,
				leadingIconImageVector = Icons.Default.Place,
				keyboardOptions = KeyboardOptions(
					imeAction = ImeAction.Done,
					autoCorrect = false,
					capitalization = KeyboardCapitalization.None,
					keyboardType = KeyboardType.Password
				),
				keyboardActions = KeyboardActions(
					onDone = {
						focusManager.clearFocus()
					}
				),
			)
		}
	}
}

@Composable
private fun animateBorderStrokeAsState(
	isError: Boolean,
	interactionSource: InteractionSource,
): State<BorderStroke> {
	val focused by interactionSource.collectIsFocusedAsState()
	val targetValue = when {
		isError -> MaterialTheme.colorScheme.error
		else -> Color.Transparent
	}
	val indicatorColor = animateColorAsState(
		targetValue, tween(durationMillis = 150),
		label = "indicatorColor"
	)
	val targetThickness = if (focused) 2.dp else 1.dp
	val animatedThickness = animateDpAsState(
		targetThickness, tween(durationMillis = 150),
		label = "animatedThickness"
	)
	return rememberUpdatedState(
		BorderStroke(animatedThickness.value, SolidColor(indicatorColor.value))
	)
}

@Composable
private fun TextInputRow(
	value: String,
	onValueChange: (String) -> Unit,
	@StringRes placeholder: Int,
	leadingIconImageVector: ImageVector,
	keyboardOptions: KeyboardOptions,
	keyboardActions: KeyboardActions,
	clearErrorMessage: () -> Unit = {},
	@StringRes errorMessage: Int? = null,
) {
	val textFieldColors = OutlinedTextFieldDefaults.colors(
		cursorColor = colors.secondary.copy(alpha = ContentAlpha.high),
		focusedBorderColor = Color.Transparent,
		unfocusedBorderColor = Color.Transparent,
		errorBorderColor = Color.Transparent,
		errorContainerColor = Color.Transparent,
	)
	LaunchedEffect(key1 = value) {
		if (errorMessage != null) clearErrorMessage()
	}
	val interactionSource = remember { MutableInteractionSource() }
	val isValid by remember(errorMessage) { mutableStateOf(errorMessage == null) }
	val borderStroke =
		animateBorderStrokeAsState(isError = !isValid, interactionSource = interactionSource)
	
	Column(
		modifier = Modifier.fillMaxWidth(),
	) {
		Box(
			Modifier
				.border(borderStroke.value, MaterialTheme.shapes.extraSmall)
				.padding(horizontal = dimensionResource(id = R.dimen.horizontal_margin))
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			) {
				Icon(
					imageVector = leadingIconImageVector,
					contentDescription = stringResource(R.string.leading_icon_of_row),
					modifier = Modifier.size(18.dp)
				)
				OutlinedTextField(
					value = value,
					modifier = Modifier.fillMaxWidth(),
					onValueChange = onValueChange,
					placeholder = {
						Text(
							text = stringResource(id = placeholder),
							style = MaterialTheme.typography.titleMedium
						)
					},
					textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
					maxLines = 1,
					colors = textFieldColors,
					keyboardOptions = keyboardOptions,
					keyboardActions = keyboardActions,
					isError = !isValid,
					interactionSource = interactionSource
				)
			}
		}
		if (!isValid && errorMessage != null) {
			Text(
				text = stringResource(errorMessage),
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.error,
				modifier = Modifier
					.padding(horizontal = dimensionResource(id = R.dimen.horizontal_margin))
					.align(Alignment.End)
			)
		}
	}
}

@Composable
private fun DateRow(
	date: LocalDate?,
	onDateChanged: (LocalDate) -> Unit,
) {
	val datePickerDialog = rememberMaterialDialogState(false)
	val pickDateText = date?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
		?: stringResource(R.string.pick_date_hint)
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable { datePickerDialog.show() }
			.padding(dimensionResource(R.dimen.vertical_margin)),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			Icons.Default.CalendarToday,
			stringResource(R.string.picked_date),
			Modifier.size(18.dp)
		)
		Spacer(modifier = Modifier.padding(horizontal = 8.dp))
		Text(
			text = pickDateText,
			style = MaterialTheme.typography.titleMedium,
		)
	}
	DatePickerDialog(
		dialogState = datePickerDialog,
		onDateChanged = onDateChanged,
		initialDate = date,
		allowedDateValidator = { it.dayOfWeek != DayOfWeek.SUNDAY }
	)
}

@Composable
private fun SubjectRow(
	isLoading: Boolean,
	subject: SubjectEntity?,
	initialSubjectSelection: Int?,
	onSubjectChanged: (Int) -> Unit,
	subjects: List<SubjectEntity>,
	onSubjectDialogShown: () -> Unit,
	subjectDialog: MaterialDialogState = rememberMaterialDialogState(false),
	onCreateSubject: () -> Unit,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable {
				onSubjectDialogShown()
				subjectDialog.show()
			}
			.padding(dimensionResource(id = R.dimen.horizontal_margin)),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			Icons.Default.School,
			stringResource(R.string.picked_subject),
			Modifier.size(18.dp)
		)
		Spacer(modifier = Modifier.padding(horizontal = 8.dp))
		Text(
			text = subject?.getName() ?: stringResource(R.string.pick_subject_hint),
			style = MaterialTheme.typography.titleMedium,
		)
	}
	SubjectPickerDialog(
		isLoading = isLoading,
		dialogState = subjectDialog,
		subjects = subjects.map { listSubject -> listSubject.getName() },
		initialSelection = initialSubjectSelection,
		onChoiceChange = onSubjectChanged,
		onCreateSubject = onCreateSubject
	)
}

@Composable
private fun DatePickerDialog(
	dialogState: MaterialDialogState,
	onDateChanged: (LocalDate) -> Unit,
	initialDate: LocalDate?,
	allowedDateValidator: (LocalDate) -> Boolean = { true },
) {
	MaterialDialog(
		dialogState = dialogState,
		buttons = {
			positiveButton(res = R.string.btn_select)
			negativeButton(res = R.string.btn_cancel)
		}
	) {
		datepicker(
			initialDate = initialDate ?: Utils.currentDate,
			waitForPositiveButton = true,
			// TODO: add option in settings to configure this behaviour
			allowedDateValidator = allowedDateValidator,
			onDateChange = onDateChanged
		)
	}
}

@Composable
private fun SubjectPickerDialog(
	isLoading: Boolean,
	dialogState: MaterialDialogState,
	subjects: List<String>,
	initialSelection: Int?,
	onChoiceChange: (Int) -> Unit,
	onCreateSubject: () -> Unit,
) {
	if (!isLoading) {
		MaterialDialog(
			dialogState = dialogState,
			buttons = {
				button(
					res = R.string.btn_add,
					onClick = onCreateSubject
				)
				positiveButton(res = R.string.btn_select)
				negativeButton(res = R.string.btn_cancel)
			}
		) {
			listItemsSingleChoice(
				list = subjects,
				waitForPositiveButton = true,
				initialSelection = initialSelection,
				onChoiceChange = onChoiceChange
			)
		}
	}
}

@Preview(
	device = "id:pixel_4", showSystemUi = true, showBackground = true,
	uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
private fun AddEditScheduleContentPreview() {
	Surface {
		AddEditLessonContent(
			modifier = Modifier,
			isLoading = false,
//			date = null,
			pickedSubject = SubjectEntity("Русский язык", "210"),
			indexInPattern = "",
//			index = 0,
			date = Utils.currentDate,
//			lesson = null,
			cabinet = "556",
			subjects = listOf(
				SubjectEntity("Русский язык", "210"),
				SubjectEntity("Геометрия", "310"),
				SubjectEntity("Физика", "313"),
				SubjectEntity("Иностранный язык (английский)", "316"),
				SubjectEntity("Английский язык", "316"),
			),
			initialSubjectSelection = null,
			indexErrorMessage = null,
			clearIndexErrorMessage = {},
			onSubjectDialogShown = {},
			onSubjectChanged = {},
			onDateChanged = {},
			onIndexUpdate = {},
			{}
		) {}
	}
}

@Preview(
	device = "id:pixel_4", showSystemUi = true, showBackground = true,
	uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
private fun DatePickerPreview() {
	DatePickerDialog(
		dialogState = rememberMaterialDialogState(true),
		onDateChanged = {},
		initialDate = Utils.currentDate.minusDays(1),
		allowedDateValidator = { it.dayOfWeek != DayOfWeek.SUNDAY }
	)
}