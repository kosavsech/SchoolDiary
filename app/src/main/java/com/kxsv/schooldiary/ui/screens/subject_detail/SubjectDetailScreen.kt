package com.kxsv.schooldiary.ui.screens.subject_detail

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
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
import com.kxsv.schooldiary.data.local.features.grade.GradeEntity
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.ui.main.topbar.SubjectDetailTopAppBar
import com.kxsv.schooldiary.util.Mark.Companion.getStringValueFrom
import com.kxsv.schooldiary.util.ui.LoadingContent
import java.time.format.DateTimeFormatter

@Composable
fun SubjectDetailScreen(
	@StringRes userMessage: Int?,
	onGradeClick: (String) -> Unit,
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
	subject: SubjectEntity?,
	grades: List<GradeEntity>,
	onGradeClick: (String) -> Unit,
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
			if (subject != null) {
				SubjectInfo(subject = subject, onEditSubject = onEditSubject)
				Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.vertical_margin)))
				ElevatedCard(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = dimensionResource(R.dimen.horizontal_margin)),
				) {
					// TODO: make noContent cover
					Column(
						modifier = Modifier.padding(dimensionResource(R.dimen.vertical_margin))
					) {
						Row {
							Text(
								text = "Average mark: ",
								style = MaterialTheme.typography.titleMedium,
							)
						}
					}
				}
			}
			Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.vertical_margin)))
			GradesHistory(grades = grades, onGradeClick = onGradeClick)
		}
	}
}


@Composable
private fun GradesHistory(
	grades: List<GradeEntity>,
	onGradeClick: (String) -> Unit,
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.wrapContentHeight()
	) {
		Text(
			text = stringResource(R.string.grade_history),
			style = MaterialTheme.typography.titleLarge,
			modifier = Modifier.padding(
				horizontal = dimensionResource(R.dimen.horizontal_margin),
			)
		)
		Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding)))
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
						text = getStringValueFrom(grade.mark),
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

@Composable
private fun SubjectInfo(
	subject: SubjectEntity,
	modifier: Modifier = Modifier,
	onEditSubject: (Long) -> Unit,
) {
	ElevatedCard(
		modifier = modifier
	) {
		// TODO: make noContent cover
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