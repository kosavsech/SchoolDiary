package com.kxsv.schooldiary.ui.screens.edu_performance

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.edu_performance.EduPerformanceWithSubject
import com.kxsv.schooldiary.data.mapper.toEduPerformanceEntities
import com.kxsv.schooldiary.data.remote.util.NetworkException
import com.kxsv.schooldiary.data.repository.EduPerformanceRepository
import com.kxsv.schooldiary.data.repository.SubjectRepository
import com.kxsv.schooldiary.data.repository.UpdateRepository
import com.kxsv.schooldiary.data.repository.UserPreferencesRepository
import com.kxsv.schooldiary.data.util.AppVersionState
import com.kxsv.schooldiary.data.util.EduPerformancePeriod
import com.kxsv.schooldiary.data.util.user_preferences.Period.Companion.getTypeByPeriod
import com.kxsv.schooldiary.data.util.user_preferences.PeriodType
import com.kxsv.schooldiary.di.util.AppDispatchers
import com.kxsv.schooldiary.di.util.Dispatcher
import com.kxsv.schooldiary.ui.util.WhileUiSubscribed
import com.kxsv.schooldiary.util.Utils.getCurrentPeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EduPerformanceUiState(
	val eduPerformanceList: List<EduPerformanceWithSubject> = emptyList(),
	val isLoading: Boolean = false,
	val userMessage: Int? = null,
	val period: EduPerformancePeriod = EduPerformancePeriod.FIRST,
)

private const val TAG = "EduPerformanceViewModel"

@HiltViewModel
class EduPerformanceViewModel @Inject constructor(
	private val eduPerformanceRepository: EduPerformanceRepository,
	private val subjectRepository: SubjectRepository,
	private val userPreferencesRepository: UserPreferencesRepository,
	private val updateRepository: UpdateRepository,
	@Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
	
	private val _period = MutableStateFlow(EduPerformancePeriod.FIRST)
	private val _periodType = MutableStateFlow(PeriodType.SEMESTERS)
	val periodType = _periodType.asStateFlow()
	
	@OptIn(ExperimentalCoroutinesApi::class)
	private val _eduPerformanceAsync = _period
		.flatMapLatest { period ->
			eduPerformanceRepository.observeAllWithSubjectForPeriod(period)
		}
		.stateIn(viewModelScope, WhileUiSubscribed, emptyList())
	
	private val _uiState = MutableStateFlow(EduPerformanceUiState())
	val uiState = combine(
		_uiState, _eduPerformanceAsync, _period
	) { state, gradesAsync, period ->
		state.copy(
			eduPerformanceList = gradesAsync,
			period = period,
		)
	}.stateIn(viewModelScope, WhileUiSubscribed, EduPerformanceUiState())
	
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
		viewModelScope.launch(ioDispatcher) {
			val periodType = userPreferencesRepository.getEducationPeriodType()
			_periodType.update { periodType }
			val periodsRanges = userPreferencesRepository.getPeriodsRanges()
				.filter { getTypeByPeriod(it.period) == periodType }
			val currentPeriod = periodsRanges.getCurrentPeriod()
			if (currentPeriod != null) {
				changePeriod(currentPeriod)
			}
			refresh()
		}
	}
	// todo add showeditresult message
	
	fun snackbarMessageShown() {
		_uiState.update { it.copy(userMessage = null) }
	}
	
	/**
	 * Refresh
	 *
	 * @throws NetworkException.NotLoggedInException
	 */
	fun refresh() {
		_uiState.update { it.copy(isLoading = true) }
		
		viewModelScope.launch(ioDispatcher) {
			try {
				eduPerformanceRepository.fetchEduPerformance().let {
					it.forEach { performanceDtos ->
						Log.d(
							TAG, "performanceEntities.forEach: performanceDtos = $performanceDtos"
						)
						eduPerformanceRepository.upsertAll(
							performanceDtos.toEduPerformanceEntities(
								subjectRepository
							)
						)
					}
				}
				_uiState.update { it.copy(isLoading = false) }
			} catch (e: Exception) {
				_uiState.update { it.copy(isLoading = false) }
				Log.e(TAG, "refresh: ", e)
				showSnackbarMessage(R.string.exception_occurred)
			}
		}
	}
	
	fun changePeriod(newPeriod: EduPerformancePeriod) {
		_period.update { newPeriod }
	}
	
	private fun showSnackbarMessage(message: Int) {
		_uiState.update { it.copy(userMessage = message) }
	}
}