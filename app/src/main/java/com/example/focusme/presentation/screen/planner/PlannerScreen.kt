package com.example.focusme.presentation.screen.planner

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
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
import kotlinx.coroutines.launch

@Composable
fun PlannerScreen(
    onBack: () -> Unit,
    onAddTask: (LocalDate) -> Unit,
    onEditTask: (Long) -> Unit,
) {
    val context = LocalContext.current
    val vm: PlannerViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory(context.applicationContext as Application)
    )
    val ui by vm.ui.collectAsState()
    val deleteId by vm.deleteId.collectAsState()

    val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val today = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    fun dateKey(d: LocalDate): String =
        "%04d-%02d-%02d".format(d.year, d.monthNumber, d.dayOfMonth)

    var selectedDate by remember { mutableStateOf(today) }

    // ✅ 1er jour du mois affiché
    var monthStart by remember { mutableStateOf(LocalDate(today.year, today.monthNumber, 1)) }

    // ✅ IMPORTANT : une seule fois + format correct
    LaunchedEffect(selectedDate) {
        vm.setDate(dateKey(selectedDate))
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark)
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
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Prev", tint = PinkPrimary)
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
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next", tint = PinkPrimary)
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

                    val daysInMonth = daysInMonth(monthStart.year, monthStart.monthNumber)
                    val leadingEmpty = leadingEmptyCellsMondayFirst(monthStart)
                    val totalCells = leadingEmpty + daysInMonth
                    val rows = (totalCells + 6) / 7

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        var day = 1
                        repeat(rows) { r ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                repeat(7) { c ->
                                    val cell = r * 7 + c
                                    if (cell < leadingEmpty || day > daysInMonth) {
                                        Box(modifier = Modifier.weight(1f).height(44.dp))
                                    } else {
                                        val date = LocalDate(monthStart.year, monthStart.monthNumber, day)
                                        val isSelected = date == selectedDate

                                        Box(
                                            modifier = Modifier.weight(1f).height(44.dp),
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

            // Day row + Copy ALL tasks
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dowFr = dowShortFr(selectedDate)
                val monthFr = monthShortFr(selectedDate.monthNumber)

                Text(
                    text = "${dowFr}, ${selectedDate.dayOfMonth} $monthFr.",
                    color = TextDark,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(Modifier.weight(1f))

                OutlinedButton(
                    onClick = {
                        val text = buildString {
                            append("📅 ${dowShortFr(selectedDate)}, ${selectedDate.dayOfMonth} ${monthShortFr(selectedDate.monthNumber)}\n\n")
                            if (ui.tasks.isEmpty()) append("Aucune tâche")
                            ui.tasks.forEachIndexed { i, t ->
                                append("${i + 1}) ${t.title}\n")
                                if (t.description.isNotBlank()) append("   - ${t.description}\n")
                                append("   - ${t.minutes} min\n\n")
                            }
                        }
                        clipboard.setText(AnnotatedString(text))
                        scope.launch { snackbarHostState.showSnackbar("Tâches copiées ✅") }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PinkPrimary)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = PinkPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Copier", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(10.dp))

            // Tasks list
            if (ui.tasks.isEmpty()) {
                Text("Aucune tâche", color = TextGray)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ui.tasks.forEach { task ->
                        TaskCard(
                            title = task.title,
                            description = task.description,
                            minutes = task.minutes,
                            onEdit = { onEditTask(task.id) },
                            onDelete = { vm.askDelete(task.id) }
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Add task button
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                    onClick = { onAddTask(selectedDate) },
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                    modifier = Modifier.height(56.dp)
                ) {
                    Text("+", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge, color = Color.White)
                    Spacer(Modifier.width(10.dp))
                    Text("Ajouter tâche", fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }

            Spacer(Modifier.height(10.dp))
        }

        // ✅ Delete dialog (EN DEHORS column, dans Scaffold)
        if (deleteId != null) {
            AlertDialog(
                onDismissRequest = { vm.cancelDelete() },
                title = { Text("Supprimer tâche ?") },
                text = { Text("Voulez-vous vraiment supprimer cette tâche ?") },
                confirmButton = {
                    TextButton(onClick = { vm.confirmDelete() }) {
                        Text("Supprimer", color = Color(0xFFE95B5B), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { vm.cancelDelete() }) {
                        Text("Annuler")
                    }
                }
            )
        }
    }
}

@Composable
private fun TaskCard(
    title: String,
    description: String,
    minutes: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()

    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.ExtraBold, color = TextDark)
                if (description.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(description, color = TextGray)
                }
                Spacer(Modifier.height(6.dp))
                Text("$minutes min", color = TextGray, fontWeight = FontWeight.SemiBold)
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PinkPrimary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE95B5B))
            }
        }
    }
}

/* ---------------- helpers (API 24 OK) ---------------- */

private fun daysInMonth(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if (isLeapYear(year)) 29 else 28
    else -> 30
}

private fun isLeapYear(year: Int): Boolean =
    (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

private fun leadingEmptyCellsMondayFirst(firstOfMonth: LocalDate): Int =
    firstOfMonth.dayOfWeek.ordinal

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
