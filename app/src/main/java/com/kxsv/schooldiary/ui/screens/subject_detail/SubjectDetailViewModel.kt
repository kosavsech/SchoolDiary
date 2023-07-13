package com.kxsv.schooldiary.ui.screens.subject_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.ADD_EDIT_RESULT_OK
import com.kxsv.schooldiary.AppDestinationsArgs
import com.kxsv.schooldiary.DELETE_RESULT_OK
import com.kxsv.schooldiary.EDIT_RESULT_OK
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.features.grade.Grade
import com.kxsv.schooldiary.data.features.subject.Subject
import com.kxsv.schooldiary.domain.GradeRepository
import com.kxsv.schooldiary.domain.SubjectRepository
import com.kxsv.schooldiary.util.Async
import com.kxsv.schooldiary.util.WhileUiSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubjectDetailUiState(
	val subject: Subject? = null,
	val grades: List<Grade> = emptyList(),
	val isLoading: Boolean = false,
	val userMessage: Int? = null,
	val isSubjectDeleted: Boolean = false,
)

@HiltViewModel
class SubjectDetailViewModel @Inject constructor(
	private val subjectRepository: SubjectRepository,
	gradeRepository: GradeRepository,
	savedStateHandle: SavedStateHandle,
) : ViewModel() {
	
	val subjectId: Long = savedStateHandle[AppDestinationsArgs.SUBJECT_ID_ARG]!!
	
	private val _subjectAsync =
		subjectRepository.getSubjectStream(subjectId)
			.map { handleSubject(it) }
			.catch { emit(Async.Error(R.string.loading_subject_error)) }
	
	private val _gradesAsync =
		gradeRepository.getGradesBySubjectIdStream(subjectId)
			.map { Async.Success(it) }
			.catch<Async<List<Grade>>> { emit(Async.Error(R.string.loading_grades_error)) }
	
	private val _uiState = MutableStateFlow(SubjectDetailUiState())
	val uiState: StateFlow<SubjectDetailUiState> = combine(
		_uiState, _subjectAsync, _gradesAsync
	) { state, subject, grades ->
		when (grades) {
			Async.Loading -> {
				_uiState.update { it.copy(isLoading = true) }
			}
			
			is Async.Error -> {
				_uiState.update { it.copy(userMessage = grades.errorMessage) }
			}
			
			is Async.Success -> {
				_uiState.update { it.copy(grades = grades.data) }
			}
		}
		when (subject) {
			Async.Loading -> {
				SubjectDetailUiState(isLoading = true)
			}
			
			is Async.Error -> {
				SubjectDetailUiState(
					userMessage = subject.errorMessage,
					isSubjectDeleted = state.isSubjectDeleted
				)
			}
			
			is Async.Success -> {
				SubjectDetailUiState(
					subject = subject.data,
					grades = state.grades,
					userMessage = state.userMessage,
					isLoading = state.isLoading,
					isSubjectDeleted = state.isSubjectDeleted
				)
			}
		}
	}.stateIn(viewModelScope, WhileUiSubscribed, SubjectDetailUiState(isLoading = true))
	
	fun deleteSubject() = viewModelScope.launch {
		subjectRepository.deleteSubject(subjectId)
		_uiState.update {
			it.copy(isSubjectDeleted = true)
		}
	}
	
	fun snackbarMessageShown() {
		_uiState.update {
			it.copy(userMessage = null)
		}
	}
	
	private fun showSnackbarMessage(message: Int) {
		_uiState.update {
			it.copy(userMessage = message)
		}
	}
	
	private fun handleSubject(subject: Subject?): Async<Subject?> {
		if (subject == null) {
			return Async.Error(R.string.subject_not_found)
		}
		return Async.Success(subject)
	}
	
	fun showEditResultMessage(result: Int) {
		when (result) {
			EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_grade_message)
			ADD_EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_grade_message)
			DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_grade_message)
		}
	}
}
