package com.kxsv.schooldiary.ui.screens.task_list.task_detail.add_edit

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
import androidx.compose.ui.Alignment
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
import com.kxsv.schooldiary.ui.main.navigation.ADD_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.EDIT_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.nav_actions.AddEditTaskScreenNavActions
import com.kxsv.schooldiary.ui.util.LoadingContent
import com.kxsv.schooldiary.util.Utils
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.listItems
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.vanpra.composematerialdialogs.title
import java.time.LocalDate

private const val TAG = "AddEditTaskScreen"

data class AddEditTaskScreenNavArgs(
	val taskId: String?,
	val isEditingFetchedTask: Boolean,
)

@Destination(
	navArgsDelegate = AddEditTaskScreenNavArgs::class
)
@Composable
fun AddEditTaskScreen(
	destinationsNavigator: DestinationsNavigator,
	resultNavigator: ResultBackNavigator<Int>,
	viewModel: AddEditTaskViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState,
) {
	val uiState = viewModel.uiState.collectAsState().value
	val navigator = AddEditTaskScreenNavActions(
		destinationsNavigator = destinationsNavigator,
		resultNavigator = resultNavigator
	)
	
	LaunchedEffect(uiState.isTaskSaved) {
		if (uiState.isTaskSaved) {
			navigator.backWithResult(
				if (viewModel.taskId == null) ADD_RESULT_OK else EDIT_RESULT_OK
			)
		}
	}
	val onBack = remember {
		{ navigator.popBackStack() }
	}
	val fetchNet = remember {
		{ viewModel.fetchNet() }
	}
	Scaffold(
		modifier = Modifier.fillMaxSize(),
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		topBar = {
			AddEditTaskTopAppBar(
				onBack = onBack,
				fetchNet = fetchNet,
				fetchEnabled = (uiState.subject != null)
			)
		},
		floatingActionButton = {
			Row {
				FloatingActionButton(
					onClick = { viewModel.saveTask() }
				) {
					Icon(Icons.Filled.Done, stringResource(R.string.save_task))
				}
			}
		}
	) { paddingValues ->
		
		val onImmutableFieldEdit = remember {
			{ viewModel.onFetchedTaskImmutableFieldEdit() }
		}
		val changeTitle = remember<(String) -> Unit> {
			{ newTitle -> viewModel.changeTitle(newTitle) }
		}
		val changeDescription = remember<(String) -> Unit> {
			{ newDescription -> viewModel.changeDescription(newDescription) }
		}
		val changeDate = remember<(LocalDate) -> Unit> {
			{ newDate -> viewModel.changeDate(newDate) }
		}
		val changeSubject = remember<(SubjectEntity) -> Unit> {
			{ newSubject -> viewModel.changeSubject(newSubject) }
		}
		AddEditSubjectContent(
			modifier = Modifier.padding(paddingValues),
			isLoading = uiState.isLoading,
			isEditingFetchedTask = viewModel.isEditingFetchedTask,
			name = uiState.title,
			description = uiState.description,
			dueDate = uiState.dueDate,
			subject = uiState.subject,
			availableSubjects = uiState.availableSubjects,
			onImmutableFieldEdit = onImmutableFieldEdit,
			changeTitle = changeTitle,
			changeDescription = changeDescription,
			changeDate = changeDate,
			changeSubject = changeSubject
		)
		
		val taskVariantsListDialogState = rememberMaterialDialogState(false)
		
		LaunchedEffect(uiState.fetchedVariants) {
			if (uiState.fetchedVariants != null) {
				// todo add ability to configure task fetching 1 variant override behavior
				//  do override or show dialog to consider picking
				if (uiState.fetchedVariants.size == 1) {
					viewModel.pickFetchedVariant(uiState.fetchedVariants.first())
					viewModel.onFetchedVariantChosen()
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
					onClick = { viewModel.onFetchedVariantChosen() })
			}
		) {
			title(res = R.string.pick_task_title_hint)
			listItems(
				list = uiState.fetchedVariants!!.map { taskDto ->
					"Lesson #${taskDto.lessonIndex + 1}\n ${taskDto.title}"
				}
			) { index, _ ->
				viewModel.pickFetchedVariant(uiState.fetchedVariants[index])
				viewModel.onFetchedVariantChosen()
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
	isEditingFetchedTask: Boolean,
	name: String,
	description: String,
	dueDate: LocalDate,
	subject: SubjectEntity?,
	availableSubjects: List<SubjectEntity>,
	onImmutableFieldEdit: () -> Unit,
	changeTitle: (String) -> Unit,
	changeDescription: (String) -> Unit,
	changeDate: (LocalDate) -> Unit,
	changeSubject: (SubjectEntity) -> Unit,
) {
	LoadingContent(
		loading = isLoading,
		empty = false,
	) {
		Column(
			modifier
				.fillMaxWidth()
				.padding(
					horizontal = dimensionResource(id = R.dimen.horizontal_margin),
					vertical = dimensionResource(id = R.dimen.vertical_margin)
				)
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
					if (!isEditingFetchedTask) {
						subjectDialog.show()
					} else {
						onImmutableFieldEdit()
					}
				}
				.padding(vertical = dimensionResource(id = R.dimen.vertical_margin)),
				verticalAlignment = Alignment.CenterVertically
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
					if (!isEditingFetchedTask) {
						dateDialog.show()
					} else {
						onImmutableFieldEdit()
					}
				}
				.padding(vertical = dimensionResource(id = R.dimen.vertical_margin)),
				verticalAlignment = Alignment.CenterVertically
			) {
				Icon(
					imageVector = Icons.Default.CalendarMonth,
					contentDescription = stringResource(R.string.due_date)
				)
				Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.horizontal_margin)))
				val dueDateText = dueDate.format(Utils.taskDueDateFormatterLong)
				Text(
					text = dueDateText,
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