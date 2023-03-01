package com.kxsv.schooldiary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.layoutId
import com.kxsv.schooldiary.ui.theme.BorderOfBoxes
import com.kxsv.schooldiary.ui.theme.MainText


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
    ConstraintLayout(
        constraintSet = c,
        modifier = Modifier
            .fillMaxSize()
    ) {
        TopBar("Расписание")
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
            text = "У тебя ${lessonsAmount} уроков",
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
    ),
    time: List<Pair<String, String>> = listOf(
        Pair("9:30", "10:15"),
        Pair("10:30", "11:15"),
        Pair("11:25", "12:10"),
        Pair("12:30", "13:15"),
        Pair("13:35", "14:20"),
        Pair("14:30", "15:15"),
        Pair("15:25", "16:10"),
    ),
    cabinets: List<String> = listOf(
        "310",
        "310",
        "310",
        "310",
        "310",
        "310",
        "310",
    ),
    tags: List<String> = listOf(
        "govno",
        "govno",
        "govno",
        "govno",
        "govno",
        "govno",
        "govno",
    ),
) {

    lessons.forEachIndexed { it, lesson ->
        if ((it == 0) or (it >= 10)) {
            return@forEachIndexed
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp),

            ) {
            Text(
                text = "${time[it].first} - ${time[it].second}",
                textAlign = TextAlign.Start,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = BorderOfBoxes,
            )
            Text(
                text = lesson,
                textAlign = TextAlign.Start,
                fontSize = 23.sp,
                fontWeight = FontWeight.SemiBold,
                color = MainText
            )
            Row {
                Row(
                    modifier = Modifier
                        .padding(end = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_cabinet),
                        contentDescription = "ic_cabinet",
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .size(13.dp, 15.dp)
                            .padding(end = 6.dp)
                    )
                    Text(
                        text = cabinets[it],
                        textAlign = TextAlign.Start,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BorderOfBoxes
                    )
                }
                Text(
                    text = "Теги: ${tags[it]}",
                    textAlign = TextAlign.Start,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BorderOfBoxes
                )
            }

        }
        if (it + 1 != lessons.size) {
            Divider(
                modifier = Modifier
                    .padding(bottom = 8.dp, top = 12.dp)
                    .fillMaxWidth(1f),
                color = com.kxsv.schooldiary.ui.theme.Divider,
                thickness = 1.dp,
            )
        }
    }
}