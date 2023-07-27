package com.kxsv.schooldiary.ui.screens.subject_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.repository.SubjectRepository
import com.kxsv.schooldiary.ui.main.navigation.ADD_EDIT_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.DELETE_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.EDIT_RESULT_OK
import com.kxsv.schooldiary.util.ui.Async
import com.kxsv.schooldiary.util.ui.WhileUiSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SubjectsUiState(
	val subjects: List<SubjectEntity> = emptyList(),
	val isLoading: Boolean = false,
	val userMessage: Int? = null,
)

@HiltViewModel
class SubjectsViewModel @Inject constructor(
	private val subjectRepository: SubjectRepository,
) : ViewModel() {
	
	private val _subjectsAsync =
		subjectRepository.observeAll()
			.map { Async.Success(it) }
			.catch<Async<List<SubjectEntity>>> { emit(Async.Error(R.string.loading_subjects_error)) }
	
	private val _uiState = MutableStateFlow(SubjectsUiState())
	val uiState: StateFlow<SubjectsUiState> = combine(
		_uiState, _subjectsAsync
	) { state, subjects ->
		when (subjects) {
			Async.Loading -> {
				SubjectsUiState(isLoading = true)
			}
			
			is Async.Error -> {
				SubjectsUiState(
					userMessage = subjects.errorMessage
				)
			}
			
			is Async.Success -> {
				SubjectsUiState(
					subjects = subjects.data,
					userMessage = state.userMessage,
					isLoading = state.isLoading
				)
			}
		}
	}.stateIn(viewModelScope, WhileUiSubscribed, SubjectsUiState(isLoading = true))
	
	fun showEditResultMessage(result: Int) {
		when(result){
			EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_subject_message)
			ADD_EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_subject_message)
			DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_subject_message)
		}
	}
	
	fun snackbarMessageShown() {
		_uiState.update { it.copy(userMessage = null) }
	}
	
	private fun showSnackbarMessage(message: Int) {
		_uiState.update { it.copy(userMessage = message) }
	}
}
