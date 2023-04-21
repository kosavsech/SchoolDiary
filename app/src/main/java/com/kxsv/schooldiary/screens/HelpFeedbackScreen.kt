package com.kxsv.schooldiary.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.constraintlayout.compose.layoutId
import androidx.navigation.NavController
import com.kxsv.schooldiary.core.presentation.components.TopBar
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpFeedback(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    val selectedItem = remember { mutableStateOf(SideMenuScreens[9]) }
    SideMenu(
        navController = navController,
        selectedItem = selectedItem,
        drawerState = drawerState,
        scope = scope
    ) {
        androidx.compose.material.Scaffold(
            topBar = {
                TopBar(
                    "Помощь и обратная связь",
                    drawerState = drawerState,
                    scope = scope,
                    navController = navController
                )

            },
        ) { innerPadding ->
            Box(
                modifier = Modifier.padding(innerPadding.calculateTopPadding())
            ){
                Text(text = "NOT DONE YET", Modifier.layoutId("content"), color = Color.Red) // TODO
            }
        }
    }
}
