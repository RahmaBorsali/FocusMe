package com.example.focusme.presentation.screen.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.focusme.presentation.ui.theme.PinkPrimary
import com.example.focusme.presentation.ui.theme.TextDark
import com.example.focusme.presentation.ui.theme.TextGray

@Composable
fun SessionSummaryScreen(
    sessionSeconds: Int,
    tasks: Int,
    xp: Int,
    onIgnore: () -> Unit,
    onSave: (title: String, focusRate: Int, satisfactionRate: Int, visibility: String, allowComments: Boolean) -> Unit
) {
    val mm = sessionSeconds / 60
    val ss = sessionSeconds % 60
    val timeText = "%d:%02d".format(mm, ss)

    var title by remember { mutableStateOf("Étude") }
    var focusStars by remember { mutableStateOf(0) }
    var satisfactionStars by remember { mutableStateOf(0) }
    var friends by remember { mutableStateOf(true) }
    var comments by remember { mutableStateOf(true) }

    // ✅ dialogs
    var showMissingEvalDialog by remember { mutableStateOf(false) } // cap2
    var showIgnoreConfirmDialog by remember { mutableStateOf(false) } // cap3

    val canSave = focusStars > 0 && satisfactionStars > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBEAF2))
    ) {

        // ✅ contenu scrollable + IMPORTANT padding bottom
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(18.dp)
                .padding(bottom = 110.dp) // ✅ pour ne pas cacher la fin par la barre en bas
        ) {

            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PinkPrimary.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) { Text("🏆") }

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        "Félicitations !",
                        color = TextDark,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text("Vous avez terminé une session d'étude", color = TextGray)
                }
            }

            Spacer(Modifier.height(14.dp))

            // Stats card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(PinkPrimary.copy(alpha = 0.14f))
                    .padding(14.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatCell("⏱️", "Temps de\nsession", timeText)
                    StatCell("✍️", "Tâches", tasks.toString())
                    StatCell("⭐", "Points XP", xp.toString())
                }
            }

            Spacer(Modifier.height(12.dp))

            // Quote
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.7f))
                    .padding(14.dp)
            ) {
                Text(
                    "💡 \"Chaque session écrit une ligne de plus à ton histoire de réussite.\"",
                    color = PinkPrimary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(12.dp))

            // Title + ratings
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .padding(14.dp)
            ) {
                Column {
                    Text("✏️  Titre de la session", color = TextGray, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ex: Courte session") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PinkPrimary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color(0xFFF3F3F3),
                            unfocusedContainerColor = Color(0xFFF3F3F3),
                            cursorColor = PinkPrimary
                        ),
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            color = TextDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Spacer(Modifier.height(14.dp))

                    Text("Évalue ta session", color = TextGray, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))

                    RatingRow("🎯", "Niveau de focus", focusStars) { focusStars = it }
                    Spacer(Modifier.height(10.dp))
                    RatingRow("😊", "Satisfaction", satisfactionStars) { satisfactionStars = it }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Visibility
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .padding(14.dp)
            ) {
                Column {
                    Text("👁️  Visibilité de la session", color = TextGray, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PillButton(
                            text = "👥  Amis",
                            selected = friends,
                            onClick = { friends = true },
                            modifier = Modifier.weight(1f)
                        )
                        PillButton(
                            text = "🔒  Moi uniquement",
                            selected = !friends,
                            onClick = { friends = false },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "💬  Autoriser les commentaires",
                            color = TextDark,
                            modifier = Modifier.weight(1f)
                        )
                        ToggleChip(on = comments) { comments = it }
                    }

                    Spacer(Modifier.height(14.dp))

                    Box(                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(PinkPrimary)
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✨  Montrez vos réussites", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ✅ BARRE FIXE EN BAS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFBEAF2))
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .clickable { showIgnoreConfirmDialog = true }, // ✅ cap3
                contentAlignment = Alignment.Center
            ) {
                Text("Ignorer", color = TextDark, fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (canSave) PinkPrimary else Color(0xFF6E6E6E))
                    .clickable {
                        if (!canSave) {
                            showMissingEvalDialog = true
                        } else {
                            val visibility = if (friends) "friends" else "private"
                            onSave(title, focusStars, satisfactionStars, visibility, comments)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Enregistrer la session", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }

    // ✅ CAP 2 : popup si pas d’évaluation
    if (showMissingEvalDialog) {
        AlertDialog(
            onDismissRequest = { showMissingEvalDialog = false },
            confirmButton = {
                Text(
                    "OK",
                    modifier = Modifier
                        .padding(12.dp)
                        .clickable { showMissingEvalDialog = false },
                    color = PinkPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            title = { Text("Évalue ta session") },
            text = {
                Text(
                    "Pour enregistrer la session, tu dois évaluer :\n" +
                            "• Niveau de concentration (⭐)\n" +
                            "• Satisfaction (😊)"
                )
            }
        )
    }

    // ✅ CAP 3 : confirmation ignorer
    if (showIgnoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showIgnoreConfirmDialog = false },
            dismissButton = {
                Text(
                    "Retour",
                    modifier = Modifier
                        .padding(12.dp)
                        .clickable { showIgnoreConfirmDialog = false },
                    color = PinkPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            confirmButton = {
                Text(
                    "Ignorer",
                    modifier = Modifier
                        .padding(12.dp)
                        .clickable {
                            showIgnoreConfirmDialog = false
                            onIgnore()
                        },
                    color = Color(0xFFD07A00),
                    fontWeight = FontWeight.Bold
                )
            },
            title = { Text("Voulez-vous vraiment supprimer cette session ?") },
            text = { Text("Votre temps d'étude ne sera pas enregistré et cette action est irréversible.") }
        )
    }
}

/* ---------------------------
   Helpers
   --------------------------- */

@Composable
private fun StatCell(icon: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon)
        Spacer(Modifier.height(6.dp))
        Text(label, color = TextGray, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(value, color = TextDark, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun RatingRow(icon: String, label: String, stars: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(icon)
        Spacer(Modifier.width(8.dp))
        Text(label, color = PinkPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (1..5).forEach { i ->
                Text(
                    text = if (i <= stars) "★" else "☆",
                    color = if (i <= stars) PinkPrimary else Color(0xFFBDBDBD),
                    modifier = Modifier.clickable { onChange(i) }
                )
            }
        }
    }
}

@Composable
private fun PillButton(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) PinkPrimary else Color(0xFFF3F3F3))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (selected) Color.White else TextDark, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ToggleChip(on: Boolean, onToggle: (Boolean) -> Unit) {
    Box(
        modifier = Modifier
            .width(54.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (on) PinkPrimary else Color(0xFFDADADA))
            .clickable { onToggle(!on) }
            .padding(4.dp),
        contentAlignment = if (on) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}
