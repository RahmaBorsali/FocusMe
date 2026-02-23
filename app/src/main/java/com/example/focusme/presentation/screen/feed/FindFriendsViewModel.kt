package com.example.focusme.presentation.screen.feed

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusme.data.repository.FriendsRepository
import com.example.focusme.presentation.model.UserUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FindFriendsUiState(
    val query: String = "",
    val results: List<UserUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FindFriendsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = FriendsRepository(app)

    private val _uiState = MutableStateFlow(FindFriendsUiState())
    val uiState: StateFlow<FindFriendsUiState> = _uiState.asStateFlow()

    private val _friendsFlow = MutableStateFlow<List<UserUi>>(emptyList())
    val friendsFlow: StateFlow<List<UserUi>> = _friendsFlow.asStateFlow()

    // Pour désactiver le bouton "add" après envoi
    private val sentRequests = mutableSetOf<String>() // userId

    init {
        refreshFriends()
    }

    fun refreshFriends() {
        viewModelScope.launch {
            repo.friends()
                .onSuccess { list ->
                    _friendsFlow.value = list.map {
                        UserUi(
                            id = it.id,
                            name = it.username, // backend: username = "Rahma Borsali"
                            username = it.username.replace(" ", "").lowercase()
                        )
                    }
                }
        }
    }

    fun onQueryChange(q: String) {
        _uiState.value = _uiState.value.copy(query = q, error = null)
        if (q.isBlank()) {
            _uiState.value = _uiState.value.copy(results = emptyList(), isLoading = false)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repo.search(q)
                .onSuccess { users ->
                    val friendIds = _friendsFlow.value.map { it.id }.toSet()

                    val mapped = users.map { u ->
                        val id = u.id
                        val isFriend = friendIds.contains(id) || sentRequests.contains(id)

                        UserUi(
                            id = id,
                            name = u.username,
                            username = u.username.replace(" ", "").lowercase(),
                            isFriend = isFriend
                        )
                    }
                    _uiState.value = _uiState.value.copy(results = mapped, isLoading = false)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Erreur")
                }
        }
    }

    fun addFriend(user: UserUi) {
        viewModelScope.launch {
            repo.sendRequest(user.id)
                .onSuccess {
                    sentRequests.add(user.id)
                    // maj UI results pour disable
                    _uiState.value = _uiState.value.copy(
                        results = _uiState.value.results.map {
                            if (it.id == user.id) it.copy(isFriend = true) else it
                        }
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message ?: "Erreur envoi demande")
                }
        }
    }

    fun removeFriend(friendId: String) {
        viewModelScope.launch {
            repo.deleteFriend(friendId)
                .onSuccess { refreshFriends() }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message ?: "Erreur suppression")
                }
        }
    }
}