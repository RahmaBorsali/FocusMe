package com.example.focusme.presentation.screen.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusme.data.local.TokenStore
import com.example.focusme.presentation.model.FeedItem
import com.example.focusme.presentation.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun FriendsFeedScreen(
    onBack: () -> Unit,
    vm: FriendsFeedViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current
    val userId = remember { TokenStore(context).getUserIdBlocking() ?: "" }

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            vm.loadFeed(userId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBEAF2))
    ) {
        // Simple Header (Reverted Design)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextDark)
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.People,
                contentDescription = null,
                tint = PinkPrimary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                "Activité récente",
                color = TextDark,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PinkPrimary
                    )
                }
                state.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Oups ! Une erreur est survenue.", fontWeight = FontWeight.Bold, color = TextDark)
                        Text(state.error!!, color = TextGray, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { vm.loadFeed(userId) },
                            colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
                        ) {
                            Text("Réessayer", color = Color.White)
                        }
                    }
                }
                state.items.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = TextGray.copy(alpha = 0.3f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Aucune activité récente",
                            color = TextDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            "Tes amis n'ont pas encore partagé d'activité aujourd'hui.",
                            color = TextGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.items) { item ->
                            FeedItemCard(item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeedItemCard(item: FeedItem) {
    val relativeTime = remember(item.timestamp) {
        formatRelativeTime(item.timestamp)
    }

    // Initials from friendName (e.g. "Takwa Borsali" -> "TB")
    val initials = remember(item.friendName) {
        item.friendName.split(" ")
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 0.dp // Reduit pour match le screenshot
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Initials Avatar
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(PinkPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = PinkPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.friendName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextDark
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    item.message,
                    color = TextGray,
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                )
            }

            Spacer(Modifier.width(8.dp))

            Text(
                relativeTime,
                color = TextGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun formatRelativeTime(isoTimestamp: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(isoTimestamp) ?: return "il y a un moment"

        val diff = System.currentTimeMillis() - date.time
        
        when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "à l'instant"
            diff < TimeUnit.HOURS.toMillis(1) -> "il y a ${TimeUnit.MILLISECONDS.toMinutes(diff)}min"
            diff < TimeUnit.DAYS.toMillis(1) -> "il y a ${TimeUnit.MILLISECONDS.toHours(diff)}h"
            diff < TimeUnit.DAYS.toMillis(7) -> "il y a ${TimeUnit.MILLISECONDS.toDays(diff)}j"
            else -> SimpleDateFormat("dd/MM", Locale.getDefault()).format(date)
        }
    } catch (e: Exception) {
        "récent"
    }
}
