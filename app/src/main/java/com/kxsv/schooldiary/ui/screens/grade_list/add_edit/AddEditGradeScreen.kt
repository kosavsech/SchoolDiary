package com.kxsv.schooldiary.ui.screens.grade_list.add_edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.subject.Subject
import com.kxsv.schooldiary.util.ui.AddEditGradeTopAppBar
import com.kxsv.schooldiary.util.ui.LoadingContent
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.listItemsSingleChoice
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AddEditGradeScreen(
	//@StringRes topBarTitle: Int, // is needed for title?
	onGradeUpdate: () -> Unit,
	onBack: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: AddEditGradeViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
	val uiState = viewModel.uiState.collectAsState().value
	
	Scaffold(
		modifier = modifier.fillMaxSize(),
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		topBar = { AddEditGradeTopAppBar(onBack) },
		floatingActionButton = {
			Row {
				FloatingActionButton(onClick = viewModel::saveGrade) {
					Icon(Icons.Filled.Done, stringResource(R.string.save_grade))
				}
			}
		}
	) { paddingValues ->
		
		AddEditSubjectContent(
			modifier = Modifier.padding(paddingValues),
			isLoading = uiState.isLoading,
			mark = uiState.mark,
			typeOfWork = uiState.typeOfWork,
			gradeDate = uiState.gradeDate,
			subjects = uiState.availableSubjects,
			pickedSubject = uiState.pickedSubject,
			initialSubjectSelection = uiState.initialSubjectSelection,
			onSubjectDialogShown = viewModel::loadAvailableSubjects,
			onSubjectChanged = viewModel::saveSelectedSubject,
			onMarkChanged = viewModel::updateMark,
			onTypeOfWorkChanged = viewModel::updateTypeOfWork,
			onDateChanged = viewModel::updateDate,
		)
		
		// Check if the pattern is saved and call onPatternUpdate event
		LaunchedEffect(uiState.isGradeSaved) {
			if (uiState.isGradeSaved) {
				onGradeUpdate()
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
private fun AddEditSubjectContent(
	modifier: Modifier = Modifier,
	isLoading: Boolean,
	mark: String,
	typeOfWork: String,
	gradeDate: LocalDate?,
	subjects: List<Subject>,
	pickedSubject: Subject?,
	initialSubjectSelection: Int?,
	onMarkChanged: (String) -> Unit,
	onTypeOfWorkChanged: (String) -> Unit,
	onSubjectDialogShown: () -> Unit,
	onSubjectChanged: (Int) -> Unit,
	onDateChanged: (LocalDate) -> Unit,
) {
	LoadingContent(
		loading = isLoading,
		empty = false,
		onRefresh = { /*TODO*/ }
	) {
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
			MarkRow(
				mark = mark,
				onMarkChanged = onMarkChanged,
				textFieldColors = textFieldColors
			)
			
			Divider(
				Modifier
					.fillMaxWidth()
					.align(CenterHorizontally)
			)
			
			TypeOfWorkRow(
				typeOfWork = typeOfWork,
				onTypeOfWorkChanged = onTypeOfWorkChanged,
				textFieldColors = textFieldColors
			)
			
			Divider(
				Modifier
					.fillMaxWidth()
					.align(CenterHorizontally)
			)
			
			DateRow(
				date = gradeDate,
				onDateChanged = onDateChanged
			)
			
			Divider(
				Modifier
					.fillMaxWidth()
					.align(CenterHorizontally)
			)
			
			SubjectRow(
				isLoading = isLoading,
				subject = pickedSubject,
				initialSubjectSelection = initialSubjectSelection,
				onSubjectChanged = onSubjectChanged,
				subjects = subjects,
				onSubjectDialogShown = onSubjectDialogShown
			)
		}
	}
}

@Composable
private fun MarkRow(
	mark: String,
	onMarkChanged: (String) -> Unit,
	textFieldColors: TextFieldColors,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			imageVector = Icons.Default.Grade,
			contentDescription = stringResource(R.string.mark_value),
			modifier = Modifier.size(18.dp)
		)
		OutlinedTextField(
			value = mark,
			modifier = Modifier.fillMaxWidth(),
			onValueChange = onMarkChanged,
			placeholder = {
				Text(
					text = stringResource(id = R.string.mark_hint),
					style = MaterialTheme.typography.h6
				)
			},
			textStyle = MaterialTheme.typography.h6,
			maxLines = 1,
			colors = textFieldColors
		)
	}
}

@Composable
private fun TypeOfWorkRow(
	typeOfWork: String,
	onTypeOfWorkChanged: (String) -> Unit,
	textFieldColors: TextFieldColors,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			imageVector = Icons.Default.Info,
			contentDescription = stringResource(R.string.type_of_work_mark),
			modifier = Modifier.size(18.dp)
		)
		OutlinedTextField(
			value = typeOfWork,
			onValueChange = onTypeOfWorkChanged,
			placeholder = {
				Text(
					text = stringResource(id = R.string.type_of_work_hint),
					style = MaterialTheme.typography.h6
				)
			},
			textStyle = MaterialTheme.typography.h6,
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
			imageVector = Icons.Default.CalendarToday,
			contentDescription = stringResource(R.string.picked_date),
			modifier = Modifier.size(18.dp)
		)
		Spacer(modifier = Modifier.padding(horizontal = 8.dp))
		Text(
			text = pickDateText,
			style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
		)
	}
	DatePickerDialog(
		dialogState = datePickerDialog,
		onDateChanged = onDateChanged,
		date = date
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
			style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
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
