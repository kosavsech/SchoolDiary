package com.kxsv.schooldiary.ui.screens.settings.categories.terms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.data.repository.UserPreferencesRepository
import com.kxsv.schooldiary.data.util.user_preferences.PeriodType
import com.kxsv.schooldiary.data.util.user_preferences.PeriodWithRange
import com.kxsv.schooldiary.di.util.AppDispatchers
import com.kxsv.schooldiary.di.util.Dispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "TermsSettingsViewModel"

data class TermsSettingsUiState(
	val educationPeriodType: PeriodType? = null,
	val allPeriodRanges: PersistentList<PeriodWithRange>? = null,
	val periodWithRangeToUpdate: PeriodWithRange? = null,
	
	val isSaved: Boolean = false,
	
	val userMessage: Int? = null,
	val userMessageArgs: Array<out Any>? = null,
	val isLoading: Boolean = false,
)

@HiltViewModel
class TermsSettingsViewModel @Inject constructor(
	private val userPreferencesRepository: UserPreferencesRepository,
	@Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
	
	private val _uiState = MutableStateFlow(TermsSettingsUiState())
	val uiState: StateFlow<TermsSettingsUiState> = _uiState.asStateFlow()
	
	init {
		loadSettings()
	}
	
	@Suppress("DeferredResultUnused")
	private fun loadSettings() {
		viewModelScope.launch(ioDispatcher) {
			async { _uiState.update { it.copy(educationPeriodType = userPreferencesRepository.getEducationPeriodType()) } }
			async { _uiState.update { it.copy(allPeriodRanges = userPreferencesRepository.getPeriodsRanges()) } }
		}
	}
	
	fun changeEducationPeriodType(newValue: PeriodType) {
		_uiState.update { it.copy(educationPeriodType = newValue) }
	}
	
	fun selectEntry(newValue: PeriodWithRange) {
		_uiState.update { it.copy(periodWithRangeToUpdate = newValue) }
	}
	
	fun unselectEntry() {
		_uiState.update { it.copy(periodWithRangeToUpdate = null) }
	}
	
	fun savePeriodsRanges() {
		val allPeriodRanges = uiState.value.allPeriodRanges
			?: throw IllegalStateException("Tried to save period but period ranges are null")
		val periodToUpdate = uiState.value.periodWithRangeToUpdate
			?: throw IllegalStateException("Tried to save period but it's null")
		viewModelScope.launch(ioDispatcher) {
			val newPeriodsRanges = allPeriodRanges
				.filterNot { it.period == periodToUpdate.period }
				.plus(periodToUpdate)
				.sortedBy { it.period.ordinal }
				.toPersistentList()
			
			_uiState.update { it.copy(allPeriodRanges = newPeriodsRanges) }
			unselectEntry()
		}
	}
	
	fun updateEntry(newValue: PeriodWithRange) {
		_uiState.update { it.copy(periodWithRangeToUpdate = newValue) }
	}
	
	fun snackbarMessageShown() {
		_uiState.update {
			it.copy(userMessage = null, userMessageArgs = null)
		}
	}
	
	private fun showSnackbarMessage(message: Int) {
		_uiState.update { it.copy(userMessage = message) }
	}
	
	private fun setSnackbarArgs(vararg args: Any) {
		_uiState.update { it.copy(userMessageArgs = args) }
	}
	
	fun save() {
		val educationPeriodType = uiState.value.educationPeriodType
			?: throw RuntimeException("Tried to save settings but they are null")
		val periodsRanges = uiState.value.allPeriodRanges
			?: throw RuntimeException("Tried to save settings but they are null")
		
		viewModelScope.launch(ioDispatcher) {
			userPreferencesRepository.setEducationPeriodType(educationPeriodType)
			userPreferencesRepository.setPeriodsRanges(periodsRanges)
			_uiState.update { it.copy(isSaved = true) }
		}
	}
	
}