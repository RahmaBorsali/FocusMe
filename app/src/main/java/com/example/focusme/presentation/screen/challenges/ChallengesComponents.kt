package com.example.focusme.presentation.screen.challenges

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.focusme.data.repository.Challenge
import com.example.focusme.data.repository.ChallengeInvitation
import com.example.focusme.data.repository.ChallengeRole
import com.example.focusme.data.repository.ChallengeStatus
import com.example.focusme.data.repository.ChallengeVisibility
import com.example.focusme.data.repository.JoinRequestType
import com.example.focusme.data.repository.LeaderboardEntry
import com.example.focusme.presentation.ui.components.SoftCard
import com.example.focusme.presentation.ui.theme.AppBg
import com.example.focusme.presentation.ui.theme.BorderSoft
import com.example.focusme.presentation.ui.theme.PinkPrimary
import com.example.focusme.presentation.ui.theme.TextDark
import com.example.focusme.presentation.ui.theme.TextGray
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

private val subtleHeaderGradient = Brush.verticalGradient(
    listOf(Color(0xFFFCEAF2), Color(0xFFFBEAF2))
)

@Composable
fun ChallengeScreenContainer(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actions: @Composable (RowScope.() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBg)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(subtleHeaderGradient)
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = TextDark,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        color = TextGray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (actions != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), content = actions)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        content()
    }
}

