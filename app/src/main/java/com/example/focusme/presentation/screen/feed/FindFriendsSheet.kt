package com.example.focusme.presentation.screen.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.focusme.presentation.ui.theme.PinkPrimary
import com.example.focusme.presentation.ui.theme.TextDark
import com.example.focusme.presentation.ui.theme.TextGray
import kotlinx.coroutines.delay

@Composable
fun FindFriendsSheet(
    state: FindFriendsUiState,
    onQueryChange: (String) -> Unit,
    onAddFriend: (UserDto) -> Unit,
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
            .background(Color(0xFFFBEAF2))
            .padding(bottom = 18.dp)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(PinkPrimary, PinkPrimary.copy(alpha = 0.85f))
                    )
                )
                .padding(horizontal = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔎", color = Color.White)

                Spacer(Modifier.width(10.dp))

                Text(
                    text = "Trouver des amis",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = localQuery,
            onValueChange = { localQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            placeholder = { Text("Rechercher par nom d'utilisateur...", color = TextGray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextGray) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = PinkPrimary
            )
        )

        Spacer(Modifier.height(14.dp))

        if (state.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                color = PinkPrimary
            )
            Spacer(Modifier.height(10.dp))
        }

        // Hint
        if (state.results.isEmpty() && localQuery.isBlank()) {
            Spacer(Modifier.height(20.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🔍", style = MaterialTheme.typography.displaySmall)
                Spacer(Modifier.height(10.dp))
                Text("Rechercher des amis", color = TextDark, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Saisissez le nom d'utilisateur ou le nom affiché",
                    color = TextGray
                )
            }
            return
        }

        // Results
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
        ) {
            items(state.results, key = { it.id }) { user ->
                FriendRow(
                    user = user,
                    onAdd = { onAddFriend(user) }
                )
                Spacer(Modifier.height(10.dp))
            }
        }

        if (state.error != null) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = state.error,
                color = Color(0xFFD32F2F),
                modifier = Modifier.padding(horizontal = 18.dp)
            )
        }
    }
}

@Composable
private fun FriendRow(user: UserDto, onAdd: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
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
        ) {
            Text("👤")
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(user.name, color = TextDark, fontWeight = FontWeight.Bold)
            Text("@${user.username}", color = TextGray)
        }

        IconButton(onClick = onAdd, enabled = !user.isFriend) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = "Add",
                tint = if (user.isFriend) Color(0xFF9AA0A6) else PinkPrimary
            )
        }
    }
}
