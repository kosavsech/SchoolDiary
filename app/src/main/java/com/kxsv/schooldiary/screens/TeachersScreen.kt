package com.kxsv.schooldiary.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.layoutId
import androidx.navigation.NavController
import com.kxsv.schooldiary.ui.theme.MainText
import com.kxsv.schooldiary.ui.theme.TopBarBackground
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeachersPreview(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    val c = ConstraintSet {
        val topBar = createRefFor("topbar")
        val content = createRefFor("content")

        constrain(content) {
            top.linkTo(topBar.bottom)
        }
    }
    val selectedItem = remember { mutableStateOf(items[5]) }
    SideMenu(
        navController = navController,
        selectedItem = selectedItem,
        drawerState = drawerState,
        scope = scope
    ) {
        ConstraintLayout(
            constraintSet = c,
            modifier = Modifier
                .fillMaxSize()
        ) {
            TopBar("Учителя", bottomPadding = 0.dp, drawerState = drawerState, scope = scope)
            TeachersTable()
        }
    }

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
    LazyColumn(Modifier.layoutId("content")) {
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
