package com.example.focusme.presentation.screen.feed

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusme.data.repository.FriendsRepository
import com.example.focusme.presentation.model.FriendStatus
import com.example.focusme.presentation.model.UserUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FindFriendsUiState(
    val query: String = "",
    val friends: List<UserUi> = emptyList(),        // ✅ mes amis actuels
    val suggestions: List<UserUi> = emptyList(),    // ✅ users proposés
    val results: List<UserUi> = emptyList(),        // ✅ résultats de search (si query)
    val isLoading: Boolean = false,
    val error: String? = null
)

class FindFriendsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = FriendsRepository(app)

    private val _uiState = MutableStateFlow(FindFriendsUiState())
    val uiState: StateFlow<FindFriendsUiState> = _uiState.asStateFlow()

    // Flow for the main FeedScreen (kept for compatibility or if used elsewhere)
    private val _friendsFlow = MutableStateFlow<List<UserUi>>(emptyList())
    val friendsFlow: StateFlow<List<UserUi>> = _friendsFlow.asStateFlow()

    private val _incomingRequests = MutableStateFlow<Map<String, String>>(emptyMap()) // userId -> requestId
    private val _pendingIds = MutableStateFlow<Set<String>>(emptySet()) // outgoing toUserIds

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val friendsRes = repo.friends()
            val suggestionsRes = repo.suggestions()
            val outgoingRes = repo.outgoing()
            val incomingRes = repo.incoming()

            val pendingIds = outgoingRes.getOrDefault(emptyList()).mapNotNull { it.toUser?.id }.toSet()
            _pendingIds.value = pendingIds

            val incomingMap = incomingRes.getOrDefault(emptyList()).associate { it.fromUser?.id.orEmpty() to it.requestId }
            _incomingRequests.value = incomingMap

            val friendsList = friendsRes.getOrDefault(emptyList()).map {
                UserUi(
                    id = it.id, 
                    name = it.username, 
                    username = it.username.replace(" ", "").lowercase(), 
                    status = FriendStatus.FRIEND
                )
            }
            val friendIds = friendsList.map { it.id }.toSet()

            val suggestionsList = suggestionsRes.getOrDefault(emptyList())
                .filter { !friendIds.contains(it.id) }
                .map {
                    val id = it.id
                    val status = when {
                        pendingIds.contains(id) -> FriendStatus.OUTGOING_PENDING
                        incomingMap.containsKey(id) -> FriendStatus.INCOMING_PENDING
                        else -> FriendStatus.NONE
                    }
                    UserUi(
                        id = id, 
                        name = it.username, 
                        username = it.username.replace(" ", "").lowercase(), 
                        status = status,
                        incomingRequestId = incomingMap[id]
                    )
                }

            _uiState.value = _uiState.value.copy(
                friends = friendsList,
                suggestions = suggestionsList,
                isLoading = false
            )
            _friendsFlow.value = friendsList
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
                    val friendIds = _uiState.value.friends.map { it.id }.toSet()
                    val pendingIds = _pendingIds.value
                    val incomingMap = _incomingRequests.value

                    val mapped = users.map { u ->
                        val id = u.id
                        val status = when {
                            friendIds.contains(id) -> FriendStatus.FRIEND
                            pendingIds.contains(id) -> FriendStatus.OUTGOING_PENDING
                            incomingMap.containsKey(id) -> FriendStatus.INCOMING_PENDING
                            else -> FriendStatus.NONE
                        }

                        UserUi(
                            id = id,
                            name = u.username,
                            username = u.username.replace(" ", "").lowercase(),
                            status = status,
                            incomingRequestId = incomingMap[id]
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
                    _pendingIds.value = _pendingIds.value + user.id
                    refreshAll() // Simplified: refresh to get full truth
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message ?: "Erreur envoi demande")
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

    fun removeFriend(friendId: String) {
        viewModelScope.launch {
            repo.deleteFriend(friendId)
                .onSuccess { refreshAll() }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message ?: "Erreur suppression")
                }
        }
    }
}
