package com.kxsv.schooldiary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.layoutId
import com.kxsv.schooldiary.ui.theme.BorderOfBoxes
import com.kxsv.schooldiary.ui.theme.MainText
import com.kxsv.schooldiary.ui.theme.SecondaryText
import com.kxsv.schooldiary.ui.theme.TopBarBackground
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val c = ConstraintSet {
        val topBar = createRefFor("topbar")
        val chips = createRefFor("chips")
        val weeklyReport = createRefFor("weeklyreport")
        val currentDay = createRefFor("currentday")

        constrain(chips) {
            top.linkTo(topBar.bottom)
        }
        constrain(weeklyReport) {
            top.linkTo(chips.bottom)
            start.linkTo(parent.start)
        }
        constrain(currentDay) {
            top.linkTo(weeklyReport.bottom)
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
                TopBar(scope = scope, drawerState = drawerState)
                LazyColumn(
                    modifier = Modifier
                        .layoutId("chips")
                ) {
                    for (i in 1..2) {
                        item {
                            ChipSection(chips = listOf("Расписание", "Задания", "Оценки"))
                        }
                        item {
                            WeeklyReport(days = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб"))
                        }
                        item {
                            CurrentDay()
                        }
                        item {
                            RegularDay()
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String = "Главное меню",
    isDeadEnd: Boolean = false,
    bottomPadding: Dp = 14.dp,
    scope: CoroutineScope,
    drawerState: DrawerState
) {
    val imageVector = if (isDeadEnd) {
        Icons.Filled.Close
    } else {
        Icons.Filled.Menu
    }
    TopAppBar(
        modifier = Modifier
            .padding(bottom = bottomPadding)
            .layoutId("topbar"),
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
            navigationIconContentColor = BorderOfBoxes,
            titleContentColor = BorderOfBoxes,
        ),
        navigationIcon = {
            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                Icon(
                    imageVector = imageVector,
                    contentDescription = ""/* TODO Localized description*/
                )
            }
        }
    )
}

@Composable
fun ChipSection(
    chips: List<String>
) {
    LazyRow(
        modifier = Modifier
        //.layoutId("chips")
    ) {
        items(chips.size) {
            OutlinedButton(
                onClick = {/* TODO goToScreen(it) */ },
                border = BorderStroke(1.dp, BorderOfBoxes),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MainText),
                contentPadding = PaddingValues(16.dp, 10.dp),
                modifier = Modifier
                    .padding(start = 18.dp, bottom = 20.dp)
            ) {
                Text(
                    text = chips[it],
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    //fontFamily = fontFamily,
                    fontWeight = FontWeight.Normal,
                    color = MainText,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun WeeklyReport(
    days: List<String>
) {
    Column(
        // REVIEW make MainScreenCard to receive layoutId and padding to get rid of redundant column
        modifier = Modifier
            .layoutId("weeklyreport")
            .padding(bottom = 24.dp),
    ) {
        MainScreenCard {
            CardSubHeader(Icons.Outlined.AccountCircle, "Еженедельный отчет", 19.sp)
            /*Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = ""*//* TODO Localized description*//*,
                    modifier = Modifier
                        .padding(10.dp)
                )
                Text(
                    text = "Еженедельный отчет",
                    textAlign = TextAlign.Left,
                    fontSize = 19.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Normal,
                    color = MainText,
                    overflow = TextOverflow.Ellipsis,
                )
            }*/
            Column(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 8.dp, top = 16.dp, bottom = 60.dp)
            ) {
                Text(
                    text = "x событий",
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    //fontFamily = fontFamily,
                    fontWeight = FontWeight.Normal,
                    color = MainText,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "следующие 7 дней",
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp,
                    //fontFamily = fontFamily,
                    fontWeight = FontWeight.Normal,
                    color = SecondaryText,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, bottom = 16.dp, end = 105.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(days.size) {
                    Text(
                        text = days[it],
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        //fontFamily = fontFamily,
                        fontWeight = FontWeight.Normal,
                        color = MainText,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            ShowMore()
        }
    }
}

@Composable
fun ShowMore(
    // TODO link to the screen that opens after click
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /*TODO callback*/ }
    ) {
        Divider(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 8.dp)
                .fillMaxWidth(0.9512195f),
            color = com.kxsv.schooldiary.ui.theme.Divider,
            thickness = 1.dp
        )
        Row {
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = ""/* TODO Localized description*/,
                modifier = Modifier
                    .padding(start = 10.dp)
            )
            Text(
                text = "Показать больше",
                textAlign = TextAlign.Left,
                fontSize = 14.sp,
                //fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                color = MainText,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(top = 4.dp, bottom = 14.dp, start = 10.dp)
            )
        }
    }
}

@Composable
fun DayHeader(
    dayOfWeek: String = "where?",
    date: String = "где?"
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .wrapContentHeight()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = dayOfWeek, // TODO localize
            textAlign = TextAlign.Start,
            fontSize = 19.sp,
            //fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            color = MainText,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .alignByBaseline()
        )
        Text(
            text = date,
            textAlign = TextAlign.Right,
            fontSize = 12.sp,
            //fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            color = SecondaryText,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(end = 12.dp) // REVIEW
                .alignByBaseline()
        )
    }
}

@Composable
fun CurrentDay(
    // TODO make string localization
    icon: ImageVector = Icons.Outlined.Notifications,
    heading: String = "Прямо сейчас",
    dayOfWeek: String = "Сегодня", // REVIEW may be useless, just take today's day
    date: String = "19 февраля"
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 18.dp)
            .layoutId("currentday"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DayHeader(dayOfWeek, date)
        MainScreenCard {
            CardSubHeader(icon, heading, 14.sp)
            LessonDetailed()
        }
        // TODO CHECK FOR LESSON and if there are then do this part
        Spacer(
            modifier = Modifier
                .height(8.dp)
        )
        ScheduleDetailed()
    }
}

@Composable
fun RegularDay(
    icon: ImageVector = Icons.Outlined.Notifications,
    heading: String = "Предстоящие задания",
    dayOfWeek: String = "где он?", // REVIEW may be useless, just take today's day
    date: String = "где?"
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DayHeader(dayOfWeek, date)
        MainScreenCard {
            CardSubHeader(icon, heading, 14.sp)
            TaskGrid()
            ShowMore()
        }
        MiniSchedule()
    }
}

@Composable
fun MiniSchedule(
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
        Pair("8:30", "9:15"),
        Pair("9:30", "10:15"),
        Pair("10:30", "11:15"),
        Pair("11:25", "12:10"),
        Pair("12:30", "13:15"),
        Pair("13:35", "14:20"),
        Pair("14:30", "15:15"),
        Pair("15:25", "16:10"),
    )
) {
    if (lessons.isEmpty() or time.isEmpty()) {
        // TODO THROW error
        return
    }
    Column(
        modifier = Modifier
            .wrapContentWidth()
            .fillMaxWidth(0.9f)
            .padding(start = 10.dp, top = 8.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top

    ) {
        lessons.forEachIndexed { index, lesson ->
            ScheduleRow(time[index], lesson)
        }
    }
}

@Composable
fun ScheduleRow(
    time: Pair<String, String> = Pair("8:30", "9:15"),
    lesson: String = "Геометрия"
) {
    Row(
        modifier = Modifier
            .wrapContentSize()
            .padding(bottom = 0.dp)
    ) {
        Text(
            text = "${time.first} - ${time.second}",
            textAlign = TextAlign.End,
            fontSize = 10.sp,
            //fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            color = MainText,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .width(65.dp) // REVIEW
                .padding(end = 8.dp)
                .alignByBaseline()
        )
        Text(
            text = lesson,
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            //fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            color = MainText,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .alignByBaseline()
        )
    }
}

@Composable
fun TaskGrid(
    tasks: List<Pair<String, String>> = listOf(
        Pair("Пресс качат", "Русский язык"),
        Pair("т) бегит", "Физика"),
        Pair("турник", "Математика"),
        Pair("Анжуманя", "Английский язык"),
        Pair("Пресс качат", "Немецкий язык"),
        Pair("бегит", "Алгебра"),
        Pair("турник", "Геометрия"),
        Pair("Анжуманя", "Физкультура"),
        Pair("гантели", "Обществознание"),
    )
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp, bottom = 12.dp),
    ) {
        // REVIEW full remake?
        // TODO COLUMNS\ROWS AMOUNT SCREEN ADAPTIVE
        var rowsAmount = 3
        var columnsAmount = 2
        var alreadyPrinted = 0

        if (tasks.size <= rowsAmount) {
            rowsAmount = tasks.size
            columnsAmount = 1
        }

        for (column in 0 until columnsAmount) {
            var padding = 32.dp
            if ((column + 1) == columnsAmount) { // isLastColumn
                padding = 0.dp
            }
            Column(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(end = padding)
            ) {
                for (row in 0 until rowsAmount) {
                    Text(
                        text = "${row + 1 + alreadyPrinted}) ${tasks[row + alreadyPrinted].first}",
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        //fontFamily = fontFamily,
                        fontWeight = FontWeight.Normal,
                        color = MainText,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = tasks[row + alreadyPrinted].second,
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        //fontFamily = fontFamily,
                        fontWeight = FontWeight.Normal,
                        color = MainText,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                alreadyPrinted += rowsAmount
                if (tasks.size < 6) rowsAmount = tasks.size - alreadyPrinted
            }
        }
    }
}

@Composable
fun MainScreenCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            border = BorderStroke(1.dp, BorderOfBoxes),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            content = content
        )
    }
}

