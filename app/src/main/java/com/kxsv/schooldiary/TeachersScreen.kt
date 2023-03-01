package com.kxsv.schooldiary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.kxsv.schooldiary.ui.theme.TopBarBackground
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun TeachersPreview() {
    val c = ConstraintSet {
        val topBar = createRefFor("topbar")
        val table = createRefFor("table")

        constrain(table) {
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
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
            ) {
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
                TopBar("Учителя", bottomPadding = 0.dp, drawerState = drawerState, scope = scope)
                Column(
                    modifier = Modifier
                        .layoutId("table")
                ) {
                    //items(1) {
                    TeachersTable()
                    //}
                }
            }
        }
    )
}

@Composable
fun TeachersTable(
    teachers: List<Pair<String, String>> = listOf(
        Pair("Пермякова П.А.", "+19631223511"),
        Pair("Муллин А.А.", "+29631223512"),
        Pair("Лихашерстный В.Ю.", "+39631223513"),
        Pair("Пермякова П.А.", "+19631223511"),
        Pair("Муллин А.А.", "+29631223512"),
    )
) {
    LazyColumn {
        teachers.forEachIndexed { index, teacher ->
            // REVIEW)))
            val rowColor = if (index % 2 == 0) Color.Transparent else TopBarBackground
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(42.dp)
                        .background(rowColor),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                    ) {
                        Text(
                            text = teacher.first,
                            textAlign = TextAlign.Center,
                            fontSize = 19.sp,
                            //fontFamily = fontFamily,
                            fontWeight = FontWeight.Normal,
                            color = MainText,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = teacher.second,
                            textAlign = TextAlign.Center,
                            fontSize = 19.sp,
                            //fontFamily = fontFamily,
                            fontWeight = FontWeight.Normal,
                            color = MainText,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}
