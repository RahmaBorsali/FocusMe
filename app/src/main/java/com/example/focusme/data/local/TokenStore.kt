package com.example.focusme.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "auth")

class TokenStore(private val context: Context) {

    private val KEY_TOKEN = stringPreferencesKey("access_token")
    private val KEY_USER_ID = stringPreferencesKey("user_id")

    suspend fun saveToken(token: String) {
        context.dataStore.edit { it[KEY_TOKEN] = token }
    }

    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { it[KEY_USER_ID] = userId }
    }

    suspend fun clear() {
        context.dataStore.edit {
            it.remove(KEY_TOKEN)
            it.remove(KEY_USER_ID)
        }
    }

    fun getTokenBlocking(): String? = runBlocking {
        context.dataStore.data.first()[KEY_TOKEN]
    }

    fun getUserIdBlocking(): String? = runBlocking {
        context.dataStore.data.first()[KEY_USER_ID]
    }
}