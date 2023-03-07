package com.kxsv.schooldiary

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kxsv.schooldiary.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigation() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    NavHost(navController = navController, startDestination = Screen.HomeScreen.route) {
        composable(route = Screen.HomeScreen.route) {
            MainScreen(navController = navController, drawerState = drawerState, scope = scope)
        }
        composable(route = Screen.ScheduleScreen.route) {
            DaySchedulePreview(
                navController = navController,
                drawerState = drawerState,
                scope = scope
            )
        }
        composable(route = Screen.TeachersScreen.route) {
            TeachersPreview(navController = navController, drawerState = drawerState, scope = scope)
        }
        composable(route = Screen.GradesScreen.route) {
            GradesPreview(navController = navController, drawerState = drawerState, scope = scope)
        }
        composable(route = Screen.TasksScreen.route) {
            TasksPreview(navController = navController, drawerState = drawerState, scope = scope)
        }
        composable(route = Screen.AttendanceScreen.route) {
            AttendanceScreen(
                navController = navController,
                drawerState = drawerState,
                scope = scope
            )
        }
        composable(route = Screen.NotesScreen.route) {
            NotesScreen(navController = navController, drawerState = drawerState, scope = scope)
        }
        composable(
            route = Screen.NoteDetailedScreen.route + "/{name}",
            arguments = listOf(
                navArgument("name") {
                    type = NavType.StringType
                    defaultValue = "TextName"
                }
            )
        ) { entry ->
            NoteDetailedScreen(
                navController = navController,
                drawerState = drawerState,
                scope = scope,
                name = entry.arguments?.getString("name")
            )
        }
        composable(route = Screen.RecordingsScreen.route) {
            RecordingsScreen(
                navController = navController,
                drawerState = drawerState,
                scope = scope
            )
        }
        composable(route = Screen.StudySitesScreen.route) {
            StudySites(navController = navController, drawerState = drawerState, scope = scope)
        }
        composable(route = Screen.HelpFeedbackScreen.route) {
            HelpFeedback(navController = navController, drawerState = drawerState, scope = scope)
        }
        composable(route = Screen.SettingsScreen.route) {
            SettingsScreen(navController = navController, drawerState = drawerState, scope = scope)
        }
    }
}