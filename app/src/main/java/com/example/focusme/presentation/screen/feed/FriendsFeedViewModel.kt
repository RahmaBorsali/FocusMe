package com.example.focusme.presentation.screen.feed

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusme.data.repository.SocialRepository
import com.example.focusme.presentation.model.FeedItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FriendsFeedUiState(
    val items: List<FeedItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FriendsFeedViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SocialRepository(application)
    private val _uiState = MutableStateFlow(FriendsFeedUiState())
    val uiState: StateFlow<FriendsFeedUiState> = _uiState

    fun loadFeed(userId: String) {
        if (userId.isBlank()) return
        
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            repository.getFriendsFeed(userId)
                .onSuccess { dtoItems ->
                    val uiItems = dtoItems.map { dto ->
                        FeedItem(
                            friendId = dto.friendId,
                            friendName = dto.friendName,
                            avatarUrl = dto.avatarUrl ?: "",
                            actionType = dto.actionType,
                            value = dto.value,
                            message = dto.message,
                            timestamp = dto.timestamp
                        )
                    }
                    _uiState.update { it.copy(items = uiItems, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message ?: "Erreur inconnue", isLoading = false) }
                }
        }
    }
}
