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
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusme.data.repository.ChallengeMessage
import com.example.focusme.presentation.ui.components.SoftCard
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.ui.platform.LocalContext
import com.example.focusme.data.repository.ChallengeAttachment
import com.example.focusme.presentation.ui.theme.AppBg
import com.example.focusme.presentation.ui.theme.PinkPrimary
import com.example.focusme.presentation.ui.theme.TextDark
import com.example.focusme.presentation.ui.theme.TextGray
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size

@Composable
fun ChallengeChatScreen(
    id: String,
    onBack: () -> Unit,
    vm: ChallengeChatViewModel = viewModel()
) {
    val ui by vm.uiState.collectAsState()
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val file = uriToFile(context, it)
            if (file != null) vm.selectFile(file)
        }
    }

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
                ui.selectedFile?.let { file ->
                    SoftCard(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        padding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        containerColor = PinkPrimary.copy(alpha = 0.1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = PinkPrimary, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(file.name, style = MaterialTheme.typography.bodyMedium, color = Color.Black, modifier = Modifier.weight(1f), maxLines = 1)
                            IconButton(onClick = vm::clearAttachment) {
                                Icon(Icons.Default.Close, contentDescription = "Supprimer", tint = TextGray)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    IconButton(onClick = { filePickerLauncher.launch(arrayOf("*/*")) }) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Joindre", tint = TextGray)
                    }
                    OutlinedTextField(
                        value = ui.composer,
                        onValueChange = vm::updateComposer,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ex: J'ai fini mon bloc de 50 min") },
                        minLines = 1,
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
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White)
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
                    
                    message.attachment?.let { attachment ->
                        AttachmentView(attachment = attachment)
                        if (!message.text.isNullOrBlank()) Spacer(Modifier.height(8.dp))
                    }
                    
                    if (!message.text.isNullOrBlank()) {
                        Text(message.text, style = MaterialTheme.typography.bodyMedium)
                    }
                }
        }
    }
}

@Composable
private fun AttachmentView(
    attachment: ChallengeAttachment
) {
    val context = LocalContext.current
    val isImage = attachment.type.startsWith("image/", ignoreCase = true)

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AppBg)
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
                tint = PinkPrimary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    text = attachment.fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextDark,
                    maxLines = 1
                )
                Text(
                    text = "${attachment.fileSize / 1024} KB",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Télécharger",
                tint = TextGray,
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
