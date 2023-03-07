package com.kxsv.schooldiary.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.layoutId
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    val c = ConstraintSet {
        val topBar = createRefFor("topbar")
        val content = createRefFor("content")

        constrain(content) {
            top.linkTo(topBar.bottom)
        }
    }

    val selectedItem = remember { mutableStateOf(SideMenuScreens[10]) }
    SideMenu(
        navController = navController,
        selectedItem = selectedItem,
        drawerState = drawerState,
        scope = scope
    ) {
        ConstraintLayout(
            constraintSet = c,
            modifier = Modifier
                .fillMaxSize()
        ) {
            TopBar("Настройки", drawerState = drawerState, scope = scope, navController = navController)
            Text(text = "NOT DONE YET", Modifier.layoutId("content"), color = Color.Red) // TODO
        }
    }
}