package com.kxsv.schooldiary.ui.screens.edu_performance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.data.local.features.edu_performance.EduPerformanceWithSubject
import com.kxsv.schooldiary.data.repository.EduPerformanceRepository
import com.kxsv.schooldiary.di.IoDispatcher
import com.kxsv.schooldiary.util.ui.EduPerformancePeriod
import com.kxsv.schooldiary.util.ui.WhileUiSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
	val period: EduPerformancePeriod = EduPerformancePeriod.FOURTH_TERM,
)

@HiltViewModel
class EduPerformanceViewModel @Inject constructor(
	private val eduPerformanceRepository: EduPerformanceRepository,
	@IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
	
	private val _period = MutableStateFlow(EduPerformancePeriod.FOURTH_TERM)
	
	@OptIn(ExperimentalCoroutinesApi::class)
	private val _eduPerformanceAsync = _period
		.flatMapLatest { period ->
			eduPerformanceRepository.observeAllWithSubjectForPeriod(period)
		}
		.stateIn(viewModelScope, WhileUiSubscribed, emptyList())
	
	private val _uiState = MutableStateFlow(EduPerformanceUiState())
	val uiState = combine(_uiState, _eduPerformanceAsync, _period) { state, gradesAsync, period ->
		state.copy(
			eduPerformanceList = gradesAsync,
			period = period,
		)
	}.stateIn(viewModelScope, WhileUiSubscribed, EduPerformanceUiState())
	
	init {
//		refresh()
	}
	
	fun snackbarMessageShown() {
		_uiState.update { it.copy(userMessage = null) }
	}
	
	fun refresh() {
		viewModelScope.launch {
			eduPerformanceRepository.fetchEduPerformance()
		}
	}
	
	fun changePeriod(newPeriod: EduPerformancePeriod) {
		_period.update { newPeriod }
	}
	
	private fun showSnackbarMessage(message: Int) {
		_uiState.update { it.copy(userMessage = message) }
	}
}