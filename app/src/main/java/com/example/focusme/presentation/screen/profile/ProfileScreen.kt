package com.example.focusme.presentation.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusme.presentation.ui.components.PrimaryButton
import com.example.focusme.presentation.ui.theme.AppBg
import com.example.focusme.presentation.ui.theme.BorderSoft
import com.example.focusme.presentation.ui.theme.PinkPrimary
import com.example.focusme.presentation.ui.theme.TextDark
import com.example.focusme.presentation.ui.theme.TextGray
import java.util.Locale

private data class ProfileInfoDialog(
    val title: String,
    val message: String
)

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onOpenLogin: () -> Unit,
    onOpenSignup: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenAchievements: () -> Unit,
    onOpenSettings: () -> Unit,
    vm: ProfileViewModel = viewModel()
) {
    val ui by vm.uiState.collectAsState()
    val levelInfo = remember(ui.totalXp) { profileLevelInfo(ui.totalXp) }
    val weeklySummary = remember(ui.sessions) { computeWeeklySummary(ui.sessions) }

    var showEditDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var infoDialog by remember { mutableStateOf<ProfileInfoDialog?>(null) }

    var displayNameDraft by rememberSaveable(ui.displayName) { mutableStateOf(ui.displayName) }
    var studyGoalDraft by rememberSaveable(ui.studyGoal) { mutableStateOf(ui.studyGoal) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeroProfileCard(
                ui = ui,
                levelInfo = levelInfo,
                onEdit = { showEditDialog = true },
                onOpenLogin = onOpenLogin,
                onOpenSignup = onOpenSignup
            )
        }

        if (ui.message != null) {
            item {
                StatusBanner(
                    text = ui.message.orEmpty(),
                    tint = Color(0xFF2E7D32),
                    background = Color(0xFFE8F5E9),
                    onDismiss = vm::clearMessage
                )
            }
        }

        if (ui.error != null) {
            item {
                StatusBanner(
                    text = ui.error.orEmpty(),
                    tint = Color(0xFFD32F2F),
                    background = Color(0xFFFFEBEE),
                    onDismiss = vm::clearError
                )
            }
        }

        item {
            WeeklyOverviewCard(
                summary = weeklySummary,
                onOpenHistory = onOpenHistory
            )
        }

        item { SectionTitle("Actions") }

        item {
            MenuCard(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                iconTint = Color(0xFFEC5A9A),
                title = "Historique des sessions",
                subtitle = "Parcourir tes sessions jour apres jour",
                onClick = onOpenHistory
            )
        }

        item {
            MenuCard(
                icon = Icons.Default.AutoGraph,
                iconTint = Color(0xFF6C8EF5),
                title = "Statistiques avancees",
                subtitle = "Tendances, heatmap et analyses",
                onClick = onOpenStats
            )
        }

        item {
            MenuCard(
                icon = Icons.Default.EmojiEvents,
                iconTint = Color(0xFFF3A638),
                title = "Succes",
                subtitle = "Niveaux, progression et jalons atteints",
                onClick = onOpenAchievements
            )
        }

        item { SectionTitle("Reglages") }

        item {
            MenuCard(
                icon = Icons.Default.Settings,
                iconTint = Color(0xFF9C6ADE),
                title = "Reglages de l'app",
                subtitle = "Notifications, son et visibilite",
                onClick = onOpenSettings
            )
        }

        item {
            MenuCard(
                icon = Icons.Default.Language,
                iconTint = Color(0xFF5D9BFF),
                title = "Langue de l'app",
                subtitle = "Francais par defaut pour le moment",
                onClick = {
                    infoDialog = ProfileInfoDialog(
                        title = "Langue de l'app",
                        message = "Le profil est deja configure en francais. Le changement de langue complet sera ajoute dans une prochaine iteration."
                    )
                }
            )
        }

        item {
            MenuCard(
                icon = Icons.Default.School,
                iconTint = Color(0xFF49B96E),
                title = "Tutoriel de l'app",
                subtitle = "Retrouver les bases pour bien demarrer",
                onClick = {
                    infoDialog = ProfileInfoDialog(
                        title = "Tutoriel",
                        message = "Commence par regler un temps dans le minuteur, ajoute tes taches, puis enregistre tes sessions pour faire grimper tes stats."
                    )
                }
            )
        }

        item {
            MenuCard(
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                iconTint = Color(0xFF5D9BFF),
                title = "Aide",
                subtitle = "Support et questions frequentes",
                onClick = {
                    infoDialog = ProfileInfoDialog(
                        title = "Aide",
                        message = "Si quelque chose bloque, verifie d'abord ta connexion et les permissions de notifications. On peut aussi enrichir cette section avec une vraie FAQ ensuite."
                    )
                }
            )
        }

        item {
            MenuCard(
                icon = Icons.Default.TipsAndUpdates,
                iconTint = Color(0xFFFFB300),
                title = "Suggerer une fonctionnalite",
                subtitle = "Partager une idee d'amelioration",
                onClick = {
                    infoDialog = ProfileInfoDialog(
                        title = "Suggestion",
                        message = "Cette zone peut accueillir un vrai formulaire plus tard. Pour l'instant, l'architecture du profil est prete a recevoir d'autres modules."
                    )
                }
            )
        }

        item {
            MenuCard(
                icon = Icons.Default.Security,
                iconTint = Color(0xFFB042FF),
                title = "Politique de confidentialite",
                subtitle = "Gestion des donnees personnelles",
                onClick = {
                    infoDialog = ProfileInfoDialog(
                        title = "Confidentialite",
                        message = "Les donnees du profil sont actuellement stockees localement pour l'experience de l'application. Une politique complete peut etre branchee ici quand le contenu officiel sera pret."
                    )
                }
            )
        }

        item {
            MenuCard(
                icon = Icons.Default.Description,
                iconTint = Color(0xFFFF6C63),
                title = "Conditions d'utilisation",
                subtitle = "Regles d'usage de l'application",
                onClick = {
                    infoDialog = ProfileInfoDialog(
                        title = "Conditions d'utilisation",
                        message = "Cette entree est en place pour recevoir les vraies conditions de l'application. Pour le moment, elle sert de point d'ancrage dans le profil."
                    )
                }
            )
        }

        item {
            MenuCard(
                icon = Icons.Default.DeleteOutline,
                iconTint = Color(0xFFFF6257),
                title = "Supprimer le compte",
                subtitle = "Suppression definitive bientot disponible",
                destructive = true,
                onClick = {
                    infoDialog = ProfileInfoDialog(
                        title = "Suppression du compte",
                        message = "La suppression definitive du compte cote serveur n'est pas encore connectee. En attendant, tu peux effacer toutes les donnees locales depuis cet ecran."
                    )
                }
            )
        }

        item {
            MenuCard(
                icon = Icons.AutoMirrored.Filled.Logout,
                iconTint = Color(0xFFE53935),
                title = "Deconnexion",
                subtitle = if (ui.accountMode == ProfileAccountMode.AUTHENTICATED) {
                    "Terminer la session utilisateur"
                } else {
                    "Quitter le mode invite"
                },
                destructive = true,
                onClick = { showLogoutDialog = true }
            )
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            displayName = displayNameDraft,
            onDisplayNameChange = { displayNameDraft = it },
            studyGoal = studyGoalDraft,
            onStudyGoalChange = { studyGoalDraft = it },
            isSaving = ui.isSavingProfile,
            onDismiss = { showEditDialog = false },
            onSave = {
                vm.saveProfile(displayNameDraft, studyGoalDraft)
                showEditDialog = false
            }
        )
    }

    if (showLogoutDialog) {
        ConfirmDialog(
            title = if (ui.accountMode == ProfileAccountMode.AUTHENTICATED) "Se deconnecter ?" else "Quitter le mode invite ?",
            text = if (ui.accountMode == ProfileAccountMode.AUTHENTICATED) {
                "Tu reviendras a l'accueil et ta session sera fermee."
            } else {
                "Tu reviendras a l'accueil et le mode invite sera ferme."
            },
            confirmLabel = "Continuer",
            confirmColor = Color(0xFFE53935),
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                vm.logout(onDone = onLogout)
            }
        )
    }

    if (showClearDataDialog) {
        ConfirmDialog(
            title = "Effacer les donnees locales ?",
            text = "Les sessions, taches, matieres et morceaux sauvegardes sur cet appareil seront supprimes.",
            confirmLabel = "Effacer",
            confirmColor = Color(0xFFE53935),
            onDismiss = { showClearDataDialog = false },
            onConfirm = {
                showClearDataDialog = false
                vm.clearAllLocalData()
            }
        )
    }

    if (infoDialog != null) {
        AlertDialog(
            onDismissRequest = { infoDialog = null },
            title = { Text(infoDialog!!.title, fontWeight = FontWeight.ExtraBold) },
            text = { Text(infoDialog!!.message) },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (infoDialog!!.title == "Suppression du compte") {
                            showClearDataDialog = true
                        }
                        infoDialog = null
                    }
                ) {
                    Text(
                        text = if (infoDialog!!.title == "Suppression du compte") "Effacer mes donnees" else "Fermer",
                        color = if (infoDialog!!.title == "Suppression du compte") Color(0xFFE53935) else PinkPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                if (infoDialog!!.title == "Suppression du compte") {
                    TextButton(onClick = { infoDialog = null }) {
                        Text("Annuler", color = TextDark)
                    }
                }
            }
        )
    }
}

