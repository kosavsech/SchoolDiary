package com.kxsv.schooldiary.ui.screens.subject_list.subject_detail.add_edit

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.local.features.teacher.TeacherEntity
import com.kxsv.schooldiary.data.repository.EduPerformanceRepository
import com.kxsv.schooldiary.data.repository.GradeRepository
import com.kxsv.schooldiary.data.repository.SubjectRepository
import com.kxsv.schooldiary.data.repository.TeacherRepository
import com.kxsv.schooldiary.di.util.AppDispatchers
import com.kxsv.schooldiary.di.util.Dispatcher
import com.kxsv.schooldiary.ui.main.navigation.ADD_RESULT_OK
import com.kxsv.schooldiary.ui.screens.navArgs
import com.kxsv.schooldiary.ui.util.Async
import com.kxsv.schooldiary.ui.util.WhileUiSubscribed
import com.kxsv.schooldiary.util.Extensions.nonEmptyTrim
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

data class AddEditSubjectUiState(
	val fullName: String = "",
	val displayName: String = "",
	val cabinet: String = "",
	val selectedTeachersIds: Set<String> = emptySet(),
	val availableTeachers: List<TeacherEntity> = emptyList(),
	
	val firstName: String = "",
	val lastName: String = "",
	val patronymic: String = "",
	val teacherId: Int? = null,
	val phoneNumber: String = "",
	
	val isTeacherCreated: Boolean = false,
	val isSubjectSaved: Boolean = false,
	val isLoading: Boolean = false,
	val userMessage: Int? = null,
	val errorMessage: Int? = null,
)

private const val TAG = "AddEditSubjectViewModel"

@HiltViewModel
class AddEditSubjectViewModel @Inject constructor(
	private val subjectRepository: SubjectRepository,
	private val teacherRepository: TeacherRepository,
	private val eduPerformanceRepository: EduPerformanceRepository,
	private val gradeRepository: GradeRepository,
	@Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
	savedStateHandle: SavedStateHandle,
) : ViewModel() {
	
	val navArgs: AddEditSubjectScreenNavArgs = savedStateHandle.navArgs()
	val subjectId = navArgs.subjectId
	
	private val _availableTeachersAsync = teacherRepository.observeTeachers()
		.map { Async.Success(it) }
		.catch<Async<List<TeacherEntity>>> { emit(Async.Error(R.string.loading_teachers_error)) }
	
	private val _uiState = MutableStateFlow(AddEditSubjectUiState())
	val uiState = combine(
		_uiState, _availableTeachersAsync
	) { state, availableTeachers ->
		when (availableTeachers) {
			Async.Loading -> AddEditSubjectUiState(isLoading = true)
			
			is Async.Error -> AddEditSubjectUiState(userMessage = availableTeachers.errorMessage)
			
			is Async.Success -> {
				state.copy(
					availableTeachers = availableTeachers.data
				)
			}
		}
	}.stateIn(viewModelScope, WhileUiSubscribed, AddEditSubjectUiState(isLoading = true))
	
	init {
		if (subjectId != null) loadSubject(subjectId)
	}
	
	fun showEditResultMessage(result: Int) {
		when (result) {
//			EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_teacher_message)
			ADD_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_teacher_message)
//			DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_teacher_message)
		}
	}
	
	private fun showSnackbarMessage(message: Int) {
		_uiState.update {
			it.copy(
				userMessage = message
			)
		}
	}
	
	fun saveSubject() {
		if (uiState.value.fullName.isEmpty()) {
			_uiState.update {
				it.copy(
					userMessage = R.string.empty_subject_message,
					isSubjectSaved = false
				)
			}
			return
		}
		
		viewModelScope.launch(ioDispatcher) {
			try {
				if (subjectId == null) {
					createNewSubject()
				} else {
					updateSubject()
				}
				_uiState.update { it.copy(isSubjectSaved = true) }
			} catch (e: SQLiteConstraintException) {
				_uiState.update {
					it.copy(
						userMessage = R.string.full_name_is_reserved,
						isSubjectSaved = false
					)
				}
			}
		}
	}
	
	fun snackbarMessageShown() {
		_uiState.update {
			it.copy(userMessage = null)
		}
	}
	
	fun updateFullName(name: String) {
		_uiState.update {
			it.copy(
				fullName = name
			)
		}
	}
	
	fun updateDisplayName(name: String) {
		_uiState.update {
			it.copy(
				displayName = name
			)
		}
	}
	
	fun updateCabinet(newCabinet: String) {
		_uiState.update {
			it.copy(
				cabinet = newCabinet
			)
		}
	}
	
	fun saveNewTeacher() {
		viewModelScope.launch(ioDispatcher) {
			try {
				createNewTeacher()
				showEditResultMessage(ADD_RESULT_OK)
				_uiState.update { it.copy(isTeacherCreated = true) }
			} catch (e: SQLiteConstraintException) {
				_uiState.update {
					it.copy(
						errorMessage = R.string.full_name_is_reserved,
						isTeacherCreated = false
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
				teacherId = null
			)
		}
	}
	
	private suspend fun createNewTeacher() {
		teacherRepository.createTeacher(
			TeacherEntity(
				lastName = uiState.value.lastName,
				firstName = uiState.value.firstName,
				patronymic = uiState.value.patronymic,
				phoneNumber = uiState.value.phoneNumber
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
	
	fun updateSelectedTeachers(newSelectedTeachersIds: Set<String>) {
		Log.i(TAG, "updateSelectedTeachers: newSelectedTeachersIds $newSelectedTeachersIds")
		_uiState.update { it.copy(selectedTeachersIds = newSelectedTeachersIds) }
	}
	
	private suspend fun createNewSubject() {
		subjectRepository.createSubject(
			subject = SubjectEntity(
				fullName = uiState.value.fullName.trim(),
				cabinet = uiState.value.cabinet.nonEmptyTrim(),
				displayName = uiState.value.displayName.nonEmptyTrim()
			),
			teachersIds = uiState.value.selectedTeachersIds
		)
	}
	
	private suspend fun updateSubject() {
		if (subjectId == null) throw RuntimeException("updateSubject() was called but subject is new.")
		Log.d(
			TAG,
			"updateSubject: updating subject ${uiState.value.fullName.trim()} id = $subjectId"
		)
		subjectRepository.updateSubject(
			subject = SubjectEntity(
				fullName = uiState.value.fullName.trim(),
				cabinet = uiState.value.cabinet.nonEmptyTrim(),
				displayName = uiState.value.displayName.nonEmptyTrim(),
				subjectId = subjectId
			),
			teachersIds = uiState.value.selectedTeachersIds
		)
	}
	
	private fun loadSubject(subjectId: String) {
		_uiState.update {
			it.copy(isLoading = true)
		}
		
		viewModelScope.launch(ioDispatcher) {
			subjectRepository.getSubjectWithTeachers(subjectId).let { subjectWithTeachers ->
				if (subjectWithTeachers != null) {
					val selectedTeachersIds = subjectWithTeachers.teachers.map {
						it.teacherId
					}.toSet()
					_uiState.update {
						it.copy(
							fullName = subjectWithTeachers.subject.fullName,
							displayName = subjectWithTeachers.subject.getDisplayNameString(),
							selectedTeachersIds = selectedTeachersIds,
							cabinet = subjectWithTeachers.subject.getCabinetString(),
							isLoading = false
						)
					}
				} else {
					_uiState.update {
						it.copy(isLoading = false)
					}
				}
			}
		}
	}
	
	fun clearErrorMessage() {
		_uiState.update {
			it.copy(errorMessage = null)
		}
	}
}