package com.kxsv.schooldiary.ui.screens.subject_list.subject_detail.add_edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.local.features.teacher.TeacherEntity
import com.kxsv.schooldiary.data.repository.SubjectRepository
import com.kxsv.schooldiary.data.repository.TeacherRepository
import com.kxsv.schooldiary.ui.main.navigation.ADD_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.EDIT_RESULT_OK
import com.kxsv.schooldiary.ui.screens.navArgs
import com.kxsv.schooldiary.util.Utils.nonEmptyTrim
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditSubjectUiState(
	val fullName: String = "",
	val displayName: String = "",
	val cabinet: String = "",
	val initialSelection: Set<Int> = emptySet(),
	val selectedTeachers: Set<TeacherEntity> = emptySet(),
	val availableTeachers: List<TeacherEntity> = emptyList(),
	val isLoading: Boolean = false,
	val userMessage: Int? = null,
)

@HiltViewModel
class AddEditSubjectViewModel @Inject constructor(
	private val subjectRepository: SubjectRepository,
	private val teacherRepository: TeacherRepository,
	savedStateHandle: SavedStateHandle,
) : ViewModel() {
	
	val navArgs: AddEditSubjectScreenNavArgs = savedStateHandle.navArgs()
	val subjectId = navArgs.subjectId
	
	private val _uiState = MutableStateFlow(AddEditSubjectUiState())
	val uiState: StateFlow<AddEditSubjectUiState> = _uiState.asStateFlow()
	
	init {
		if (subjectId != null) loadSubject(subjectId)
	}
	
	fun saveSubject(): Int? {
		if (uiState.value.fullName.isEmpty()) {
			_uiState.update {
				it.copy(userMessage = R.string.empty_subject_message)
			}
			return null
		}
		
		return if (subjectId == null) {
			createNewSubject()
			ADD_RESULT_OK
		} else {
			updateSubject()
			EDIT_RESULT_OK
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
	
	fun saveSelectedTeachers(newIndices: Set<Int>) {
		val newSelectedTeachers: MutableSet<TeacherEntity> = mutableSetOf()
		newIndices.forEach { index ->
			newSelectedTeachers.add(uiState.value.availableTeachers[index])
		}
		_uiState.update {
			it.copy(
				selectedTeachers = newSelectedTeachers,
				initialSelection = emptySet()
			)
		}
	}
	
	fun loadAvailableTeachers() {
		_uiState.update {
			it.copy(isLoading = true)
		}
		
		viewModelScope.launch {
			teacherRepository.getTeachers().let { teachers ->
				val updatedSelectedTeachersIndices: MutableSet<Int> = mutableSetOf()
				uiState.value.selectedTeachers.forEach { teacher ->
					updatedSelectedTeachersIndices.add(
						teachers.binarySearch(teacher, compareBy { it.patronymic })
					)
				}
				_uiState.update {
					it.copy(
						availableTeachers = teachers,
						initialSelection = updatedSelectedTeachersIndices,
						isLoading = false
					)
				}
			}
		}
	}
	
	private fun createNewSubject() = viewModelScope.launch {
		subjectRepository.createSubject(
			SubjectEntity(
				uiState.value.fullName.trim(),
				uiState.value.displayName.nonEmptyTrim(),
				uiState.value.cabinet.nonEmptyTrim()
			),
			uiState.value.selectedTeachers
		)
	}
	
	private fun updateSubject() {
		if (subjectId == null) throw RuntimeException("updateSubject() was called but subject is new.")
		
		viewModelScope.launch {
			subjectRepository.updateSubject(
				subject = SubjectEntity(
					fullName = uiState.value.fullName.trim(),
					cabinet = uiState.value.cabinet.nonEmptyTrim(),
					displayName = uiState.value.displayName.nonEmptyTrim(),
					subjectId = subjectId
				),
				teachers = uiState.value.selectedTeachers
			)
		}
	}
	
	private fun loadSubject(subjectId: Long) {
		_uiState.update {
			it.copy(isLoading = true)
		}
		
		viewModelScope.launch {
			subjectRepository.getSubjectWithTeachers(subjectId).let { subjectWithTeachers ->
				if (subjectWithTeachers != null) {
					_uiState.update {
						it.copy(
							fullName = subjectWithTeachers.subject.fullName,
							displayName = subjectWithTeachers.subject.getDisplayNameString(),
							cabinet = subjectWithTeachers.subject.getCabinetString(),
							selectedTeachers = subjectWithTeachers.teachers,
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
}