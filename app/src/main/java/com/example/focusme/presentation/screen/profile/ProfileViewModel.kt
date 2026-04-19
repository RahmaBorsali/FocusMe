package com.example.focusme.presentation.screen.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusme.data.repository.ProfileRepository
import com.example.focusme.data.repository.ProfileSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ProfileAccountMode {
    AUTHENTICATED,
    GUEST
}

data class ProfileUiState(
    val accountMode: ProfileAccountMode = ProfileAccountMode.GUEST,
    val displayName: String = "",
    val username: String = "",
    val email: String = "",
    val userId: String? = null,
    val avatarLabel: String = "FM",
    val studyGoal: String = "",
    val notificationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val defaultVisibility: String = "friends",
    val defaultAllowComments: Boolean = true,
    val alarmSound: String = "classic",
    val sessions: List<com.example.focusme.data.local.StudySessionEntity> = emptyList(),
    val totalSessions: Int = 0,
    val totalFocusSeconds: Int = 0,
    val totalXp: Int = 0,
    val longestSessionSeconds: Int = 0,
    val averageSessionSeconds: Int = 0,
    val averageFocusRate: Int = 0,
    val averageSatisfactionRate: Int = 0,
    val bestFocusRate: Int = 0,
    val streakDays: Int = 0,
    val plannedTasksCount: Int = 0,
    val completedTasksCount: Int = 0,
    val subjectsCount: Int = 0,
    val isSavingProfile: Boolean = false,
    val isLoggingOut: Boolean = false,
    val isClearingHistory: Boolean = false,
    val isClearingAllData: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

private data class ProfileOperationState(
    val isSavingProfile: Boolean = false,
    val isLoggingOut: Boolean = false,
    val isClearingHistory: Boolean = false,
    val isClearingAllData: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class ProfileViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ProfileRepository(app.applicationContext)
    private val operationState = MutableStateFlow(ProfileOperationState())

    init {
        viewModelScope.launch {
            runCatching { repo.refreshRemoteProfile() }
        }
    }

    val uiState: StateFlow<ProfileUiState> =
        combine(repo.observeProfile(), operationState) { snapshot, operations ->
            snapshot.toUiState(operations)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProfileUiState()
        )

    fun saveProfile(displayName: String, studyGoal: String) {
        viewModelScope.launch {
            val trimmedName = displayName.trim()
            val trimmedGoal = studyGoal.trim()

            if (trimmedName.isBlank()) {
                operationState.value = operationState.value.copy(error = "Le nom du profil ne peut pas etre vide.")
                return@launch
            }

            operationState.value = operationState.value.copy(
                isSavingProfile = true,
                error = null,
                message = null
            )

            runCatching {
                repo.saveProfile(trimmedName, trimmedGoal)
            }.onSuccess {
                operationState.value = operationState.value.copy(
                    isSavingProfile = false,
                    message = "Profil mis a jour."
                )
            }.onFailure { error ->
                operationState.value = operationState.value.copy(
                    isSavingProfile = false,
                    error = error.message ?: "Impossible de sauvegarder le profil."
                )
            }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repo.setNotificationsEnabled(enabled)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repo.setSoundEnabled(enabled)
        }
    }

    fun setDefaultVisibility(visibility: String) {
        viewModelScope.launch {
            repo.setDefaultVisibility(visibility)
        }
    }

    fun setDefaultAllowComments(enabled: Boolean) {
        viewModelScope.launch {
            repo.setDefaultAllowComments(enabled)
        }
    }

    fun setAlarmSound(sound: String) {
        viewModelScope.launch {
            repo.setAlarmSound(sound)
        }
    }

    fun deleteSession(id: Long) {
        viewModelScope.launch {
            runCatching {
                repo.deleteSession(id)
            }.onFailure { error ->
                operationState.value = operationState.value.copy(
                    error = error.message ?: "Impossible de supprimer la session."
                )
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            operationState.value = operationState.value.copy(
                isClearingHistory = true,
                error = null,
                message = null
            )

            runCatching {
                repo.clearHistory()
            }.onSuccess {
                operationState.value = operationState.value.copy(
                    isClearingHistory = false,
                    message = "Historique supprime."
                )
            }.onFailure { error ->
                operationState.value = operationState.value.copy(
                    isClearingHistory = false,
                    error = error.message ?: "Impossible d'effacer l'historique."
                )
            }
        }
    }

    fun clearAllLocalData() {
        viewModelScope.launch {
            operationState.value = operationState.value.copy(
                isClearingAllData = true,
                error = null,
                message = null
            )

            runCatching {
                repo.clearAllLocalData()
            }.onSuccess {
                operationState.value = operationState.value.copy(
                    isClearingAllData = false,
                    message = "Toutes les donnees locales ont ete effacees."
                )
            }.onFailure { error ->
                operationState.value = operationState.value.copy(
                    isClearingAllData = false,
                    error = error.message ?: "Impossible d'effacer les donnees locales."
                )
            }
        }
    }

    fun deleteAccount(onDone: () -> Unit) {
        viewModelScope.launch {
            operationState.value = operationState.value.copy(
                isClearingAllData = true,
                error = null,
                message = null
            )

            runCatching {
                repo.deleteAccount()
            }.onSuccess {
                operationState.value = operationState.value.copy(
                    isClearingAllData = false
                )
                onDone()
            }.onFailure { error ->
                operationState.value = operationState.value.copy(
                    isClearingAllData = false,
                    error = error.message ?: "Impossible de supprimer le compte."
                )
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            operationState.value = operationState.value.copy(
                isLoggingOut = true,
                error = null,
                message = null
            )

            runCatching {
                repo.logout()
            }.onSuccess {
                operationState.value = operationState.value.copy(
                    isLoggingOut = false,
                    message = null
                )
                onDone()
            }.onFailure { error ->
                operationState.value = operationState.value.copy(
                    isLoggingOut = false,
                    error = error.message ?: "Impossible de se deconnecter."
                )
            }
        }
    }

    fun clearMessage() {
        operationState.value = operationState.value.copy(message = null)
    }

    fun clearError() {
        operationState.value = operationState.value.copy(error = null)
    }

    private fun ProfileSnapshot.toUiState(operations: ProfileOperationState): ProfileUiState {
        val accountMode = if (session.isAuthenticated) {
            ProfileAccountMode.AUTHENTICATED
        } else {
            ProfileAccountMode.GUEST
        }

        val emailLabel = when {
            session.email.isNotBlank() -> session.email
            session.isGuest -> "Mode invite"
            else -> "Compte local"
        }

        return ProfileUiState(
            accountMode = accountMode,
            displayName = session.resolvedDisplayName,
            username = session.username,
            email = emailLabel,
            userId = session.userId,
            avatarLabel = session.resolvedInitials,
            studyGoal = session.studyGoal,
            notificationsEnabled = session.notificationsEnabled,
            soundEnabled = session.soundEnabled,
            defaultVisibility = session.defaultVisibility,
            defaultAllowComments = session.defaultAllowComments,
            alarmSound = session.alarmSound,
            sessions = sessions,
            totalSessions = sessions.size,
            totalFocusSeconds = totalFocusSeconds,
            totalXp = totalXp,
            longestSessionSeconds = longestSessionSeconds,
            averageSessionSeconds = averageSessionSeconds,
            averageFocusRate = averageFocusRate,
            averageSatisfactionRate = averageSatisfactionRate,
            bestFocusRate = bestFocusRate,
            streakDays = streakDays,
            plannedTasksCount = plannedTasksCount,
            completedTasksCount = completedTasksCount,
            subjectsCount = subjectsCount,
            isSavingProfile = operations.isSavingProfile,
            isLoggingOut = operations.isLoggingOut,
            isClearingHistory = operations.isClearingHistory,
            isClearingAllData = operations.isClearingAllData,
            message = operations.message,
            error = operations.error
        )
    }
}
