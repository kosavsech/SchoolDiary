package com.kxsv.schooldiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kxsv.schooldiary.core.domain.util.EduTatarParser
import dagger.hilt.android.AndroidEntryPoint

val user = EduTatarParser()
val DB = DataBase()

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Navigation()
        }

        /*runBlocking {
            user.Auth(DB.login, DB.password)
            //user.GetColumnFromDay(columnName = "grade")
            //user.GetMarkDescription()
        }*/
    }
}