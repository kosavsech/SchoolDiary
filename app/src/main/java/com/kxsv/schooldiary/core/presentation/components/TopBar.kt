package com.kxsv.schooldiary.core.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.layoutId
import androidx.navigation.NavController
import com.kxsv.schooldiary.main_presentation.ui.theme.SecondaryText
import com.kxsv.schooldiary.main_presentation.ui.theme.TopBarBackground
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String = "Главное меню",
    navController: NavController,
    isDeadEnd: Boolean = false,
    scope: CoroutineScope,
    drawerState: DrawerState
) {
    Column{
        TopAppBar(
            title = {
                Text(
                    title,
                    maxLines = 1,
                    fontSize = 23.sp,
                    //fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryText,
                    overflow = TextOverflow.Ellipsis
                )
            },
            colors = TopAppBarDefaults.smallTopAppBarColors(
                containerColor = TopBarBackground,
                navigationIconContentColor = SecondaryText,
                titleContentColor = SecondaryText,
            ),
            navigationIcon = {
                IconButton(onClick = {
                    if (isDeadEnd) {
                        navController.popBackStack()
                    } else {
                        scope.launch { drawerState.open() }
                    }
                }) {
                    Icon(
                        imageVector =
                        if (isDeadEnd) {
                            Icons.Filled.Close
                        } else {
                            Icons.Filled.Menu
                        },
                        contentDescription = ""/* TODO Localized description*/
                    )
                }
            },
            modifier = Modifier.layoutId("topbar"),
        )
    }
}