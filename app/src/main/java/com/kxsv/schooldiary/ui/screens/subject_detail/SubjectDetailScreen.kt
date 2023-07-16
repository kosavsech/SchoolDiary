package com.kxsv.schooldiary.ui.screens.subject_detail

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.grade.Grade
import com.kxsv.schooldiary.data.local.features.subject.Subject
import com.kxsv.schooldiary.util.ui.LoadingContent
import com.kxsv.schooldiary.util.ui.SubjectDetailTopAppBar
import java.time.format.DateTimeFormatter

@Composable
fun SubjectDetailScreen(
	@StringRes userMessage: Int?,
	onGradeClick: (Long) -> Unit,
	onEditSubject: (Long) -> Unit,
	onBack: () -> Unit,
	onDeleteSubject: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: SubjectDetailViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
	val uiState = viewModel.uiState.collectAsState().value
	Scaffold(
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		modifier = modifier.fillMaxSize(),
		topBar = {
			SubjectDetailTopAppBar(
				title = uiState.subject?.getName(),
				onBack = onBack,
				onDelete = viewModel::deleteSubject
			)
		},
	) { paddingValues ->
		SubjectContent(
			loading = uiState.isLoading,
			empty = (uiState.subject == null && uiState.grades.isEmpty()) && !uiState.isLoading,
			subject = uiState.subject,
			grades = uiState.grades,
			onGradeClick = onGradeClick,
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
		
		if (userMessage != null) {
			LaunchedEffect(userMessage != 0) {
				viewModel.showEditResultMessage(userMessage)
			}
		}
	}
}

@Composable
private fun SubjectContent(
	loading: Boolean,
	empty: Boolean,
	subject: Subject?,
	grades: List<Grade>,
	onGradeClick: (Long) -> Unit,
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
		loading = loading,
		isContentScrollable = true,
		empty = empty,
		emptyContent = { Text(text = stringResource(R.string.no_data), modifier = commonModifier) },
		onRefresh = {}
	) {
		Column {
			ElevatedCard(
				modifier = commonModifier
			) {
				// TODO: make noContent cover
				if (subject != null) {
					Column(
						modifier = Modifier.padding(dimensionResource(R.dimen.vertical_margin))
					) {
						Text(
							text = subject.getName(),
							style = MaterialTheme.typography.titleMedium,
						)
						Text(
							text = subject.getCabinetString(),
							style = MaterialTheme.typography.titleMedium,
						)
						Button(onClick = { onEditSubject(subject.subjectId) }) {
							Text(
								text = stringResource(R.string.edit_subject),
								style = MaterialTheme.typography.labelMedium
							)
						}
					}
				}
			}
			ElevatedCard(
				modifier = commonModifier
			) {
				// TODO: make noContent cover
				if (subject != null) {
					Column(
						modifier = Modifier.padding(dimensionResource(R.dimen.vertical_margin))
					) {
						Row {
							Text(
								text = "Average mark:",
								style = MaterialTheme.typography.titleMedium,
							)
						}
					}
				}
			}
			LazyColumn {
				items(grades) { grade ->
					Row(
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.SpaceBetween,
						modifier = Modifier
							.fillMaxWidth()
							.clickable { onGradeClick(grade.gradeId) }
							.padding(
								horizontal = dimensionResource(R.dimen.horizontal_margin),
								vertical = dimensionResource(R.dimen.vertical_margin)
							)
					) {
						Text(
							text = grade.mark.getValue(),
							style = MaterialTheme.typography.titleMedium,
						)
						Text(
							text = grade.date.format(DateTimeFormatter.ISO_LOCAL_DATE),
							style = MaterialTheme.typography.titleMedium,
						)
					}
				}
			}
		}
	}
}
