package com.kxsv.schooldiary.ui.screens.main_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.mapper.toSubjectEntitiesIndexed
import com.kxsv.schooldiary.data.repository.LessonRepository
import com.kxsv.schooldiary.data.repository.PatternStrokeRepository
import com.kxsv.schooldiary.data.repository.StudyDayRepository
import com.kxsv.schooldiary.data.repository.SubjectRepository
import com.kxsv.schooldiary.data.repository.TaskRepository
import com.kxsv.schooldiary.data.repository.UserPreferencesRepository
import com.kxsv.schooldiary.di.util.IoDispatcher
import com.kxsv.schooldiary.util.Utils
import com.kxsv.schooldiary.util.Utils.measurePerformanceInMS
import com.kxsv.schooldiary.util.Utils.toList
import com.kxsv.schooldiary.util.remote.NetworkException
import com.kxsv.schooldiary.util.ui.WhileUiSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.io.IOException
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
	private val subjectRepository: SubjectRepository,
	private val taskRepository: TaskRepository,
	private val lessonRepository: LessonRepository,
	private val studyDayRepository: StudyDayRepository,
	private val strokeRepository: PatternStrokeRepository,
	private val userPreferencesRepository: UserPreferencesRepository,
	@IoDispatcher private val ioDispatcher: CoroutineDispatcher,
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
	
	private val _classes = lessonRepository.observeDayAndLessonsWithSubjectByDateRange(
		startRange = startRange,
		endRange = endRange
	)
	
	private val _uiState = MutableStateFlow(MainUiState())
	val uiState = combine(
		_uiState, _tasks, /*_classes*/
	) { state, tasks /*classes*/ ->
		val itemList = mutableListOf<MainScreenItem>()
		(startRange..endRange).toList().forEach { itemDate ->
			val itemWithTasks = state.itemList.find { it.date == itemDate }?.let {
				it.copy(tasks = tasks[itemDate] ?: it.tasks)
			}
			Log.d(TAG, "combine:\nloaded tasks = ${itemWithTasks?.tasks}")
			if (itemWithTasks != null) {
				itemList.add(itemWithTasks)
			}
		}
		state.copy(
			itemList = itemList.sortedBy { it.date }
		)
	}.stateIn(viewModelScope, WhileUiSubscribed, MainUiState(isLoading = true))
	
	init {
		measurePerformanceInMS(
			logger = { time, _ ->
				Log.d(TAG, "retrieveSchedule: performance is $time MS")
			}
		) {
			retrieveSchedule()
		}
	}
	
	@OptIn(ExperimentalCoroutinesApi::class)
	fun retrieveSchedule() = viewModelScope.launch(ioDispatcher) {
		Log.d(TAG, "retrieveSchedule start: itemList.size = ${uiState.value.itemList.size}")
		_uiState.update { it.copy(isLoading = true) }
		
		val doneItemList: MutableList<Deferred<MainScreenItem>> = mutableListOf()
		val localClasses = lessonRepository
			.getDayAndLessonsWithSubjectByDateRange(startRange = startRange, endRange = endRange)
		
		(startRange..endRange).toList().forEach { itemDate ->
			val newItem = async {
				Log.d(TAG, "retrieveSchedule: forEach async started on date = $itemDate")
				val pattern = async {
					val studyDay = studyDayRepository.getByDate(itemDate)
					val appliedPatternId =
						studyDay?.appliedPatternId ?: userPreferencesRepository.getPatternId()
					strokeRepository.getStrokesByPatternId(appliedPatternId)
				}
				
				val localClassesForDate = localClasses[itemDate]
				
				val classedRetrieved = async {
					if (localClassesForDate != null) {
						val mappedClasses = mutableMapOf<Int, SubjectEntity>()
						localClassesForDate.forEach { mappedClasses[it.lesson.index] = it.subject }
						Log.d(TAG, "retrieveSchedule: found local classes")
						mappedClasses
					} else {
						Log.d(TAG, "retrieveSchedule: started fetching classes")
						loadNetworkScheduleOnDate(itemDate)
					}
				}
				
				val newMenuItem = MainScreenItem(
					date = itemDate,
					tasks = emptyList(),
					classes = classedRetrieved.await(),
					pattern = pattern.await()
				)
				
				Log.d(TAG, "retrieveSchedule: oldItemList(size = ${uiState.value.itemList.size})")
				val newItemList = uiState.value.itemList.filterNot { item ->
					item.date == itemDate
				} as MutableList
				Log.d(TAG, "retrieveSchedule: newItemList(size = ${newItemList.size})")
				
				Log.d(TAG, "retrieveSchedule: added from net $newMenuItem")
				newItemList.add(newMenuItem)
				
				newMenuItem
			}
			doneItemList.add(newItem)
		}
		doneItemList.awaitAll()
		Log.d(TAG, "retrieveSchedule: newItemList $doneItemList size is ${doneItemList.size}")
		if (doneItemList.size == 8) {
			Log.d(TAG, "retrieveSchedule: done")
			_uiState.update {
				it.copy(
					isLoading = false,
					itemList = doneItemList.map { item -> item.getCompleted() }
						.sortedBy { item -> item.date }
				)
			}
			
		}
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
	
	private suspend fun loadNetworkScheduleOnDate(date: LocalDate): Map<Int, SubjectEntity> {
		try {
			val fetchedClasses = Utils.measurePerformanceInMS(logger = { time, result ->
				Log.i(
					TAG,
					"loadNetworkScheduleOnDate(date = $date): performance is $time ms\n" +
							"result is: $result"
				)
			}) {
				withTimeout(15000L) {
					lessonRepository.fetchLessonsOnDate(date)
						.toSubjectEntitiesIndexed(
							subjectRepository = subjectRepository,
							studyDayRepository = studyDayRepository
						)
				}
			}
			return fetchedClasses
		} catch (e: NetworkException) {
			Log.e(TAG, "loadNetworkScheduleOnDate: exception on login", e)
			return emptyMap()
		} catch (e: IOException) {
			Log.e(TAG, "loadNetworkScheduleOnDate: exception on response parseTerm", e)
			return emptyMap()
		} catch (e: TimeoutCancellationException) {
			// TODO: show message that couldn't connect to site
			Log.e(TAG, "loadNetworkScheduleOnDate: connection timed-out", e)
			return emptyMap()
		} catch (e: CancellationException) {
			Log.w(TAG, "loadNetworkScheduleOnDate: canceled", e)
			return emptyMap()
		} catch (e: Exception) {
			Log.e(TAG, "loadNetworkScheduleOnDate: exception", e)
			return emptyMap()
		}
	}
	
	fun completeTask(id: Long, isDone: Boolean) = viewModelScope.launch(ioDispatcher) {
		val taskToUpdate = taskRepository.getById(id)?.copy(isDone = isDone)
			?: return@launch showSnackbarMessage(R.string.task_not_found)
		taskRepository.updateTask(taskToUpdate)
	}
}