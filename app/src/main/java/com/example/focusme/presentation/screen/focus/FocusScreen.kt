package com.example.focusme.presentation.screen.focus


import TasksSheet
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusme.R
import com.example.focusme.notification.NotificationHelper
import com.example.focusme.presentation.ui.components.PrimaryButton
import com.example.focusme.presentation.ui.components.SoftCard
import com.example.focusme.presentation.ui.theme.PinkPrimary
import com.example.focusme.presentation.ui.theme.TextDark
import com.example.focusme.presentation.ui.theme.TextGray
import com.example.focusme.reminder.ReminderScheduler
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Surface
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.focusme.presentation.ui.theme.AppBg
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun FocusScreen(vm: FocusViewModel = viewModel(), onOpenPlanner: () -> Unit = {} ) {
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(state.alarmTrigger) {
        if (state.alarmTrigger != 0L) {
            NotificationHelper.showTimerFinished(context)
            val mp = android.media.MediaPlayer.create(context, R.raw.alarm_sound)
            mp.setOnCompletionListener { it.release() }
            mp.start()
        }
    }


    if (state.showSummary) {
        SessionSummaryScreen(
            sessionSeconds = state.sessionSeconds,
            tasks = state.tasksCount,
            xp = state.xpPoints,
            onIgnore = vm::closeSummaryAndReset,
            onSave = { title, focusRate, satisfactionRate, visibility, allowComments ->
                vm.onSaveClick(
                    context = context,
                    title = title,
                    focusRate = focusRate,
                    satisfactionRate = satisfactionRate,
                    visibility = visibility,
                    allowComments = allowComments
                )
            }
        )
        return
    }


    // 🔔 notif + 🔊 son
    LaunchedEffect(state.alarmTrigger) {
        if (state.alarmTrigger != 0L) {
            NotificationHelper.showTimerFinished(context)
            val mp = android.media.MediaPlayer.create(context, R.raw.alarm_sound)
            mp.setOnCompletionListener { it.release() }
            mp.start()
        }
    }

    val sessionActive = state.startedAtMillis != null
    val isPaused = sessionActive && !state.isRunning

    Box(modifier = Modifier.fillMaxSize().background(AppBg)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp)
                .padding(bottom = 110.dp)
        ) {

            // TOP BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_focusme_logo),
                    contentDescription = "FocusMe",
                    modifier = Modifier.size(44.dp)
                )

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "FocusMe",
                        color = TextDark,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        "Minuteur",
                        color = TextGray,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                IconButton(onClick = { ReminderScheduler.scheduleInMinutes(context, 1) }) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = "Reminder",
                        tint = PinkPrimary
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PlannerCard(onClick = onOpenPlanner)

                Spacer(Modifier.height(12.dp))

                var mode by remember { mutableStateOf(TimerMode.FOCUS) }

                ModeTabs(
                    selected = mode,
                    onSelect = { mode = it }
                )




                Spacer(Modifier.height(14.dp))


                TimerCircle(
                    remainingSeconds = state.remainingSeconds,
                    highlightRing = sessionActive,
                    isPaused = isPaused,
                    onClickSetTime = vm::openSetTimeDialog
                )

                Spacer(Modifier.height(10.dp))

                // -5/+5 seulement quand session a commencé
                if (sessionActive) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "-5 min",
                            color = PinkPrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { vm.addMinutes(-5) }
                        )
                        Text(
                            "+5 min",
                            color = PinkPrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { vm.addMinutes(+5) }
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                } else {
                    Spacer(Modifier.height(14.dp))
                }

                if (state.showQuickButtons) {
                    Text(
                        text = "Démarrage rapide",
                        color = TextGray,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        textAlign = TextAlign.Center
                    )

                    QuickStartRow(
                        on15 = { vm.setMinutesQuick(15) },
                        on25 = { vm.setMinutesQuick(25) },
                        on45 = { vm.setMinutesQuick(45) }
                    )
                } else {
                    when {
                        state.isRunning -> {
                            RunningControls(
                                onPause = vm::pauseTimer,
                                onStop = vm::askStop
                            )
                        }

                        isPaused -> {
                            PausedControls(
                                onResume = vm::resumeTimer,
                                onStop = vm::askStop
                            )
                        }

                        else -> {
                            PrimaryButton(
                                text = "▶ Démarrer",
                                onClick = vm::onStartPressed,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                    }
                }
                if (sessionActive && state.sessionTasks.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))

                    TaskInProgressCard(
                        taskTitle = state.sessionTasks.getOrNull(state.currentTaskIndex) ?: "",
                        onManage = { vm.openTasksSheet() } // ouvre le sheet
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            SoftCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { vm.openTasksSheet() }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(PinkPrimary.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "+",
                            color = PinkPrimary,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Ajouter des tâches à la session",
                            color = TextDark,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Indique ce que tu étudieras",
                            color = TextGray,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
        }
        if (state.showTasksSheet) {
            TasksSheet(
                sessionSeconds = state.remainingSeconds, // ou state.totalSeconds selon ce que tu veux afficher
                tasks = state.sessionTasks,
                taskInput = state.tempTaskText,
                onTaskInputChange = vm::updateTempTask,
                onAddTask = vm::addTempTask,
                onRemoveTask = vm::removeTask,
                onPickFromPlanner = { /* plus tard: nav vers planner */ },
                onCancel = vm::closeTasksSheet,
                onStart = {
                    vm.closeTasksSheet()
                    vm.onStartPressed()
                },
                onClose = vm::closeTasksSheet
            )
        }


        // OVERLAYS
        if (state.showSetTimeDialog) {
            SetTimeDialog(
                minutes = state.tempMinutes,
                onMinus = vm::decTempMinutes,
                onPlus = vm::incTempMinutes,
                onCancel = vm::closeSetTimeDialog,
                onConfirm = vm::confirmMinutes
            )
        }

        if (state.showStopDialog) {
            StopConfirmDialog(
                studiedMinutes = studiedMinutes(state.startedAtMillis),
                onContinue = vm::cancelStop,
                onStop = vm::confirmStop
            )
        }
    }
}


