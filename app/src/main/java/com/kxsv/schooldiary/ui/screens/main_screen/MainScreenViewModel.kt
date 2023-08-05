package com.kxsv.schooldiary.ui.screens.main_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.repository.LessonRepository
import com.kxsv.schooldiary.data.repository.PatternStrokeRepository
import com.kxsv.schooldiary.data.repository.StudyDayRepository
import com.kxsv.schooldiary.data.repository.TaskRepository
import com.kxsv.schooldiary.data.repository.UserPreferencesRepository
import com.kxsv.schooldiary.di.util.IoDispatcher
import com.kxsv.schooldiary.util.Utils.toList
import com.kxsv.schooldiary.util.remote.NetworkException
import com.kxsv.schooldiary.util.ui.WhileUiSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject


data class MainUiState(
	val itemList: List<MainScreenItem> = emptyList(),
	val isLoading: Boolean = false,
	val userMessage: Int? = null,
)

private const val TAG = "MainScreenViewModel"

@HiltViewModel
class MainScreenViewModel @Inject constructor(
	taskRepository: TaskRepository,
	private val lessonRepository: LessonRepository,
	private val studyDayRepository: StudyDayRepository,
	private val strokeRepository: PatternStrokeRepository,
	private val userPreferencesRepository: UserPreferencesRepository,
	@IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
	
	private val startRange = LocalDate.now()
	private val endRange = LocalDate.now().plusDays(7)
	
	@OptIn(ExperimentalCoroutinesApi::class)
	private val _tasks = taskRepository.observeAllWithSubjectForDateRange(
		startRange = startRange,
		endRange = endRange
	).flatMapLatest { taskWithSubjectList ->
		flowOf(taskWithSubjectList.groupBy {
			it.taskEntity.dueDate
		})
	}
	
	private val _classes = lessonRepository.observeDayAndLessonsWithSubjectByDateRange(
		startRange = startRange,
		endRange = endRange
	)
	
	private val _uiState = MutableStateFlow(MainUiState())
	val uiState = combine(
		_uiState, _tasks, _classes
	) { state, tasks, classes ->
		
		val itemList = mutableListOf<MainScreenItem>()
		(startRange..endRange).toList().forEach { itemDate ->
			val studyDay = studyDayRepository.getByDate(itemDate)
			
			val appliedPatternId =
				studyDay?.appliedPatternId ?: userPreferencesRepository.getPatternId()
			val pattern = strokeRepository.getStrokesByPatternId(appliedPatternId)
			
			val mappedClasses = mutableMapOf<Int, SubjectEntity>()
			classes[itemDate]?.forEach { mappedClasses[it.lesson.index] = it.subject }
			
			val item = MainScreenItem(
				date = itemDate,
				tasks = tasks[itemDate] ?: emptyList(),
				classes = mappedClasses,
				pattern = pattern
			)
			Log.d(TAG, "combine:\nitemList.add = ${item.classes}")
			
			itemList.add(item)
		}
		state.copy(
			itemList = itemList.sortedBy { it.date }
		)
	}.stateIn(viewModelScope, WhileUiSubscribed, MainUiState(isLoading = true))
	
	init {
//		refresh()
	}
	
	fun snackbarMessageShown() {
		_uiState.update { it.copy(userMessage = null) }
	}
	
	/**
	 * Refresh
	 *
	 * @throws NetworkException.NotLoggedInException
	 */
	fun refresh() {
		viewModelScope.launch(ioDispatcher) {
//			eduPerformanceRepository.fetchEduPerformance()
		}
	}
	
	private fun showSnackbarMessage(message: Int) {
		_uiState.update { it.copy(userMessage = message) }
	}
}