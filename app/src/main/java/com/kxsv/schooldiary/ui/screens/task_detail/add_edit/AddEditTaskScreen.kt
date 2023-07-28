package com.kxsv.schooldiary.ui.screens.task_detail.add_edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.ui.main.app_bars.topbar.AddEditTaskTopAppBar
import com.kxsv.schooldiary.util.ui.LoadingContent
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.listItems
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.vanpra.composematerialdialogs.title
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val TAG = "AddEditTaskScreen"

@Composable
fun AddEditTaskScreen(
	onTaskSave: () -> Unit,
	onBack: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: AddEditTaskViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
	val uiState = viewModel.uiState.collectAsState().value
	
	Scaffold(
		modifier = modifier.fillMaxSize(),
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		topBar = {
			AddEditTaskTopAppBar(
				onBack = onBack,
				fetchNet = { viewModel.fetchNet() },
				fetchEnabled = (uiState.subject != null)
			)
		},
		floatingActionButton = {
			Row {
				FloatingActionButton(onClick = { viewModel.saveTask() }) {
					Icon(Icons.Filled.Done, stringResource(R.string.save_task))
				}
			}
		}
	) { paddingValues ->
		
		AddEditSubjectContent(
			isLoading = uiState.isLoading,
			name = uiState.title,
			description = uiState.description,
			dueDate = uiState.dueDate,
			subject = uiState.subject,
			availableSubjects = uiState.availableSubjects,
			changeTitle = { newTitle -> viewModel.changeTitle(newTitle) },
			changeDescription = { newDescription -> viewModel.changeDescription(newDescription) },
			changeDate = { newDate -> viewModel.changeDate(newDate) },
			changeSubject = { newSubject -> viewModel.changeSubject(newSubject) },
			modifier = Modifier.padding(paddingValues)
		)
		
		// Check if the pattern is saved and call onPatternUpdate event
		LaunchedEffect(uiState.isTaskSaved) {
			if (uiState.isTaskSaved) {
				onTaskSave()
			}
		}
		
		val taskVariantsListDialogState = rememberMaterialDialogState(false)
		
		LaunchedEffect(uiState.fetchedVariants) {
			if (uiState.fetchedVariants != null) {
				// todo add ability to configure task fetch override behavior
				if (uiState.fetchedVariants.size == 1) {
					viewModel.changeTitle(uiState.fetchedVariants.first().title)
					viewModel.onFetchedTitleChoose()
				} else {
					taskVariantsListDialogState.show()
				}
			}
		}
		MaterialDialog(
			dialogState = taskVariantsListDialogState,
			buttons = {
				negativeButton(
					res = R.string.btn_cancel,
					onClick = { viewModel.onFetchedTitleChoose() })
			}
		) {
			title(res = R.string.pick_task_title_hint)
			listItems(
				list = uiState.fetchedVariants!!.map { taskDto ->
					"Lesson #${taskDto.lessonIndex + 1}\n ${taskDto.title}"
				}
			) { index, _ ->
				viewModel.changeTitle(uiState.fetchedVariants[index].title)
				viewModel.onFetchedTitleChoose()
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
	isLoading: Boolean,
	name: String,
	description: String,
	dueDate: LocalDate,
	subject: SubjectEntity?,
	availableSubjects: List<SubjectEntity>,
	changeTitle: (String) -> Unit,
	changeDescription: (String) -> Unit,
	changeDate: (LocalDate) -> Unit,
	changeSubject: (SubjectEntity) -> Unit,
	modifier: Modifier = Modifier,
) {
	LoadingContent(
		loading = isLoading,
		empty = false,
		emptyContent = { },
		onRefresh = { }
	) {
		Column(
			modifier
				.fillMaxWidth()
				.padding(
					horizontal = dimensionResource(id = R.dimen.horizontal_margin),
					vertical = dimensionResource(id = R.dimen.vertical_margin)
				)
				.clickable { }
		) {
			val textFieldColors = OutlinedTextFieldDefaults.colors(
				unfocusedBorderColor = Color.Transparent,
				focusedBorderColor = Color.Transparent,
			)
			val focusManager = LocalFocusManager.current
			OutlinedTextField(
				value = name,
				modifier = Modifier.fillMaxWidth(),
				onValueChange = changeTitle,
				leadingIcon = {
					Icon(
						imageVector = Icons.Filled.Title,
						contentDescription = stringResource(R.string.task_title)
					)
				},
				placeholder = {
					Text(
						text = stringResource(id = R.string.full_name_hint),
						style = MaterialTheme.typography.bodyMedium
					)
				},
				keyboardOptions = KeyboardOptions(
					imeAction = ImeAction.Done,
					capitalization = KeyboardCapitalization.None,
					keyboardType = KeyboardType.Text
				),
				keyboardActions = KeyboardActions(
					onDone = { focusManager.clearFocus() }
				),
				textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
				singleLine = true,
				colors = textFieldColors
			)
			
			Divider(
				Modifier
					.fillMaxWidth()
					.align(CenterHorizontally)
			)
			
			val subjectDialog = rememberMaterialDialogState(false)
			val dateDialog = rememberMaterialDialogState(false)
			
			Row(modifier = Modifier
				.fillMaxWidth()
				.clickable {
					subjectDialog.show()
				}
				.padding(vertical = dimensionResource(id = R.dimen.vertical_margin))
			) {
				Icon(
					imageVector = Icons.Default.School,
					contentDescription = stringResource(R.string.picked_subject)
				)
				Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.horizontal_margin)))
				val text = subject?.getName() ?: stringResource(R.string.pick_subject_hint)
				Text(
					text = text,
					style = MaterialTheme.typography.bodyMedium
				)
				
			}
			
			Divider(
				Modifier
					.fillMaxWidth()
					.align(CenterHorizontally)
			)
			
			Row(modifier = Modifier
				.fillMaxWidth()
				.clickable {
					dateDialog.show()
				}
				.padding(vertical = dimensionResource(id = R.dimen.vertical_margin))
			
			) {
				Icon(
					imageVector = Icons.Default.CalendarMonth,
					contentDescription = stringResource(R.string.due_date)
				)
				Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.horizontal_margin)))
				val text = dueDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
				Text(
					text = text,
					style = MaterialTheme.typography.bodyMedium
				)
				
			}
			
			Divider(
				Modifier
					.fillMaxWidth()
					.align(CenterHorizontally)
			)
			
			OutlinedTextField(
				value = description,
				modifier = Modifier.fillMaxWidth(),
				onValueChange = changeDescription,
				leadingIcon = {
					Icon(
						imageVector = Icons.Default.Description,
						contentDescription = stringResource(R.string.task_description),
						modifier = Modifier.size(18.dp)
					)
				},
				placeholder = {
					Text(
						text = stringResource(id = R.string.description_hint),
						style = MaterialTheme.typography.bodyMedium
					)
				},
				keyboardOptions = KeyboardOptions(
					imeAction = ImeAction.Done,
					capitalization = KeyboardCapitalization.Sentences,
					keyboardType = KeyboardType.Text
				),
				keyboardActions = KeyboardActions(
					onDone = { focusManager.clearFocus() }
				),
				textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
				colors = textFieldColors,
			)
			
			MaterialDialog(
				dialogState = subjectDialog,
				buttons = {
					negativeButton(res = R.string.btn_cancel)
				}
			) {
				title(res = R.string.pick_subject_hint)
				listItems(
					list = availableSubjects.map { subjectEntity ->
						subjectEntity.getName()
					}
				) { index, _ ->
					changeSubject(availableSubjects[index])
				}
			}
			
			MaterialDialog(
				dialogState = dateDialog,
				buttons = {
					negativeButton(res = R.string.btn_cancel)
					positiveButton(res = R.string.btn_select)
				}
			) {
				datepicker(
					initialDate = dueDate,
					title = stringResource(id = R.string.select_due_date),
					waitForPositiveButton = true,
				) {
					changeDate(it)
				}
			}
		}
	}
}