/* ---------------------------
   QUICK START
   --------------------------- */

@Composable
private fun QuickStartRow(
    on15: () -> Unit,
    on25: () -> Unit,
    on45: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickCard("15\nmin", preselected = false, modifier = Modifier.weight(1f), onClick = on15)
        QuickCard("25\nmin", preselected = true, modifier = Modifier.weight(1f), onClick = on25)
        QuickCard("45\nmin", preselected = false, modifier = Modifier.weight(1f), onClick = on45)
    }
}

@Composable
private fun QuickCard(
    label: String,
    preselected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = if (preselected) PinkPrimary else MaterialTheme.colorScheme.surface
    val textColor = if (preselected) Color.White else TextDark

    Box(
        modifier = modifier
            .height(82.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = textColor,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}

/* ---------------------------
   TIMER CIRCLE
   --------------------------- */

@Composable
private fun TimerCircle(
    remainingSeconds: Int,
    highlightRing: Boolean,
    isPaused: Boolean,
    onClickSetTime: () -> Unit
) {
    val mm = remainingSeconds / 60
    val ss = remainingSeconds % 60
    val timeText = "%02d:%02d".format(mm, ss)

    Box(
        modifier = Modifier
            .size(305.dp)
            .clip(CircleShape)
            .background(if (highlightRing) PinkPrimary else PinkPrimary.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(270.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .clickable { onClickSetTime() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    timeText,
                    style = MaterialTheme.typography.displayLarge,
                    color = if (isPaused) PinkPrimary.copy(alpha = 0.55f) else TextDark,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(8.dp))

                if (isPaused) {
                    Text("⏸ PAUSE", color = PinkPrimary, fontWeight = FontWeight.Bold)
                } else {
                    Text(
                        "Appuie pour définir l'heure",
                        color = TextGray,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

/* ---------------------------
   CONTROLS
   --------------------------- */

@Composable
private fun RunningControls(
    onPause: () -> Unit,
    onStop: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PrimaryButton(text = "⏸ Pause", onClick = onPause, modifier = Modifier.weight(1f))
        DangerButton(text = "⏹ Arrêter", onClick = onStop, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PausedControls(
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PrimaryButton(text = "▶ Reprendre", onClick = onResume, modifier = Modifier.weight(1f))
        DangerButton(text = "⏹ Arrêter", onClick = onStop, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun DangerButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE53935))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

/* ---------------------------
   STOP DIALOG helpers
   --------------------------- */

private fun studiedMinutes(startedAtMillis: Long?): Int {
    if (startedAtMillis == null) return 0
    val diff = System.currentTimeMillis() - startedAtMillis
    return (diff / 60000L).toInt().coerceAtLeast(0)
}
@Composable
private fun StopConfirmDialog(
    studiedMinutes: Int,
    onContinue: () -> Unit,
    onStop: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFEBEE)),
                contentAlignment = Alignment.Center
            ) {
                Text("⏹", color = Color(0xFFE53935), fontWeight = FontWeight.Black)
            }

            Spacer(Modifier.height(10.dp))

            Text("Arrêter la session ?", color = TextDark, fontWeight = FontWeight.ExtraBold)

            Spacer(Modifier.height(6.dp))

            Text(
                "Tu étudies depuis $studiedMinutes min",
                color = PinkPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            Text("Tes progrès seront enregistrés", color = TextGray)

            Spacer(Modifier.height(14.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlineButton("Continuer", onContinue, modifier = Modifier.weight(1f))
                DangerButton("Arrêter", onStop, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun OutlineButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = PinkPrimary, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PlannerCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val grad = Brush.horizontalGradient(
        colors = listOf(
            PinkPrimary,
            PinkPrimary.copy(alpha = 0.75f),
            Color(0xFFFFB3D1)
        )
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(92.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable { onClick() },
        color = Color.Transparent,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(grad)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Calendar",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Planifie ta journée\nd'étude",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Crée un planning de tâches",
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Go",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

enum class TimerMode { FOCUS, STOPWATCH, POMODORO }


@Composable
fun ModeTabs(
    selected: TimerMode,
    onSelect: (TimerMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        color = Color.White,
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabPill(
                text = "Focus",
                icon = Icons.Outlined.AccessTime,
                selected = selected == TimerMode.FOCUS,
                onClick = { onSelect(TimerMode.FOCUS) },
                modifier = Modifier.weight(0.9f)
            )

            TabPill(
                text = "Stopwatch",
                icon = Icons.Outlined.Timer,
                selected = selected == TimerMode.STOPWATCH,
                onClick = { onSelect(TimerMode.STOPWATCH) },
                modifier = Modifier.weight(1.35f)
            )

            TabPill(
                text = "Pomodoro",
                icon = Icons.Outlined.Coffee,
                selected = selected == TimerMode.POMODORO,
                onClick = { onSelect(TimerMode.POMODORO) },
                modifier = Modifier.weight(1.25f)
            )
        }
    }
}


@Composable
private fun TabPill(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) PinkPrimary else Color.Transparent
    val content = if (selected) Color.White else Color(0xFF6B7280)

    Row(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 10.dp),   // important
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = content,
            modifier = Modifier.size(19.dp)
        )
        Spacer(Modifier.width(6.dp))

        Text(
            text = text,
            color = content,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,      // Pixel 5 OK
            maxLines = 1,
            softWrap = false
        )
    }
}
@Composable
fun TaskInProgressCard(
    taskTitle: String,
    onManage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(PinkPrimary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📝", fontSize = 18.sp)
                }

                Spacer(Modifier.width(12.dp))

                Text(
                    text = "Tâche en cours",
                    color = TextDark,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("•", color = PinkPrimary, fontSize = 26.sp, lineHeight = 0.sp)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = taskTitle,
                    color = TextDark,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = onManage,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = TextDark
                )
            ) {
                Text("Gérer les tâches", fontWeight = FontWeight.Bold)
            }
        }
    }
}



