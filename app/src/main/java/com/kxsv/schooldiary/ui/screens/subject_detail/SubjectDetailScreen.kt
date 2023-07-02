package com.kxsv.schooldiary.ui.screens.subject_detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.features.subjects.Subject
import com.kxsv.schooldiary.util.ui.LoadingContent
import com.kxsv.schooldiary.util.ui.SubjectDetailTopAppBar

@Composable
fun SubjectDetailScreen(
    // TODO: add other things for grades, etc...
    onEditSubject: (Long) -> Unit,
    onBack: () -> Unit,
    onDeleteSubject: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SubjectDetailViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val uiState = viewModel.uiState.collectAsState().value
    androidx.compose.material3.Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize(),
        topBar = {
            SubjectDetailTopAppBar(
                uiState.subject?.name,
                onBack,
                viewModel::deleteSubject
            )
        },
    ) { paddingValues ->
        SubjectContent(
            loading = uiState.isLoading,
            empty = uiState.subject == null && !uiState.isLoading,
            subject = uiState.subject,
            onEditSubject = onEditSubject,
            modifier = Modifier.padding(paddingValues)
        )

        uiState.userMessage?.let { userMessage ->
            val snackbarText = stringResource(userMessage)
            LaunchedEffect(snackbarHostState, viewModel, userMessage, snackbarText) {
                snackbarHostState.showSnackbar(snackbarText)
                viewModel.snackbarMessageShown()
            }
        }

        LaunchedEffect(uiState.isSubjectDeleted) {
            if (uiState.isSubjectDeleted) {
                onDeleteSubject()
            }
        }
    }
}

@Composable
fun SubjectContent(
    loading: Boolean,
    empty: Boolean,
    subject: Subject?,
    onEditSubject: (Long) -> Unit,
    modifier: Modifier,
) {
    val screenPadding = Modifier.padding(
        horizontal = dimensionResource(id = R.dimen.horizontal_margin),
        vertical = dimensionResource(id = R.dimen.vertical_margin),
    )
    val commonModifier = modifier
        .fillMaxWidth()
        .then(screenPadding)

    LoadingContent(
        loading,
        empty,
        emptyContent = { Text(text = stringResource(R.string.no_data), modifier = commonModifier) },
        onRefresh = {}
    ) {
        Column {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(screenPadding)
            ) {
                // TODO: make noContent cover
                Column(
                    modifier = Modifier.padding(dimensionResource(R.dimen.vertical_margin))
                ) {
                    if (!loading) {
                        // TODO: remake for teacher full name
                        Text(
                            text = subject!!.name,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = subject.cabinet,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    Button(onClick = { onEditSubject(subject?.subjectId!!) }) {
                        Text(
                            text = stringResource(R.string.edit_subject),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

            }

        }
    }
}
