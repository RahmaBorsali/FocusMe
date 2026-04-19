package com.example.focusme.presentation.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusme.presentation.ui.theme.AppBg
import com.example.focusme.presentation.ui.theme.BorderSoft
import com.example.focusme.presentation.ui.theme.PinkPrimary
import com.example.focusme.presentation.ui.theme.TextDark
import com.example.focusme.presentation.ui.theme.TextGray

@Composable
fun ProfileSettingsScreen(
    onBack: () -> Unit,
    onOpenMusicSubscription: () -> Unit,
    vm: ProfileViewModel = viewModel()
) {
    val ui by vm.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsHeader(onBack = onBack)
        }

        item {
            SettingsSwitchCard(
                icon = Icons.Default.NotificationsActive,
                iconTint = Color(0xFF9C6ADE),
                title = "Notifications",
                subtitle = "Alerte de fin de session",
                checked = ui.notificationsEnabled,
                onCheckedChange = vm::setNotificationsEnabled
            )
        }

        item {
            SettingsSwitchCard(
                icon = Icons.Default.Tune,
                iconTint = Color(0xFF49B96E),
                title = "Son",
                subtitle = "Lecture de la sonnerie de fin",
                checked = ui.soundEnabled,
                onCheckedChange = vm::setSoundEnabled
            )
        }
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFFFF8E1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFFFB300))
                        }
                        Spacer(Modifier.size(14.dp))
                        Column {
                            Text(
                                text = "Sonnerie de fin",
                                color = TextDark,
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Choisir l'ambiance de l'alerte",
                                color = TextGray
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingsChoiceChip(
                            text = "Classique",
                            selected = ui.alarmSound == "classic",
                            modifier = Modifier.weight(1f),
                            onClick = { vm.setAlarmSound("classic") }
                        )
                        SettingsChoiceChip(
                            text = "Zen",
                            selected = ui.alarmSound == "zen",
                            modifier = Modifier.weight(1f),
                            onClick = { vm.setAlarmSound("zen") }
                        )
                        SettingsChoiceChip(
                            text = "Alerte",
                            selected = ui.alarmSound == "alert",
                            modifier = Modifier.weight(1f),
                            onClick = { vm.setAlarmSound("alert") }
                        )
                    }
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 0.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .clickable { onOpenMusicSubscription() }
            ) {
                Row(
                    modifier = Modifier.padding(18.dp, 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFFF0F5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = PinkPrimary)
                    }
                    Spacer(Modifier.size(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Abonnement Musique", color = TextDark, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
                        Text("Parametrer tes catalogues musicaux", color = TextGray)
                    }
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFEEF4FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Public, contentDescription = null, tint = Color(0xFF5D9BFF))
                        }
                        Spacer(Modifier.size(14.dp))
                        Column {
                            Text(
                                text = "Visibilite par defaut",
                                color = TextDark,
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Choisir qui voit tes nouvelles sessions",
                                color = TextGray
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SettingsChoiceChip(
                            text = "Amis",
                            selected = ui.defaultVisibility == "friends",
                            modifier = Modifier.weight(1f),
                            onClick = { vm.setDefaultVisibility("friends") }
                        )
                        SettingsChoiceChip(
                            text = "Prive",
                            selected = ui.defaultVisibility == "private",
                            modifier = Modifier.weight(1f),
                            onClick = { vm.setDefaultVisibility("private") }
                        )
                    }
                }
            }
        }

        item {
            SettingsSwitchCard(
                icon = Icons.Default.Email,
                iconTint = Color(0xFFFFB300),
                title = "Commentaires",
                subtitle = "Autoriser les commentaires par defaut",
                checked = ui.defaultAllowComments,
                onCheckedChange = vm::setDefaultAllowComments
            )
        }

        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Resume des reglages",
                        color = TextDark,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    SettingsSummaryRow("Notifications", if (ui.notificationsEnabled) "Activees" else "Desactivees")
                    SettingsSummaryRow("Son", if (ui.soundEnabled) "Actif" else "Desactive")
                    SettingsSummaryRow("Sonnerie", ui.alarmSound.replaceFirstChar { it.uppercase() })
                    SettingsSummaryRow("Visibilite", if (ui.defaultVisibility == "friends") "Amis" else "Prive")
                    SettingsSummaryRow("Commentaires", if (ui.defaultAllowComments) "Autorises" else "Desactives")
                }
            }
        }
    }
}

@Composable
private fun SettingsHeader(onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = TextDark)
        }
        Spacer(Modifier.size(16.dp))
        Column {
            Text(
                text = "Reglages de l'app",
                color = TextDark,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Notifications, son et visibilite",
                color = PinkPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SettingsSwitchCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(iconTint.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = iconTint)
            }
            Spacer(Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextDark, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
                Text(subtitle, color = TextGray)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PinkPrimary,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = BorderSoft
                )
            )
        }
    }
}

@Composable
private fun SettingsChoiceChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) PinkPrimary else Color(0xFFF8F8FB))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else TextDark,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SettingsSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextGray)
        Text(value, color = TextDark, fontWeight = FontWeight.Bold)
    }
}
