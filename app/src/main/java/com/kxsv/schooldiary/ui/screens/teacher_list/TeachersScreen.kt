package com.kxsv.schooldiary.ui.screens.teacher_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.teacher.Teacher
import com.kxsv.schooldiary.util.ui.TeachersTopAppBar


@Composable
fun TeachersScreen(
    //@StringRes userMessage: Int,
    //onUserMessageDisplayed: () -> Unit,
    openDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TeachersViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    androidx.compose.material3.Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize(),
        topBar = { TeachersTopAppBar(openDrawer = openDrawer) },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::showTeacherDialog) {
                Icon(Icons.Default.Add, stringResource(R.string.add_teacher))
            }
        }
    ) { paddingValues ->
        val uiState = viewModel.uiState.collectAsState().value

        TeachersContent(
            teachers = uiState.teachers,
            onTeacherClick = viewModel::onTeacherClick,
            deleteTeacher = viewModel::deleteTeacher,
            modifier = Modifier.padding(paddingValues)
        )

        if (uiState.isTeacherDialogShown) {
            AddEditTeacherDialog(
                hideTeacherDialog = viewModel::hideTeacherDialog,
                saveTeacher = viewModel::saveTeacher,
                updateFirstName = viewModel::updateFirstName,
                updateLastName = viewModel::updateLastName,
                updatePatronymic = viewModel::updatePatronymic,
                updatePhoneNumber = viewModel::updatePhoneNumber,
                firstName = uiState.firstName,
                lastName = uiState.lastName,
                patronymic = uiState.patronymic,
                phoneNumber = uiState.phoneNumber
            )
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
private fun AddEditTeacherDialog(
    hideTeacherDialog: () -> Unit,
    saveTeacher: () -> Unit,
    updateFirstName: (String) -> Unit,
    updateLastName: (String) -> Unit,
    updatePatronymic: (String) -> Unit,
    updatePhoneNumber: (String) -> Unit,
    firstName: String,
    lastName: String,
    patronymic: String,
    phoneNumber: String,
) {
    AlertDialog(
        onDismissRequest = { hideTeacherDialog() },
        title = { Text(text = stringResource(R.string.add_teacher)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = firstName,
                    onValueChange = { updateFirstName(it) },
                    placeholder = {
                        Text(text = stringResource(R.string.first_name_hint) )
                    }
                )
                TextField(
                    value = lastName,
                    onValueChange = { updateLastName(it) },
                    placeholder = {
                        Text(text = stringResource(R.string.last_name_hint))
                    }
                )
                TextField(
                    value = patronymic,
                    onValueChange = { updatePatronymic(it) },
                    placeholder = {
                        Text(text = stringResource(R.string.patronymic_hint))
                    }
                )
                TextField(
                    value = phoneNumber,
                    onValueChange = { updatePhoneNumber(it) },
                    placeholder = {
                        Text(text = stringResource(R.string.phone_number_hint))
                    }
                )
            }
        },
        buttons = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Button(onClick = { saveTeacher() }) {
                    Text(text = stringResource(R.string.save_teacher))
                }
            }
        }
    )
}

@Composable
private fun TeachersContent(
	teachers: List<Teacher>,
	onTeacherClick: (Teacher) -> Unit,
	deleteTeacher: (Teacher) -> Unit,
	modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .padding(vertical = dimensionResource(R.dimen.vertical_margin)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_item_padding))
    ) {
        items(teachers) { teacher ->
            TeacherItem(
                teacher = teacher,
                onTeacherClick = { onTeacherClick(teacher) },
                deleteTeacher = { deleteTeacher(teacher) }
            )
        }
    }
}

@Composable
private fun TeacherItem(
	teacher: Teacher,
	onTeacherClick: () -> Unit,
	deleteTeacher: () -> Unit,
) {
    Row(
        modifier = Modifier
	        .fillMaxWidth()
	        .clickable { onTeacherClick() }
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = teacher.firstName + " " + teacher.patronymic,
                style = androidx.compose.material.MaterialTheme.typography.h6,
                color = Color.Black,
            )
            Text(
                text = teacher.phoneNumber,
                style = MaterialTheme.typography.labelMedium
            )
        }
        IconButton(onClick = deleteTeacher) {
            Icon(Icons.Default.Delete, stringResource(R.string.delete_teacher))
        }
    }
}

@Preview
@Composable
private fun TeachersContentPreview() {
    Surface {
        TeachersContent(
            teachers = listOf(
                Teacher(
                    firstName = "Ivan",
                    lastName = "Stepanov",
                    patronymic = "Petrovich",
                    phoneNumber = "+756248932572"
                ),
                Teacher(
                    firstName = "Stepan",
                    lastName = "Ivanov",
                    patronymic = "Vasilievich",
                    phoneNumber = "+756248932572"
                ),
                Teacher(
                    firstName = "Oleg",
                    lastName = "Kostilev",
                    patronymic = "Sergeevich",
                    phoneNumber = "+756248932572"
                ),
                Teacher(
                    firstName = "Alex",
                    lastName = "Simonov",
                    patronymic = "Yegorovich",
                    phoneNumber = "+756248932572"
                ),
            ), onTeacherClick = {}, deleteTeacher = {}
        )
    }
}

@Preview
@Composable
private fun TeacherItemPreview() {
    Surface {
        TeacherItem(
            teacher = Teacher(
                firstName = "Ivan",
                lastName = "Stepanov",
                patronymic = "Yegorovich",
                phoneNumber = "+756248932572"
            ),
            onTeacherClick = {}, deleteTeacher = {}
        )
    }
}