@Composable
fun CardSubHeader(
    icon: ImageVector,
    heading: String,
    headingSize: TextUnit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .wrapContentHeight()
            .padding(top = 8.dp, start = 10.dp, bottom = 10.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "",/* TODO Localized description*/
            modifier = Modifier
                .padding(end = 10.dp),
        )
        Text(
            text = heading,
            fontSize = headingSize,
            //fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            color = MainText,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun LessonDetailed(
    lessonName: String? = "Русский язык",
    time: Pair<String, String> = Pair("8:30", "9:15"),
    cabinet: String = "555"
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(Alignment.Bottom)
                .padding(start = 10.dp, end = 12.dp, bottom = 8.dp),
        ) {
            Text(
                text = lessonName.toString(),
                textAlign = TextAlign.Center,
                fontSize = 19.sp,
                //fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                color = MainText,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_clock),
                    contentDescription = "",/* TODO Localized description*/
                    modifier = Modifier
                        .size(20.dp, 20.dp)
                        .padding(end = 8.dp),
                )
                Text(
                    text = "30 м",/* TODO calculation from nowTime and endTime*/
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    //fontFamily = fontFamily,
                    fontWeight = FontWeight.Normal,
                    color = SecondaryText,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(Alignment.Bottom)
                .padding(start = 10.dp, end = 10.dp, bottom = 14.dp),
        ) {
            Text(
                text = "${time.first} - ${time.second}",
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                //fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                color = MainText,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(end = 14.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .wrapContentWidth()
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_cabinet),
                    contentDescription = "", /*TODO DESCRIPTION*/
                    modifier = Modifier
                        .size(13.dp, 15.dp)
                        .padding(end = 4.dp)
                )
                Text(
                    text = cabinet,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    //fontFamily = fontFamily,
                    fontWeight = FontWeight.Normal,
                    color = SecondaryText,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun ScheduleDetailed(
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
    )
) {
    if (lessons.isEmpty() or time.isEmpty()) {
        // TODO THROW error
        return
    }
    MainScreenCard {
        CardSubHeader(Icons.Filled.DateRange, "Следующие уроки", 14.sp)
        LessonDetailed(lessons[0], time[0], "310")
        if (lessons.size > 1) {
            Divider(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp)
                    .fillMaxWidth(0.9512195f),
                color = com.kxsv.schooldiary.ui.theme.Divider,
                thickness = 1.dp
            )
        }
        lessons.forEachIndexed { it, v ->
            if ((it == 0) or (it >= 4)) {
                return@forEachIndexed
            }
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(Alignment.Bottom)
                        .padding(start = 10.dp, bottom = 6.dp),
                ) {
                    Text(
                        text = v,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        //fontFamily = fontFamily,
                        fontWeight = FontWeight.Normal,
                        color = MainText,
                        overflow = TextOverflow.Ellipsis,
                    )
                    // REVIEW ICON BUTTON FOR SOME ACTIONS?
                    /*Icon(
                        painter = painterResource(R.drawable.ic_clock),
                        contentDescription = "", // TODO Localized description
                        modifier = Modifier
                            .size(20.dp, 20.dp)
                            .padding(end = 8.dp),
                    )*/

                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(Alignment.Bottom)
                        .padding(start = 10.dp, bottom = 12.dp),
                ) {
                    Text(
                        text = "${time[it].first} - ${time[it].second}",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        //fontFamily = fontFamily,
                        fontWeight = FontWeight.Normal,
                        color = MainText,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(end = 14.dp)
                    )
                }
            }
        }
        ShowMore()
    }
}