package com.kxsv.schooldiary.ui.screens.subject_list.subject_detail.add_edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.teacher.TeacherEntity
import com.kxsv.schooldiary.data.local.features.teacher.TeacherEntity.Companion.fullName
import com.kxsv.schooldiary.ui.main.app_bars.topbar.AddEditSubjectTopAppBar
import com.kxsv.schooldiary.util.ui.LoadingContent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.listItemsMultiChoice
import com.vanpra.composematerialdialogs.rememberMaterialDialogState

data class AddEditSubjectScreenNavArgs(
	val subjectId: Long?,
)

@Destination(
	navArgsDelegate = AddEditSubjectScreenNavArgs::class
)
@Composable
fun AddEditSubjectScreen(
	resultNavigator: ResultBackNavigator<Int>,
	navigator: DestinationsNavigator,
	viewModel: AddEditSubjectViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState,
) {
	val uiState = viewModel.uiState.collectAsState().value
	
	Scaffold(
		modifier = Modifier.fillMaxSize(),
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		topBar = { AddEditSubjectTopAppBar { navigator.popBackStack() } },
		floatingActionButton = {
			Row {
				FloatingActionButton(onClick = {
					val result = viewModel.saveSubject()
					if (result != null) resultNavigator.navigateBack(result)
				}) {
					Icon(Icons.Filled.Done, stringResource(R.string.save_subject))
				}
			}
		}
	) { paddingValues ->
		
		val teacherListLoad = remember { { viewModel.loadAvailableTeachers() } }
		val saveSelectedTeachers = remember<(Set<Int>) -> Unit> {
			{ viewModel.saveSelectedTeachers(it) }
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
		
		AddEditSubjectContent(
			isLoading = uiState.isLoading,
			fullName = uiState.fullName,
			displayName = uiState.displayName,
			cabinet = uiState.cabinet,
			selectedTeachers = uiState.selectedTeachers,
			teachers = uiState.availableTeachers,
			initialSelection = uiState.initialSelection,
			onAddTeacher = teacherListLoad,
			onTeacherChanged = saveSelectedTeachers,
			onFullNameChanged = updateFullName,
			onDisplayNameChanged = updateDisplayName,
			onCabinetChanged = updateCabinet,
			modifier = Modifier.padding(paddingValues)
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
	isLoading: Boolean,
	fullName: String,
	displayName: String,
	cabinet: String,
	selectedTeachers: Set<TeacherEntity>,
	teachers: List<TeacherEntity>,
	initialSelection: Set<Int>,
	onAddTeacher: () -> Unit,
	onTeacherChanged: (Set<Int>) -> Unit,
	onFullNameChanged: (String) -> Unit,
	onDisplayNameChanged: (String) -> Unit,
	onCabinetChanged: (String) -> Unit,
	modifier: Modifier = Modifier,
) {
	LoadingContent(
		loading = isLoading,
		empty = false,
		emptyContent = { Text(text = "Empty") },
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
			OutlinedTextField(
				value = fullName,
				modifier = Modifier.fillMaxWidth(),
				onValueChange = onFullNameChanged,
				placeholder = {
					Text(
						text = stringResource(id = R.string.full_name_hint),
						style = MaterialTheme.typography.h6
					)
				},
				textStyle = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
				maxLines = 2,
				colors = textFieldColors
			)
			
			Divider(
				Modifier
					.fillMaxWidth()
					.align(CenterHorizontally)
			)
			
			OutlinedTextField(
				value = displayName,
				modifier = Modifier.fillMaxWidth(),
				onValueChange = onDisplayNameChanged,
				placeholder = {
					Text(
						text = stringResource(id = R.string.display_name_hint),
						style = MaterialTheme.typography.h6
					)
				},
				textStyle = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
				maxLines = 1,
				colors = textFieldColors
			)
			
			Divider(
				Modifier
					.fillMaxWidth()
					.align(CenterHorizontally)
			)
			
			OutlinedTextField(
				value = cabinet,
				modifier = Modifier.fillMaxWidth(),
				onValueChange = onCabinetChanged,
				placeholder = {
					Text(
						text = stringResource(id = R.string.cabinet_hint),
						style = MaterialTheme.typography.h6
					)
				},
				textStyle = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
				maxLines = 1,
				colors = textFieldColors
			)
			
			val teacherDialog = rememberMaterialDialogState(false)
			Row(modifier = Modifier
				.fillMaxWidth()
				.clickable {
					onAddTeacher()
					teacherDialog.show()
				}
			) {
				Icon(Icons.Default.Person, stringResource(R.string.teacher_icon))
				Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.horizontal_margin)))
				val teacherRowText = if (selectedTeachers.isNotEmpty()) {
					val rememberedTeacherRowText = remember(selectedTeachers) {
						var text = ""
						selectedTeachers.forEachIndexed { index, teacher ->
							text += (if (index != 0) ", " else "")
							text += teacher.fullName()
						}
						return@remember text
					}
					rememberedTeacherRowText
				} else {
					stringResource(R.string.add_teacher_hint)
				}
				Text(
					text = teacherRowText,
					style = MaterialTheme.typography.body1
				)
			}
			val listItems = remember(teachers) {
				teachers.map { teacher -> teacher.fullName() }
			}
			MaterialDialog(
				dialogState = teacherDialog,
				buttons = {
					positiveButton(res = R.string.btn_select)
					negativeButton(res = R.string.btn_cancel)
				}
			) {
				listItemsMultiChoice(
					list = listItems,
					waitForPositiveButton = true,
					initialSelection = initialSelection,
					onCheckedChange = onTeacherChanged
				)
			}
		}
	}
}