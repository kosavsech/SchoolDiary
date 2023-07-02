package com.kxsv.schooldiary.ui.screens.subject_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.AppDestinationsArgs
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.features.subjects.Subject
import com.kxsv.schooldiary.data.features.subjects.SubjectRepository
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
    val isLoading: Boolean = false,
    val userMessage: Int? = null,
    val isSubjectDeleted: Boolean = false,
)

@HiltViewModel
class SubjectDetailViewModel @Inject constructor(
    private val subjectRepository: SubjectRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val subjectId: Long = savedStateHandle[AppDestinationsArgs.SUBJECT_ID_ARG]!!

    private val _subjectAsync =
        subjectRepository.getSubjectStream(subjectId)
            .map { handleSubject(it) }
            .catch { emit(Async.Error(R.string.loading_subject_error)) }

    private val _uiState = MutableStateFlow(SubjectDetailUiState())
    val uiState: StateFlow<SubjectDetailUiState> =
        combine(_uiState, _subjectAsync) { state, subject ->
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
}
