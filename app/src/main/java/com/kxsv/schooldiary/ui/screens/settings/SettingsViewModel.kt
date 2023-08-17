package com.kxsv.schooldiary.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.repository.UserPreferencesRepository
import com.kxsv.schooldiary.di.util.AppDispatchers
import com.kxsv.schooldiary.di.util.Dispatcher
import com.kxsv.schooldiary.ui.main.navigation.ADD_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.DELETE_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.EDIT_RESULT_OK
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

data class SettingsUiState(
	val defaultTargetMark: Double? = null,
	val defaultRoundRule: Double? = null,
	val defaultLessonDuration: Long? = null,
	val suppressInitLogin: Boolean? = null,
	
	val userMessage: Int? = null,
	val userMessageArgs: Array<out Any>? = null,
	val isLoading: Boolean = false,
)

private data class AsyncData(
	val defaultTargetMark: Double? = null,
	val defaultRoundRule: Double? = null,
	val defaultLessonDuration: Long? = null,
	val suppressInitLogin: Boolean? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
	private val userPreferencesRepository: UserPreferencesRepository,
	@Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
	
	private val _defaultTargetMark = userPreferencesRepository.observeTargetMark()
	private val _defaultRoundRule = userPreferencesRepository.observeRoundRule()
	private val _defaultLessonDuration = userPreferencesRepository.observeLessonDuration()
	private val _suppressInitLogin = userPreferencesRepository.observeInitLoginSuppression()
	
	private val _asyncData = combine(
		flow = _defaultTargetMark,
		flow2 = _suppressInitLogin,
		flow3 = _defaultLessonDuration,
		flow4 = _defaultRoundRule
	) { targetMark, suppressLogin, defaultLessonDuration, roundRule ->
		AsyncData(
			defaultTargetMark = targetMark,
			defaultRoundRule = roundRule,
			defaultLessonDuration = defaultLessonDuration,
			suppressInitLogin = suppressLogin,
		)
	}
		.map { handleAsyncData(it) }
		.catch { emit(Async.Error(R.string.loading_settings_error)) }
		.stateIn(viewModelScope, WhileUiSubscribed, Async.Loading)
	
	private fun handleAsyncData(asyncData: AsyncData): Async<AsyncData> {
		if (asyncData.defaultTargetMark == null) return Async.Error(R.string.default_target_mark_not_found)
		if (asyncData.defaultRoundRule == null) return Async.Error(R.string.default_round_rule_not_found)
		if (asyncData.defaultLessonDuration == null) return Async.Error(R.string.default_lesson_duration_not_found)
		if (asyncData.suppressInitLogin == null) return Async.Error(R.string.login_suppression_not_found)
		
		return Async.Success(asyncData)
	}
	
	private val _uiState = MutableStateFlow(SettingsUiState())
	val uiState: StateFlow<SettingsUiState> = combine(
		_uiState, _asyncData
	) { state, asyncData ->
		when (asyncData) {
			Async.Loading -> state.copy(isLoading = true)
			is Async.Error -> state.copy(userMessage = asyncData.errorMessage)
			is Async.Success -> {
				state.copy(
					defaultTargetMark = asyncData.data.defaultTargetMark,
					defaultRoundRule = asyncData.data.defaultRoundRule,
					defaultLessonDuration = asyncData.data.defaultLessonDuration,
					suppressInitLogin = asyncData.data.suppressInitLogin
				)
			}
		}
	}.stateIn(viewModelScope, WhileUiSubscribed, SettingsUiState(isLoading = true))
	
	fun changeDefaultTargetMark(newTarget: Double) = viewModelScope.launch(ioDispatcher) {
		userPreferencesRepository.setTargetMark(newTarget)
	}
	
	fun changeDefaultRoundRule(newRule: Double) = viewModelScope.launch(ioDispatcher) {
		userPreferencesRepository.setRoundRule(newRule)
	}
	
	fun changeDefaultLessonDuration(newDuration: Long) = viewModelScope.launch(ioDispatcher) {
		userPreferencesRepository.setLessonDuration(newDuration)
	}
	
	fun changeLoginSuppression(newValue: Boolean) = viewModelScope.launch(ioDispatcher) {
		userPreferencesRepository.setInitLoginSuppression(newValue)
	}
	
	fun snackbarMessageShown() {
		_uiState.update {
			it.copy(userMessage = null, userMessageArgs = null)
		}
	}
	
	fun showEditResultMessage(result: Int) {
		when (result) {
			EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_class_message)
			ADD_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_class_message)
			DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_class_message)
		}
	}
	
	private fun showSnackbarMessage(message: Int) {
		_uiState.update { it.copy(userMessage = message) }
	}
	
	private fun setSnackbarArgs(vararg args: Any) {
		_uiState.update { it.copy(userMessageArgs = args) }
	}
	
}