@Composable
private fun HeroProfileCard(
    ui: ProfileUiState,
    levelInfo: ProfileLevelInfo,
    onEdit: () -> Unit,
    onOpenLogin: () -> Unit,
    onOpenSignup: () -> Unit
) {
    val usernameLabel = remember(ui.username, ui.displayName) {
        "@${ui.username.ifBlank { ui.displayName.lowercase(Locale.getDefault()).replace(" ", "") }}"
    }

    Surface(
        shape = RoundedCornerShape(30.dp),
        color = Color.White,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                GradientAvatar(ui.avatarLabel)
                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = usernameLabel,
                        color = TextDark,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = ui.email,
                        color = TextGray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFFCECF5))
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = PinkPrimary)
                }
            }

            Spacer(Modifier.height(18.dp))

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFFDF2F8),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Niveau ${levelInfo.level}",
                        color = PinkPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = levelInfo.title,
                        color = TextDark,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "${levelInfo.currentXp} XP • +${levelInfo.remainingXp} jusqu'au niveau suivant",
                        color = TextGray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { levelInfo.progress.coerceAtLeast(0.02f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(999.dp)),
                        color = PinkPrimary,
                        trackColor = Color(0xFFF7D5E6)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HeroStat(
                    icon = Icons.Default.LocalFireDepartment,
                    label = "jours d'affilee",
                    value = ui.streakDays.toString(),
                    modifier = Modifier.weight(1f)
                )
                HeroStat(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    label = "temps total",
                    value = formatDurationLong(ui.totalFocusSeconds),
                    modifier = Modifier.weight(1f)
                )
                HeroStat(
                    icon = Icons.Default.Timer,
                    label = "session la plus longue",
                    value = formatDurationLong(ui.longestSessionSeconds),
                    modifier = Modifier.weight(1f)
                )
            }

            if (ui.studyGoal.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                GoalCard(ui.studyGoal)
            }

            if (ui.accountMode == ProfileAccountMode.GUEST) {
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PrimaryButton(
                        text = "Se connecter",
                        onClick = onOpenLogin,
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryButton(
                        text = "Creer un compte",
                        onClick = onOpenSignup,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun GradientAvatar(label: String) {
    Box(
        modifier = Modifier
            .size(92.dp)
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFE73C7E), Color(0xFFFFD33D))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun HeroStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = label, tint = TextGray, modifier = Modifier.size(26.dp))
        Spacer(Modifier.height(8.dp))
        Text(
            text = value,
            color = TextDark,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = TextGray,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun GoalCard(goal: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFFF8F1FF))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "Objectif du moment",
                color = PinkPrimary,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = goal,
                color = TextDark,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun StatusBanner(
    text: String,
    tint: Color,
    background: Color,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = text,
                color = tint,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.SemiBold
            )
            TextButton(onClick = onDismiss) {
                Text("Fermer", color = tint, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun WeeklyOverviewCard(
    summary: ProfileWeeklySummary,
    onOpenHistory: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(30.dp),
        color = Color.White,
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Cette semaine",
                        color = TextDark,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = formatWeekRange(summary),
                        color = TextGray,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFF1F3F7))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Lun-Dim",
                        color = TextGray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            if (summary.hasActivity) {
                WeeklyBars(summary)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "${formatDurationLong(summary.totalSeconds)} cette semaine",
                    color = PinkPrimary,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFFFFBFD))
                        .padding(horizontal = 18.dp, vertical = 26.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Aucune activite cette semaine",
                            color = TextDark,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "(Lun-Dim): chaque semaine est un nouveau depart",
                            color = TextGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            TextButton(onClick = onOpenHistory, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(
                    text = "Voir l'historique complet",
                    color = PinkPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun WeeklyBars(summary: ProfileWeeklySummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        summary.days.forEach { day ->
            val ratio = if (summary.bestDaySeconds == 0) 0f else day.totalSeconds / summary.bestDaySeconds.toFloat()
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(26.dp)
                        .height((28 + (ratio * 90)).dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 10.dp, bottomEnd = 10.dp))
                        .background(
                            if (day.totalSeconds > 0) {
                                Brush.verticalGradient(listOf(Color(0xFFF06AAE), PinkPrimary))
                            } else {
                                Brush.verticalGradient(listOf(Color(0xFFF1F3F7), Color(0xFFE7EAF0)))
                            }
                        )
                )
                Spacer(Modifier.height(8.dp))
                Text(day.label, color = TextGray, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = TextDark,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.ExtraBold
    )
}

@Composable
private fun MenuCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    destructive: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(iconTint.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(28.dp))
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (destructive) Color(0xFFE53935) else TextDark,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = TextGray,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = if (destructive) Color(0xFFE53935) else Color(0xFFAFB6C4)
            )
        }
    }
}

@Composable
private fun EditProfileDialog(
    displayName: String,
    onDisplayNameChange: (String) -> Unit,
    studyGoal: String,
    onStudyGoalChange: (String) -> Unit,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier le profil", fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = onDisplayNameChange,
                    singleLine = true,
                    label = { Text("Nom affiche") },
                    colors = profileFieldColors()
                )
                OutlinedTextField(
                    value = studyGoal,
                    onValueChange = onStudyGoalChange,
                    minLines = 3,
                    label = { Text("Objectif d'etude") },
                    colors = profileFieldColors()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = displayName.trim().isNotEmpty() && !isSaving
            ) {
                Text(
                    text = if (isSaving) "Enregistrement..." else "Sauvegarder",
                    color = PinkPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = TextDark)
            }
        }
    )
}

@Composable
private fun ConfirmDialog(
    title: String,
    text: String,
    confirmLabel: String,
    confirmColor: Color,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.ExtraBold) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, color = confirmColor, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = TextDark)
            }
        }
    )
}

@Composable
private fun profileFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PinkPrimary,
    unfocusedBorderColor = BorderSoft,
    focusedLabelColor = PinkPrimary
)
