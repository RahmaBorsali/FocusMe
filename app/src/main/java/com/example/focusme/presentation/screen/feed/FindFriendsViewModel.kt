package com.example.focusme.presentation.screen.feed

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusme.data.local.DbProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FindFriendsUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<UserDto> = emptyList(),
    val error: String? = null
)

class FindFriendsViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = DbProvider.db(app).friendsDao()
    private val repo = FriendsRepository(dao)

    // ✅ amis enregistrés Room
    val friendsFlow = repo.observeFriends()

    private val _uiState = MutableStateFlow(FindFriendsUiState())
    val uiState: StateFlow<FindFriendsUiState> = _uiState

    private var job: Job? = null

    fun onQueryChange(q: String) {
        _uiState.update { it.copy(query = q, error = null) }

        job?.cancel()

        val trimmed = q.trim()
        if (trimmed.length < 2) {
            _uiState.update { it.copy(results = emptyList(), isLoading = false) }
            return
        }

        job = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            runCatching { repo.searchUsers(trimmed) }
                .onSuccess { res ->
                    _uiState.update { st -> st.copy(results = res, isLoading = false) }
                }
                .onFailure {
                    _uiState.update { st -> st.copy(isLoading = false, error = "Erreur") }
                }
        }
    }

    fun addFriend(user: UserDto) {
        viewModelScope.launch {
            runCatching { repo.addFriend(user) }
                .onSuccess {
                    // ✅ Rafraîchir la liste de recherche (marquer isFriend = true)
                    _uiState.update { st ->
                        st.copy(
                            results = st.results.map {
                                if (it.id == user.id) it.copy(isFriend = true) else it
                            }
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(error = "Impossible d'ajouter l'ami") }
                }
        }
    }
    fun removeFriend(id: Int) {
        viewModelScope.launch {
            runCatching { repo.removeFriend(id) }
                .onFailure { _uiState.update { it.copy(error = "Impossible de supprimer l'ami") } }
        }
    }


    fun clearError() = _uiState.update { it.copy(error = null) }
}
