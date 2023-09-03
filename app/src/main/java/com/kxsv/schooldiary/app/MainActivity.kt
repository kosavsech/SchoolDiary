package com.kxsv.schooldiary.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.kxsv.schooldiary.ui.main.navigation.NavGraph
import com.kxsv.schooldiary.ui.screens.login.SplashViewModel
import com.kxsv.schooldiary.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
	
	@Inject
	lateinit var splashViewModel: SplashViewModel
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		setContent {
			val navController = rememberNavController()
			AppTheme {
				NavGraph(
					startRoute = splashViewModel.startDestination.value,
					navController = navController,
					activity = this
				)
			}
		}
	}
}