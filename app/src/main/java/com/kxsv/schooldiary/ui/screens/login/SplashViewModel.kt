package com.kxsv.schooldiary.ui.screens.login

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.AppDestinations.GRADES_ROUTE
import com.kxsv.schooldiary.AppDestinations.LOGIN_ROUTE
import com.kxsv.schooldiary.domain.AppSettingsRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class SplashViewModel @Inject constructor(
	private val appSettingsRepository: AppSettingsRepository,
) : ViewModel() {
	
	/*	private val _isLoading: MutableState<Boolean> = mutableStateOf(true)
		val isLoading: State<Boolean> = _isLoading*/
	
	private val _startDestination: MutableState<String> = mutableStateOf(GRADES_ROUTE)
	val startDestination: State<String> = _startDestination
	
	init {
		viewModelScope.launch {
			val login = appSettingsRepository.getEduLogin()
			val password = appSettingsRepository.getEduPassword()
			val suppressed = appSettingsRepository.getInitLoginSuppression()
			val firstTime = (login.isNullOrBlank() or password.isNullOrBlank()) && !suppressed
			if (firstTime) {
				_startDestination.value = LOGIN_ROUTE
			} else {
				_startDestination.value = GRADES_ROUTE
			}
//			_isLoading.value = false
		}
	}
	
}