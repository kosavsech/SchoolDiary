package com.kxsv.schooldiary.ui.screens.subject_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.app.sync.initializers.SyncConstraints
import com.kxsv.schooldiary.app.sync.workers.DelegatingWorker
import com.kxsv.schooldiary.app.sync.workers.SubjectsSyncWorker
import com.kxsv.schooldiary.app.sync.workers.delegatedData
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.repository.SubjectRepository
import com.kxsv.schooldiary.di.util.AppDispatchers
import com.kxsv.schooldiary.di.util.Dispatcher
import com.kxsv.schooldiary.ui.main.navigation.ADD_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.DELETE_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.EDIT_RESULT_OK
import com.kxsv.schooldiary.ui.util.Async
import com.kxsv.schooldiary.ui.util.WhileUiSubscribed
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
import javax.inject.Inject

data class SubjectsUiState(
	val subjects: List<SubjectEntity> = emptyList(),
	val isLoading: Boolean = false,
	val userMessage: Int? = null,
)

private const val TAG = "SubjectsViewModel"
private const val UNIQUE_WORK_NAME = "SubjectsScreenFetchSync"

@HiltViewModel
class SubjectsViewModel @Inject constructor(
	subjectRepository: SubjectRepository,
	private val workManager: WorkManager,
	@Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
	
	private val _subjectsAsync =
		subjectRepository.observeAll()
			.map { Async.Success(it) }
			.catch<Async<List<SubjectEntity>>> { emit(Async.Error(R.string.loading_subjects_error)) }
	
	private val _uiState = MutableStateFlow(SubjectsUiState())
	val uiState: StateFlow<SubjectsUiState> = combine(
		_uiState, _subjectsAsync
	) { state, subjects ->
		when (subjects) {
			Async.Loading -> {
				SubjectsUiState(isLoading = true)
			}
			
			is Async.Error -> {
				SubjectsUiState(
					userMessage = subjects.errorMessage
				)
			}
			
			is Async.Success -> {
				SubjectsUiState(
					subjects = subjects.data,
					userMessage = state.userMessage,
					isLoading = state.isLoading
				)
			}
		}
	}.stateIn(viewModelScope, WhileUiSubscribed, SubjectsUiState(isLoading = true))
	
	private var fetchJob: Job? = null
	
	init {
		refresh()
	}
	
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
	
	fun refresh() {
		_uiState.update { it.copy(isLoading = true) }
		val subjectsSyncRequest =
			OneTimeWorkRequestBuilder<DelegatingWorker>()
				.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
				.setConstraints(SyncConstraints)
				.setInputData(SubjectsSyncWorker::class.delegatedData())
				.build()
		
		val continuation = workManager
			.beginUniqueWork(
				UNIQUE_WORK_NAME,
				ExistingWorkPolicy.KEEP,
				subjectsSyncRequest
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
}
