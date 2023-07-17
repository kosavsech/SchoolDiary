package com.kxsv.schooldiary.ui.screens.grade_list.add_edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.AppDestinationsArgs
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.grade.Grade
import com.kxsv.schooldiary.data.local.features.subject.Subject
import com.kxsv.schooldiary.domain.GradeRepository
import com.kxsv.schooldiary.domain.SubjectRepository
import com.kxsv.schooldiary.util.Mark
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AddEditGradeUiState(
	val availableSubjects: List<Subject> = emptyList(),
	val mark: String = "",
	val typeOfWork: String = "",
	val gradeDate: LocalDate? = null,
	val initialSubjectSelection: Int? = null,
	val pickedSubject: Subject? = null,
	val isLoading: Boolean = false,
	val userMessage: Int? = null,
	val isGradeSaved: Boolean = false,
)

@HiltViewModel
class AddEditGradeViewModel @Inject constructor(
	private val gradeRepository: GradeRepository,
	private val subjectRepository: SubjectRepository,
	savedStateHandle: SavedStateHandle,
) : ViewModel() {
	
	private val gradeId: Long = savedStateHandle[AppDestinationsArgs.GRADE_ID_ARG]!!
	
	private val _uiState = MutableStateFlow(AddEditGradeUiState())
	val uiState: StateFlow<AddEditGradeUiState> = _uiState.asStateFlow()
	
	init {
		if (gradeId != 0L) loadGrade(gradeId)
	}
	
	fun saveGrade() {
		if (uiState.value.mark.isEmpty() ||
			uiState.value.pickedSubject == null ||
			uiState.value.gradeDate == null
		) {
			_uiState.update {
				it.copy(userMessage = R.string.fill_required_fields_message)
			}
			return
		}
		
		if (gradeId == 0L) {
			createNewMark()
		} else {
			updateGrade()
		}
	}
	
	fun snackbarMessageShown() {
		_uiState.update {
			it.copy(userMessage = null)
		}
	}
	
	fun saveSelectedSubject(newIndex: Int) {
		val newPickedSubject: Subject = uiState.value.availableSubjects[newIndex]
		_uiState.update {
			it.copy(
				pickedSubject = newPickedSubject,
				initialSubjectSelection = null
			)
		}
	}
	
	fun loadAvailableSubjects() {
		_uiState.update {
			it.copy(isLoading = true)
		}
		
		viewModelScope.launch {
			subjectRepository.getSubjects().let { subjects ->
				val updatedSelectedSubjectIndex: Int =
					subjects.binarySearch(uiState.value.pickedSubject, compareBy { it?.subjectId })
				
				_uiState.update {
					it.copy(
						availableSubjects = subjects,
						initialSubjectSelection = updatedSelectedSubjectIndex,
						isLoading = false
					)
				}
			}
		}
	}
	
	private fun createNewMark() = viewModelScope.launch {
		gradeRepository.createGrade(
			Grade(
				mark = Mark.fromInput(uiState.value.mark),
				typeOfWork = uiState.value.typeOfWork,
				date = uiState.value.gradeDate!!,
				subjectMasterId = uiState.value.pickedSubject!!.subjectId,
			)
		)
		
		_uiState.update {
			it.copy(isGradeSaved = true)
		}
	}
	
	private fun updateGrade() {
		if (gradeId == 0L) throw RuntimeException("updateGrade() was called but grade is new.")
		
		viewModelScope.launch {
			gradeRepository.updateGrade(
				Grade(
					mark = Mark.fromInput(uiState.value.mark),
					typeOfWork = uiState.value.typeOfWork,
					date = uiState.value.gradeDate!!,
					subjectMasterId = uiState.value.pickedSubject!!.subjectId,
					gradeId = gradeId
				)
			)
			
			_uiState.update {
				it.copy(isGradeSaved = true)
			}
		}
	}
	
	private fun loadGrade(gradeId: Long) {
		_uiState.update {
			it.copy(isLoading = true)
		}
		
		viewModelScope.launch {
			gradeRepository.getGrade(gradeId).let { grade ->
				if (grade != null) {
					_uiState.update {
						it.copy(
							mark = grade.mark.getValue(),
							typeOfWork = grade.typeOfWork,
							gradeDate = grade.date,
							pickedSubject = subjectRepository.getSubject(grade.subjectMasterId),
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
	
	fun updateTypeOfWork(newTypeOfWork: String) {
		_uiState.update {
			it.copy(typeOfWork = newTypeOfWork)
		}
	}
	
	fun updateMark(newMark: String) {
		_uiState.update {
			it.copy(mark = newMark)
		}
	}
	
	fun updateDate(newDate: LocalDate) {
		_uiState.update {
			it.copy(gradeDate = newDate)
		}
	}
}