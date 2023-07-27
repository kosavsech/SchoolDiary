package com.kxsv.schooldiary.ui.screens.task_detail.add_edit

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.local.features.task.TaskEntity
import com.kxsv.schooldiary.data.remote.task.TaskDto
import com.kxsv.schooldiary.data.repository.SubjectRepository
import com.kxsv.schooldiary.data.repository.TaskRepository
import com.kxsv.schooldiary.di.IoDispatcher
import com.kxsv.schooldiary.ui.main.navigation.AppDestinationsArgs
import com.kxsv.schooldiary.util.ui.Async
import com.kxsv.schooldiary.util.ui.WhileUiSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
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

private const val TAG = "AddEditTaskViewModel"

data class AddEditTaskUiState(
	val title: String = "",
	val description: String = "",
	val subject: SubjectEntity? = null,
	val dueDate: LocalDate = LocalDate.now().plusDays(1),
	val availableSubjects: List<SubjectEntity> = emptyList(),
	val fetchedVariants: List<TaskDto>? = null,
	val isLoading: Boolean = false,
	val userMessage: Int? = null,
	val isTaskSaved: Boolean = false,
)

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
	private val taskRepository: TaskRepository,
	private val subjectRepository: SubjectRepository,
	savedStateHandle: SavedStateHandle,
	@IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
	
	val taskId: Long = savedStateHandle[AppDestinationsArgs.TASK_ID_ARG]!!
	
	private val _subjectsAsync = subjectRepository.observeAll()
		.map { Async.Success(it) }
		.catch<Async<List<SubjectEntity>>> { emit(Async.Error(R.string.loading_task_error)) }
	
	private val _uiState = MutableStateFlow(AddEditTaskUiState())
	val uiState: StateFlow<AddEditTaskUiState> = combine(
		_uiState, _subjectsAsync
	) { state, subjects ->
		when (subjects) {
			Async.Loading -> state.copy(isLoading = true)
			is Async.Error -> state.copy(userMessage = subjects.errorMessage)
			is Async.Success -> state.copy(availableSubjects = subjects.data)
		}
		
	}.stateIn(viewModelScope, WhileUiSubscribed, AddEditTaskUiState(isLoading = true))
	
	private var taskFetchJob: Job? = null
	
	init {
		if (taskId != 0L) loadTask()
	}
	
	private fun loadTask() = viewModelScope.launch(ioDispatcher) {
		_uiState.update { it.copy(isLoading = true) }
		taskRepository.getTaskWithSubject(taskId).let { taskWithSubject ->
			if (taskWithSubject != null) {
				_uiState.update {
					it.copy(
						title = taskWithSubject.taskEntity.title,
						description = taskWithSubject.taskEntity.description,
						dueDate = taskWithSubject.taskEntity.dueDate,
						subject = taskWithSubject.subject,
						isLoading = false
					)
				}
			} else {
				_uiState.update {
					it.copy(
						userMessage = R.string.task_not_found,
						isLoading = false
					)
				}
			}
		}
	}
	
	
	fun saveTask() {
		if (uiState.value.title.isEmpty() || uiState.value.subject == null) {
			_uiState.update { it.copy(userMessage = R.string.fill_required_fields_message) }
			return
		}
		
		viewModelScope.launch(ioDispatcher) {
			taskRepository.updateTask(
				task = TaskEntity(
					title = uiState.value.title,
					description = uiState.value.description,
					dueDate = uiState.value.dueDate,
					subjectMasterId = uiState.value.subject!!.subjectId,
					taskId = taskId
				)
			)
			_uiState.update {
				it.copy(isTaskSaved = true)
			}
		}
	}
	
	fun snackbarMessageShown() {
		_uiState.update {
			it.copy(userMessage = null)
		}
	}
	
	fun changeDate(newDueDate: LocalDate) {
		_uiState.update { it.copy(dueDate = newDueDate) }
	}
	
	fun changeTitle(newTitle: String) {
		_uiState.update { it.copy(title = newTitle) }
	}
	
	fun changeDescription(newDescription: String) {
		_uiState.update { it.copy(description = newDescription) }
	}
	
	fun changeSubject(newSubject: SubjectEntity) {
		_uiState.update { it.copy(subject = newSubject) }
	}
	
	fun onFetchedTitleChoose() {
		_uiState.update { it.copy(fetchedVariants = null) }
	}
	
	fun fetchNet() {
		if (uiState.value.subject == null) throw IllegalStateException("Subject shouldn't be null when fetch is called")
		
		taskFetchJob?.cancel()
		taskFetchJob = viewModelScope.launch(ioDispatcher) {
			val fetchedVariants = measurePerformanceInMS(logger = { time, result ->
				Log.d(TAG, "fetchNet: time = $time MS\nresult = $result")
			}) {
				taskRepository.fetchTask(
					date = uiState.value.dueDate,
					subject = uiState.value.subject!!
				)
			}
			if (fetchedVariants.isNotEmpty()) {
				_uiState.update {
					it.copy(
						fetchedVariants = fetchedVariants,
						userMessage = R.string.successfully_fetched_task
					)
				}
			} else {
				_uiState.update {
					it.copy(
						userMessage = R.string.task_not_found
					)
				}
			}
		}
	}
	
	//the inline performance measurement method
	private inline fun <T> measurePerformanceInMS(logger: (Long, T) -> Unit, func: () -> T): T {
		val startTime = System.currentTimeMillis()
		val result: T = func.invoke()
		val endTime = System.currentTimeMillis()
		logger.invoke(endTime - startTime, result)
		return result
	}
}