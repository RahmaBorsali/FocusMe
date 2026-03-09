package com.example.focusme.presentation.screen.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusme.data.local.TaskEntity
import com.example.focusme.presentation.ui.theme.PinkPrimary
import com.example.focusme.presentation.ui.theme.TextDark
import com.example.focusme.presentation.ui.theme.TextGray
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksSheet(
    sessionSeconds: Int,
    tasks: List<TaskEntity>,
    taskInput: String,
    onTaskInputChange: (String) -> Unit,
    onAddTask: () -> Unit,
    onRemoveTask: (Int) -> Unit,
    onCompleteTask: (index: Int) -> Unit = {},
    onPostponeTask: (index: Int, newDate: String) -> Unit = { _, _ -> },
    onPickFromPlanner: () -> Unit,
    onCancel: () -> Unit,
    onStart: () -> Unit,
    onClose: () -> Unit
) {
    val headerGrad = Brush.horizontalGradient(
        listOf(
            PinkPrimary,
            PinkPrimary.copy(alpha = 0.85f),
            Color(0xFFFFB3D1)
        )
    )

    val timeText = remember(sessionSeconds) {
        val mm = (sessionSeconds.coerceAtLeast(0)) / 60
        val ss = (sessionSeconds.coerceAtLeast(0)) % 60
        "%02d:%02d".format(mm, ss)
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.86f)
        ) {
            // HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(86.dp)
                    .background(headerGrad)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Session d'étude",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.weight(1f))

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            // CONTENT
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(18.dp)
            ) {
                Text(
                    "Minuteur définit",
                    color = TextGray,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    timeText,
                    color = TextDark,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.displaySmall
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    "Qu'est-ce que tu vas étudier ?",
                    color = TextDark,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(10.dp))

                // INPUT
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = taskInput,
                        onValueChange = onTaskInputChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ex. Maths – chapitre 5") },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PinkPrimary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color(0xFFF3F3F3),
                            unfocusedContainerColor = Color(0xFFF3F3F3),
                            cursorColor = PinkPrimary
                        )
                    )

                    Spacer(Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFF3F3F3))
                            .clickable { onAddTask() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = TextDark)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // LIST
                if (tasks.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 10.dp)
                    ) {
                        itemsIndexed(tasks) { index, task ->
                            var showPostpone by remember { mutableStateOf(false) }

                            if (showPostpone) {
                                PostponeDateDialog(
                                    onConfirm = { date ->
                                        onPostponeTask(index, date)
                                        showPostpone = false
                                    },
                                    onDismiss = { showPostpone = false }
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (task.isDone) Color(0xFFE8F5E9) else Color(0xFFF7F7F7))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    task.title,
                                    color = if (task.isDone) TextGray else TextDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                    textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None
                                )

                                if (task.dueDate != null && !task.isDone) {
                                    Text(
                                        "📅 ${task.dueDate}",
                                        color = PinkPrimary,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }

                                // Complete Icon (Gray if not done, Green if done)
                                IconButton(
                                    onClick = { if (!task.isDone) onCompleteTask(index) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle, 
                                        contentDescription = "Done", 
                                        tint = if (task.isDone) Color(0xFF4CAF50) else Color.LightGray
                                    )
                                }

                                // Postpone Icon (Only if NOT done)
                                if (!task.isDone) {
                                    IconButton(
                                        onClick = { showPostpone = true },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            androidx.compose.material.icons.Icons.Default.Schedule, 
                                            contentDescription = "Postpone", 
                                            tint = PinkPrimary
                                        )
                                    }
                                }

                                // Remove
                                IconButton(
                                    onClick = { onRemoveTask(index) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.LightGray)
                                }
                            }
                        }
                    }
                } else {
                    Spacer(Modifier.weight(1f))
                }

                Spacer(Modifier.height(10.dp))

                // PICK FROM PLANNER
                OutlinedButton(
                    onClick = onPickFromPlanner,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PinkPrimary),
                    border = null
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = PinkPrimary)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Choisir des tâches depuis le\nplanificateur du jour",
                        color = PinkPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(14.dp))
            }

            // FOOTER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(0.7f).height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = TextDark)
                ) {
                    Text("Annuler", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Button(
                    onClick = onStart,
                    modifier = Modifier.weight(1.4f).height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
                ) {
                    Text("Démarrer ! 🚀", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostponeDateDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis() + 86_400_000L
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            val selectedMillis = datePickerState.selectedDateMillis
            val isValid = selectedMillis != null && selectedMillis > System.currentTimeMillis()

            TextButton(
                onClick = {
                    if (isValid && selectedMillis != null) {
                        val cal = Calendar.getInstance().apply { timeInMillis = selectedMillis }
                        val date = "%04d-%02d-%02d".format(
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH) + 1,
                            cal.get(Calendar.DAY_OF_MONTH)
                        )
                        onConfirm(date)
                    }
                },
                enabled = isValid
            ) { Text("Confirmer", color = PinkPrimary, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler", color = TextGray) }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    "Reporter au...",
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        )
    }
}
