package com.example.focusme.presentation.screen.challenges

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusme.data.repository.ChallengeMessage
import com.example.focusme.presentation.ui.components.SoftCard
import com.example.focusme.presentation.ui.theme.PinkPrimary
import com.example.focusme.presentation.ui.theme.TextGray
import androidx.compose.ui.unit.dp

@Composable
fun ChallengeChatScreen(
    id: String,
    onBack: () -> Unit,
    vm: ChallengeChatViewModel = viewModel()
) {
    val ui by vm.uiState.collectAsState()
    LaunchedEffect(id) { vm.load(id) }

    ChallengeScreenContainer(
        title = "Chat du challenge",
        subtitle = "Encouragements, updates et petits messages rapides entre amis.",
        actions = { OutlinedButton(onClick = onBack) { Text("Retour") } }
    ) {
        Column(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
            when (val state = ui.state) {
                ContentState.Loading -> FullScreenLoading()
                is ContentState.Error -> ErrorCard(message = state.message, onRetry = { vm.load(id) })
                is ContentState.Success -> {
                    if (state.data.isEmpty()) {
                        EmptyChallengesCard(title = "Le chat est silencieux", subtitle = "Envoie le premier message pour lancer l'energie du groupe.")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = androidx.compose.ui.Modifier.weight(1f)
                        ) {
                            items(state.data, key = { it.id }) { message -> MessageBubble(message) }
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            SoftCard(modifier = androidx.compose.ui.Modifier.fillMaxWidth()) {
                Text("Nouveau message", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = ui.composer,
                        onValueChange = vm::updateComposer,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ex: J'ai fini mon bloc de 50 min") },
                        minLines = 2,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(PinkPrimary)
                            .clickable(enabled = !ui.isSending) { vm.sendMessage(id) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (ui.isSending) {
                            Text("...", color = Color.White, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                        }
                    }
                }
                if (ui.actionError != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(ui.actionError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChallengeMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Avatar(url = message.avatarUrl, fallback = message.username.take(1))
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(message.username, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text(message.createdAt.toReadableDateTime(), color = TextGray, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(8.dp))
                Text(message.text, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
