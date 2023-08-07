package com.kxsv.schooldiary.ui.screens.task_list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.task.TaskWithSubject
import com.kxsv.schooldiary.data.repository.TaskRepository
import com.kxsv.schooldiary.ui.main.navigation.ADD_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.DELETE_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.EDIT_RESULT_OK
import com.kxsv.schooldiary.util.Utils
import com.kxsv.schooldiary.util.ui.Async
import com.kxsv.schooldiary.util.ui.TasksDateFilterType
import com.kxsv.schooldiary.util.ui.TasksDoneFilterType
import com.kxsv.schooldiary.util.ui.WhileUiSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject


data class TasksUiState(
	val tasks: Map<LocalDate, List<TaskWithSubject>> = emptyMap(),
	val isLoading: Boolean = false,
	val userMessage: Int? = null,
	val userMessageArg: String? = null,
	val dateFilterType: TasksDateFilterType = TasksDateFilterType.ALL,
	val doneFilterType: TasksDoneFilterType = TasksDoneFilterType.ALL,
)

private const val TAG = "TasksViewModel"

@HiltViewModel
class TasksViewModel @Inject constructor(
	taskRepository: TaskRepository,
) : ViewModel() {
	
	private val _dateFilterType = MutableStateFlow(TasksDateFilterType.ALL)
	
	private val _doneFilterType = MutableStateFlow(TasksDoneFilterType.ALL)
	
	private val _allTasksFlow = taskRepository.observeAllWithSubject()
	
	private val _tasksDoneFilteredFlow = _doneFilterType
		.combine(_allTasksFlow) { doneFilterType, tasks ->
			when (doneFilterType) {
				TasksDoneFilterType.ALL -> {
					tasks
				}
				
				TasksDoneFilterType.IS_NOT_DONE -> {
					tasks.filterNot {
						it.taskEntity.isDone
					}
				}
			}
		}
	
	private val _filteredTasksAsync = _dateFilterType
		.combine(_tasksDoneFilteredFlow) { dateFilterType, tasks ->
			val today = Utils.currentDate
			when (dateFilterType) {
				TasksDateFilterType.YESTERDAY -> {
					tasks.filter {
						it.taskEntity.dueDate == today.minusDays(1)
					}
				}
				
				TasksDateFilterType.TODAY -> {
					tasks.filter {
						it.taskEntity.dueDate == today
					}
				}
				
				TasksDateFilterType.TOMORROW -> {
					tasks.filter {
						it.taskEntity.dueDate == today.plusDays(1)
					}
				}
				
				TasksDateFilterType.NEXT_WEEK -> {
					tasks.filter {
						it.taskEntity.dueDate.isAfter(today) &&
								it.taskEntity.dueDate.isBefore(today.plusDays(8))
					}
				}
				
				TasksDateFilterType.THIS_MONTH -> {
					tasks.filter {
						it.taskEntity.dueDate.month == today.month
					}
				}
				
				TasksDateFilterType.NEXT_MONTH -> {
					tasks.filter {
						it.taskEntity.dueDate.month == today.month.plus(1)
					}
				}
				
				TasksDateFilterType.ALL -> {
					tasks
				}
			}
		}.map { Async.Success(it) }
		.catch<Async<List<TaskWithSubject>>> {
			Log.e(TAG, "tasksAsync error: ", it)
			emit(
				Async.Error(
					errorMessage = R.string.loading_tasks_error,
					formatArg = "error: " + it.message.toString()
				)
			)
		}
	
	
	private val _uiState = MutableStateFlow(TasksUiState())
	val uiState: StateFlow<TasksUiState> = combine(
		_uiState, _filteredTasksAsync, _dateFilterType, _doneFilterType
	) { state, tasks, dateFilterType, doneFilterType ->
		when (tasks) {
			Async.Loading -> {
				TasksUiState(isLoading = true)
			}
			
			is Async.Error -> {
				TasksUiState(
					userMessage = tasks.errorMessage,
					userMessageArg = tasks.formatArg
				)
			}
			
			is Async.Success -> {
				TasksUiState(
					tasks = tasks.data.groupBy { it.taskEntity.dueDate }.toSortedMap(),
					dateFilterType = dateFilterType,
					doneFilterType = doneFilterType,
					userMessage = state.userMessage,
					isLoading = state.isLoading
				)
			}
			
		}
	}.stateIn(viewModelScope, WhileUiSubscribed, TasksUiState(isLoading = true))
	
	fun showEditResultMessage(result: Int) {
		when (result) {
			EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_subject_message)
			ADD_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_subject_message)
			DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_subject_message)
		}
	}
	
	fun snackbarMessageShown() {
		_uiState.update { it.copy(userMessage = null) }
	}
	
	private fun showSnackbarMessage(message: Int) {
		_uiState.update { it.copy(userMessage = message) }
	}
	
	fun changeDataFilter(newFilterType: TasksDateFilterType) {
		_dateFilterType.update { newFilterType }
	}
	
	fun changeDoneFilter(newFilterType: TasksDoneFilterType) {
		_doneFilterType.update { newFilterType }
	}
}
