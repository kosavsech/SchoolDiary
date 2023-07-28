package com.kxsv.schooldiary.ui.screens.subject_list

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.ui.main.app_bars.topbar.SubjectsTopAppBar
import com.kxsv.schooldiary.util.ui.LoadingContent

@Composable
fun SubjectsScreen(
	@StringRes userMessage: Int,
	onUserMessageDisplayed: () -> Unit,
	onAddSubject: () -> Unit,
	onSubjectClick: (SubjectEntity) -> Unit,
	openDrawer: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: SubjectsViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
	Scaffold(
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		topBar = { SubjectsTopAppBar(openDrawer = openDrawer) },
		modifier = modifier.fillMaxSize(),
		floatingActionButton = {
			FloatingActionButton(onClick = onAddSubject) {
				Icon(Icons.Default.Add, stringResource(R.string.add_subject))
			}
		}
	) { paddingValues ->
		val uiState = viewModel.uiState.collectAsState().value
		
		SubjectsContent(
			loading = uiState.isLoading,
			subjects = uiState.subjects,
			//noSubjectsLabel = 0,
			onSubjectClick = onSubjectClick,
			modifier = Modifier.padding(paddingValues),
		)
		
		// Check for user messages to display on the screen
		uiState.userMessage?.let { userMessage ->
			val snackbarText = stringResource(userMessage)
			LaunchedEffect(snackbarText) {
				snackbarHostState.showSnackbar(snackbarText)
				viewModel.snackbarMessageShown()
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
private fun SubjectsContent(
	loading: Boolean,
	subjects: List<SubjectEntity>,
	// TODO
	//  @StringRes noSubjectsLabel: Int,
	//  onRefresh: () -> Unit,
	onSubjectClick: (SubjectEntity) -> Unit,
	modifier: Modifier,
) {
	LoadingContent(
		modifier = modifier,
		loading = loading,
		isContentScrollable = true,
		empty = subjects.isEmpty(),
		emptyContent = { Text(text = "No subjects for yet") },
		onRefresh = { /*TODO*/ }
	) {
		LazyColumn(
			contentPadding = PaddingValues(vertical = dimensionResource(R.dimen.list_item_padding)),
		) {
			items(subjects) { subject ->
				SubjectItem(
					subject = subject,
					onSubjectClick = onSubjectClick,
				)
			}
		}
	}
}

@Composable
private fun SubjectItem(
	subject: SubjectEntity,
	onSubjectClick: (SubjectEntity) -> Unit,
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onSubjectClick(subject) }
			.padding(
				horizontal = dimensionResource(R.dimen.horizontal_margin),
				vertical = dimensionResource(R.dimen.vertical_margin)
			)
	) {
		Text(
			text = subject.getName(),
			style = MaterialTheme.typography.titleMedium,
		)
	}
	
}

@Preview
@Composable
private fun SubjectsContentPreview() {
	Surface {
		SubjectsContent(
			loading = false,
			subjects = listOf(
				SubjectEntity("Algebra", cabinet = "52"),
				SubjectEntity("Algebra", cabinet = "48"),
				SubjectEntity("Algebra", cabinet = "62"),
				SubjectEntity("Algebra", cabinet = "75"),
			),
			onSubjectClick = {},
			modifier = Modifier
		)
	}
}

@Preview
@Composable
private fun SubjectItemPreview() {
	Surface {
		SubjectItem(subject = SubjectEntity("Algebra", cabinet = "850"), onSubjectClick = {})
	}
}