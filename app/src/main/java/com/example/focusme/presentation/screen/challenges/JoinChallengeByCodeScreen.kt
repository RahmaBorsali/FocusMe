package com.example.focusme.presentation.screen.challenges

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.focusme.presentation.ui.components.SoftCard
import com.example.focusme.presentation.ui.theme.PinkPrimary
import com.example.focusme.presentation.ui.theme.TextGray

@Composable
fun JoinChallengeByCodeScreen(
    onBack: () -> Unit,
    onOpenChallenge: (String) -> Unit,
    vm: JoinByCodeViewModel = viewModel()
) {
    val ui by vm.uiState.collectAsState()

    ChallengeScreenContainer(
        title = "Rejoindre avec un code",
        subtitle = "Entre le code partage par un ami, retrouve le bon defi, puis rejoins-le en un geste.",
        actions = { OutlinedButton(onClick = onBack) { Text("Retour") } }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                JoinByCodeFormCard(
                    code = ui.code,
                    isLoading = ui.lookupState == JoinByCodeLookupState.Loading,
                    onCodeChange = vm::updateCode,
                    onFind = vm::lookupChallenge
                )
            }
            item {
                when (val state = ui.lookupState) {
                    JoinByCodeLookupState.Idle -> Unit
                    JoinByCodeLookupState.Loading -> JoinByCodeLoadingCard()
                    is JoinByCodeLookupState.Error -> {
                        StateInfoCard(
                            title = state.title,
                            message = state.message,
                            icon = Icons.Default.Search
                        )
                    }
                    is JoinByCodeLookupState.Preview -> {
                        JoinByCodePreviewCard(
                            challenge = state.challenge,
                            availability = state.availability,
                            isJoining = ui.isJoining,
                            onPrimaryAction = { vm.joinOrOpenPreviewedChallenge(onOpenChallenge) }
                        )
                    }
                }
            }
            if (!ui.actionError.isNullOrBlank()) {
                item {
                    ErrorCard(message = ui.actionError!!)
                }
            }
        }
    }
}

@Composable
private fun JoinByCodeFormCard(
    code: String,
    isLoading: Boolean,
    onCodeChange: (String) -> Unit,
    onFind: () -> Unit
) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            JoinCodeStepBadge(number = "1", label = "Saisis le code")
            Spacer(Modifier.height(12.dp))
            Text(
                "Trouve le bon defi",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Colle le code partage par ton ami. On verifie d'abord le challenge avant de te laisser le rejoindre.",
                color = TextGray,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = code,
                onValueChange = onCodeChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Code du challenge") },
                placeholder = { Text("Ex: FOCUS8") },
                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PinkPrimary,
                    focusedLabelColor = PinkPrimary,
                    focusedLeadingIconColor = PinkPrimary
                )
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Le code est visible uniquement pour le proprietaire du challenge, qui peut ensuite le partager a ses amis.",
                color = TextGray,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(14.dp))
            PrimaryChallengeButton(
                text = if (isLoading) "Recherche..." else "Trouver le challenge",
                enabled = !isLoading,
                onClick = onFind,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun JoinByCodeLoadingCard() {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = PinkPrimary)
            Spacer(Modifier.height(14.dp))
            Text(
                "Recherche du challenge...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "On verifie le code et on prepare un apercu clair avant le join.",
                color = TextGray,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun JoinCodeStepBadge(number: String, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(PinkPrimary.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = PinkPrimary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Text(
            text = label,
            color = PinkPrimary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
