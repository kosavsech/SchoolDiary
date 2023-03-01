package com.kxsv.schooldiary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.layoutId
import com.kxsv.schooldiary.ui.theme.MainText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun DaySchedulePreview() {
    val c = ConstraintSet {
        val topBar = createRefFor("topbar")
        val subject = createRefFor("subjects")

        constrain(subject) {
            top.linkTo(topBar.bottom)
        }
    }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
// icons to mimic drawer destinations
    val items = listOf(
        "Главное меню",
        "Расписание",
        "Задания",
        "Оценки",
        "divider",
        "Список учителей",
        "Посещаемость",
        "Заметки",
        "Звуковые записи",
        "Учебные сайты",
        "divider",
        "Помощь и обратная связь",
        "Настройки",
    )
    val selectedItem = remember { mutableStateOf(items[0]) }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                items.forEach { item ->
                    if (item == "divider") {
                        Divider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            color = com.kxsv.schooldiary.ui.theme.Divider,
                            thickness = 1.dp
                        )
                    } else {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Email, contentDescription = null) },
                            label = {
                                Text(
                                    text = item,
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp,
                                    //fontFamily = fontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = MainText,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            selected = item == selectedItem.value,
                            onClick = {
                                scope.launch { drawerState.close() }
                                selectedItem.value = item
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        },
        content = {
            ConstraintLayout(
                constraintSet = c,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                TopBar("Расписание", drawerState = drawerState, scope = scope)
                LazyColumn(
                    modifier = Modifier
                        .layoutId("subjects")
                ) {
                    items(1) {

                    }
                }
            }
        }
    )
}
