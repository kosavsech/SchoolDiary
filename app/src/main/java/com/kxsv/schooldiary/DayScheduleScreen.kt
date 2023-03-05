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
import androidx.compose.ui.res.painterResource
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
import com.kxsv.schooldiary.ui.theme.SecondaryText
import kotlinx.coroutines.launch

// TODO introduce DB for that stuff
val time: List<Pair<String, String>> = listOf(
    Pair("8:30", "9:15"),
    Pair("9:30", "10:15"),
    Pair("10:30", "11:15"),
    Pair("11:25", "12:10"),
    Pair("12:30", "13:15"),
    Pair("13:35", "14:20"),
    Pair("14:30", "15:15"),
    Pair("15:25", "16:10"),
    Pair("16:20", "17:05"),
    Pair("17:15", "18:00"),
    Pair("18:10", "18:55"),
)
val cabinets: List<String> = listOf(
"110",
"210",
"310",
"410",
"510",
"610",
"710",
"810",
"910",
"1010",
)
val tags: List<String> = listOf(
    "tagName",
    "tagName",
    "tagName",
    "tagName",
    "tagName",
    "tagName",
    "tagName",
    "tagName",
    "tagName",
    "tagName",
)
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
                        DayOfWeekHeader()
                        Lessons()
                    }
                }
            }
        }
    )
}

@Composable
fun DayOfWeekHeader(
    dayOfWeek: String = "dayofweek",
    lessonsAmount: String = "0"
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, bottom = 24.dp)
    ) {
        Text(
            text = dayOfWeek,
            textAlign = TextAlign.Start,
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold,
            color = MainText,
        )
        Text(
            text = "У тебя $lessonsAmount уроков",
            textAlign = TextAlign.Start,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = MainText
        )
    }
}

@Composable
fun Lessons(
    lessons: List<String> = listOf(
        "Русский язык",
        "Геометрия",
        "Физика",
        "Иностранный язык (английский)",
        "Английский язык",
        "Алгебра",
        "Немецкий язык",
        "Английский язык",
        "Алгебра",
        "Немецкий язык",
    ),
) {
    lessons.forEachIndexed { it, lesson ->
        Lesson(it, lesson)
        if (it != lessons.lastIndex) {
            Divider(
                modifier = Modifier
                    .padding(bottom = 14.dp, top = 14.dp)
                    .fillMaxWidth(),
                color = com.kxsv.schooldiary.ui.theme.Divider,
                thickness = 1.dp,
            )
        }
    }
}

@Composable
fun Lesson(
    index : Int,
    name : String = "lesson name",
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp),
    ) {
        Text(
            text = "${time[index].first} - ${time[index].second}",
            textAlign = TextAlign.Start,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = SecondaryText,
        )
        Text(
            text = name,
            textAlign = TextAlign.Start,
            fontSize = 23.sp,
            fontWeight = FontWeight.SemiBold,
            color = MainText,
            modifier = Modifier
                .padding(top = 4.dp)
        )
        Row(
            modifier = Modifier
                .padding(top = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(end = 12.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_cabinet),
                    contentDescription = "", // TODO
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(13.dp, 15.dp)
                        .padding(end = 6.dp)
                )
                Text(
                    text = cabinets[index],
                    textAlign = TextAlign.Start,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SecondaryText
                )
            }
            Text(
                text = "Теги: ${tags[index]}",
                textAlign = TextAlign.Start,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = SecondaryText
            )
        }

    }
}