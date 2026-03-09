package com.example.focusme.presentation.screen.social

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusme.data.api.dto.FriendStats
import com.example.focusme.data.repository.SocialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LeaderboardUiState(
    val items: List<FriendStats> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUserId: String = ""
)

class LeaderboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SocialRepository(application)
    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState

    fun loadLeaderboard(userId: String) {
        _uiState.update { it.copy(isLoading = true, error = null, currentUserId = userId) }
        viewModelScope.launch {
            repository.getFriendsStats(userId)
                .onSuccess { items ->
                    // Backend already provides sorted list with ranks usually, 
                    // but we re-sort to be sure and ensure ranks match index
                    val sorted = items.sortedByDescending { it.weeklyFocusMin }
                    _uiState.update { it.copy(items = sorted, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }
}
