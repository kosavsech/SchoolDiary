package com.kxsv.schooldiary.ui.screens.grade_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.ADD_EDIT_RESULT_OK
import com.kxsv.schooldiary.DELETE_RESULT_OK
import com.kxsv.schooldiary.EDIT_RESULT_OK
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.features.grade.Grade
import com.kxsv.schooldiary.domain.GradeRepository
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
import javax.inject.Inject

data class GradesUiState(
	val grades: List<Grade> = emptyList(),
	val isLoading: Boolean = false,
	val userMessage: Int? = null,
)

@HiltViewModel
class GradesViewModel @Inject constructor(
	private val gradeRepository: GradeRepository,
) : ViewModel() {
	
	private val _gradesAsync =
		gradeRepository.getGradesStream()
			.map { Async.Success(it) }
			.catch<Async<List<Grade>>> { emit(Async.Error(R.string.loading_grades_error)) }
	
	private val _uiState = MutableStateFlow(GradesUiState())
	val uiState: StateFlow<GradesUiState> = combine(
		_uiState, _gradesAsync
	) { state, grades ->
		when (grades) {
			Async.Loading -> {
				GradesUiState(isLoading = true)
			}
			
			is Async.Error -> {
				GradesUiState(
					userMessage = grades.errorMessage
				)
			}
			
			is Async.Success -> {
				GradesUiState(
					grades = grades.data,
					userMessage = state.userMessage,
					isLoading = state.isLoading
				)
			}
		}
	}.stateIn(viewModelScope, WhileUiSubscribed, GradesUiState(isLoading = true))
	
	fun showEditResultMessage(result: Int) {
		when (result) {
			EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_grade_message)
			ADD_EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_grade_message)
			DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_grade_message)
		}
	}
	
	fun snackbarMessageShown() {
		_uiState.update { it.copy(userMessage = null) }
	}
	
	private fun showSnackbarMessage(message: Int) {
		_uiState.update { it.copy(userMessage = message) }
	}
}
