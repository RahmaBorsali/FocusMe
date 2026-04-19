package com.example.focusme.presentation.screen.planner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.datetime.LocalDate
import android.app.Application
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.unit.sp

@Composable
fun AddTaskScreen(
    date: LocalDate,
    taskId: Long?,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    val app = LocalContext.current.applicationContext as Application
    val vm: AddTaskViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(app)
    )

    val ui by vm.ui.collectAsState()

    val dateKey = remember(date) { "%04d-%02d-%02d".format(date.year, date.monthNumber, date.dayOfMonth) }

    LaunchedEffect(taskId, dateKey) {
        vm.initScreen(dateKey = dateKey, taskId = taskId)
    }
    val enabled = ui.canSave && !ui.isSaving && !ui.isLoading


    val pageBg = Color(0xFFF6E9EF)
    val cardBg = Color.White
    val textDark = Color(0xFF1C1C1C)
    val textGray = Color(0xFF8F8F8F)
    val pink = Color(0xFFE84A8A)
    val chipUnselectedBorder = Color(0xFFE7E7E7)
    val prioritySelectedBg = Color(0xFFF1E2B9)
    val bottomBtnDisabled = Color(0xFFBDBDBD)


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp)
                .padding(bottom = 96.dp)
        ) {
            Spacer(Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textDark)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (taskId == null) "Nouvelle tâche" else "Modifier tâche",
                        color = textDark,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "${dowShortFr(date)}, ${date.dayOfMonth} ${monthShortFr(date.monthNumber)}.",
                        color = textGray,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            if (ui.error != null) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    color = Color(0xFFFFF3F3),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFFEB2B2)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", fontSize = 20.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = ui.error!!,
                            color = Color(0xFFC53030),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            CardLike(cardBg) {
                SectionTitle(icon = "✏️", title = "Nom de la tâche", textDark = textDark)
                Spacer(Modifier.height(10.dp))

                RoundedField(
                    value = ui.title,
                    onValueChange = vm::onTitleChange,
                    placeholder = "ex. Résolution de problèmes de maths",
                    singleLine = true,
                    pink = pink,
                    textDark = textDark
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text("${ui.title.length}/100", color = textGray, style = MaterialTheme.typography.labelMedium)
                }

                Spacer(Modifier.height(14.dp))

                SectionTitle(icon = "📝", title = "Description", textDark = textDark)
                Spacer(Modifier.height(10.dp))

                RoundedField(
                    value = ui.description,
                    onValueChange = vm::onDescriptionChange,
                    placeholder = "Ajoutez une description (détails, chapitre, objectifs...)",
                    singleLine = false,
                    maxLines = 4,
                    minHeight = 120.dp,
                    pink = pink,
                    textDark = textDark
                )
            }

            Spacer(Modifier.height(12.dp))

            CardLike(cardBg) {
                SectionTitle(icon = "⏱️", title = "Temps de travail", textDark = textDark)
                Spacer(Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf(15, 25, 30).forEach { m ->
                        TimeChip(
                            modifier = Modifier.weight(1f),
                            minutes = m,
                            selected = ui.minutes == m,
                            onClick = { vm.onMinutesChange(m) },
                            selectedBg = pink,
                            unselectedBorder = chipUnselectedBorder,
                            textDark = textDark
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf(45, 60, 90).forEach { m ->
                        TimeChip(
                            modifier = Modifier.weight(1f),
                            minutes = m,
                            selected = ui.minutes == m,
                            onClick = { vm.onMinutesChange(m) },
                            selectedBg = pink,
                            unselectedBorder = chipUnselectedBorder,
                            textDark = textDark
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StepButton(
                        label = "−",
                        onClick = { vm.onMinutesChange(ui.minutes - 5) },
                        border = chipUnselectedBorder
                    )

                    Surface(
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF7F7F7),
                        border = BorderStroke(1.dp, chipUnselectedBorder)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${ui.minutes} min",
                                fontWeight = FontWeight.ExtraBold,
                                color = textDark
                            )
                        }
                    }

                    StepButton(
                        label = "+",
                        onClick = { vm.onMinutesChange(ui.minutes + 5) },
                        border = chipUnselectedBorder
                    )
                }

            }

            Spacer(Modifier.height(12.dp))
            CardLike(cardBg) {
                SectionTitle(icon = "🕒", title = "Heure de la tâche", textDark = textDark)
                Spacer(Modifier.height(10.dp))

                val hour = ui.startTimeMinutes / 60
                val minute = ui.startTimeMinutes % 60
                val label = "%02d:%02d".format(hour, minute)

                var openPicker by remember { mutableStateOf(false) }

                Surface(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF7F7F7),
                    border = BorderStroke(1.dp, chipUnselectedBorder)
                ) {
                    Row(
                        Modifier.fillMaxSize().clickable { openPicker = true }.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, fontWeight = FontWeight.ExtraBold, color = textDark)
                        Text("Modifier", color = pink, fontWeight = FontWeight.Bold)
                    }
                }

                if (openPicker) {
                    TimePickerDialog(
                        initialHour = hour,
                        initialMinute = minute,
                        onDismiss = { openPicker = false },
                        onConfirm = { h, m ->
                            vm.onStartTimeChange(h * 60 + m)
                            openPicker = false
                        }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))


            CardLike(cardBg) {
                SectionTitle(icon = "🎯", title = "Priorité", textDark = textDark)
                Spacer(Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    PriorityButton(
                        modifier = Modifier.weight(1f),
                        emoji = "😌",
                        label = "Faible",
                        selected = ui.priority == PriorityUi.LOW,
                        onClick = { vm.onPriorityChange(PriorityUi.LOW) },
                        selectedBg = prioritySelectedBg,
                        border = chipUnselectedBorder,
                        textDark = textDark
                    )
                    PriorityButton(
                        modifier = Modifier.weight(1f),
                        emoji = "🎯",
                        label = "Moyenne",
                        selected = ui.priority == PriorityUi.MEDIUM,
                        onClick = { vm.onPriorityChange(PriorityUi.MEDIUM) },
                        selectedBg = prioritySelectedBg,
                        border = chipUnselectedBorder,
                        textDark = textDark
                    )
                    PriorityButton(
                        modifier = Modifier.weight(1f),
                        emoji = "🔥",
                        label = "Élevée",
                        selected = ui.priority == PriorityUi.HIGH,
                        onClick = { vm.onPriorityChange(PriorityUi.HIGH) },
                        selectedBg = prioritySelectedBg,
                        border = chipUnselectedBorder,
                        textDark = textDark
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            CardLike(cardBg) {
                SectionTitle(icon = "📚", title = "Matière", textDark = textDark)
                Spacer(Modifier.height(10.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                ) {
                    items(ui.subjects, key = { it.id }) { s ->
                        val isSelected = ui.selectedSubjectId == s.id
                        SubjectTile(
                            subject = s,
                            selected = isSelected,
                            onToggle = { vm.onSelectSubject(s.id) },
                            onDelete = { vm.askDeleteSubject(s.id) },
                            border = chipUnselectedBorder,
                            textDark = textDark
                        )
                    }
                    item {
                        AddSubjectTile(
                            onClick = { vm.openAddSubject() },
                            pink = pink
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            color = Color.Transparent
        ) {
            Box(modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
                Button(
                    onClick = { vm.save { onDone() } },
                    enabled = enabled,
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (enabled) pink else bottomBtnDisabled,
                        disabledContainerColor = bottomBtnDisabled
                    ),
                    modifier = Modifier.fillMaxWidth().height(64.dp)
                ) {
                    Text(
                        text = if (taskId == null) "✓  Ajouter une tâche" else "✓  Modifier la tâche",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }

        if (ui.showDeleteDialog && ui.deleteTargetId != null) {
            val target = ui.subjects.firstOrNull { it.id == ui.deleteTargetId }
            SweetDeleteDialog(
                subjectName = target?.label ?: "",
                onDismiss = { vm.dismissDelete() },
                onConfirm = { vm.confirmDelete() }
            )
        }

        if (ui.showAddSubjectDialog) {
            AddSubjectTopDialog(
                onDismiss = { vm.closeAddSubject() },
                onCreate = { newSub ->
                    vm.createSubject(
                        label = newSub.label,
                        emoji = newSub.emoji,
                        colorArgb = newSub.colorArgb
                    )
                },
                pink = pink
            )
        }
    }
}

@Composable
private fun CardLike(
    bg: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = bg,
        shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp), content = content)
    }
}

@Composable
private fun SectionTitle(icon: String, title: String, textDark: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(icon)
        Spacer(Modifier.width(10.dp))
        Text(
            text = title,
            color = textDark,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
private fun RoundedField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean,
    maxLines: Int = 1,
    minHeight: Dp = 56.dp,
    pink: Color,
    textDark: Color
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().heightIn(min = minHeight),
        placeholder = { Text(placeholder) },
        singleLine = singleLine,
        maxLines = maxLines,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = pink,
            unfocusedBorderColor = Color(0xFFE6E6E6),
            focusedTextColor = textDark,
            unfocusedTextColor = textDark,
            focusedContainerColor = Color(0xFFF7F7F7),
            unfocusedContainerColor = Color(0xFFF7F7F7),
            cursorColor = pink
        )
    )
}

@Composable
private fun TimeChip(
    modifier: Modifier = Modifier,
    minutes: Int,
    selected: Boolean,
    onClick: () -> Unit,
    selectedBg: Color,
    unselectedBorder: Color,
    textDark: Color
) {
    val bg = if (selected) selectedBg else Color.White
    val border = if (selected) selectedBg else unselectedBorder
    val txt = if (selected) Color.White else textDark

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text("${minutes}min", color = txt, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun PriorityButton(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    selectedBg: Color,
    border: Color,
    textDark: Color
) {
    Box(
        modifier = modifier
            .height(60.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) selectedBg else Color.White)
            .border(1.dp, border, RoundedCornerShape(18.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji)
            Text(
                text = label,
                color = if (selected) Color(0xFFB67A00) else textDark,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun SubjectTile(
    subject: SubjectUi,
    selected: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    border: Color,
    textDark: Color
) {
    val subjectColor = Color(subject.colorArgb.toInt())
    val bg = if (selected) subjectColor.copy(alpha = 0.18f) else Color.White
    val labelColor = if (selected) subjectColor else textDark

    Box(
        modifier = Modifier
            .height(84.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(18.dp))
            .clickable { onToggle() }
            .padding(10.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(22.dp)
                .clip(CircleShape)
                .background(Color(0xFFBDBDBD))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDelete() },
            contentAlignment = Alignment.Center
        ) {
            Text("×", color = Color.White, fontWeight = FontWeight.Black)
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(subject.emoji, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Text(
                text = subject.label,
                color = labelColor,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun AddSubjectTile(onClick: () -> Unit, pink: Color) {
    Box(
        modifier = Modifier
            .height(84.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFF8ECF2))
            .border(2.dp, Color(0xFFEDB0C7), RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("+", color = pink, fontWeight = FontWeight.Black, style = MaterialTheme.typography.headlineMedium)
            Text("Ajouter", color = pink, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun SweetDeleteDialog(
    subjectName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text("Supprimer matière ?", fontWeight = FontWeight.ExtraBold) },
        text = { Text("Voulez-vous vraiment supprimer \"$subjectName\" ?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Supprimer", color = Color(0xFFE95B5B), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddSubjectTopDialog(
    onDismiss: () -> Unit,
    onCreate: (SubjectUi) -> Unit,
    pink: Color
) {
    val border = Color(0xFFE7E7E7)
    val textDark = Color(0xFF1C1C1C)
    val textGray = Color(0xFF8F8F8F)

    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color(0xFF6C7CFF)) }
    var selectedEmoji by remember { mutableStateOf("🏆") }

    var customEmojiMode by remember { mutableStateOf(false) }
    var customEmojiText by remember { mutableStateOf("") }

    val canAdd = name.trim().isNotEmpty() && selectedEmoji.isNotBlank()

    val colors = remember {
        listOf(
            Color(0xFFE95B5B), Color(0xFFF18A2A), Color(0xFFF2B445), Color(0xFFE8C34A),
            Color(0xFF9AD14B), Color(0xFF52C16B), Color(0xFF39B7A6),
            Color(0xFF4AB7A4), Color(0xFF49B3D2), Color(0xFF4C9BEA), Color(0xFF4F7AF5),
            Color(0xFF6C7CFF), Color(0xFF8B6CFF), Color(0xFFA45CFF),
            Color(0xFFB85CFF), Color(0xFFDB5CA8), Color(0xFFE95B5B),
            Color(0xFF6C7A89), Color(0xFF59626D), Color(0xFF2F3640)
        )
    }

    val emojis = remember {
        listOf(
            "🔢","📝","🗣️","🗺️","🌱","🧠","📊",
            "💼","📄","🎯","🏆","💰","🛠️",
            "🔍","⚖️","🏛️","🔧","🤖","📚","🎨","🎵"
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .padding(top = 42.dp, start = 14.dp, end = 14.dp, bottom = 14.dp)
        ) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(22.dp),
                shadowElevation = 10.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.92f)
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp)
                ) {
                    val sectionGap = 14.dp
                    val smallGap = 10.dp

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Nouvelle matière",
                            fontWeight = FontWeight.ExtraBold,
                            color = textDark,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            "✕",
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { onDismiss() }
                                .padding(10.dp),
                            color = textDark,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(sectionGap))

                    Text("Aperçu", fontWeight = FontWeight.ExtraBold, color = textDark)
                    Spacer(Modifier.height(smallGap))

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = selectedColor.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(selectedEmoji, style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                name.ifBlank { "Nom de la matière" },
                                color = selectedColor,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(Modifier.height(sectionGap))

                    Text("Nom de la matière", fontWeight = FontWeight.ExtraBold, color = textDark)
                    Spacer(Modifier.height(smallGap))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { if (it.length <= 20) name = it },
                        placeholder = { Text("Ex. Géographie") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = pink,
                            unfocusedBorderColor = border,
                            focusedContainerColor = Color(0xFFF7F7F7),
                            unfocusedContainerColor = Color(0xFFF7F7F7),
                            cursorColor = pink
                        )
                    )

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Text("${name.length}/20", color = textGray)
                    }

                    Spacer(Modifier.height(sectionGap))

                    Text("Choisir une couleur (20)", fontWeight = FontWeight.ExtraBold, color = textDark)
                    Spacer(Modifier.height(smallGap))

                    FlowRow(
                        maxItemsInEachRow = 7,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        colors.forEach { c ->
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .clickable { selectedColor = c },
                                contentAlignment = Alignment.Center
                            ) {
                                if (c == selectedColor) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.25f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("✓", color = Color.White, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(sectionGap))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Choisir un emoji", fontWeight = FontWeight.ExtraBold, color = textDark)
                        Spacer(Modifier.weight(1f))
                        OutlinedButton(
                            onClick = { customEmojiMode = !customEmojiMode },
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(2.dp, pink),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = pink),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                if (customEmojiMode) "Choisir dans la liste" else "Ajouter perso",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(smallGap))

                    if (!customEmojiMode) {
                        FlowRow(
                            maxItemsInEachRow = 7,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            emojis.forEach { e ->
                                val isSel = e == selectedEmoji
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSel) selectedColor.copy(alpha = 0.18f) else Color.Transparent)
                                        .border(
                                            width = if (isSel) 2.dp else 1.dp,
                                            color = if (isSel) pink else border,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { selectedEmoji = e },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(e)
                                }
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF7F7F7),
                            border = BorderStroke(1.dp, border),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    "Saisir un emoji perso — Sélectionné : $selectedEmoji",
                                    fontWeight = FontWeight.SemiBold,
                                    color = textDark
                                )
                                Spacer(Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = customEmojiText,
                                    onValueChange = {
                                        customEmojiText = it
                                        val x = it.trim()
                                        if (x.isNotBlank()) selectedEmoji = x.takeLast(2).trim()
                                    },
                                    placeholder = { Text("Saisir un emoji (ex. 🎵)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(2.dp, pink),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = pink),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Text("Annuler", fontWeight = FontWeight.ExtraBold)
                        }

                        Button(
                            onClick = {
                                val id = System.currentTimeMillis()
                                onCreate(
                                    SubjectUi(
                                        id = id,
                                        label = name.trim(),
                                        emoji = selectedEmoji,
                                        colorArgb = selectedColor.toArgb().toLong()
                                    )
                                )
                            },
                            enabled = canAdd,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canAdd) pink else Color(0xFFBDBDBD),
                                disabledContainerColor = Color(0xFFBDBDBD)
                            ),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Text("Ajouter matière", fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}
@Composable
private fun StepButton(label: String, onClick: () -> Unit, border: Color) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(Color(0xFFF3F3F3))
            .border(1.dp, border, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge)
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        },
        title = { Text("Choisir l'heure") },
        text = { TimePicker(state = state) }
    )
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
