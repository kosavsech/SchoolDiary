package com.kxsv.schooldiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kxsv.schooldiary.ui.theme.SchoolDiaryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			SchoolDiaryTheme {
				AppNavGraph()
			}
		}
		
		/*runBlocking {
			user.Auth(DB.login, DB.password)
			//user.GetColumnFromDay(columnName = "grade")
			//user.GetMarkDescription()
		}*/
	}
}
