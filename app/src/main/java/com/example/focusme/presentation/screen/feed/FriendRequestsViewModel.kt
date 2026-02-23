package com.example.focusme.presentation.screen.feed

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusme.data.repository.FriendsRepository
import com.example.focusme.data.api.dto.IncomingRequestItem
import com.example.focusme.data.api.dto.OutgoingRequestItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FriendRequestsUiState(
    val incoming: List<IncomingRequestItem> = emptyList(),
    val outgoing: List<OutgoingRequestItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FriendRequestsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = FriendsRepository(app)

    private val _uiState = MutableStateFlow(FriendRequestsUiState())
    val uiState: StateFlow<FriendRequestsUiState> = _uiState.asStateFlow()

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val incomingRes = repo.incoming()
            val outgoingRes = repo.outgoing()

            if (incomingRes.isSuccess && outgoingRes.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    incoming = incomingRes.getOrDefault(emptyList()),
                    outgoing = outgoingRes.getOrDefault(emptyList()),
                    isLoading = false
                )
            } else {
                val err = incomingRes.exceptionOrNull()?.message ?: outgoingRes.exceptionOrNull()?.message
                _uiState.value = _uiState.value.copy(isLoading = false, error = err ?: "Erreur de chargement")
            }
        }
    }

    fun acceptRequest(requestId: String) {
        viewModelScope.launch {
            repo.accept(requestId)
                .onSuccess { refreshAll() }
                .onFailure { e -> _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }

    fun rejectRequest(requestId: String) {
        viewModelScope.launch {
            repo.reject(requestId)
                .onSuccess { refreshAll() }
                .onFailure { e -> _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }
}
