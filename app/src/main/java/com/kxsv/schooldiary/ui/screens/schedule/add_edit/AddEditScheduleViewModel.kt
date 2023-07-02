package com.kxsv.schooldiary.ui.screens.schedule.add_edit
/*

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.AppDestinationsArgs
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.features.schedule.ScheduleRepository
import com.kxsv.schooldiary.data.features.subjects.Subject
import com.kxsv.schooldiary.data.features.teachers.Teacher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditScheduleUiState(
    val name: String = "",
    val cabinet: String = "",
    val isLoading: Boolean = false,
    val userMessage: Int? = null,
    val isSubjectSaved: Boolean = false,
)

@HiltViewModel
class AddEditScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val scheduleId: Long = savedStateHandle[AppDestinationsArgs.SCHEDULE_ID_ARG]!!

    private val _uiState = MutableStateFlow(AddEditScheduleUiState())
    val uiState: StateFlow<AddEditScheduleUiState> = _uiState.asStateFlow()

    init {
        if (scheduleId != 0L) loadSubject(scheduleId)
    }

    fun saveSchedule() {
        if (uiState.value.name.isEmpty()) {
            _uiState.update {
                it.copy(userMessage = R.string.empty_subject_message)
            }
            return
        }

        if (scheduleId == 0L) {
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

    fun updateName(name: String) {
        _uiState.update {
            it.copy(
                name = name
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
        val newSelectedTeachers: MutableSet<Teacher> = mutableSetOf()
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
        scheduleRepository.createSubject(
            Subject(uiState.value.name, uiState.value.cabinet),
            uiState.value.selectedTeachers
        )

        _uiState.update {
            it.copy(isSubjectSaved = true)
        }
    }

    private fun updateSubject() {
        if (scheduleId == 0L) throw RuntimeException("updateSubject() was called but subject is new.")

        viewModelScope.launch {
            scheduleRepository.updateSubject(
                Subject(uiState.value.name, uiState.value.cabinet, scheduleId),
                uiState.value.selectedTeachers
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
            scheduleRepository.getSubjectWithTeachers(subjectId).let { subjectWithTeachers ->
                if (subjectWithTeachers != null) {
                    _uiState.update {
                        it.copy(
                            name = subjectWithTeachers.subject.name,
                            cabinet = subjectWithTeachers.subject.cabinet,
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
}*/
