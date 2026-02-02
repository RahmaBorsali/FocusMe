package com.example.focusme.presentation.screen.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.focusme.presentation.ui.theme.AppBg
import com.example.focusme.presentation.ui.theme.PinkPrimary
import com.example.focusme.presentation.ui.theme.TextDark
import com.example.focusme.presentation.ui.theme.TextGray
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

@Composable
fun PlannerScreen(
    onBack: () -> Unit
) {
    val today = remember {
        Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
    }

    // 1er jour du mois affiché
    var monthStart by remember {
        mutableStateOf(LocalDate(today.year, today.monthNumber, 1))
    }
    var selectedDate by remember { mutableStateOf(today) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Spacer(Modifier.height(6.dp))

        // Top bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextDark)
            }

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    text = "Plan du jour",
                    color = TextDark,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Organisez vos tâches",
                    color = TextGray,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Calendar card
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                // Month header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val prev = monthStart.minus(1, DateTimeUnit.MONTH)
                            monthStart = LocalDate(prev.year, prev.monthNumber, 1)
                        }
                    ) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = "Prev",
                            tint = PinkPrimary
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    Text(
                        text = "${monthNameEn(monthStart.monthNumber)} ${monthStart.year}",
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(Modifier.weight(1f))

                    IconButton(
                        onClick = {
                            val next = monthStart.plus(1, DateTimeUnit.MONTH)
                            monthStart = LocalDate(next.year, next.monthNumber, 1)
                        }
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Next",
                            tint = PinkPrimary
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Week labels
                val weekLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                Row(modifier = Modifier.fillMaxWidth()) {
                    weekLabels.forEach {
                        Text(
                            text = it,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            color = TextGray,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Calendar grid (API 24 OK)
                val daysInMonth = daysInMonth(monthStart.year, monthStart.monthNumber)
                val leadingEmpty = leadingEmptyCellsMondayFirst(monthStart) // 0..6

                val totalCells = leadingEmpty + daysInMonth
                val rows = (totalCells + 6) / 7

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    var day = 1
                    repeat(rows) { r ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            repeat(7) { c ->
                                val cell = r * 7 + c
                                if (cell < leadingEmpty || day > daysInMonth) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                    )
                                } else {
                                    val date = LocalDate(monthStart.year, monthStart.monthNumber, day)
                                    val isSelected = date == selectedDate

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) PinkPrimary else Color.Transparent)
                                                .clickable { selectedDate = date },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = day.toString(),
                                                color = if (isSelected) Color.White else TextDark,
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                                            )
                                        }
                                    }
                                    day++
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Day row + copy
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val dowFr = dowShortFr(selectedDate)             // "Dim"
            val monthFr = monthShortFr(selectedDate.monthNumber) // "févr"

            Text(
                text = "${dowFr}, ${selectedDate.dayOfMonth} $monthFr.",
                color = TextDark,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.weight(1f))

            OutlinedButton(
                onClick = { /* plus tard */ },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PinkPrimary)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = PinkPrimary)
                Spacer(Modifier.width(8.dp))
                Text("Copier", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(8.dp))
        Text("Aucune tâche", color = TextGray)

        Spacer(Modifier.weight(1f))

        // Add task button
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(
                onClick = { /* plus tard */ },
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                modifier = Modifier.height(56.dp)
            ) {
                Text(
                    "+",
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Spacer(Modifier.width(10.dp))
                Text("Ajouter tâche", fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }

        Spacer(Modifier.height(10.dp))
    }
}

/* ---------------- helpers (API 24 OK) ---------------- */

private fun daysInMonth(year: Int, month: Int): Int {
    return when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> 30
    }
}

private fun isLeapYear(year: Int): Boolean {
    return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
}

// Monday-first grid: monday = 0 ... sunday = 6
private fun leadingEmptyCellsMondayFirst(firstOfMonth: LocalDate): Int {
    // dayOfWeek.ordinal: Monday=0 .. Sunday=6 (kotlinx-datetime)
    return firstOfMonth.dayOfWeek.ordinal
}

private fun monthNameEn(month: Int): String = when (month) {
    1 -> "January"
    2 -> "February"
    3 -> "March"
    4 -> "April"
    5 -> "May"
    6 -> "June"
    7 -> "July"
    8 -> "August"
    9 -> "September"
    10 -> "October"
    11 -> "November"
    12 -> "December"
    else -> ""
}

private fun monthShortFr(month: Int): String = when (month) {
    1 -> "janv"
    2 -> "févr"
    3 -> "mars"
    4 -> "avr"
    5 -> "mai"
    6 -> "juin"
    7 -> "juil"
    8 -> "août"
    9 -> "sept"
    10 -> "oct"
    11 -> "nov"
    12 -> "déc"
    else -> ""
}

// ordinal: Monday=0 ... Sunday=6
private fun dowShortFr(date: LocalDate): String = when (date.dayOfWeek.ordinal) {
    0 -> "Lun"
    1 -> "Mar"
    2 -> "Mer"
    3 -> "Jeu"
    4 -> "Ven"
    5 -> "Sam"
    6 -> "Dim"
    else -> ""
}
