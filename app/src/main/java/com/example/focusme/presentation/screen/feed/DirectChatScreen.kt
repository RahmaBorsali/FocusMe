package com.example.focusme.presentation.screen.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.example.focusme.data.repository.ChatAttachment
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
    val context = LocalContext.current
    
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val file = uriToFile(context, it)
            if (file != null) {
                vm.selectFile(file)
            }
        }
    }
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

        ui.selectedFile?.let { file ->
            SoftCard(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                padding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                containerColor = PinkPrimary.copy(alpha = 0.1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = PinkPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextDark,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    IconButton(onClick = vm::clearAttachment) {
                        Icon(Icons.Default.Close, contentDescription = "Supprimer", tint = TextGray)
                    }
                }
            }
        }

        SoftCard(
            modifier = Modifier.fillMaxWidth(),
            padding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { 
                    filePickerLauncher.launch(arrayOf("*/*")) 
                }) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Joindre un fichier",
                        tint = TextGray
                    )
                }
                
                OutlinedTextField(
                    value = ui.composer,
                    onValueChange = vm::updateComposer,
                    modifier = Modifier.weight(1f),
                    minLines = 1,
                    maxLines = 5,
                    placeholder = { Text("Ecris un message...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
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
            }

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
                    Column {
                        message.attachment?.let { attachment ->
                            AttachmentView(attachment = attachment, isMine = isMine)
                            if (!message.text.isNullOrBlank()) {
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                        if (!message.text.isNullOrBlank()) {
                            Text(
                                text = message.text,
                                color = if (isMine) Color.White else TextDark,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
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

@Composable
private fun AttachmentView(
    attachment: ChatAttachment,
    isMine: Boolean
) {
    val context = LocalContext.current
    val isImage = attachment.type.startsWith("image/", ignoreCase = true)

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isMine) Color.Black.copy(alpha = 0.1f) else AppBg)
            .padding(8.dp)
            .clickable {
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(attachment.url))
                    context.startActivity(intent)
                } catch (e: Exception) { }
            }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isImage) Icons.Default.Image else Icons.Default.Description,
                contentDescription = null,
                tint = if (isMine) Color.White else PinkPrimary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = attachment.fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isMine) Color.White else TextDark,
                    maxLines = 1
                )
                Text(
                    text = "${attachment.fileSize / 1024} KB",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isMine) Color.White.copy(alpha = 0.7f) else TextGray
                )
            }
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Télécharger",
                tint = if (isMine) Color.White else TextGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun uriToFile(context: android.content.Context, uri: android.net.Uri): java.io.File? {
    return runCatching {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        } ?: "upload_${System.currentTimeMillis()}"

        val file = java.io.File(context.cacheDir, fileName)
        file.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        file
    }.getOrNull()
}
