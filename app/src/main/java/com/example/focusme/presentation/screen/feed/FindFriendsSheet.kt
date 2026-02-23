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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

        // Empty/Initial State
        if (state.results.isEmpty() && !state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(AppBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PersonSearch,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = PinkPrimary.copy(alpha = 0.5f)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "Cherche par pseudo",
                    color = TextDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    "Tes futurs amis t'attendent !",
                    color = TextGray,
                    fontSize = 14.sp
                )
            }
        }

        // Results
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.results, key = { it.id }) { user ->
                FriendRow(
                    user = user,
                    onAdd = { onAddFriend(user) }
                )
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
private fun FriendRow(user: UserUi, onAdd: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, BorderSoft)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AppBg),
                contentAlignment = Alignment.Center
            ) {
                Text("👤", fontSize = 20.sp)
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
                    "@${user.username}",
                    color = TextGray,
                    fontSize = 13.sp
                )
            }

            if (user.isFriend) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Added",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Button(
                    onClick = onAdd,
                    colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Ajouter", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
