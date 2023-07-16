package com.kxsv.schooldiary.ui.screens.schedule.add_edit

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.subject.Subject
import com.kxsv.schooldiary.util.ui.AddEditScheduleTopAppBar
import com.kxsv.schooldiary.util.ui.LoadingContent
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.listItemsSingleChoice
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun AddEditScheduleScreen(
	onScheduleUpdate: (Long) -> Unit,
	onBack: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: AddEditScheduleViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
	val uiState = viewModel.uiState.collectAsState().value
	
	Scaffold(
		modifier = modifier.fillMaxSize(),
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		topBar = { AddEditScheduleTopAppBar(onBack) },
		floatingActionButton = {
			FloatingActionButton(onClick = viewModel::saveSchedule) {
				Icon(Icons.Filled.Done, stringResource(R.string.save_schedule))
			}
			
		}
	) { paddingValues ->
		
		AddEditScheduleContent(
			isLoading = uiState.isLoading,
			pickedSubject = uiState.pickedSubject,
			classIndex = uiState.classIndex,
			classDate = uiState.classDate,
			subjects = uiState.availableSubjects,
			initialSubjectSelection = uiState.initialSubjectSelection,
			onSubjectDialogShown = viewModel::loadAvailableSubjects,
			onSubjectChanged = viewModel::saveSelectedSubject,
			onDateChanged = viewModel::updateDate,
			onIndexUpdate = viewModel::updateIndex,
			modifier = Modifier.padding(paddingValues)
		)
		
		LaunchedEffect(uiState.isClassSaved) {
			if (uiState.isClassSaved) {
				onScheduleUpdate(localDateToTimestamp(uiState.classDate!!))
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

private fun localDateToTimestamp(date: LocalDate): Long =
	date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()

@Composable
private fun AddEditScheduleContent(
	isLoading: Boolean,
	pickedSubject: Subject?,
	classIndex: String,
	classDate: LocalDate?,
	subjects: List<Subject>,
	initialSubjectSelection: Int?,
	onSubjectDialogShown: () -> Unit,
	onSubjectChanged: (Int) -> Unit,
	onDateChanged: (LocalDate) -> Unit,
	onIndexUpdate: (String) -> Unit,
	modifier: Modifier = Modifier,
) {
	LoadingContent(
		loading = isLoading,
		empty = false,
		emptyContent = { Text(text = "Empty") },
		onRefresh = {}) {
		Column(
			modifier
				.fillMaxWidth()
				.padding(dimensionResource(id = R.dimen.horizontal_margin))
		) {
			SubjectRow(
				isLoading = isLoading,
				subject = pickedSubject,
				initialSubjectSelection = initialSubjectSelection,
				onSubjectChanged = onSubjectChanged,
				subjects = subjects,
				onSubjectDialogShown = onSubjectDialogShown
			)
			Divider(
				Modifier
					.fillMaxWidth()
					.align(Alignment.CenterHorizontally)
			)
			DateRow(
				date = classDate,
				onDateChanged = onDateChanged
			)
			Divider(
				Modifier
					.fillMaxWidth()
					.align(Alignment.CenterHorizontally)
			)
			ClassNumberRow(classIndex, onIndexUpdate)
		}
	}
}


@Composable
private fun ClassNumberRow(
	index: String,
	onIndexUpdate: (String) -> Unit,
) {
	val textFieldColors =
		androidx.compose.material.TextFieldDefaults.outlinedTextFieldColors(
			focusedBorderColor = Color.Transparent,
			unfocusedBorderColor = Color.Transparent,
			cursorColor = colors.secondary.copy(
				alpha = ContentAlpha.high
			)
		)
	Row(
		modifier = Modifier
			.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			Icons.Default.Schedule,
			stringResource(R.string.picked_class_number),
			Modifier.size(18.dp)
		)
		OutlinedTextField(
			value = index,
			modifier = Modifier.fillMaxWidth(),
			onValueChange = onIndexUpdate,
			placeholder = {
				Text(
					text = stringResource(id = R.string.pick_index_hint),
					style = MaterialTheme.typography.titleMedium
				)
			},
			textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
			maxLines = 1,
			colors = textFieldColors
		)
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
			.padding(vertical = dimensionResource(R.dimen.vertical_margin)),
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
		date = date
	)
}

@Composable
private fun SubjectRow(
	isLoading: Boolean,
	subject: Subject?,
	initialSubjectSelection: Int?,
	onSubjectChanged: (Int) -> Unit,
	subjects: List<Subject>,
	onSubjectDialogShown: () -> Unit,
	subjectDialog: MaterialDialogState = rememberMaterialDialogState(false),
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable {
				onSubjectDialogShown()
				subjectDialog.show()
			}
			.padding(vertical = dimensionResource(R.dimen.vertical_margin)),
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
		onChoiceChange = onSubjectChanged
	)
}

@Composable
private fun DatePickerDialog(
	dialogState: MaterialDialogState,
	onDateChanged: (LocalDate) -> Unit,
	date: LocalDate?,
) {
	val initialDate = date ?: LocalDate.now()
	
	MaterialDialog(
		dialogState = dialogState,
		buttons = {
			positiveButton(res = R.string.btn_select)
			negativeButton(res = R.string.btn_cancel)
		}
	) {
		datepicker(
			initialDate = initialDate,
			waitForPositiveButton = true,
			// TODO: add option in settings to configure this behaviour
			allowedDateValidator = { it.dayOfWeek != DayOfWeek.SUNDAY },
			onDateChange = onDateChanged
		)
	}
}

@Composable
private fun SubjectPickerDialog(
	dialogState: MaterialDialogState,
	subjects: List<String>,
	initialSelection: Int?,
	onChoiceChange: (Int) -> Unit,
	isLoading: Boolean,
) {
	if (!isLoading) {
		MaterialDialog(
			dialogState = dialogState,
			buttons = {
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
		AddEditScheduleContent(
			isLoading = false,
//			date = null,
			pickedSubject = Subject("Русский язык", "210"),
			classIndex = "",
//			index = 0,
			classDate = LocalDate.now(),
//			lesson = null,
			subjects = listOf(
				Subject("Русский язык", "210"),
				Subject("Геометрия", "310"),
				Subject("Физика", "313"),
				Subject("Иностранный язык (английский)", "316"),
				Subject("Английский язык", "316"),
			),
			initialSubjectSelection = null,
			onSubjectDialogShown = {},
			onSubjectChanged = {},
			onDateChanged = {},
			onIndexUpdate = {}
		)
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
		date = null
	)
}