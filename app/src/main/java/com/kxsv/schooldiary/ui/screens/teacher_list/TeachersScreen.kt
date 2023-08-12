package com.kxsv.schooldiary.ui.screens.teacher_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DrawerState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.teacher.TeacherEntity
import com.kxsv.schooldiary.data.local.features.teacher.TeacherEntity.Companion.shortName
import com.kxsv.schooldiary.ui.main.app_bars.topbar.TeachersTopAppBar
import com.kxsv.schooldiary.util.ui.LoadingContent
import com.ramcosta.composedestinations.annotation.Destination
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Destination
@Composable
fun TeachersScreen(
	drawerState: DrawerState,
	coroutineScope: CoroutineScope,
	viewModel: TeachersViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState,
) {
	val teacherDialogState = rememberMaterialDialogState(false)
	Scaffold(
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		modifier = Modifier.fillMaxSize(),
		topBar = { TeachersTopAppBar(openDrawer = { coroutineScope.launch { drawerState.open() } }) },
		floatingActionButton = {
			FloatingActionButton(onClick = { teacherDialogState.show() }) {
				Icon(
					imageVector = Icons.Default.Add,
					contentDescription = stringResource(R.string.add_teacher)
				)
			}
		}
	) { paddingValues ->
		val uiState = viewModel.uiState.collectAsState().value
		
		val onTeacherClick = remember<(TeacherEntity) -> Unit> {
			{
				viewModel.onTeacherClick(it)
				teacherDialogState.show()
			}
		}
		val deleteTeacher = remember<(String) -> Unit> {
			{ viewModel.deleteTeacher(it) }
		}
		TeachersContent(
			isLoading = uiState.isLoading,
			teacherEntities = uiState.teachers,
			onTeacherClick = onTeacherClick,
			onDeleteClick = deleteTeacher,
			modifier = Modifier.padding(paddingValues)
		)
		
		val saveTeacher = remember {
			{ viewModel.saveTeacher() }
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
		AddEditTeacherDialog(
			dialogState = teacherDialogState,
			firstName = uiState.firstName,
			lastName = uiState.lastName,
			patronymic = uiState.patronymic,
			phoneNumber = uiState.phoneNumber,
			updateFirstName = updateFirstName,
			updateLastName = updateLastName,
			updatePatronymic = updatePatronymic,
			updatePhoneNumber = updatePhoneNumber,
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
private fun TeachersContent(
	isLoading: Boolean,
	teacherEntities: List<TeacherEntity>,
	onTeacherClick: (TeacherEntity) -> Unit,
	onDeleteClick: (String) -> Unit,
	modifier: Modifier = Modifier,
) {
	LoadingContent(
		modifier = modifier,
		loading = isLoading,
		empty = teacherEntities.isEmpty(),
		isContentScrollable = true,
		emptyContent = {
			Box(
				modifier = Modifier.fillMaxSize(),
			) {
				Text(
					text = stringResource(R.string.no_teachers_yet),
					style = MaterialTheme.typography.displayMedium,
					modifier = Modifier.align(Alignment.Center)
				)
			}
		}
	) {
		LazyColumn(
			contentPadding = PaddingValues(vertical = dimensionResource(R.dimen.vertical_margin)),
		) {
			items(teacherEntities) { teacherEntity ->
				TeacherItem(
					teacherEntity = teacherEntity,
					onTeacherClick = { onTeacherClick(teacherEntity) },
					onDeleteClick = { onDeleteClick(teacherEntity.teacherId) }
				)
			}
		}
	}
}

@Composable
private fun TeacherItem(
	teacherEntity: TeacherEntity,
	onTeacherClick: () -> Unit,
	onDeleteClick: () -> Unit,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onTeacherClick() }
			.padding(vertical = dimensionResource(R.dimen.list_item_padding)),
		horizontalArrangement = Arrangement.Center,
		verticalAlignment = Alignment.CenterVertically
	) {
		Column(
			modifier = Modifier
				.weight(1f)
				.padding(horizontal = dimensionResource(R.dimen.horizontal_margin))
		) {
			Text(
				text = teacherEntity.shortName(),
				style = MaterialTheme.typography.bodyLarge,
			)
			if (teacherEntity.phoneNumber.isNotBlank()) {
				Text(
					text = teacherEntity.phoneNumber,
					style = MaterialTheme.typography.labelMedium
				)
			}
		}
		IconButton(onClick = onDeleteClick) {
			Icon(
				imageVector = Icons.Default.Delete,
				contentDescription = stringResource(R.string.delete_teacher)
			)
		}
	}
}

@Preview
@Composable
private fun TeachersContentPreview() {
	Surface {
		TeachersContent(
			isLoading = false,
			teacherEntities = listOf(
				TeacherEntity(
					lastName = "Stepanov",
					firstName = "Ivan",
					patronymic = "Petrovich",
					phoneNumber = "+756248932572"
				),
				TeacherEntity(
					lastName = "Ivanov",
					firstName = "Stepan",
					patronymic = "Vasilievich",
					phoneNumber = ""
				),
				TeacherEntity(
					lastName = "Kostilev",
					firstName = "Oleg",
					patronymic = "Sergeevich",
					phoneNumber = "+756248932572"
				),
				TeacherEntity(
					lastName = "Simonov",
					firstName = "Alex",
					patronymic = "Yegorovich",
					phoneNumber = "+756248932572"
				),
			),
			onTeacherClick = {}, onDeleteClick = {}
		)
	}
}

@Preview
@Composable
private fun TeacherItemPreview() {
	Surface {
		TeacherItem(
			teacherEntity = TeacherEntity(
				lastName = "Stepanov",
				firstName = "Ivan",
				patronymic = "Yegorovich",
				phoneNumber = "+756248932572"
			),
			onTeacherClick = {}, onDeleteClick = {}
		)
	}
}
