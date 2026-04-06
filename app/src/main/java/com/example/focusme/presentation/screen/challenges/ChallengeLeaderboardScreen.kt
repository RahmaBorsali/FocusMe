package com.example.focusme.presentation.screen.challenges

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.unit.dp

@Composable
fun ChallengeLeaderboardScreen(
    id: String,
    onBack: () -> Unit,
    vm: ChallengeLeaderboardViewModel = viewModel()
) {
    val ui by vm.uiState.collectAsState()
    LaunchedEffect(id) { vm.load(id) }

    ChallengeScreenContainer(
        title = "Classement",
        subtitle = "Un leaderboard lisible, motive et construit pour donner envie de relancer une session.",
        actions = { OutlinedButton(onClick = onBack) { Text("Retour") } }
    ) {
        when (val state = ui.state) {
            ContentState.Loading -> FullScreenLoading()
            is ContentState.Error -> ErrorCard(message = state.message, onRetry = { vm.load(id) })
            is ContentState.Success -> {
                if (state.data.isEmpty()) {
                    EmptyChallengesCard(title = "Classement vide", subtitle = "Le leaderboard apparaitra des que le challenge aura ses premiers participants.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                        items(state.data, key = { it.userId }) { entry ->
                            LeaderboardRow(entry = entry)
                        }
                    }
                }
            }
        }
    }
}
