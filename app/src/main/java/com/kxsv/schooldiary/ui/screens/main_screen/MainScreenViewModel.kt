package com.kxsv.schooldiary.ui.screens.main_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeEntity
import com.kxsv.schooldiary.data.repository.LessonRepository
import com.kxsv.schooldiary.data.repository.PatternStrokeRepository
import com.kxsv.schooldiary.data.repository.TaskRepository
import com.kxsv.schooldiary.data.repository.UserPreferencesRepository
import com.kxsv.schooldiary.di.util.IoDispatcher
import com.kxsv.schooldiary.util.Utils
import com.kxsv.schooldiary.util.Utils.toList
import com.kxsv.schooldiary.util.ui.WhileUiSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class MainUiState(
	val itemList: List<MainScreenItem> = emptyList(),
	val isLoading: Boolean = false,
	val userMessage: Int? = null,
)

@HiltViewModel
class MainScreenViewModel @Inject constructor(
	private val taskRepository: TaskRepository,
	private val strokeRepository: PatternStrokeRepository,
	private val userPreferencesRepository: UserPreferencesRepository,
	@IoDispatcher private val ioDispatcher: CoroutineDispatcher,
	lessonRepository: LessonRepository,
) : ViewModel() {
	
	private val startRange = Utils.currentDate
	private val endRange = Utils.currentDate.plusDays(7)
	
	@OptIn(ExperimentalCoroutinesApi::class)
	private val _tasks = taskRepository.observeAllWithSubjectForDateRange(
		startRange = startRange,
		endRange = endRange
	).flatMapLatest { taskWithSubjectList ->
		flowOf(taskWithSubjectList.groupBy {
			it.taskEntity.dueDate
		})
	}
	
	private val _classes = lessonRepository.observeAllWithSubjectForDateRange(
		startRange = startRange,
		endRange = endRange
	)
	
	private val _patterns = strokeRepository.observeAllWithStrokesForDateRange(
		startRange = startRange,
		endRange = endRange
	)
	
	private var defaultPattern: List<PatternStrokeEntity>? = null
	
	private val _uiState = MutableStateFlow(MainUiState())
	val uiState: StateFlow<MainUiState> = combine(
		_uiState, _classes, _tasks, _patterns
	) { state, classes, tasks, patterns ->
		val itemList = mutableListOf<MainScreenItem>()
		(startRange..endRange).toList().forEach { itemDate ->
			val item = MainScreenItem(
				date = itemDate,
				tasks = tasks[itemDate] ?: emptyList(),
				classes = classes[itemDate]?.associate {
					Pair(it.lesson.index, it.subject)
				} ?: emptyMap(),
				pattern = patterns[itemDate] ?: defaultPattern
				?: strokeRepository.getStrokesByPatternId(userPreferencesRepository.getPatternId())
			)
			itemList.add(item)
		}
		state.copy(
			itemList = itemList.sortedBy { it.date }
		)
	}.stateIn(viewModelScope, WhileUiSubscribed, MainUiState(isLoading = true))
	
	init {
		viewModelScope.launch(ioDispatcher) {
			defaultPattern =
				strokeRepository.getStrokesByPatternId(userPreferencesRepository.getPatternId())
		}
	}
	
	fun snackbarMessageShown() {
		_uiState.update { it.copy(userMessage = null) }
	}
	
	private fun showSnackbarMessage(message: Int) {
		_uiState.update { it.copy(userMessage = message) }
	}
	
	fun completeTask(id: Long, isDone: Boolean) = viewModelScope.launch(ioDispatcher) {
		val taskToUpdate = taskRepository.getById(id)?.copy(isDone = isDone)
			?: return@launch showSnackbarMessage(R.string.task_not_found)
		taskRepository.updateTask(taskToUpdate)
	}
}