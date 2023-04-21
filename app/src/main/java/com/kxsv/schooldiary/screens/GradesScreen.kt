package com.kxsv.schooldiary.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kxsv.schooldiary.DB
import com.kxsv.schooldiary.core.presentation.components.TopBar
import com.kxsv.schooldiary.main_presentation.ui.theme.MainText
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradesPreview(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    val selectedItem = remember { mutableStateOf(SideMenuScreens[3]) }
    SideMenu(
        navController = navController,
        selectedItem = selectedItem,
        drawerState = drawerState,
        scope = scope
    ) {
        androidx.compose.material.Scaffold(
            topBar = {
                TopBar(
                    "Оценки",
                    drawerState = drawerState,
                    scope = scope,
                    navController = navController
                )

            },
        ) { innerPadding ->
            LazyColumn(modifier = Modifier.padding(innerPadding.calculateTopPadding())) {
                DB.subjectFullNames.forEach { lessonName ->
                    item {
                        SubjectGrade(lesson = lessonName)
                    }
                }
            }
        }
    }
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
                RowGrade() //todo show last 3 or just last if less than 3 marks
                RowGrade()
                RowGrade()
            }
        }
        Divider(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp)
                .fillMaxWidth(0.9512195f),
            color = com.kxsv.schooldiary.main_presentation.ui.theme.Divider,
            thickness = 1.dp
        )
    }

}

@Composable
fun RowGrade(
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
