package com.kxsv.schooldiary.ui.screens.teacher_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.features.teachers.Teacher
import com.kxsv.schooldiary.data.features.teachers.TeacherRepository
import com.kxsv.schooldiary.util.Async
import com.kxsv.schooldiary.util.WhileUiSubscribed
import com.kxsv.schooldiary.util.copyExclusively
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeachersUiState(
    val teachers: List<Teacher> = emptyList(),
    val teacher: Teacher? = null,
    val firstName: String = "",
    val lastName: String = "",
    val patronymic: String = "",
    val phoneNumber: String = "",
    val isTeacherDialogShown: Boolean = false,
    val isLoading: Boolean = false,
    val userMessage: Int? = null,
)

@HiltViewModel
class TeachersViewModel @Inject constructor(
    private val teacherRepository: TeacherRepository,
) : ViewModel() {

    private val _teachersAsync = teacherRepository.getTeachersStream()
        .map { Async.Success(it) }
        .catch<Async<List<Teacher>>> { emit(Async.Error(R.string.loading_teachers_error)) }

    private val _uiState = MutableStateFlow(TeachersUiState())
    val uiState = combine(_uiState, _teachersAsync) { state, teachersAsync ->
        when (teachersAsync) {
            Async.Loading -> {
                TeachersUiState(isLoading = true)
            }

            is Async.Error -> {
                TeachersUiState(
                    userMessage = teachersAsync.errorMessage
                )
            }

            is Async.Success -> {
                TeachersUiState(
                    teachers = teachersAsync.data,
                    teacher = state.teacher,
                    firstName = state.firstName,
                    lastName = state.lastName,
                    patronymic = state.patronymic,
                    phoneNumber = state.phoneNumber,
                    isTeacherDialogShown = state.isTeacherDialogShown,
                    isLoading = state.isLoading,
                    userMessage = state.userMessage,
                )
            }
        }
    }.stateIn(viewModelScope, WhileUiSubscribed, TeachersUiState(isLoading = true))

    private fun showSnackBarMessage(message: Int) {
        _uiState.update {
            it.copy(
                userMessage = message
            )
        }
    }

    fun snackbarMessageShown() {
        _uiState.update {
            it.copy(
                userMessage = null
            )
        }
    }

    fun deleteTeacher(teacher: Teacher) = viewModelScope.launch {
        if (teacher.teacherId != 0) teacherRepository.deleteTeacher(teacher.teacherId)

        val newTeachers = copyExclusively(teacher, uiState.value.teachers)
        _uiState.update {
            it.copy(
                teachers = newTeachers
            )
        }
        showSnackBarMessage(R.string.successfully_deleted_teacher)
    }


    fun saveTeacher() {
        if (uiState.value.patronymic.isBlank() and
            uiState.value.lastName.isBlank() and
            uiState.value.firstName.isBlank()
        ) {
            _uiState.update {
                it.copy(userMessage = R.string.empty_teacher_message)
            }
            return
        }

        viewModelScope.launch {
            if (uiState.value.teacher != null) {
                teacherRepository.updateTeacher(uiState.value.teacher!!)
            } else {
                teacherRepository.createTeacher(
                    Teacher(
                        uiState.value.firstName,
                        uiState.value.lastName,
                        uiState.value.patronymic,
                        uiState.value.phoneNumber
                    )
                )
            }
        }
        _uiState.update {
            it.copy(
                isTeacherDialogShown = false,
                firstName = "",
                lastName = "",
                patronymic = "",
                phoneNumber = "",
                teacher = null
            )
        }
    }

    fun updateFirstName(firstName: String) {
        _uiState.update {
            it.copy(
                firstName = firstName
            )
        }
    }

    fun updateLastName(newLastName: String) {
        _uiState.update {
            it.copy(
                lastName = newLastName
            )
        }
    }

    fun updatePatronymic(newPatronymic: String) {
        _uiState.update {
            it.copy(
                patronymic = newPatronymic
            )
        }
    }

    fun updatePhoneNumber(newPhoneNumber: String) {
        _uiState.update {
            it.copy(
                phoneNumber = newPhoneNumber
            )
        }
    }

    fun hideTeacherDialog() {
        _uiState.update {
            it.copy(
                isTeacherDialogShown = false
            )
        }
    }

    fun showTeacherDialog() {
        _uiState.update {
            it.copy(
                isTeacherDialogShown = true
            )
        }
    }

    fun onTeacherClick(teacher: Teacher) {
        _uiState.update {
            it.copy(
                isTeacherDialogShown = true,
                firstName = teacher.firstName,
                lastName = teacher.lastName,
                patronymic = teacher.patronymic,
                phoneNumber = teacher.phoneNumber,
                teacher = teacher
            )
        }
    }

}