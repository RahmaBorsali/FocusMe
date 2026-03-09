package com.example.focusme.presentation.screen.social

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusme.data.api.dto.FriendStats
import com.example.focusme.data.local.TokenStore
import com.example.focusme.data.repository.SocialRepository
import com.example.focusme.presentation.ui.theme.PinkPrimary
import com.example.focusme.presentation.ui.theme.TextDark
import com.example.focusme.presentation.ui.theme.TextGray

@Composable
fun LeaderboardScreen(
    context: Context = LocalContext.current
) {
    val viewModel: LeaderboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val state by viewModel.uiState.collectAsState()

    val userId = remember { TokenStore(context).getUserIdBlocking() ?: "" }

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) viewModel.loadLeaderboard(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBEAF2))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🏆", fontSize = 26.sp)
            Spacer(Modifier.width(10.dp))
            Text(
                "Classement",
                color = TextDark,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleLarge
            )
        }

        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PinkPrimary)
                }
            }

            state.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Impossible de charger le classement.\n${state.error}",
                        color = TextGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }

            state.items.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🥇", fontSize = 64.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Classement vide",
                            color = TextDark,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Commence une session pour\napparaître dans le classement !",
                            color = TextGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(state.items) { index, friend ->
                        LeaderboardCard(
                            rank = index + 1, 
                            friend = friend,
                            isCurrentUser = friend.userId == state.currentUserId
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardCard(rank: Int, friend: FriendStats, isCurrentUser: Boolean) {
    val rankEmoji = when (rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> "#$rank"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCurrentUser -> PinkPrimary.copy(alpha = 0.08f)
                rank <= 3 -> Color(0xFFFFF8E1)
                else -> Color.White
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = if (isCurrentUser) BorderStroke(1.dp, PinkPrimary.copy(alpha = 0.2f)) else null
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (rank <= 3) Color(0xFFFFE082) else Color(0xFFF0F0F0)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    rankEmoji,
                    fontSize = if (rank <= 3) 22.sp else 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }

            Spacer(Modifier.width(12.dp))

            // Name
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        friend.name, 
                        fontWeight = FontWeight.Bold, 
                        color = if (isCurrentUser) PinkPrimary else TextDark
                    )
                    if (isCurrentUser) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            color = PinkPrimary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "C'est toi !",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    "${friend.weeklyFocusMin} min cette semaine",
                    color = TextGray,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Streak
            if (friend.streak > 0) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔥", fontSize = 18.sp)
                    Text(
                        "${friend.streak}j",
                        color = Color(0xFFFF6F00),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
