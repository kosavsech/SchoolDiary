package com.kxsv.schooldiary.ui.screens.subject_list.subject_detail.add_edit

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ContentAlpha
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.teacher.TeacherEntity
import com.kxsv.schooldiary.data.local.features.teacher.TeacherEntity.Companion.shortName
import com.kxsv.schooldiary.ui.main.app_bars.topbar.AddEditSubjectTopAppBar
import com.kxsv.schooldiary.ui.main.navigation.ADD_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.EDIT_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.nav_actions.AddEditSubjectScreenNavActions
import com.kxsv.schooldiary.ui.screens.teacher_list.AddEditTeacherDialog
import com.kxsv.schooldiary.ui.util.LoadingContent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogScope
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.listItems
import com.vanpra.composematerialdialogs.rememberMaterialDialogState

data class AddEditSubjectScreenNavArgs(
	val subjectId: String?,
)

@Destination(
	navArgsDelegate = AddEditSubjectScreenNavArgs::class
)
@Composable
fun AddEditSubjectScreen(
	resultBackNavigator: ResultBackNavigator<Int>,
	destinationsNavigator: DestinationsNavigator,
	viewModel: AddEditSubjectViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState,
) {
	val uiState = viewModel.uiState.collectAsState().value
	val navigator = AddEditSubjectScreenNavActions(
		destinationsNavigator = destinationsNavigator,
		resultBackNavigator = resultBackNavigator
	)
	
	LaunchedEffect(uiState.isSubjectSaved) {
		if (uiState.isSubjectSaved) {
			val result = if (viewModel.subjectId == null) ADD_RESULT_OK else EDIT_RESULT_OK
			navigator.navigateBackWithResult(result)
		}
	}
	
	val teacherCreateDialog = rememberMaterialDialogState(false)
	val teacherSelectDialog = rememberMaterialDialogState(false)
	val saveSubject = remember { { viewModel.saveSubject() } }
	Scaffold(
		modifier = Modifier.fillMaxSize(),
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		topBar = { AddEditSubjectTopAppBar { navigator.popBackStack() } },
		floatingActionButton = {
			Row {
				FloatingActionButton(onClick = saveSubject) {
					Icon(Icons.Default.Save, stringResource(R.string.save_subject))
				}
			}
		}
	) { paddingValues ->
		
		val onCheckedChange = remember<(Set<String>) -> Unit> {
			{ viewModel.updateSelectedTeachers(it) }
		}
		val updateFullName = remember<(String) -> Unit> {
			{ viewModel.updateFullName(it) }
		}
		val updateDisplayName = remember<(String) -> Unit> {
			{ viewModel.updateDisplayName(it) }
		}
		val updateCabinet = remember<(String) -> Unit> {
			{ viewModel.updateCabinet(it) }
		}
		val onCreateTeacher = remember { { teacherCreateDialog.show() } }
		
		AddEditSubjectContent(
			modifier = Modifier.padding(paddingValues),
			teacherSelectDialog = teacherSelectDialog,
			isLoading = uiState.isLoading,
			fullName = uiState.fullName,
			displayName = uiState.displayName,
			cabinet = uiState.cabinet,
			availableTeachers = uiState.availableTeachers,
			selectedTeachersIds = uiState.selectedTeachersIds,
			onCreateTeacher = onCreateTeacher,
			onCheckedChange = onCheckedChange,
			onFullNameChanged = updateFullName,
			onDisplayNameChanged = updateDisplayName,
			onCabinetChanged = updateCabinet
		)
		
		val saveTeacher = remember {
			{ viewModel.saveNewTeacher() }
		}
		val updateFirstName = remember<(String) -> Unit> {
			{ viewModel.updateFirstName(it) }
		}
		val updateLastName = remember<(String) -> Unit> {
			{ viewModel.updateLastName(it) }
		}
		val updatePatronymic = remember<(String) -> Unit> {
			{ viewModel.updatePatronymic(it) }
		}
		val updatePhoneNumber = remember<(String) -> Unit> {
			{ viewModel.updatePhoneNumber(it) }
		}
		val eraseData = remember {
			{ viewModel.eraseData() }
		}
		val clearErrorMessage = remember {
			{ viewModel.clearErrorMessage() }
		}
		AddEditTeacherDialog(
			dialogState = teacherCreateDialog,
			firstName = uiState.firstName,
			lastName = uiState.lastName,
			patronymic = uiState.patronymic,
			phoneNumber = uiState.phoneNumber,
			isTeacherSaved = uiState.isTeacherCreated,
			errorMessage = uiState.errorMessage,
			updateFirstName = updateFirstName,
			updateLastName = updateLastName,
			updatePatronymic = updatePatronymic,
			updatePhoneNumber = updatePhoneNumber,
			clearErrorMessage = clearErrorMessage,
			onSaveClick = saveTeacher,
			onCancelClick = eraseData
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

@Composable
private fun AddEditSubjectContent(
	modifier: Modifier = Modifier,
	teacherSelectDialog: MaterialDialogState,
	isLoading: Boolean,
	fullName: String,
	displayName: String,
	cabinet: String,
	availableTeachers: List<TeacherEntity>,
	selectedTeachersIds: Set<String>,
	onCreateTeacher: () -> Unit,
	onCheckedChange: (Set<String>) -> Unit,
	onFullNameChanged: (String) -> Unit,
	onDisplayNameChanged: (String) -> Unit,
	onCabinetChanged: (String) -> Unit,
) {
	LoadingContent(
		modifier = modifier,
		loading = isLoading,
		empty = false,
		emptyContent = { Text(text = "Empty") },
	) {
		Column(
			modifier = Modifier.fillMaxWidth(),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.SpaceBetween
		) {
			val textFieldColors = TextFieldDefaults.outlinedTextFieldColors(
				focusedBorderColor = Color.Transparent,
				unfocusedBorderColor = Color.Transparent,
				cursorColor = MaterialTheme.colorScheme.secondary.copy(alpha = ContentAlpha.high)
			)
			OutlinedTextField(
				value = fullName,
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = (dimensionResource(id = R.dimen.horizontal_margin))),
				onValueChange = onFullNameChanged,
				placeholder = {
					Text(
						text = stringResource(id = R.string.full_name_hint),
						style = MaterialTheme.typography.headlineSmall
					)
				},
				textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
				maxLines = 2,
				colors = textFieldColors
			)
			
			Divider(
				Modifier
					.fillMaxWidth()
					.align(Alignment.CenterHorizontally)
			)
			
			OutlinedTextField(
				value = displayName,
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = (dimensionResource(id = R.dimen.horizontal_margin))),
				onValueChange = onDisplayNameChanged,
				placeholder = {
					Text(
						text = stringResource(id = R.string.display_name_hint),
						style = MaterialTheme.typography.headlineSmall
					)
				},
				textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
				maxLines = 1,
				colors = textFieldColors
			)
			
			Divider(
				Modifier
					.fillMaxWidth()
					.align(Alignment.CenterHorizontally)
			)
			
			OutlinedTextField(
				value = cabinet,
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = (dimensionResource(id = R.dimen.horizontal_margin))),
				onValueChange = onCabinetChanged,
				placeholder = {
					Text(
						text = stringResource(id = R.string.cabinet_hint),
						style = MaterialTheme.typography.headlineSmall
					)
				},
				textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
				maxLines = 1,
				colors = textFieldColors
			)
			
			Row(modifier = Modifier
				.fillMaxWidth()
				.clickable { teacherSelectDialog.show() }
				.padding(
					vertical = dimensionResource(R.dimen.vertical_margin),
					horizontal = dimensionResource(id = R.dimen.horizontal_margin),
				)
			) {
				Icon(Icons.Default.Person, stringResource(R.string.teacher_icon))
				Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.horizontal_margin)))
				val selectedTeacherEntities = remember(selectedTeachersIds, availableTeachers) {
					return@remember availableTeachers.filter { selectedTeachersIds.contains(it.teacherId) }
				}
				val teacherRowText = if (selectedTeacherEntities.isNotEmpty()) {
					val rememberedTeacherRowText = remember(selectedTeacherEntities) {
						var text = ""
						selectedTeacherEntities.forEachIndexed { index, teacher ->
							text += (if (index != 0) ", " else "")
							text += teacher.shortName()
						}
						return@remember text
					}
					rememberedTeacherRowText
				} else {
					stringResource(R.string.add_teacher_hint)
				}
				Text(
					text = teacherRowText,
					style = MaterialTheme.typography.bodyLarge
				)
			}
			MaterialDialog(
				dialogState = teacherSelectDialog,
				buttons = {
					button(
						res = R.string.btn_add,
						onClick = onCreateTeacher
					)
					positiveButton(res = R.string.btn_select)
					negativeButton(res = R.string.btn_cancel)
				}
			) {
				ModdedListItemsMultiChoice(
					list = availableTeachers,
					selectedTeachersIds = selectedTeachersIds,
					onCheckedChange = onCheckedChange,
				)
			}
		}
	}
}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun MaterialDialogScope.ModdedListItemsMultiChoice(
	list: List<TeacherEntity>,
	state: LazyListState = rememberLazyListState(),
	selectedTeachersIds: Set<String> = setOf(),
	onCheckedChange: (Set<String>) -> Unit = {},
) {
	var dialogSelectedTeachersIds by remember { mutableStateOf(selectedTeachersIds) }
	
	DialogCallback { onCheckedChange(dialogSelectedTeachersIds) }
	
	val onChecked = { teacherEntityId: String ->
		/* Have to create temp var as mutableState doesn't trigger on adding to set */
		val newSelectedTeachersIds = dialogSelectedTeachersIds.toMutableSet()
		if (teacherEntityId in dialogSelectedTeachersIds) {
			newSelectedTeachersIds.remove(teacherEntityId)
		} else {
			newSelectedTeachersIds.add(teacherEntityId)
		}
		dialogSelectedTeachersIds = newSelectedTeachersIds
		
	}
	
	listItems(
		list = list,
		state = state,
		onClick = { _, teacherEntity -> onChecked(teacherEntity.teacherId) },
		closeOnClick = false
	) { index, teacherEntity ->
		val selected = remember(dialogSelectedTeachersIds, list) {
			list[index].teacherId in dialogSelectedTeachersIds
		}
		MultiChoiceItem(
			item = teacherEntity,
			selected = selected,
			onChecked = { clickedTeacherEntity -> onChecked(clickedTeacherEntity.teacherId) }
		)
	}
}


@Composable
private fun MultiChoiceItem(
	item: TeacherEntity,
	selected: Boolean = false,
	onChecked: (item: TeacherEntity) -> Unit = { _ -> },
) {
	Row(
		Modifier
			.fillMaxWidth()
			.height(48.dp)
			.padding(start = 12.dp, end = 24.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Checkbox(checked = selected, onCheckedChange = { onChecked(item) })
		Spacer(
			modifier = Modifier
				.fillMaxHeight()
				.width(32.dp)
		)
		Text(
			item.shortName(),
			color = MaterialTheme.colorScheme.onSurface,
			style = MaterialTheme.typography.bodyMedium
		)
	}
}