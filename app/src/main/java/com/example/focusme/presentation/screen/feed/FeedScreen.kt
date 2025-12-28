package com.example.focusme.presentation.screen.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusme.presentation.ui.theme.PinkPrimary
import com.example.focusme.presentation.ui.theme.TextDark
import com.example.focusme.presentation.ui.theme.TextGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onNotifications: () -> Unit = {},
    vm: FindFriendsViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()
    val friends by vm.friendsFlow.collectAsState(initial = emptyList())

    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val bg = Color(0xFFFBEAF2)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Activité des amis",
                    color = TextDark,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Soutenez vos amis, motivez-vous\net progressez ensemble 💪",
                    color = TextGray,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(Modifier.width(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HeaderSquareButton(
                    icon = Icons.Default.Notifications,
                    label = "Notifications",
                    onClick = onNotifications
                )
                HeaderSquareButton(
                    icon = Icons.Default.PersonAdd,
                    label = "Ajouter",
                    onClick = { showSheet = true }
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        if (friends.isEmpty()) {
            // EMPTY STATE
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        tint = Color(0xFF9AA0A6),
                        modifier = Modifier.size(86.dp)
                    )

                    Spacer(Modifier.height(18.dp))

                    Text(
                        text = "Aucune activité pour le moment",
                        color = TextDark,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Ajoute des amis pour voir ici leurs sessions\nd'étude",
                        color = TextGray,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(22.dp))

                    AddFriendsBigButton(
                        text = "Ajouter des amis",
                        onClick = { showSheet = true }
                    )
                }
            }
        } else {
            // ✅ LISTE DES AMIS AJOUTÉS (Room)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(friends, key = { it.id }) { f ->
                    FriendInFeedCard(
                        friend = f,
                        onRemove = { vm.removeFriend(f.id) }
                    )
                }

            }

        }
    }

    // Bottom sheet
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFFFBEAF2)
        ) {
            FindFriendsSheet(
                state = state,
                onQueryChange = vm::onQueryChange,
                onAddFriend = vm::addFriend,
                onClose = { showSheet = false }
            )
        }
    }
}

@Composable
private fun FriendInFeedCard(
    friend: UserDto,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(PinkPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) { Text("👤") }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(friend.name, color = TextDark, fontWeight = FontWeight.Bold)
            Text("@${friend.username}", color = TextGray)
        }

        // ✅ BOUTON SUPPRIMER ICI
        TextButton(onClick = onRemove) {
            Text(
                "Supprimer",
                color = Color(0xFFD32F2F),
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
private fun HeaderSquareButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = PinkPrimary)
        }
        Spacer(Modifier.height(6.dp))
        Text(text = label, color = TextGray, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun AddFriendsBigButton(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(54.dp)
            .fillMaxWidth(0.72f)
            .clip(RoundedCornerShape(28.dp))
            .background(PinkPrimary)
            .clickable { onClick() }
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(text = text, color = Color.White, fontWeight = FontWeight.Bold)
    }
}
