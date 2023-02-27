package com.kxsv.schooldiary

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.layoutId

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

            }
        }
    }
}
