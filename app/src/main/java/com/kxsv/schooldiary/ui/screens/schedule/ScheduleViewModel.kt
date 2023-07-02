package com.kxsv.schooldiary.ui.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.ADD_EDIT_RESULT_OK
import com.kxsv.schooldiary.DELETE_RESULT_OK
import com.kxsv.schooldiary.EDIT_RESULT_OK
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.features.schedule.ScheduleRepository
import com.kxsv.schooldiary.data.features.schedule.ScheduleWithSubject
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
import java.time.LocalDate
import javax.inject.Inject

data class ScheduleUiState(
    // TODO: getting of lessons for current study week
    val lessons: List<ScheduleWithSubject> = emptyList(),
    val lesson: ScheduleWithSubject? = null,
    val userMessage: Int? = null,
    val isLoading: Boolean = false,
    val isLessonDialogShown: Boolean = false,
    val isSubjectDeleted: Boolean = false,
)

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
) : ViewModel() {

    private val today = LocalDate.now()

    private val _lessonsAsync =
        scheduleRepository.getSchedulesWithSubjectByDateStream(today)
            .map { Async.Success(it) }
            .catch<Async<List<ScheduleWithSubject>>> { emit(Async.Error(R.string.loading_schedule_error)) }

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = combine(
        _uiState, _lessonsAsync
    ) { state, lessons ->
        when (lessons) {
            Async.Loading -> {
                ScheduleUiState(isLoading = true)
            }

            is Async.Error -> {
                ScheduleUiState(
                    userMessage = lessons.errorMessage
                )
            }

            is Async.Success -> {
                ScheduleUiState(
                    lessons = lessons.data,
                    userMessage = state.userMessage,
                    isLoading = state.isLoading
                )
            }
        }
    }.stateIn(viewModelScope, WhileUiSubscribed, ScheduleUiState())

    fun snackbarMessageShown() {
        _uiState.update {
            it.copy(userMessage = null)
        }
    }

    fun showEditResultMessage(result: Int) {
        // TODO: fix local and messages
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

    fun deleteSchedule(scheduleId: Long) = viewModelScope.launch {
        scheduleRepository.deleteSchedule(scheduleId)
    }

    fun showDialog(lesson: ScheduleWithSubject) {
        _uiState.update {
            it.copy(lesson = lesson, isLessonDialogShown = true)
        }
    }

    fun hideDialog() {
        _uiState.update {
            it.copy(lesson = null, isLessonDialogShown = false)
        }
    }

    fun editSchedule(lesson: ScheduleWithSubject) {
        _uiState.update {
            it.copy(
                lesson = lesson
            )
        }
    }

    fun onDeleteSchedule() {
        TODO("Not yet implemented")
    }
}
