package com.kxsv.schooldiary.ui.screens.teacher_list

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.teacher.TeacherEntity
import com.kxsv.schooldiary.data.repository.TeacherRepository
import com.kxsv.schooldiary.di.util.AppDispatchers
import com.kxsv.schooldiary.di.util.Dispatcher
import com.kxsv.schooldiary.ui.main.navigation.ADD_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.DELETE_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.EDIT_RESULT_OK
import com.kxsv.schooldiary.ui.util.Async
import com.kxsv.schooldiary.ui.util.WhileUiSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeachersUiState(
	val teachers: List<TeacherEntity> = emptyList(),
	val firstName: String = "",
	val lastName: String = "",
	val patronymic: String = "",
	val teacherId: String = "",
	val phoneNumber: String = "",
	
	val isTeacherSaved: Boolean = false,
	val isLoading: Boolean = false,
	val userMessage: Int? = null,
	val errorMessage: Int? = null,
)

@HiltViewModel
class TeachersViewModel @Inject constructor(
	private val teacherRepository: TeacherRepository,
	@Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
	
	private val _teachersAsync = teacherRepository.observeTeachers()
		.map { Async.Success(it) }
		.catch<Async<List<TeacherEntity>>> { emit(Async.Error(R.string.loading_teachers_error)) }
	
	private val _uiState = MutableStateFlow(TeachersUiState())
	val uiState = combine(_uiState, _teachersAsync) { state, teachersAsync ->
		when (teachersAsync) {
			Async.Loading -> TeachersUiState(isLoading = true)
			
			is Async.Error -> TeachersUiState(userMessage = teachersAsync.errorMessage)
			
			is Async.Success -> {
				state.copy(
					teachers = teachersAsync.data,
				)
			}
		}
	}.stateIn(viewModelScope, WhileUiSubscribed, TeachersUiState(isLoading = true))
	
	private fun showSnackbarMessage(message: Int) {
		_uiState.update {
			it.copy(
				userMessage = message
			)
		}
	}
	
	fun snackbarMessageShown() {
		_uiState.update { it.copy(userMessage = null) }
	}
	
	fun deleteTeacher(teacherId: String) {
		if (teacherId == "") throw RuntimeException("deleteTeacher() was called but no teacher id is provided.")
		viewModelScope.launch(ioDispatcher) {
			teacherRepository.deleteTeacher(teacherId)
			showEditResultMessage(DELETE_RESULT_OK)
		}
	}
	
	fun showEditResultMessage(result: Int) {
		when (result) {
			EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_teacher_message)
			ADD_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_teacher_message)
			DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_teacher_message)
		}
	}
	
	fun saveTeacher() {
		viewModelScope.launch(ioDispatcher) {
			try {
				if (uiState.value.teacherId == "") {
					createNewTeacher()
					showEditResultMessage(ADD_RESULT_OK)
				} else {
					updateTeacher()
					showEditResultMessage(EDIT_RESULT_OK)
				}
				_uiState.update { it.copy(isTeacherSaved = true) }
			} catch (e: SQLiteConstraintException) {
				_uiState.update {
					it.copy(
						errorMessage = R.string.full_name_is_reserved,
						isTeacherSaved = false
					)
				}
			}
		}
	}
	
	fun eraseData() {
		_uiState.update {
			it.copy(
				firstName = "",
				lastName = "",
				patronymic = "",
				phoneNumber = "",
				teacherId = "",
				isTeacherSaved = false
			)
		}
	}
	
	private suspend fun createNewTeacher() {
		teacherRepository.createTeacher(
			TeacherEntity(
				lastName = uiState.value.lastName.trim(),
				firstName = uiState.value.firstName.trim(),
				patronymic = uiState.value.patronymic.trim(),
				phoneNumber = uiState.value.phoneNumber.trim()
			)
		)
	}
	
	private suspend fun updateTeacher() {
		if (uiState.value.teacherId == "") throw RuntimeException("upsert() was called but no teacher is new.")
		teacherRepository.update(
			TeacherEntity(
				lastName = uiState.value.lastName.trim(),
				firstName = uiState.value.firstName.trim(),
				patronymic = uiState.value.patronymic.trim(),
				phoneNumber = uiState.value.phoneNumber.trim(),
				teacherId = uiState.value.teacherId
			)
		)
	}
	
	fun updateFirstName(firstName: String) {
		_uiState.update { it.copy(firstName = firstName) }
	}
	
	fun updateLastName(newLastName: String) {
		_uiState.update { it.copy(lastName = newLastName) }
	}
	
	fun updatePatronymic(newPatronymic: String) {
		_uiState.update { it.copy(patronymic = newPatronymic) }
	}
	
	fun updatePhoneNumber(newPhoneNumber: String) {
		_uiState.update { it.copy(phoneNumber = newPhoneNumber) }
	}
	
	fun onTeacherClick(teacher: TeacherEntity) {
		_uiState.update {
			it.copy(
				firstName = teacher.firstName,
				lastName = teacher.lastName,
				patronymic = teacher.patronymic,
				phoneNumber = teacher.phoneNumber,
				teacherId = teacher.teacherId
			)
		}
	}
	
	fun clearErrorMessage() {
		_uiState.update {
			it.copy(errorMessage = null)
		}
	}
	
}