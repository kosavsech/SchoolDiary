package com.kxsv.schooldiary.ui.screens.schedule

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.features.schedule.ScheduleWithSubject
import com.kxsv.schooldiary.util.ui.ScheduleTopAppBar
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.time.LocalDate

@Composable
fun ScheduleScreen(
    @StringRes userMessage: Int,
    onAddSchedule: () -> Unit,
    onUserMessageDisplayed: () -> Unit,
    openDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScheduleViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        topBar = { ScheduleTopAppBar(openDrawer) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = onAddSchedule) {
                Icon(Icons.Default.Add, stringResource(R.string.add_schedule_item))
            }
        },
    ) { paddingValues ->
        val uiState = viewModel.uiState.collectAsState().value

        DayScheduleContent(
            loading = uiState.isLoading,
            lessons = uiState.lessons,
            onScheduleClick = viewModel::showDialog,
            modifier = Modifier.padding(paddingValues),
        )

        if (uiState.isLessonDialogShown) {
            LessonDialog(
                lesson = uiState.lesson,
                hideDialog = viewModel::hideDialog,
                onDeleteSchedule = viewModel::deleteSchedule,
                onEditSchedule = viewModel::editSchedule,
            )
        }
        uiState.userMessage?.let { userMessage ->
            val snackbarText = stringResource(userMessage)
            LaunchedEffect(snackbarHostState, viewModel, userMessage, snackbarText) {
                snackbarHostState.showSnackbar(snackbarText)
                viewModel.snackbarMessageShown()
            }
        }

        LaunchedEffect(uiState.isSubjectDeleted) {
            if (uiState.isSubjectDeleted) {
                viewModel.onDeleteSchedule()
            }
        }

        // Check if there's a userMessage to show to the user
        val currentOnUserMessageDisplayed by rememberUpdatedState(onUserMessageDisplayed)
        LaunchedEffect(userMessage) {
            if (userMessage != 0) {
                viewModel.showEditResultMessage(userMessage)
                currentOnUserMessageDisplayed()
            }
        }
    }
}

@Composable
fun LessonDialog(
    lesson: ScheduleWithSubject?,
    onDeleteSchedule: (Long) -> Unit,
    onEditSchedule: (ScheduleWithSubject) -> Unit,
    hideDialog: () -> Unit,
    dialogState: MaterialDialogState = rememberMaterialDialogState(true),
) {
    if (lesson != null) {
        MaterialDialog(
            dialogState = dialogState,
            onCloseRequest = { hideDialog },
        ) {
            Text(lesson.subject.name, style = MaterialTheme.typography.titleMedium)
            Text(LocalDate.now().dayOfWeek.name, style = MaterialTheme.typography.labelMedium)
            // TODO: time
            Row(Modifier.fillMaxWidth()) {
                FilledTonalButton(onClick = { onEditSchedule(lesson) }) {
                    Text(text = stringResource(R.string.btn_edit))
                }
                FilledTonalButton(onClick = { onDeleteSchedule(lesson.schedule.scheduleId) }) {
                    Text(text = stringResource(R.string.btn_delete))
                }
            }

            Row {
                Icon(Icons.Default.LocationOn, stringResource(R.string.lesson_room))
                Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.list_item_padding)))
                Column() {
                    Text(lesson.subject.cabinet, style = MaterialTheme.typography.labelLarge)

                    Text(
                        stringResource(R.string.cabinet_hint),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
fun DayScheduleContent(
    loading: Boolean,
    lessons: List<ScheduleWithSubject>,
    //@StringRes noPatternsLabel: Int,
    //onRefresh: () -> Unit,
    onScheduleClick: (ScheduleWithSubject) -> Unit,
    lessonDialog: MaterialDialogState = rememberMaterialDialogState(false),
    modifier: Modifier,
) {
    LazyColumn(modifier = modifier) {
        item {
            DayOfWeekHeader(lessonsAmount = lessons.size)
        }
        lessons.forEachIndexed { it, lesson ->
            item {
                LessonItem(lesson, onScheduleClick)
                if (it != lessons.lastIndex) {
                    Divider(
                        modifier = Modifier
                            .padding(horizontal = dimensionResource(R.dimen.list_item_padding))
                            .fillMaxWidth(),
                        thickness = 1.dp,
                    )
                }
            }
        }

    }
}

@Composable
fun DayOfWeekHeader(
    lessonsAmount: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, bottom = 24.dp)
    ) {
        Text(LocalDate.now().dayOfWeek.name, fontSize = 23.sp)
        Text(stringResource(R.string.lessons_quantity_label, lessonsAmount), fontSize = 14.sp)
    }
}

@Composable
fun LessonItem(
    lesson: ScheduleWithSubject,
    onScheduleClick: (ScheduleWithSubject) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.clickable {
            onScheduleClick(lesson)
        }
    ) {
        // TODO: time
        /*Text(
            text = "${lessonStroke.time.first} - ${lessonStroke.time.second}",
            fontSize = 14.sp,
        )*/
        Text(lesson.subject.name, fontSize = 23.sp)
        Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding)))
        Row {
            Row {
                Icon(Icons.Default.LocationOn, stringResource(R.string.lesson_room))
                Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.list_item_padding)))
                Text(lesson.subject.cabinet, style = MaterialTheme.typography.labelMedium)
            }
            Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.list_item_padding)))
            // TODO: add tags
//            Text("Tags: ${lessonStroke.subject..tags}", fontSize = 14.sp,)
        }
    }
}

// TODO: add previews
/*val lessons = listOf(
                "Русский язык",
                "Геометрия",
                "Физика",
                "Иностранный язык (английский)",
                "Английский язык",
                "Алгебра",
                "Немецкий язык",
                "Английский язык",
                "Алгебра",
                "Немецкий язык",
            )*/