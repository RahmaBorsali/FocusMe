package com.example.focusme.presentation.screen.feed

import com.example.focusme.presentation.model.UserUi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusme.presentation.model.FriendStatus
import com.example.focusme.presentation.ui.theme.AppBg
import com.example.focusme.presentation.ui.theme.BorderSoft
import com.example.focusme.presentation.ui.theme.PinkPrimary
import com.example.focusme.presentation.ui.theme.TextDark
import com.example.focusme.presentation.ui.theme.TextGray
import kotlinx.coroutines.delay

@Composable
fun FindFriendsSheet(
    state: FindFriendsUiState,
    onQueryChange: (String) -> Unit,
    onAddFriend: (UserUi) -> Unit,
    onAcceptRequest: (String) -> Unit,
    onRejectRequest: (String) -> Unit,
    onClose: () -> Unit
) {
    var localQuery by remember { mutableStateOf(state.query) }

    // debounce
    LaunchedEffect(localQuery) {
        delay(300)
        onQueryChange(localQuery)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(bottom = 24.dp)
    ) {
        // Premium Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(PinkPrimary, PinkPrimary.copy(alpha = 0.9f))
                    )
                )
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🤝", fontSize = 22.sp)
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Trouver des amis",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "Élargis ton cercle d'étude",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Search Section with "Glass" effect container
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(20.dp),
            color = AppBg.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, PinkPrimary.copy(alpha = 0.1f))
        ) {
            OutlinedTextField(
                value = localQuery,
                onValueChange = { localQuery = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Rechercher un utilisateur...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PinkPrimary) },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PinkPrimary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = PinkPrimary,
                    focusedLabelColor = PinkPrimary,
                    unfocusedLabelColor = TextGray
                )
            )
        }

        Spacer(Modifier.height(24.dp))

        if (state.isLoading) {
            Box(Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PinkPrimary, strokeWidth = 3.dp)
            }
        }

        // Results / Suggestions / My Friends
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            // Section: Mes amis
            if (state.friends.isNotEmpty()) {
                item {
                    Text(
                        "Mes amis",
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(state.friends, key = { "friend_${it.id}" }) { user ->
                    FriendRow(
                        user = user, 
                        onAdd = { onAddFriend(user) }
                    )
                }
                item { Spacer(Modifier.height(8.dp)) }
            }

            // Section: Suggestions / Résultats
            item {
                Text(
                    if (localQuery.isBlank()) "Suggestions" else "Résultats",
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            val listToShow = if (localQuery.isBlank()) state.suggestions else state.results
            
            if (listToShow.isEmpty() && !state.isLoading) {
                item {
                    Text(
                        "Aucun résultat trouvé",
                        color = TextGray,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items(listToShow, key = { "result_${it.id}" }) { user ->
                    FriendRow(
                        user = user, 
                        onAdd = { onAddFriend(user) }
                    )
                }
            }
        }

        if (state.error != null) {
            Text(
                text = state.error,
                color = Color.Red,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun FriendRow(
    user: UserUi, 
    onAdd: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = if (user.status == FriendStatus.INCOMING_PENDING) Color(0xFFFFF5F9) else Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, if (user.status == FriendStatus.INCOMING_PENDING) PinkPrimary.copy(alpha = 0.2f) else BorderSoft)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(AppBg),
                contentAlignment = Alignment.Center
            ) {
                Text("👤", fontSize = 22.sp)
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    user.name,
                    color = TextDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    if (user.status == FriendStatus.INCOMING_PENDING) "T'a envoyé une invitation" else "@${user.username}",
                    color = if (user.status == FriendStatus.INCOMING_PENDING) PinkPrimary else TextGray,
                    fontSize = 12.sp,
                    fontWeight = if (user.status == FriendStatus.INCOMING_PENDING) FontWeight.Medium else FontWeight.Normal
                )
            }

            // Actions Area
            when (user.status) {
                FriendStatus.FRIEND -> {
                    StatusBadge(text = "Ami", icon = Icons.Default.CheckCircle, Color(0xFFE3F2FD), Color(0xFF1976D2))
                }
                FriendStatus.OUTGOING_PENDING -> {
                    StatusBadge(text = "Envoyée", icon = Icons.Default.Check, Color(0xFFE8F5E9), Color(0xFF2E7D32))
                }
                FriendStatus.NONE -> {
                    Button(
                        onClick = onAdd,
                        colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text("Ajouter", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                FriendStatus.INCOMING_PENDING -> {
                    StatusBadge(text = "En attente", icon = Icons.Default.Timer, Color(0xFFFFF9C4), Color(0xFFFBC02D))
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, bgColor: Color, contentColor: Color) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text,
                color = contentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
