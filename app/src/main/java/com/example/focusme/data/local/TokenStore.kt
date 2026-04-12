package com.example.focusme.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.focusme.data.api.dto.UserDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "auth")

data class StoredSession(
    val token: String? = null,
    val userId: String? = null,
    val username: String = "",
    val email: String = "",
    val avatarType: String? = null,
    val avatarInitials: String? = null,
    val avatarUrl: String? = null,
    val guestMode: Boolean = false,
    val displayName: String = "",
    val studyGoal: String = "",
    val notificationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val defaultVisibility: String = "friends",
    val defaultAllowComments: Boolean = true
) {
    val isAuthenticated: Boolean
        get() = !token.isNullOrBlank()

    val isGuest: Boolean
        get() = !isAuthenticated && guestMode

    val resolvedDisplayName: String
        get() = displayName.ifBlank {
            when {
                username.isNotBlank() -> username
                isGuest -> "Invite FocusMe"
                else -> "FocusMe User"
            }
        }

    val resolvedInitials: String
        get() = avatarInitials
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: resolvedDisplayName
                .split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .joinToString("") { it.first().uppercase() }
                .ifBlank { "FM" }
}

class TokenStore(private val context: Context) {

    private val KEY_TOKEN = stringPreferencesKey("access_token")
    private val KEY_USER_ID = stringPreferencesKey("user_id")
    private val KEY_USERNAME = stringPreferencesKey("username")
    private val KEY_EMAIL = stringPreferencesKey("email")
    private val KEY_AVATAR_TYPE = stringPreferencesKey("avatar_type")
    private val KEY_AVATAR_INITIALS = stringPreferencesKey("avatar_initials")
    private val KEY_AVATAR_URL = stringPreferencesKey("avatar_url")
    private val KEY_GUEST_MODE = booleanPreferencesKey("guest_mode")
    private val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
    private val KEY_STUDY_GOAL = stringPreferencesKey("study_goal")
    private val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    private val KEY_SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    private val KEY_DEFAULT_VISIBILITY = stringPreferencesKey("default_visibility")
    private val KEY_DEFAULT_ALLOW_COMMENTS = booleanPreferencesKey("default_allow_comments")

    fun observeSession(): Flow<StoredSession> =
        context.dataStore.data.map { prefs ->
            StoredSession(
                token = prefs[KEY_TOKEN],
                userId = prefs[KEY_USER_ID],
                username = prefs[KEY_USERNAME].orEmpty(),
                email = prefs[KEY_EMAIL].orEmpty(),
                avatarType = prefs[KEY_AVATAR_TYPE],
                avatarInitials = prefs[KEY_AVATAR_INITIALS],
                avatarUrl = prefs[KEY_AVATAR_URL],
                guestMode = prefs[KEY_GUEST_MODE] ?: false,
                displayName = prefs[KEY_DISPLAY_NAME].orEmpty(),
                studyGoal = prefs[KEY_STUDY_GOAL].orEmpty(),
                notificationsEnabled = prefs[KEY_NOTIFICATIONS_ENABLED] ?: true,
                soundEnabled = prefs[KEY_SOUND_ENABLED] ?: true,
                defaultVisibility = prefs[KEY_DEFAULT_VISIBILITY] ?: "friends",
                defaultAllowComments = prefs[KEY_DEFAULT_ALLOW_COMMENTS] ?: true
            )
        }

    fun getSessionBlocking(): StoredSession = runBlocking {
        observeSession().first()
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { it[KEY_TOKEN] = token }
    }

    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { it[KEY_USER_ID] = userId }
    }

    suspend fun saveSession(token: String, user: UserDto) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
            prefs[KEY_USER_ID] = user.id
            prefs[KEY_USERNAME] = user.username
            prefs[KEY_EMAIL] = user.email
            prefs[KEY_GUEST_MODE] = false

            user.avatarType?.let { prefs[KEY_AVATAR_TYPE] = it } ?: prefs.remove(KEY_AVATAR_TYPE)
            user.avatarInitials?.let { prefs[KEY_AVATAR_INITIALS] = it } ?: prefs.remove(KEY_AVATAR_INITIALS)
            user.avatarUrl?.let { prefs[KEY_AVATAR_URL] = it } ?: prefs.remove(KEY_AVATAR_URL)

            if (prefs[KEY_DISPLAY_NAME].isNullOrBlank()) {
                prefs[KEY_DISPLAY_NAME] = user.username
            }
        }
    }

    suspend fun updateProfile(displayName: String, studyGoal: String) {
        context.dataStore.edit { prefs ->
            val trimmedName = displayName.trim()
            val trimmedGoal = studyGoal.trim()

            if (trimmedName.isBlank()) {
                prefs.remove(KEY_DISPLAY_NAME)
            } else {
                prefs[KEY_DISPLAY_NAME] = trimmedName
            }

            if (trimmedGoal.isBlank()) {
                prefs.remove(KEY_STUDY_GOAL)
            } else {
                prefs[KEY_STUDY_GOAL] = trimmedGoal
            }
        }
    }

    suspend fun setGuestMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_GUEST_MODE] = enabled
            if (enabled) {
                prefs.remove(KEY_TOKEN)
                prefs.remove(KEY_USER_ID)
                prefs.remove(KEY_USERNAME)
                prefs.remove(KEY_EMAIL)
                prefs.remove(KEY_AVATAR_TYPE)
                prefs.remove(KEY_AVATAR_INITIALS)
                prefs.remove(KEY_AVATAR_URL)
                if (prefs[KEY_DISPLAY_NAME].isNullOrBlank()) {
                    prefs[KEY_DISPLAY_NAME] = "Invite FocusMe"
                }
            }
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SOUND_ENABLED] = enabled }
    }

    suspend fun setDefaultVisibility(visibility: String) {
        context.dataStore.edit { it[KEY_DEFAULT_VISIBILITY] = visibility }
    }

    suspend fun setDefaultAllowComments(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DEFAULT_ALLOW_COMMENTS] = enabled }
    }

    suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_TOKEN)
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_USERNAME)
            prefs.remove(KEY_EMAIL)
            prefs.remove(KEY_AVATAR_TYPE)
            prefs.remove(KEY_AVATAR_INITIALS)
            prefs.remove(KEY_AVATAR_URL)
            prefs.remove(KEY_GUEST_MODE)
            prefs.remove(KEY_DISPLAY_NAME)
            prefs.remove(KEY_STUDY_GOAL)
            prefs.remove(KEY_DEFAULT_VISIBILITY)
            prefs.remove(KEY_DEFAULT_ALLOW_COMMENTS)
        }
    }

    fun getTokenBlocking(): String? = runBlocking {
        context.dataStore.data.first()[KEY_TOKEN]
    }

    fun getUserIdBlocking(): String? = runBlocking {
        context.dataStore.data.first()[KEY_USER_ID]
    }

    fun isGuestBlocking(): Boolean = runBlocking {
        context.dataStore.data.first()[KEY_GUEST_MODE] ?: false
    }
}
