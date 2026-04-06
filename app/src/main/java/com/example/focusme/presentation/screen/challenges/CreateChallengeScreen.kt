package com.example.focusme.presentation.screen.challenges

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusme.data.repository.ChallengeVisibility
import com.example.focusme.data.repository.GoalType
import com.example.focusme.presentation.ui.theme.PinkPrimary
import com.example.focusme.presentation.ui.theme.TextGray
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChallengeScreen(
    onBack: () -> Unit,
    onCreated: (String) -> Unit,
    vm: CreateChallengeViewModel = viewModel()
) {
    val ui by vm.uiState.collectAsState()
    var pickingStart by remember { mutableStateOf(false) }
    var pickingEnd by remember { mutableStateOf(false) }

    ChallengeScreenContainer(
        title = "Nouveau defi",
        subtitle = "Un bon challenge doit etre simple a comprendre, ambitieux et motivant a partager.",
        actions = { OutlinedButton(onClick = onBack) { Text("Retour") } }
    ) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxSize()) {
            item { ChallengeFormTextField("Titre", ui.title, vm::updateTitle, "Ex: Sprint de revision pour les examens", ui.fieldErrors["title"]) }
            item { ChallengeFormTextField("Description", ui.description, vm::updateDescription, "Explique en une phrase ce qui rend ce defi motivant.", minLines = 4) }
            item {
                DateRow(
                    startDate = ui.startDate,
                    endDate = ui.endDate,
                    startError = ui.fieldErrors["startDate"],
                    endError = ui.fieldErrors["endDate"],
                    onPickStart = { pickingStart = true },
                    onPickEnd = { pickingEnd = true }
                )
            }
            item {
                SelectionCard(
                    title = "Visibilite",
                    subtitle = "Choisis qui peut voir ou rejoindre ce challenge.",
                    options = ChallengeVisibility.entries.map { it.label },
                    selected = ui.visibility.label,
                    onSelect = { label -> vm.updateVisibility(ChallengeVisibility.entries.first { it.label == label }) }
                )
            }
            item {
                SelectionCard(
                    title = "Type d'objectif",
                    subtitle = "Le leaderboard suivra cet objectif comme score principal.",
                    options = listOf("Minutes de focus", "Nombre de sessions", "Taches completees"),
                    selected = ui.goalType.toDisplayLabel(),
                    onSelect = { selected ->
                        vm.updateGoalType(
                            when (selected) {
                                "Nombre de sessions" -> GoalType.SESSIONS_COUNT
                                "Taches completees" -> GoalType.TASKS_COMPLETED
                                else -> GoalType.FOCUS_MINUTES
                            }
                        )
                    }
                )
            }
            item { ChallengeFormTextField("Objectif cible", ui.targetValue, vm::updateTargetValue, ui.goalType.targetHint(), ui.fieldErrors["targetValue"]) }
            item { ChallengeFormTextField("Participants max", ui.maxParticipants, vm::updateMaxParticipants, "Laisse vide pour ne pas limiter", ui.fieldErrors["maxParticipants"]) }
            item { AutoGeneratedCodeCard(visibility = ui.visibility) }
            if (ui.submitError != null) {
                item { ErrorCard(message = ui.submitError!!) }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Annuler") }
                    PrimaryChallengeButton(
                        text = if (ui.isSubmitting) "Creation..." else "Creer le challenge",
                        modifier = Modifier.weight(1f),
                        enabled = !ui.isSubmitting,
                        onClick = { vm.submit(onCreated) }
                    )
                }
            }
        }
    }

    if (pickingStart) {
        ChallengeDatePickerDialog(ui.startDate, onDismiss = { pickingStart = false }) {
            vm.updateStartDate(it)
            pickingStart = false
        }
    }
    if (pickingEnd) {
        ChallengeDatePickerDialog(ui.endDate, onDismiss = { pickingEnd = false }) {
            vm.updateEndDate(it)
            pickingEnd = false
        }
    }
}

@Composable
private fun AutoGeneratedCodeCard(
    visibility: ChallengeVisibility
) {
    Column {
        Text("Code du challenge", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
        Spacer(Modifier.height(4.dp))
        Text(
            if (visibility == ChallengeVisibility.PRIVATE) {
                "Pour un challenge prive, le backend genere automatiquement un code unique apres creation. Tu le retrouveras dans le detail du challenge."
            } else {
                "Chaque challenge recoit un code unique auto-genere. Tu pourras le copier et le partager depuis le detail du challenge."
            },
            color = TextGray,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(PinkPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Key, contentDescription = null, tint = PinkPrimary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Code auto-genere", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text("Aucune saisie manuelle n'est necessaire.", color = TextGray, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ChallengeFormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    error: String? = null,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = minLines == 1,
        minLines = minLines,
        supportingText = {
            Text(
                text = error ?: hint,
                color = if (error != null) MaterialTheme.colorScheme.error else TextGray
            )
        }
    )
}

@Composable
private fun SelectionCard(
    title: String,
    subtitle: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, color = TextGray, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(10.dp))
        ChallengeSegmentedTabs(selected = selected, options = options, onSelect = onSelect)
    }
}

@Composable
private fun DateRow(
    startDate: LocalDate,
    endDate: LocalDate,
    startError: String?,
    endError: String?,
    onPickStart: () -> Unit,
    onPickEnd: () -> Unit
) {
    Column {
        Text("Periode", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            DateField("Debut", startDate, startError, onPickStart, Modifier.weight(1f))
            DateField("Fin", endDate, endError, onPickEnd, Modifier.weight(1f))
        }
    }
}

@Composable
private fun DateField(
    label: String,
    value: LocalDate,
    error: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
            onValueChange = {},
            modifier = Modifier.fillMaxWidth().clickable { onClick() },
            readOnly = true,
            label = { Text(label) },
            leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) }
        )
        if (error != null) {
            Spacer(Modifier.height(4.dp))
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChallengeDatePickerDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val initialMillis = initialDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            PrimaryChallengeButton(text = "Valider") {
                val selectedMillis = datePickerState.selectedDateMillis ?: initialMillis
                onConfirm(Instant.ofEpochMilli(selectedMillis).atZone(ZoneOffset.UTC).toLocalDate())
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Annuler") } }
    ) {
        DatePicker(state = datePickerState)
    }
}

private fun GoalType.toDisplayLabel(): String = when (this) {
    GoalType.FOCUS_MINUTES -> "Minutes de focus"
    GoalType.SESSIONS_COUNT -> "Nombre de sessions"
    GoalType.TASKS_COMPLETED -> "Taches completees"
}

private fun GoalType.targetHint(): String = when (this) {
    GoalType.FOCUS_MINUTES -> "Ex: 300 minutes en 7 jours"
    GoalType.SESSIONS_COUNT -> "Ex: 12 sessions de travail"
    GoalType.TASKS_COMPLETED -> "Ex: 20 taches terminees"
}
