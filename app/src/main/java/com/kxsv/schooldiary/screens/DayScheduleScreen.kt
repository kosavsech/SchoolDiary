package com.kxsv.schooldiary.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.core.presentation.components.TopBar
import com.kxsv.schooldiary.main_presentation.ui.theme.MainText
import com.kxsv.schooldiary.main_presentation.ui.theme.SecondaryText
import kotlinx.coroutines.CoroutineScope

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
@Composable
fun DaySchedulePreview(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    val selectedItem = remember { mutableStateOf(SideMenuScreens[1]) }
    SideMenu(
        navController = navController,
        selectedItem = selectedItem,
        drawerState = drawerState,
        scope = scope
    ) {
        androidx.compose.material.Scaffold(
            topBar = {
                TopBar(
                    "Расписание",
                    drawerState = drawerState,
                    scope = scope,
                    navController = navController
                )

            },
        ) { innerPadding ->
            LazyColumn(modifier = Modifier.padding(innerPadding.calculateTopPadding())) {
                val lessons = listOf(
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
                )
                item {
                    DayOfWeekHeader(lessonsAmount = lessons.size)
                    LessonsList(lessons)
                }
            }
        }
    }
}

@Composable
fun DayOfWeekHeader(
    dayOfWeek: String = "dayofweek",
    lessonsAmount: Int = 0
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
fun LessonsList(
    lessons: List<String>,
) {
    lessons.forEachIndexed { it, lesson ->
        LessonStroke(it, lesson)
        if (it != lessons.lastIndex) {
            Divider(
                modifier = Modifier
                    .padding(bottom = 14.dp, top = 14.dp)
                    .fillMaxWidth(),
                color = com.kxsv.schooldiary.main_presentation.ui.theme.Divider,
                thickness = 1.dp,
            )
        }
    }
}

@Composable
fun LessonStroke(
    index: Int,
    name: String = "lesson name",
) {
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