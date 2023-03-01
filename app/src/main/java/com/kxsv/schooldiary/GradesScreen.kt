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
import androidx.compose.ui.Alignment
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
fun GradesPreview() {
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
                TopBar("Оценки", drawerState = drawerState, scope = scope)
                LazyColumn(
                    modifier = Modifier
                        .layoutId("subjects")
                ) {
                    for (i in 1..1) {
                        item {
                            SubjectGrade()
                        }
                        item {
                            SubjectGrade("Русский язык")
                        }
                        item {
                            SubjectGrade("Геометрия")
                        }
                        item {
                            SubjectGrade("Английский язык")
                        }
                        item {
                            SubjectGrade("Иностранный язык (английский)")
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun SubjectGrade(
    lesson: String = "Русский язык",
    isTags: Boolean = true,
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
            ) {
                Text(
                    text = lesson,
                    textAlign = TextAlign.Start,
                    fontSize = 23.sp,
                    //fontFamily = fontFamily,
                    fontWeight = FontWeight.Normal,
                    color = MainText,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .widthIn(max = 208.dp)
                )
                if (isTags) {
                    Text(
                        text = "Теги: tagname",
                        textAlign = TextAlign.Start,
                        fontSize = 14.sp,
                        //fontFamily = fontFamily,
                        fontWeight = FontWeight.Normal,
                        color = MainText,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .widthIn(max = 208.dp)

                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier
                    .wrapContentSize()
            ) {
                Grade() //todo show last 3 or just last if less than 3 marks
                Grade()
                Grade()
            }
        }
        Divider(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp)
                .fillMaxWidth(0.9512195f),
            color = com.kxsv.schooldiary.ui.theme.Divider,
            thickness = 1.dp
        )
    }

}

@Composable
fun Grade(
    date: String = "10.12",
    mark: String = "3",
    //workType: String = "Вид работ"
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .wrapContentSize()
    ) {
        Text(
            text = date,
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            //fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            color = MainText,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = mark,
            textAlign = TextAlign.Center,
            fontSize = 19.sp,
            //fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            color = MainText,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
