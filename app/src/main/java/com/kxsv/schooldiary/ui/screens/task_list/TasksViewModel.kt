package com.kxsv.schooldiary.ui.screens.task_list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.app.sync.initializers.SyncConstraints
import com.kxsv.schooldiary.app.sync.workers.DelegatingWorker
import com.kxsv.schooldiary.app.sync.workers.TaskSyncWorker
import com.kxsv.schooldiary.app.sync.workers.delegatedData
import com.kxsv.schooldiary.data.local.features.task.TaskWithSubject
import com.kxsv.schooldiary.data.repository.TaskRepository
import com.kxsv.schooldiary.data.repository.UpdateRepository
import com.kxsv.schooldiary.data.util.AppVersionState
import com.kxsv.schooldiary.di.util.AppDispatchers
import com.kxsv.schooldiary.di.util.Dispatcher
import com.kxsv.schooldiary.ui.main.navigation.ADD_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.DELETE_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.EDIT_RESULT_OK
import com.kxsv.schooldiary.ui.util.Async
import com.kxsv.schooldiary.ui.util.TasksDateFilterType
import com.kxsv.schooldiary.ui.util.TasksDoneFilterType
import com.kxsv.schooldiary.ui.util.WhileUiSubscribed
import com.kxsv.schooldiary.util.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject


data class TasksUiState(
	val tasks: Map<LocalDate, List<TaskWithSubject>> = emptyMap(),
	val isLoading: Boolean = false,
	val userMessage: Int? = null,
	val userMessageArg: String? = null,
	val dateFilterType: TasksDateFilterType = TasksDateFilterType.NEXT_WEEK,
	val doneFilterType: TasksDoneFilterType = TasksDoneFilterType.ALL,
)

private const val TAG = "TasksViewModel"
private const val UNIQUE_WORK_NAME = "TasksScreenFetchSync"

@HiltViewModel
class TasksViewModel @Inject constructor(
	taskRepository: TaskRepository,
	private val workManager: WorkManager,
	private val updateRepository: UpdateRepository,
	@Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
	
	private val _dateFilterType = MutableStateFlow(TasksDateFilterType.NEXT_WEEK)
	
	private val _doneFilterType = MutableStateFlow(TasksDoneFilterType.ALL)
	
	private val _allTasksFlow = taskRepository.observeAllWithSubject()
	
	private val _tasksDoneFilteredFlow = _doneFilterType
		.combine(_allTasksFlow) { doneFilterType, tasks ->
			when (doneFilterType) {
				TasksDoneFilterType.ALL -> tasks
				
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
				
				TasksDateFilterType.ALL -> tasks
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
	
	private var fetchJob: Job? = null
	
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
	
	val toShowUpdateDialog = MutableStateFlow<AppVersionState>(AppVersionState.NotFound)
	
	private fun observeIsUpdateAvailable() {
		viewModelScope.launch(ioDispatcher) {
			updateRepository.isUpdateAvailable.collect {
				toShowUpdateDialog.value = it
			}
		}
	}
	
	fun onUpdateDialogShown() {
		viewModelScope.launch(ioDispatcher) {
			toShowUpdateDialog.value = AppVersionState.Suppressed
			updateRepository.suppressUpdateUntilNextAppStart()
		}
	}
	
	init {
		observeIsUpdateAvailable()
	}
	
	fun showEditResultMessage(result: Int) {
		when (result) {
			EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_task_message)
			ADD_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_task_message)
			DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_task_message)
		}
	}
	
	fun refresh() = viewModelScope.launch(ioDispatcher) {
		_uiState.update { it.copy(isLoading = true) }
		val tasksSyncRequest =
			OneTimeWorkRequestBuilder<DelegatingWorker>()
				.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
				.setConstraints(SyncConstraints)
				.setInputData(TaskSyncWorker::class.delegatedData())
				.build()
		
		val continuation = workManager
			.beginUniqueWork(
				UNIQUE_WORK_NAME,
				ExistingWorkPolicy.KEEP,
				tasksSyncRequest
			)
		
		continuation.enqueue()
		fetchJob = viewModelScope.launch(ioDispatcher) {
			val workInfosFlow = workManager.getWorkInfosForUniqueWorkFlow(UNIQUE_WORK_NAME)
			workInfosFlow.collectLatest { workInfos ->
				val isFinished = workInfos.all { it.state.isFinished }
				if (isFinished) {
					_uiState.update { it.copy(isLoading = false) }
					fetchJob?.cancel()
				}
			}
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
