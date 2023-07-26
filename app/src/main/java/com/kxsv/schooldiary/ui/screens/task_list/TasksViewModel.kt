package com.kxsv.schooldiary.ui.screens.task_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.task.TaskEntity
import com.kxsv.schooldiary.data.repository.TaskRepository
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


data class TasksUiState(
	val tasks: List<TaskEntity> = emptyList(),
	val isLoading: Boolean = false,
	val userMessage: Int? = null,
)

@HiltViewModel
class TasksViewModel @Inject constructor(
	private val taskRepository: TaskRepository,
) : ViewModel() {
	
	private val _tasksAsync =
		taskRepository.observeAll()
			.map { Async.Success(it) }
			.catch<Async<List<TaskEntity>>> { emit(Async.Error(R.string.loading_tasks_error)) }
	
	private val _uiState = MutableStateFlow(TasksUiState())
	val uiState: StateFlow<TasksUiState> = combine(
		_uiState, _tasksAsync
	) { state, tasks ->
		when (tasks) {
			Async.Loading -> {
				TasksUiState(isLoading = true)
			}
			
			is Async.Error -> {
				TasksUiState(
					userMessage = tasks.errorMessage
				)
			}
			
			is Async.Success -> {
				TasksUiState(
					tasks = tasks.data,
					userMessage = state.userMessage,
					isLoading = state.isLoading
				)
			}
		}
	}.stateIn(viewModelScope, WhileUiSubscribed, TasksUiState(isLoading = true))
	
	fun showEditResultMessage(result: Int) {
		when (result) {
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
