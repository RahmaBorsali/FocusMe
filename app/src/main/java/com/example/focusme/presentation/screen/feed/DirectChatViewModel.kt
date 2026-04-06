package com.example.focusme.presentation.screen.feed

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusme.data.local.TokenStore
import com.example.focusme.data.repository.ChatRepository
import com.example.focusme.data.repository.DirectChatMessage
import com.example.focusme.presentation.screen.challenges.ContentState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class DirectChatUiState(
    val friendId: String = "",
    val friendName: String = "",
    val friendUsername: String = "",
    val friendAvatarUrl: String = "",
    val conversationId: String? = null,
    val currentUserId: String = "",
    val state: ContentState<List<DirectChatMessage>> = ContentState.Loading,
    val composer: String = "",
    val isSending: Boolean = false,
    val actionError: String? = null
)

class DirectChatViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ChatRepository(app)
    private val currentUserId = TokenStore(app).getUserIdBlocking().orEmpty()

    private val _uiState = MutableStateFlow(
        DirectChatUiState(currentUserId = currentUserId)
    )
    val uiState: StateFlow<DirectChatUiState> = _uiState.asStateFlow()

    fun updateComposer(value: String) {
        _uiState.update { it.copy(composer = value, actionError = null) }
    }

    fun openConversation(
        friendId: String,
        fallbackName: String,
        fallbackUsername: String
    ) {
        if (friendId.isBlank()) {
            _uiState.update {
                it.copy(
                    state = ContentState.Error("Ami introuvable."),
                    actionError = "Impossible d'ouvrir cette conversation."
                )
            }
            return
        }

        if (
            uiState.value.friendId == friendId &&
            uiState.value.conversationId != null &&
            uiState.value.state !is ContentState.Error
        ) {
            refreshMessagesSilently()
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    friendId = friendId,
                    friendName = fallbackName,
                    friendUsername = fallbackUsername,
                    friendAvatarUrl = "",
                    conversationId = null,
                    state = ContentState.Loading,
                    actionError = null
                )
            }

            runCatching { repo.getOrCreateConversation(friendId) }
                .onSuccess { conversation ->
                    _uiState.update {
                        it.copy(
                            conversationId = conversation.id,
                            friendName = conversation.peer.username.ifBlank { fallbackName.ifBlank { "Ami" } },
                            friendUsername = conversation.peer.username.ifBlank { fallbackUsername },
                            friendAvatarUrl = conversation.peer.avatarUrl
                        )
                    }
                    loadMessages(refresh = false)
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            state = ContentState.Error(throwable.toDirectChatMessage()),
                            actionError = throwable.toDirectChatMessage()
                        )
                    }
                }
        }
    }

    fun loadMessages(refresh: Boolean = false) {
        val conversationId = uiState.value.conversationId ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    state = if (!refresh) ContentState.Loading else it.state,
                    actionError = null
                )
            }

            runCatching { repo.getMessages(conversationId) }
                .onSuccess { messages ->
                    _uiState.update {
                        it.copy(
                            state = ContentState.Success(messages),
                            actionError = null
                        )
                    }
                    runCatching { repo.markConversationRead(conversationId) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            state = ContentState.Error(throwable.toDirectChatMessage()),
                            actionError = throwable.toDirectChatMessage()
                        )
                    }
                }
        }
    }

    fun refreshMessagesSilently() {
        val conversationId = uiState.value.conversationId ?: return
        viewModelScope.launch {
            runCatching { repo.getMessages(conversationId) }
                .onSuccess { messages ->
                    _uiState.update { it.copy(state = ContentState.Success(messages)) }
                    runCatching { repo.markConversationRead(conversationId) }
                }
        }
    }

    fun sendMessage() {
        val conversationId = uiState.value.conversationId ?: return
        val text = uiState.value.composer.trim()
        if (text.isBlank()) {
            _uiState.update { it.copy(actionError = "Ecris un message avant d'envoyer.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, actionError = null) }
            runCatching { repo.sendMessage(conversationId, text) }
                .onSuccess {
                    _uiState.update { it.copy(isSending = false, composer = "") }
                    loadMessages(refresh = true)
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            actionError = throwable.toDirectChatMessage()
                        )
                    }
                }
        }
    }
}

private fun Throwable.toDirectChatMessage(): String = when (this) {
    is HttpException -> when (code()) {
        400 -> "La conversation n'a pas pu etre ouverte."
        401 -> "Ta session a expire. Reconnecte-toi."
        403 -> "Tu ne peux pas chatter avec cet ami."
        404 -> "Conversation introuvable."
        else -> "Impossible de charger le chat pour le moment."
    }
    else -> {
        val raw = message.orEmpty()
        when {
            raw.contains("UNAUTHORIZED", ignoreCase = true) ->
                "Ta session a expire. Reconnecte-toi."
            raw.contains("CHAT_ONLY_AVAILABLE_WITH_FRIENDS", ignoreCase = true) ->
                "Le chat est disponible uniquement entre amis."
            raw.contains("CONVERSATION_NOT_FOUND", ignoreCase = true) ||
                raw.contains("INVALID_CONVERSATION", ignoreCase = true) ->
                "Conversation introuvable."
            raw.contains("EMPTY_MESSAGE", ignoreCase = true) ->
                "Ecris un message avant d'envoyer."
            raw.contains("USER_NOT_FOUND", ignoreCase = true) ||
                raw.contains("INVALID_TARGET_USER", ignoreCase = true) ->
                "Cet ami est introuvable."
            raw.contains("FORBIDDEN", ignoreCase = true) ->
                "Tu ne peux pas chatter avec cet ami."
            raw.isNotBlank() -> raw
            else -> "Une erreur reseau est survenue."
        }
    }
}
