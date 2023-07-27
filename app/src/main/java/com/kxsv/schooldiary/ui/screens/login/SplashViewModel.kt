package com.kxsv.schooldiary.ui.screens.login

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.data.repository.UserPreferencesRepository
import com.kxsv.schooldiary.di.IoDispatcher
import com.kxsv.schooldiary.ui.main.navigation.AppDestinations.LOGIN_ROUTE
import com.kxsv.schooldiary.ui.main.navigation.AppDestinations.TASKS_ROUTE
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

class SplashViewModel @Inject constructor(
	private val userPreferencesRepository: UserPreferencesRepository,
	@IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
	
	/*	private val _isLoading: MutableState<Boolean> = mutableStateOf(true)
		val isLoading: State<Boolean> = _isLoading*/
	
	private val _startDestination: MutableState<String> = mutableStateOf(LOGIN_ROUTE)
	val startDestination: State<String> = _startDestination
	
	init {
		viewModelScope.launch(ioDispatcher) {
			val login = userPreferencesRepository.getEduLogin()
			val password = userPreferencesRepository.getEduPassword()
			val suppressed = userPreferencesRepository.getInitLoginSuppression()
			val firstTime = (login.isNullOrBlank() or password.isNullOrBlank()) && !suppressed
			if (firstTime) {
				_startDestination.value = LOGIN_ROUTE
			} else {
				_startDestination.value = TASKS_ROUTE
			}
//			_isLoading.value = false
		}
	}
	
}