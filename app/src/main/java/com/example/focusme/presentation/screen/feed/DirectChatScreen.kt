package com.example.focusme.presentation.screen.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusme.data.repository.DirectChatMessage
import com.example.focusme.presentation.screen.challenges.Avatar
import com.example.focusme.presentation.screen.challenges.ContentState
import com.example.focusme.presentation.screen.challenges.EmptyChallengesCard
import com.example.focusme.presentation.screen.challenges.ErrorCard
import com.example.focusme.presentation.screen.challenges.toReadableDateTime
import com.example.focusme.presentation.ui.components.SoftCard
import com.example.focusme.presentation.ui.theme.AppBg
import com.example.focusme.presentation.ui.theme.PinkPrimary
import com.example.focusme.presentation.ui.theme.TextDark
import com.example.focusme.presentation.ui.theme.TextGray
import kotlinx.coroutines.delay

@Composable
fun DirectChatScreen(
    friendId: String,
    friendName: String,
    friendUsername: String,
    onBack: () -> Unit,
    vm: DirectChatViewModel = viewModel()
) {
    val ui by vm.uiState.collectAsState()
    val listState = rememberLazyListState()
    val messages = (ui.state as? ContentState.Success)?.data.orEmpty()

    LaunchedEffect(friendId) {
        vm.openConversation(
            friendId = friendId,
            fallbackName = friendName,
            fallbackUsername = friendUsername
        )
    }

    LaunchedEffect(ui.conversationId) {
        ui.conversationId ?: return@LaunchedEffect
        while (true) {
            delay(4000)
            vm.refreshMessagesSilently()
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .imePadding()
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint = TextDark
                )
            }
            Avatar(
                url = ui.friendAvatarUrl.takeIf { it.isNotBlank() },
                fallback = ui.friendName.ifBlank { friendName }.take(1).ifBlank { "A" }
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ui.friendName.ifBlank { friendName.ifBlank { "Ami" } },
                    color = TextDark,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "@${ui.friendUsername.ifBlank { friendUsername.ifBlank { "ami" } }}",
                    color = TextGray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (val state = ui.state) {
                ContentState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PinkPrimary
                    )
                }

                is ContentState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 12.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        ErrorCard(
                            message = state.message,
                            onRetry = { vm.openConversation(friendId, friendName, friendUsername) }
                        )
                    }
                }

                is ContentState.Success -> {
                    if (state.data.isEmpty()) {
                        EmptyChallengesCard(
                            title = "Aucun message pour l'instant",
                            subtitle = "Lance la discussion avec ${ui.friendName.ifBlank { friendName.ifBlank { "cet ami" } }}."
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.data, key = { it.id }) { message ->
                                DirectMessageBubble(
                                    message = message,
                                    isMine = message.sender.id == ui.currentUserId
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        SoftCard(
            modifier = Modifier.fillMaxWidth(),
            padding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
        ) {
            OutlinedTextField(
                value = ui.composer,
                onValueChange = vm::updateComposer,
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 5,
                placeholder = { Text("Ecris un message...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = PinkPrimary,
                    focusedLabelColor = PinkPrimary,
                    focusedTrailingIconColor = PinkPrimary,
                    unfocusedTrailingIconColor = PinkPrimary
                ),
                trailingIcon = {
                    IconButton(
                        onClick = vm::sendMessage,
                        enabled = !ui.isSending
                    ) {
                        if (ui.isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = PinkPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Envoyer",
                                tint = PinkPrimary
                            )
                        }
                    }
                }
            )

            if (!ui.actionError.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = ui.actionError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}

@Composable
private fun DirectMessageBubble(
    message: DirectChatMessage,
    isMine: Boolean
) {
    val metaText = buildString {
        append(message.createdAt.toReadableDateTime())
        if (isMine && !message.readAt.isNullOrBlank()) {
            append(" | Lu")
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!isMine) {
                Avatar(
                    url = message.sender.avatarUrl.takeIf { it.isNotBlank() },
                    fallback = message.sender.username.take(1).ifBlank { "A" },
                    modifier = Modifier.size(34.dp)
                )
            }

            Column(
                horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
            ) {
                Box(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                topStart = 18.dp,
                                topEnd = 18.dp,
                                bottomStart = if (isMine) 18.dp else 6.dp,
                                bottomEnd = if (isMine) 6.dp else 18.dp
                            )
                        )
                        .background(if (isMine) PinkPrimary else Color.White)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = message.text,
                        color = if (isMine) Color.White else TextDark,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = metaText,
                    color = TextGray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
