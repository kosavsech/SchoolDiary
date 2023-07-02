package com.kxsv.schooldiary.ui.screens.schedule.add_edit

/*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.features.teachers.Teacher
import com.kxsv.schooldiary.util.ui.AddEditScheduleTopAppBar
import com.kxsv.schooldiary.util.ui.LoadingContent
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.listItemsMultiChoice
import com.vanpra.composematerialdialogs.rememberMaterialDialogState

@Composable
fun AddEditScheduleScreen(
    //@StringRes topBarTitle: Int, // is needed for title?
    onScheduleUpdate: () -> Unit,
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
            loading = uiState.isLoading,
            name = uiState.name,
            cabinet = uiState.cabinet,
            onAddTeacher = viewModel::loadAvailableTeachers,
            onTeacherChanged = viewModel::saveSelectedTeachers,
            onNameChanged = viewModel::updateName,
            onCabinetChanged = viewModel::updateCabinet,
            modifier = Modifier.padding(paddingValues)
        )

        // Check if the pattern is saved and call onPatternUpdate event
        LaunchedEffect(uiState.isScheduleSaved) {
            if (uiState.isScheduleSaved) {
                onScheduleUpdate()
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
private fun AddEditScheduleContent(
    loading: Boolean,
    name: String,
    cabinet: String,
    selectedTeachers: Set<Teacher>,
    teachers: List<Teacher>,
    initialSelection: Set<Int>,
    onAddTeacher: () -> Unit,
    onTeacherChanged: (Set<Int>) -> Unit,
    onNameChanged: (String) -> Unit,
    onCabinetChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LoadingContent(loading, false, emptyContent = { Text(text = "Empty") }, onRefresh = {}) {
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
                        text = stringResource(id = R.string.name_hint),
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
                    .align(Alignment.CenterHorizontally)
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
                var text = ""
                if (selectedTeachers.isNotEmpty()) {
                    selectedTeachers.forEachIndexed { index, teacher ->
                        text += (if (index != 0) ", " else "") + if (teacher.lastName.isNotEmpty()) {
                            teacher.firstName[0] + "." + teacher.lastName[0] + ". " +
                                    teacher.patronymic
                        } else if (teacher.firstName.isNotEmpty()) {
                            teacher.firstName[0] + "." + teacher.patronymic
                        } else {
                            teacher.patronymic
                        }
                    }
                } else {
                    text = stringResource(R.string.add_teacher_hint)
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.body1
                )

            }
            MaterialDialog(
                dialogState = teacherDialog,
                buttons = {
                    positiveButton(res = R.string.btn_select)
                    negativeButton(res = R.string.btn_cancel)
                }
            ) {
                listItemsMultiChoice(
                    list = teachers.map { teacher ->
                        teacher.firstName + " " + teacher.patronymic
                    },
                    waitForPositiveButton = true,
                    initialSelection = initialSelection,
                    onCheckedChange = onTeacherChanged
                )
            }
        }
    }
}*/
