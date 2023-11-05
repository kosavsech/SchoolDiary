package com.kxsv.schooldiary.ui.screens.settings.categories.grade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.repository.UserPreferencesRepository
import com.kxsv.schooldiary.di.util.AppDispatchers
import com.kxsv.schooldiary.di.util.Dispatcher
import com.kxsv.schooldiary.ui.util.Async
import com.kxsv.schooldiary.ui.util.WhileUiSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GradeSettingsUiState(
	val defaultTargetMark: Double? = null,
	val defaultLowerBoundMark: Double? = null,
	val defaultRoundRule: Double? = null,
	
	val userMessage: Int? = null,
	val userMessageArgs: Array<out Any>? = null,
	val isLoading: Boolean = false,
)

private data class AsyncData(
	val defaultTargetMark: Double? = null,
	val defaultLowerBoundMark: Double? = null,
	val defaultRoundRule: Double? = null,
)

@HiltViewModel
class GradeSettingsViewModel @Inject constructor(
	private val userPreferencesRepository: UserPreferencesRepository,
	@Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
	
	private val _defaultTargetMark = userPreferencesRepository.observeTargetMark()
	private val _defaultLowerBoundMark = userPreferencesRepository.observeLowerBoundMark()
	private val _defaultRoundRule = userPreferencesRepository.observeRoundRule()
	
	private val _asyncData = combine(
		flow = _defaultTargetMark,
		flow2 = _defaultRoundRule,
		flow3 = _defaultLowerBoundMark
	) { targetMark, roundRule, lowerBoundMark ->
		AsyncData(
			defaultTargetMark = targetMark,
			defaultRoundRule = roundRule,
			defaultLowerBoundMark = lowerBoundMark
		)
	}
		.map { handleAsyncData(it) }
		.catch { emit(Async.Error(R.string.loading_settings_error)) }
		.stateIn(viewModelScope, WhileUiSubscribed, Async.Loading)
	
	private fun handleAsyncData(asyncData: AsyncData): Async<AsyncData> {
		if (asyncData.defaultTargetMark == null) return Async.Error(R.string.default_target_mark_not_found)
		if (asyncData.defaultRoundRule == null) return Async.Error(R.string.default_round_rule_not_found)
		if (asyncData.defaultLowerBoundMark == null) return Async.Error(R.string.default_lower_bound_mark_not_found)
		
		return Async.Success(asyncData)
	}
	
	private val _uiState = MutableStateFlow(GradeSettingsUiState())
	val uiState: StateFlow<GradeSettingsUiState> = combine(
		_uiState, _asyncData
	) { state, asyncData ->
		when (asyncData) {
			Async.Loading -> state.copy(isLoading = true)
			is Async.Error -> state.copy(userMessage = asyncData.errorMessage)
			is Async.Success -> {
				state.copy(
					defaultTargetMark = asyncData.data.defaultTargetMark,
					defaultLowerBoundMark = asyncData.data.defaultLowerBoundMark,
					defaultRoundRule = asyncData.data.defaultRoundRule,
				)
			}
		}
	}.stateIn(viewModelScope, WhileUiSubscribed, GradeSettingsUiState(isLoading = true))
	
	fun changeDefaultTargetMark(newTarget: Double) = viewModelScope.launch(ioDispatcher) {
		userPreferencesRepository.setTargetMark(newTarget)
	}
	
	fun changeDefaultLowerBoundMark(newLowerBound: Double) = viewModelScope.launch(ioDispatcher) {
		userPreferencesRepository.setLowerBoundMark(newLowerBound)
	}
	
	fun changeDefaultRoundRule(newRule: Double) = viewModelScope.launch(ioDispatcher) {
		userPreferencesRepository.setRoundRule(newRule)
	}
	
	fun snackbarMessageShown() {
		_uiState.update {
			it.copy(userMessage = null, userMessageArgs = null)
		}
	}
	
	
	/*private fun showSnackbarMessage(message: Int) {
		_uiState.upsert { it.copy(userMessage = message) }
	}
	
	private fun setSnackbarArgs(vararg args: Any) {
		_uiState.upsert { it.copy(userMessageArgs = args) }
	}*/
	
}