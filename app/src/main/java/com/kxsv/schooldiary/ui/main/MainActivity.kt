package com.kxsv.schooldiary.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kxsv.schooldiary.ui.main.navigation.NavGraph
import com.kxsv.schooldiary.ui.screens.login.SplashViewModel
import com.kxsv.schooldiary.ui.theme.SchoolDiaryTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
	
	@Inject
	lateinit var splashViewModel: SplashViewModel
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		setContent {
			SchoolDiaryTheme {
				NavGraph(startDestination = splashViewModel.startDestination.value)
			}
		}
	}
}
