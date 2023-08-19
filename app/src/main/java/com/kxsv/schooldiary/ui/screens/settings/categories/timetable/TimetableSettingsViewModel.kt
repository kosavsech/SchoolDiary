package com.kxsv.schooldiary.ui.screens.settings.categories.timetable

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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TimetableSettingsUiState(
	val defaultLessonDuration: Long? = null,
	
	val userMessage: Int? = null,
	val userMessageArgs: Array<out Any>? = null,
	val isLoading: Boolean = false,
)

private data class AsyncData(
	val defaultLessonDuration: Long? = null,
)

@HiltViewModel
class TimetableSettingsViewModel @Inject constructor(
	private val userPreferencesRepository: UserPreferencesRepository,
	@Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
	
	private val _defaultLessonDuration = userPreferencesRepository.observeLessonDuration()
	private val _temp = flowOf(0)
	
	private val _asyncData = combine(
		_defaultLessonDuration, _temp
	) { defaultLessonDuration, _ ->
		AsyncData(
			defaultLessonDuration = defaultLessonDuration,
		)
	}
		.map { handleAsyncData(it) }
		.catch { emit(Async.Error(R.string.loading_settings_error)) }
		.stateIn(viewModelScope, WhileUiSubscribed, Async.Loading)
	
	private fun handleAsyncData(asyncData: AsyncData): Async<AsyncData> {
		if (asyncData.defaultLessonDuration == null) return Async.Error(R.string.default_lesson_duration_not_found)
		
		return Async.Success(asyncData)
	}
	
	private val _uiState = MutableStateFlow(TimetableSettingsUiState())
	val uiState: StateFlow<TimetableSettingsUiState> = combine(
		_uiState, _asyncData
	) { state, asyncData ->
		when (asyncData) {
			Async.Loading -> state.copy(isLoading = true)
			is Async.Error -> state.copy(userMessage = asyncData.errorMessage)
			is Async.Success -> {
				state.copy(
					defaultLessonDuration = asyncData.data.defaultLessonDuration,
				)
			}
		}
	}.stateIn(viewModelScope, WhileUiSubscribed, TimetableSettingsUiState(isLoading = true))
	
	fun changeDefaultLessonDuration(newDuration: Long) = viewModelScope.launch(ioDispatcher) {
		userPreferencesRepository.setLessonDuration(newDuration)
	}
	
	
	fun snackbarMessageShown() {
		_uiState.update {
			it.copy(userMessage = null, userMessageArgs = null)
		}
	}
	
	
	/*private fun showSnackbarMessage(message: Int) {
		_uiState.update { it.copy(userMessage = message) }
	}
	
	private fun setSnackbarArgs(vararg args: Any) {
		_uiState.update { it.copy(userMessageArgs = args) }
	}*/
	
}