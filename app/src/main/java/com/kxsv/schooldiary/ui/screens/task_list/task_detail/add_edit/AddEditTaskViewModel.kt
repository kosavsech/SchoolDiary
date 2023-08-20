package com.kxsv.schooldiary.ui.screens.task_list.task_detail.add_edit

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.local.features.task.TaskEntity
import com.kxsv.schooldiary.data.remote.dtos.TaskDto
import com.kxsv.schooldiary.data.remote.util.NetworkException
import com.kxsv.schooldiary.data.repository.SubjectRepository
import com.kxsv.schooldiary.data.repository.TaskRepository
import com.kxsv.schooldiary.di.util.AppDispatchers
import com.kxsv.schooldiary.di.util.Dispatcher
import com.kxsv.schooldiary.ui.screens.navArgs
import com.kxsv.schooldiary.ui.util.Async
import com.kxsv.schooldiary.ui.util.WhileUiSubscribed
import com.kxsv.schooldiary.util.Utils
import com.kxsv.schooldiary.util.Utils.measurePerformanceInMS
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
	val dueDate: LocalDate = Utils.currentDate.plusDays(1),
	
	val fetchedTitleBoundToId: String? = null,
	val availableSubjects: List<SubjectEntity> = emptyList(),
	val fetchedVariants: List<TaskDto>? = null,
	
	val selectedFetchedVariantIndex: Int? = null,
	val isFetched: Boolean = false,
	
	val isTaskSaved: Boolean = false,
	val isLoading: Boolean = false,
	val userMessage: Int? = null,
)

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
	private val taskRepository: TaskRepository,
	subjectRepository: SubjectRepository,
	savedStateHandle: SavedStateHandle,
	@Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
	
	private val navArgs: AddEditTaskScreenNavArgs = savedStateHandle.navArgs()
	val taskId: String? = navArgs.taskId
	val isEditingFetchedTask: Boolean = navArgs.isEditingFetchedTask
	
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
		if (taskId != null) loadTask()
	}
	
	private fun loadTask() {
		if (taskId == null) throw RuntimeException("loadTask() was called but task is new.")
		
		viewModelScope.launch(ioDispatcher) {
			_uiState.update { it.copy(isLoading = true) }
			taskRepository.getTaskWithSubject(taskId).let { taskWithSubject ->
				if (taskWithSubject != null) {
					_uiState.update {
						it.copy(
							title = taskWithSubject.taskEntity.title,
							description = taskWithSubject.taskEntity.description,
							dueDate = taskWithSubject.taskEntity.dueDate,
							subject = taskWithSubject.subject,
							isFetched = isEditingFetchedTask,
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
	}
	
	
	fun saveTask() {
		if (uiState.value.title.isEmpty() || uiState.value.subject == null) {
			_uiState.update {
				it.copy(userMessage = R.string.fill_required_fields_message)
			}
		}
		
		return if (taskId == null) {
			createNewTask()
		} else {
			updateTask()
		}
	}
	
	private fun createNewTask() {
		val subjectMasterId = uiState.value.subject?.subjectId
			?: throw RuntimeException("updateTask() was called but subjectMasterId is null.")
		
		viewModelScope.launch(ioDispatcher) {
			try {
				taskRepository.createTask(
					task = TaskEntity(
						title = uiState.value.title,
						description = uiState.value.description,
						dueDate = uiState.value.dueDate,
						subjectMasterId = subjectMasterId,
						fetchedTitleBoundToId = uiState.value.fetchedTitleBoundToId,
						isFetched = uiState.value.isFetched,
					),
					fetchedLessonIndex = uiState.value.selectedFetchedVariantIndex
				)
				_uiState.update { it.copy(isTaskSaved = true) }
			} catch (e: SQLiteConstraintException) {
				_uiState.update { it.copy(userMessage = R.string.task_duplicate) }
			}
		}
	}
	
	private fun updateTask() {
		if (taskId == null) throw RuntimeException("updateTask() was called but Task is new.")
		val subjectMasterId = uiState.value.subject?.subjectId
			?: throw RuntimeException("updateTask() was called but subjectMasterId is null.")
		
		viewModelScope.launch(ioDispatcher) {
			taskRepository.updateTask(
				task = TaskEntity(
					title = uiState.value.title,
					description = uiState.value.description,
					dueDate = uiState.value.dueDate,
					subjectMasterId = subjectMasterId,
					fetchedTitleBoundToId = uiState.value.fetchedTitleBoundToId,
					isFetched = isEditingFetchedTask,
					taskId = taskId,
				)
			)
			_uiState.update { it.copy(isTaskSaved = true) }
		}
	}
	
	fun snackbarMessageShown() {
		_uiState.update {
			it.copy(userMessage = null)
		}
	}
	
	fun onFetchedTaskImmutableFieldEdit() {
		_uiState.update { it.copy(userMessage = R.string.cannot_be_edited_for_fetched_task) }
	}
	
	fun pickFetchedVariant(pickedVariant: TaskDto) {
		_uiState.update {
			it.copy(
				title = pickedVariant.title,
				selectedFetchedVariantIndex = pickedVariant.lessonIndex,
				isFetched = true
			)
		}
	}
	
	fun onFetchedVariantChosen() {
		_uiState.update { it.copy(fetchedVariants = null) }
	}
	
	private fun clearIsFetchedTag() {
		if (!isEditingFetchedTask)
			_uiState.update { it.copy(selectedFetchedVariantIndex = null, isFetched = false) }
	}
	
	fun changeTitle(newTitle: String) {
		if (uiState.value.title != newTitle.trim()) {
			if (isEditingFetchedTask && uiState.value.fetchedTitleBoundToId == null) {
				_uiState.update { it.copy(fetchedTitleBoundToId = uiState.value.title) }
			}
			clearIsFetchedTag()
			_uiState.update { it.copy(title = newTitle.trim()) }
		}
	}
	
	fun changeDescription(newDescription: String) {
		if (uiState.value.description != newDescription.trim()) {
			clearIsFetchedTag()
			_uiState.update { it.copy(description = newDescription.trim()) }
		}
	}
	
	fun changeDate(newDueDate: LocalDate) {
		if (uiState.value.dueDate != newDueDate) {
			clearIsFetchedTag()
			_uiState.update { it.copy(dueDate = newDueDate) }
		}
	}
	
	fun changeSubject(newSubject: SubjectEntity) {
		if (uiState.value.subject != newSubject) {
			clearIsFetchedTag()
			_uiState.update { it.copy(subject = newSubject) }
		}
	}
	
	/**
	 * @throws NetworkException.NotLoggedInException
	 */
	fun fetchNet() {
		if (uiState.value.subject == null) throw IllegalStateException("Subject shouldn't be null when fetch is called")
		
		taskFetchJob?.cancel()
		taskFetchJob = viewModelScope.launch(ioDispatcher) {
			val fetchedVariants = measurePerformanceInMS(logger = { time, result ->
				Log.d(TAG, "fetchNet: time = $time MS\nresult = $result")
			}) {
				taskRepository.fetchTaskVariantsForSubjectByDate(
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
						userMessage = R.string.net_task_variants_not_found
					)
				}
			}
		}
	}
}