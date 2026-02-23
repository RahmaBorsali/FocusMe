package com.example.focusme.presentation.screen.feed

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusme.presentation.ui.theme.*
import com.example.focusme.data.api.dto.*

@Composable
fun FriendRequestsScreen(
    onBack: () -> Unit,
    vm: FriendRequestsViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: Incoming, 1: Outgoing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Gradient Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(PinkPrimary, PinkPrimary.copy(alpha = 0.9f))
                    )
                )
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(
                        "Demandes d'amis",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // Custom Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .height(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AppBg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabItem(
                text = "Reçues",
                isSelected = selectedTab == 0,
                modifier = Modifier.weight(1f),
                onClick = { selectedTab = 0 }
            )
            TabItem(
                text = "Envoyées",
                isSelected = selectedTab == 1,
                modifier = Modifier.weight(1f),
                onClick = { selectedTab = 1 }
            )
        }

        // Error display
        if (state.error != null) {
            Text(
                state.error!!,
                color = Color.Red,
                modifier = Modifier.padding(horizontal = 24.dp),
                fontSize = 12.sp
            )
        }

        // Content
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PinkPrimary)
            } else {
                if (selectedTab == 0) {
                    IncomingTabContent(
                        list = state.incoming,
                        onAccept = vm::acceptRequest,
                        onReject = vm::rejectRequest
                    )
                } else {
                    OutgoingTabContent(list = state.outgoing)
                }
            }
        }
    }
}

@Composable
fun TabItem(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color.White else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) PinkPrimary else TextGray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

// MOCK CONTENT FOR NOW (Will connect to VM after redesign approval)
@Composable
fun IncomingTabContent(
    list: List<IncomingRequestItem>,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit
) {
    if (list.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Inbox,
            text = "Aucune demande reçue",
            subtext = "Elle s'afficheront ici quand quelqu'un t'ajoutera."
        )
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
            items(list, key = { it.requestId }) { item ->
                val user = item.fromUser
                RequestCard(
                    name = user?.username ?: "Utilisateur",
                    username = user?.username?.replace(" ", "")?.lowercase() ?: "inconnu",
                    time = item.createdAt.take(10), // Simplifié
                    actions = {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { onAccept(item.requestId) },
                                colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) { Text("Accepter", color = Color.White) }
                            OutlinedButton(
                                onClick = { onReject(item.requestId) },
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSoft),
                                modifier = Modifier.weight(1f)
                            ) { Text("Refuser", color = TextGray) }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun OutgoingTabContent(list: List<OutgoingRequestItem>) {
    if (list.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Send,
            text = "Aucune demande envoyée",
            subtext = "Trouve des amis pour commencer à te motiver !"
        )
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
            items(list, key = { it.requestId }) { item ->
                val user = item.toUser
                RequestCard(
                    name = user?.username ?: "Utilisateur",
                    username = user?.username?.replace(" ", "")?.lowercase() ?: "inconnu",
                    time = item.createdAt.take(10),
                    actions = {
                        Text(
                            "En attente de réponse...",
                            color = PinkPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun EmptyState(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, subtext: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(60.dp),
            tint = TextGray.copy(alpha = 0.3f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text,
            color = TextDark,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            subtext,
            color = TextGray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun RequestCard(name: String, username: String, time: String, actions: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSoft)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(AppBg),
                    contentAlignment = Alignment.Center
                ) { Text("👤", fontSize = 24.sp) }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(name, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextDark)
                    Text("@$username • $time", fontSize = 13.sp, color = TextGray)
                }
            }
            Spacer(Modifier.height(16.dp))
            actions()
        }
    }
}