@Composable
fun ChallengeSegmentedTabs(
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        padding = androidx.compose.foundation.layout.PaddingValues(4.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            options.forEach { option ->
                val selectedOption = option == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (selectedOption) Color.White else Color.Transparent)
                        .clickable { onSelect(option) }
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        color = if (selectedOption) PinkPrimary else TextGray,
                        fontWeight = if (selectedOption) FontWeight.SemiBold else FontWeight.Medium,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
fun ChallengeCard(
    challenge: Challenge,
    ctaLabel: String,
    onClick: () -> Unit,
    onPrimaryActionClick: (() -> Unit)? = null,
    primaryActionEnabled: Boolean = true,
    supportingNote: String? = null,
    supportingNoteColor: Color = PinkPrimary,
    membershipLabel: String? = null,
    membershipTint: Color = PinkPrimary,
    membershipBackground: Color = PinkPrimary.copy(alpha = 0.10f),
    secondaryLabel: String? = null,
    onSecondaryClick: (() -> Unit)? = null,
    emphasize: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = if (emphasize) PinkPrimary.copy(alpha = 0.14f) else BorderSoft,
                shape = RoundedCornerShape(22.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = challenge.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = challenge.description.ifBlank { "Un defi motive pour progresser ensemble chaque jour." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(12.dp))
            ChallengeStatusChip(status = challenge.status)
        }

        Spacer(Modifier.height(12.dp))

        if (!membershipLabel.isNullOrBlank()) {
            ChallengeMembershipChip(
                label = membershipLabel,
                tint = membershipTint,
                background = membershipBackground
            )
            Spacer(Modifier.height(10.dp))
        }

        Text(
            text = "${challenge.goal.targetValue} ${challenge.goal.unit} • ${challenge.participantsCount}/${challenge.maxParticipants ?: "∞"} membres",
            color = TextDark,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${challenge.visibility.label} • ${challenge.dateRangeLabel()}",
            color = TextGray,
            style = MaterialTheme.typography.bodySmall
        )

        if (!supportingNote.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = supportingNote,
                color = supportingNoteColor,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onPrimaryActionClick != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PrimaryChallengeButton(
                        text = ctaLabel,
                        enabled = primaryActionEnabled,
                        onClick = onPrimaryActionClick,
                        modifier = Modifier.weight(1f)
                    )
                    if (secondaryLabel != null && onSecondaryClick != null) {
                        OutlinedButton(
                            onClick = onSecondaryClick,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDark),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(secondaryLabel, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                Text(
                    text = ctaLabel,
                    color = PinkPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
                if (secondaryLabel != null && onSecondaryClick != null) {
                    OutlinedButton(
                        onClick = onSecondaryClick,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDark)
                    ) {
                        Text(secondaryLabel, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = PinkPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChallengeMembershipChip(
    label: String,
    tint: Color,
    background: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            color = tint,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun ChallengeStatusChip(status: ChallengeStatus) {
    val (bg, fg) = when (status) {
        ChallengeStatus.UPCOMING -> Color(0xFFF4EDFF) to Color(0xFF7B58C6)
        ChallengeStatus.ONGOING -> Color(0xFFEAFBF2) to Color(0xFF299764)
        ChallengeStatus.FINISHED -> Color(0xFFF3F3F5) to TextGray
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(status.label, color = fg, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun SectionTitle(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, color = TextDark, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        if (action != null && onAction != null) {
            Text(
                text = action,
                color = PinkPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onAction() }
            )
        }
    }
}

@Composable
fun EmptyChallengesCard(
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = PinkPrimary,
                modifier = Modifier.size(30.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(title, color = TextDark, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(subtitle, color = TextGray, style = MaterialTheme.typography.bodyMedium)
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(14.dp))
                PrimaryChallengeButton(text = actionLabel, onClick = onAction)
            }
        }
    }
}

@Composable
fun StateScaffold(
    state: ContentState<List<*>>,
    emptyTitle: String,
    emptySubtitle: String,
    content: @Composable (List<*>) -> Unit
) {
    when (state) {
        ContentState.Loading -> FullScreenLoading()
        is ContentState.Error -> ErrorCard(message = state.message)
        is ContentState.Success -> {
            if (state.data.isEmpty()) {
                EmptyChallengesCard(title = emptyTitle, subtitle = emptySubtitle)
            } else {
                content(state.data)
            }
        }
    }
}

@Composable
fun FullScreenLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = PinkPrimary)
    }
}

@Composable
fun ErrorCard(message: String, onRetry: (() -> Unit)? = null) {
    if (message.contains("Ta session a expire", ignoreCase = true) ||
        message.contains("Reconnecte-toi", ignoreCase = true)
    ) {
        return
    }
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text("Impossible de charger cette section", color = TextDark, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(message, color = TextGray)
            if (onRetry != null) {
                Spacer(Modifier.height(12.dp))
                PrimaryChallengeButton(text = "Reessayer", onClick = onRetry)
            }
        }
    }
}

@Composable
fun StateInfoCard(
    title: String,
    message: String,
    icon: ImageVector = Icons.Default.Key,
    tint: Color = PinkPrimary
) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextDark, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(message, color = TextGray, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun JoinByCodeEntryCard(
    code: String,
    errorMessage: String?,
    isJoining: Boolean,
    onCodeChange: (String) -> Unit,
    onJoinClick: () -> Unit
) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PinkPrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = PinkPrimary)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Rejoindre avec un code", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Colle le code d'un ami et on t'ouvre directement le bon challenge.",
                        color = TextGray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = code,
                onValueChange = onCodeChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Code du challenge") },
                placeholder = { Text("Ex: FOCUS8") },
                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                isError = errorMessage != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PinkPrimary,
                    focusedLabelColor = PinkPrimary,
                    focusedLeadingIconColor = PinkPrimary
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = errorMessage ?: "Le proprietaire retrouve ce code dans le detail du challenge puis le partage a ses amis.",
                color = if (errorMessage != null) MaterialTheme.colorScheme.error else TextGray,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(12.dp))
            PrimaryChallengeButton(
                text = if (isJoining) "Connexion..." else "Rejoindre avec ce code",
                enabled = !isJoining,
                onClick = onJoinClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ChallengeCodeCard(
    code: String,
    feedback: String?,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text("Code du challenge", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(6.dp))
            Text(
                "Partage ce code avec tes amis pour qu'ils rejoignent rapidement ton challenge.",
                color = TextGray,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFFFF7FA))
                    .border(1.dp, BorderSoft, RoundedCornerShape(20.dp))
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = code,
                    color = TextDark,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onCopy,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Copier")
                }
                PrimaryChallengeButton(
                    text = "Partager",
                    modifier = Modifier.weight(1f),
                    onClick = onShare
                )
            }
            if (!feedback.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(feedback, color = PinkPrimary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun JoinByCodePreviewCard(
    challenge: Challenge,
    availability: JoinCodeAvailability,
    isJoining: Boolean,
    onPrimaryAction: () -> Unit
) {
    val infoMessage = when (availability) {
        JoinCodeAvailability.READY -> "Tout est pret. Tu peux rejoindre ce challenge maintenant."
        JoinCodeAvailability.ALREADY_JOINED -> "Tu fais deja partie de ce challenge."
        JoinCodeAvailability.CHALLENGE_FULL -> "Ce challenge a deja atteint sa limite de participants."
        JoinCodeAvailability.CHALLENGE_FINISHED -> "Ce challenge est termine."
        JoinCodeAvailability.ACCESS_RESTRICTED -> "Tu n'as pas acces a ce challenge."
        JoinCodeAvailability.JOINED_SUCCESS -> "Bravo, tu as rejoint ce challenge."
    }

    val ctaLabel = when (availability) {
        JoinCodeAvailability.READY -> if (isJoining) "Connexion..." else "Rejoindre le challenge"
        JoinCodeAvailability.ALREADY_JOINED, JoinCodeAvailability.JOINED_SUCCESS -> "Ouvrir le challenge"
        JoinCodeAvailability.CHALLENGE_FULL -> "Challenge complet"
        JoinCodeAvailability.CHALLENGE_FINISHED -> "Challenge termine"
        JoinCodeAvailability.ACCESS_RESTRICTED -> "Acces refuse"
    }

    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            SectionTitle(title = "Challenge trouve")
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = challenge.title,
                        color = TextDark,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = challenge.description.ifBlank { "Un challenge motive pour progresser entre amis." },
                        color = TextGray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(Modifier.width(12.dp))
                ChallengeStatusChip(challenge.status)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "${challenge.goal.targetValue} ${challenge.goal.unit}",
                color = TextDark,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${challenge.participantsCount}/${challenge.maxParticipants ?: "∞"} participants • ${challenge.visibility.label}",
                color = TextGray,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = challenge.dateRangeLabel(),
                color = TextGray,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(12.dp))
            StateInfoCard(
                title = when (availability) {
                    JoinCodeAvailability.READY -> "Pret a rejoindre"
                    JoinCodeAvailability.ALREADY_JOINED -> "Deja rejoint"
                    JoinCodeAvailability.CHALLENGE_FULL -> "Challenge complet"
                    JoinCodeAvailability.CHALLENGE_FINISHED -> "Challenge termine"
                    JoinCodeAvailability.ACCESS_RESTRICTED -> "Acces refuse"
                    JoinCodeAvailability.JOINED_SUCCESS -> "Challenge rejoint"
                },
                message = infoMessage,
                icon = when (availability) {
                    JoinCodeAvailability.READY,
                    JoinCodeAvailability.ALREADY_JOINED,
                    JoinCodeAvailability.JOINED_SUCCESS -> Icons.Default.CheckCircle
                    else -> Icons.Default.Key
                },
                tint = when (availability) {
                    JoinCodeAvailability.READY,
                    JoinCodeAvailability.ALREADY_JOINED,
                    JoinCodeAvailability.JOINED_SUCCESS -> Color(0xFF299764)
                    else -> PinkPrimary
                }
            )
            Spacer(Modifier.height(12.dp))
            PrimaryChallengeButton(
                text = ctaLabel,
                enabled = availability == JoinCodeAvailability.READY ||
                    availability == JoinCodeAvailability.ALREADY_JOINED ||
                    availability == JoinCodeAvailability.JOINED_SUCCESS,
                onClick = onPrimaryAction,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun PrimaryChallengeButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
        modifier = modifier.heightIn(min = 48.dp)
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LeaderboardPreviewCard(entries: List<LeaderboardEntry>, onSeeAll: () -> Unit) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            SectionTitle(title = "Top du moment", action = "Voir tout", onAction = onSeeAll)
            Spacer(Modifier.height(12.dp))
            entries.forEach { entry ->
                LeaderboardRow(entry = entry)
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun LeaderboardRow(entry: LeaderboardEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (entry.isCurrentUser) Color(0xFFFFF4F8) else Color(0xFFFDFDFD))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            RankBadge(entry.rank)
            Spacer(Modifier.width(12.dp))
            Avatar(url = entry.avatarUrl, fallback = entry.username.take(1))
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.username, color = TextDark, fontWeight = FontWeight.Bold)
                Text("${entry.progress}% de l'objectif", color = TextGray, style = MaterialTheme.typography.bodySmall)
            }
        }
        Text("${entry.score}", color = PinkPrimary, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun RankBadge(rank: Int) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(if (rank <= 3) Color(0xFFFFE7B2) else Color(0xFFF3F1F6)),
        contentAlignment = Alignment.Center
    ) {
        Text("#$rank", color = TextDark, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun Avatar(url: String?, fallback: String, modifier: Modifier = Modifier) {
    if (!url.isNullOrBlank()) {
        AsyncImage(
            model = url,
            contentDescription = null,
            modifier = modifier.size(42.dp).clip(CircleShape)
        )
    } else {
        Box(
            modifier = modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFD7E8)),
            contentAlignment = Alignment.Center
        ) {
            Text(fallback.uppercase(), color = PinkPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

val ChallengeVisibility.label: String
    get() = when (this) {
        ChallengeVisibility.PUBLIC -> "Public"
        ChallengeVisibility.PRIVATE -> "Prive"
        ChallengeVisibility.FRIENDS -> "Entre amis"
    }

val ChallengeStatus.label: String
    get() = when (this) {
        ChallengeStatus.UPCOMING -> "A venir"
        ChallengeStatus.ONGOING -> "En cours"
        ChallengeStatus.FINISHED -> "Termine"
    }

fun Challenge.dateRangeLabel(): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM")
    return runCatching {
        val start = LocalDate.parse(startDate).format(formatter)
        val end = LocalDate.parse(endDate).format(formatter)
        "$start - $end"
    }.getOrElse { "$startDate - $endDate" }
}

fun String?.toReadableDateTime(): String {
    if (this.isNullOrBlank()) return ""
    return runCatching {
        OffsetDateTime.parse(this).format(DateTimeFormatter.ofPattern("dd MMM, HH:mm"))
    }.getOrElse { this }
}

fun JoinRequestType?.requestLabel(): String = when (this) {
    JoinRequestType.REQUEST_ACCESS -> "Demande d'acces"
    JoinRequestType.JOIN, null -> "Demande de participation"
}

fun JoinRequestType.ownerActionText(challengeTitle: String): String = when (this) {
    JoinRequestType.REQUEST_ACCESS -> "Demande l'acces a $challengeTitle"
    JoinRequestType.JOIN -> "Veut rejoindre $challengeTitle"
}

fun ChallengeInvitation.challengeTitle(): String = challenge?.title ?: "Challenge prive"
