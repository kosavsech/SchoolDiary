package com.kxsv.schooldiary.ui.screens.subject_detail.add_edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.local.features.teacher.TeacherEntity
import com.kxsv.schooldiary.data.repository.SubjectRepository
import com.kxsv.schooldiary.data.repository.TeacherRepository
import com.kxsv.schooldiary.ui.main.navigation.AppDestinationsArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditSubjectUiState(
	val fullName: String = "",
	val displayName: String? = null,
	val cabinet: String = "",
	val initialSelection: Set<Int> = emptySet(),
	val selectedTeachers: Set<TeacherEntity> = emptySet(),
	val availableTeachers: List<TeacherEntity> = emptyList(),
	val isLoading: Boolean = false,
	val userMessage: Int? = null,
	val isSubjectSaved: Boolean = false,
	val isSubjectDeleted: Boolean = false,
)

@HiltViewModel
class AddEditSubjectViewModel @Inject constructor(
	private val subjectRepository: SubjectRepository,
	private val teacherRepository: TeacherRepository,
	savedStateHandle: SavedStateHandle,
) : ViewModel() {
	
	private val subjectId: Long = savedStateHandle[AppDestinationsArgs.SUBJECT_ID_ARG]!!
	
	private val _uiState = MutableStateFlow(AddEditSubjectUiState())
	val uiState: StateFlow<AddEditSubjectUiState> = _uiState.asStateFlow()
	
	init {
		if (subjectId != 0L) loadSubject(subjectId)
	}
	
	fun saveSubject() {
		if (uiState.value.fullName.isEmpty()) {
			_uiState.update {
				it.copy(userMessage = R.string.empty_subject_message)
			}
			return
		}
		
		if (subjectId == 0L) {
			createNewSubject()
		} else {
			updateSubject()
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
			SubjectEntity(uiState.value.fullName, uiState.value.displayName, uiState.value.cabinet),
			uiState.value.selectedTeachers
		)
		
		_uiState.update {
			it.copy(isSubjectSaved = true)
		}
	}
	
	private fun updateSubject() {
		if (subjectId == 0L) throw RuntimeException("updateSubject() was called but subject is new.")
		
		viewModelScope.launch {
			subjectRepository.updateSubject(
				subject = SubjectEntity(
					fullName = uiState.value.fullName,
					cabinet = uiState.value.cabinet,
					displayName = uiState.value.displayName,
					subjectId = subjectId
				),
				teachers = uiState.value.selectedTeachers
			)
			_uiState.update {
				it.copy(isSubjectSaved = true)
			}
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
							displayName = subjectWithTeachers.subject.displayName,
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