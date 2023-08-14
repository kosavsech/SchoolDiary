package com.kxsv.schooldiary.ui.screens.grade_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.app.sync.initializers.SyncConstraints
import com.kxsv.schooldiary.app.sync.workers.DelegatingWorker
import com.kxsv.schooldiary.app.sync.workers.GradeSyncWorker
import com.kxsv.schooldiary.app.sync.workers.delegatedData
import com.kxsv.schooldiary.data.local.features.grade.GradeWithSubject
import com.kxsv.schooldiary.data.repository.GradeRepository
import com.kxsv.schooldiary.di.util.AppDispatchers
import com.kxsv.schooldiary.di.util.Dispatcher
import com.kxsv.schooldiary.ui.main.navigation.ADD_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.DELETE_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.EDIT_RESULT_OK
import com.kxsv.schooldiary.ui.util.GradesSortType
import com.kxsv.schooldiary.ui.util.WhileUiSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GradesUiState(
	val grades: List<GradeWithSubject> = emptyList(),
	val isLoading: Boolean = false,
	val userMessage: Int? = null,
	val sortType: GradesSortType = GradesSortType.MARK_DATE,
)

private const val TAG = "GradeTableViewModel"
private const val UNIQUE_WORK_NAME = "GradesScreenFetchSync"

@HiltViewModel
class GradesViewModel @Inject constructor(
	private val gradeRepository: GradeRepository,
	private val workManager: WorkManager,
	@Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
	
	private val _sortType = MutableStateFlow(GradesSortType.MARK_DATE)
	
	@OptIn(ExperimentalCoroutinesApi::class)
	private val _gradesAsyncSorted = _sortType
		.flatMapLatest { sortType ->
			when (sortType) {
				GradesSortType.MARK_DATE -> gradeRepository.observeAllWithSubjectOrderedByMarkDate()
				GradesSortType.FETCH_DATE -> gradeRepository.observeAllWithSubjectOrderedByFetchDate()
			}
		}
		.stateIn(viewModelScope, WhileUiSubscribed, emptyList())
	
	private val _uiState = MutableStateFlow(GradesUiState())
	val uiState = combine(_uiState, _gradesAsyncSorted, _sortType) { state, gradesAsync, sortType ->
		state.copy(
			grades = gradesAsync,
			sortType = sortType
		)
	}.stateIn(viewModelScope, WhileUiSubscribed, GradesUiState())
	
	private var fetchJob: Job? = null
	
	init {
		fetchGrades()
	}
	
	fun showEditResultMessage(result: Int) {
		when (result) {
			EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_grade_message)
			ADD_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_grade_message)
			DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_grade_message)
		}
	}
	
	fun sortGrades(sortType: GradesSortType) {
		_sortType.update { sortType }
	}
	
	fun snackbarMessageShown() {
		_uiState.update { it.copy(userMessage = null) }
	}
	
	fun fetchGrades() {
		_uiState.update { it.copy(isLoading = true) }
		val gradesSyncRequest =
			OneTimeWorkRequestBuilder<DelegatingWorker>()
				.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
				.setConstraints(SyncConstraints)
				.setInputData(GradeSyncWorker::class.delegatedData())
				.build()
		
		val continuation = workManager
			.beginUniqueWork(
				UNIQUE_WORK_NAME,
				ExistingWorkPolicy.KEEP,
				gradesSyncRequest
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
	
	private fun showSnackbarMessage(message: Int) {
		_uiState.update { it.copy(userMessage = message) }
	}
	
}
