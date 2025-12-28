package com.example.focusme.presentation.screen.challenges

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusme.data.db.ChallengeEntity
import com.example.focusme.presentation.ui.theme.PinkPrimary
import com.example.focusme.presentation.ui.theme.TextDark
import com.example.focusme.presentation.ui.theme.TextGray

@Composable
fun ChallengesScreen(
    onGoCreate: () -> Unit,
    vm: ChallengesViewModel = viewModel()
) {
    val ui by vm.uiState.collectAsState()
    val filtered by vm.filteredChallenges.collectAsState()

    val bg = Color(0xFFFBEAF2)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🏆", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Défis",
                color = TextDark,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.headlineLarge
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Compétition entre amis",
            color = PinkPrimary,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        SegmentedTabs(
            selected = ui.selectedTab,
            labels = listOf("Actives", "En attente", "Terminé"),
            onSelect = vm::selectTab
        )

        Spacer(Modifier.height(18.dp))

        if (filtered.isEmpty()) {
            EmptyChallenges(
                tab = ui.selectedTab,
                onCreate = onGoCreate
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id }) { ch ->
                    ChallengeCard(
                        ch = ch,
                        tab = ui.selectedTab,
                        onConfirm = { vm.confirmPending(ch) },
                        onFinish = { vm.finishActive(ch) },
                        onDelete = { vm.deleteChallenge(ch.id) }
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            PinkPillButton(
                text = "+  Créer nouveau",
                onClick = onGoCreate,
                widthFraction = 0.55f
            )
        }

        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun EmptyChallenges(tab: Int, onCreate: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎯", style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(16.dp))

        Text(
            text = when (tab) {
                0 -> "Aucun défi actif"
                1 -> "Aucun défi en attente"
                else -> "Aucun défi terminé"
            },
            color = TextDark,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Les défis apparaîtront ici lorsque\nvous en aurez créé un.\n\nClique sur “Créer le premier défi”.",
            color = TextGray,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(22.dp))

        PinkPillButton(
            text = "+  Créer le premier défi",
            onClick = onCreate,
            widthFraction = 0.78f
        )
    }
}

@Composable
private fun ChallengeCard(
    ch: ChallengeEntity,
    tab: Int,
    onConfirm: () -> Unit,
    onFinish: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(14.dp)
    ) {
        Text(ch.title, color = TextDark, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
        if (ch.description.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(ch.description, color = TextGray, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // statut badge
            val badgeText = when (ch.status) {
                "PENDING" -> "En attente"
                "ACTIVE" -> "Actif"
                else -> "Terminé"
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(PinkPrimary.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(badgeText, color = PinkPrimary, fontWeight = FontWeight.Bold)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Action principale selon tab
                when (tab) {
                    1 -> SmallActionButton(text = "Confirmer", onClick = onConfirm) // PENDING -> ACTIVE
                    0 -> SmallActionButton(text = "Terminer", onClick = onFinish)   // ACTIVE -> FINISHED
                    else -> {} // terminé -> rien
                }


                Text(
                    text = "Supprimer",
                    color = Color(0xFFD32F2F),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onDelete() }
                )
            }
        }
    }
}

@Composable
private fun SmallActionButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(PinkPrimary)
            .clickable { onClick() }
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SegmentedTabs(
    selected: Int,
    labels: List<String>,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        labels.forEachIndexed { i, label ->
            val isSelected = i == selected
            val bg = if (isSelected) Color.White else Color.Transparent
            val text = if (isSelected) TextDark else TextGray

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(bg)
                    .clickable { onSelect(i) },
                contentAlignment = Alignment.Center
            ) {
                Text(label, color = text, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun PinkPillButton(text: String, onClick: () -> Unit, widthFraction: Float) {
    val grad = Brush.horizontalGradient(
        colors = listOf(PinkPrimary, PinkPrimary.copy(alpha = 0.88f))
    )

    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(54.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(grad)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
    }
}
