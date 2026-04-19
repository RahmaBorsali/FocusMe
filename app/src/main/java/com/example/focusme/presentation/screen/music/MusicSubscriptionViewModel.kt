package com.example.focusme.presentation.screen.music

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusme.data.api.MusicPackDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SubscriptionUiState(
    val loading: Boolean = false,
    val packs: List<MusicPackDto> = emptyList(),
    val userSubscriptions: List<String> = emptyList(),
    val error: String? = null
)

class MusicSubscriptionViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = MusicRepository(app)
    
    private val _ui = MutableStateFlow(SubscriptionUiState())
    val ui = _ui.asStateFlow()

    init {
        loadPacks()
    }

    fun loadPacks() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true) }
            val packs = repo.fetchAvailablePacks()
            _ui.update { it.copy(loading = false, packs = packs) }
            
            // On rafraîchit aussi les pistes pour être sûr
            repo.fetchMyTracks() 
        }
    }

    fun toggleSubscription(packId: String, currentSubscribed: Boolean) {
        viewModelScope.launch {
            repo.updateSubscription(packId, !currentSubscribed)
            loadPacks() // Recharger pour voir le changement
        }
    }
}
