package com.kxsv.schooldiary.ui.screens.task_list.task_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.task.TaskWithSubject
import com.kxsv.schooldiary.data.repository.TaskRepository
import com.kxsv.schooldiary.di.util.IoDispatcher
import com.kxsv.schooldiary.ui.screens.navArgs
import com.kxsv.schooldiary.util.ui.Async
import com.kxsv.schooldiary.util.ui.WhileUiSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskDetailUiState(
	val taskWithSubject: TaskWithSubject? = null,
	val isLoading: Boolean = false,
	val userMessage: Int? = null,
)

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
	private val taskRepository: TaskRepository,
	savedStateHandle: SavedStateHandle,
	@IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
	
	private var navArgs: TaskDetailScreenNavArgs = savedStateHandle.navArgs()
	val taskId = navArgs.taskId
	
	private val _taskWithSubjectAsync = taskRepository.observeTaskWithSubject(taskId)
		.map { handleTask(it) }
		.catch { emit(Async.Error(R.string.loading_task_error)) }
	
	
	private val _uiState = MutableStateFlow(TaskDetailUiState())
	val uiState: StateFlow<TaskDetailUiState> = combine(
		_uiState, _taskWithSubjectAsync
	) { state, taskWithSubject ->
		when (taskWithSubject) {
			Async.Loading -> state.copy(isLoading = true)
			is Async.Error -> state.copy(userMessage = taskWithSubject.errorMessage)
			is Async.Success -> state.copy(
				taskWithSubject = taskWithSubject.data
			)
		}
		
	}.stateIn(viewModelScope, WhileUiSubscribed, TaskDetailUiState(isLoading = true))
	
	fun deleteTask() = viewModelScope.launch(ioDispatcher) {
		taskRepository.deleteTask(taskId)
	}
	
	fun completeTask() = viewModelScope.launch(ioDispatcher) {
		if (uiState.value.taskWithSubject?.taskEntity == null) throw IllegalStateException("Task entity shouldn't be null when completing task.")
		val taskToUpdate =
			uiState.value.taskWithSubject!!.taskEntity.let { it.copy(isDone = !it.isDone) }
		taskRepository.updateTask(taskToUpdate)
	}
	
	fun snackbarMessageShown() {
		_uiState.update {
			it.copy(userMessage = null)
		}
	}
	
	// todo add edit result message
	private fun showSnackbarMessage(message: Int) {
		_uiState.update {
			it.copy(userMessage = message)
		}
	}
	
	private fun handleTask(task: TaskWithSubject?): Async<TaskWithSubject?> {
		if (task == null) return Async.Error(R.string.task_not_found)
		
		return Async.Success(task)
	}
}
