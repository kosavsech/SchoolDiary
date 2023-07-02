package com.kxsv.schooldiary.ui.screens.patterns_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.ADD_EDIT_RESULT_OK
import com.kxsv.schooldiary.DELETE_RESULT_OK
import com.kxsv.schooldiary.EDIT_RESULT_OK
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.features.time_pattern.PatternWithStrokes
import com.kxsv.schooldiary.data.features.time_pattern.TimePatternRepository
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

data class PatternsUiState(
    val patterns: List<PatternWithStrokes> = emptyList(),
    val userMessage: Int? = null,
    val isLoading: Boolean = false,
    val isPatternDeleted: Boolean = false,
)

@HiltViewModel
class PatternsViewModel @Inject constructor(
    private val patternRepository: TimePatternRepository,
) : ViewModel() {

    private val _patternsAsync =
        patternRepository.getPatternsWithStrokesStream()
            .map { Async.Success(it) }
            .catch<Async<List<PatternWithStrokes>>> { emit(Async.Error(R.string.loading_patterns_error)) }

    private val _uiState = MutableStateFlow(PatternsUiState())
    val uiState: StateFlow<PatternsUiState> = combine(
        _uiState, _patternsAsync
    ) { state, patterns ->
        when (patterns) {
            Async.Loading -> {
                PatternsUiState(isLoading = true)
            }

            is Async.Error -> {
                PatternsUiState(
                    userMessage = patterns.errorMessage,
                    isPatternDeleted = state.isPatternDeleted
                )
            }

            is Async.Success -> {
                PatternsUiState(
                    patterns = patterns.data,
                    userMessage = state.userMessage,
                    isLoading = state.isLoading,
                    isPatternDeleted = state.isPatternDeleted
                )
            }
        }
    }.stateIn(viewModelScope, WhileUiSubscribed, PatternsUiState())

    fun snackbarMessageShown() {
        _uiState.update {
            it.copy(userMessage = null)
        }
    }

    // FIXME: bug with repeat of latest result message when go back from addEdit screen
    fun showEditResultMessage(result: Int) {
        when (result) {
            EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_pattern_message)
            ADD_EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_pattern_message)
            DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_pattern_message)
        }
    }

    private fun showSnackbarMessage(message: Int) {
        _uiState.update {
            it.copy(userMessage = message)
        }
    }

    fun deletePattern(patternId: Long) = viewModelScope.launch {
        patternRepository.deletePattern(patternId)
        _uiState.update { it.copy(isPatternDeleted = true) }
    }
}
