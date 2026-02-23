package com.example.focusme.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "auth")

class TokenStore(private val context: Context) {

    private val KEY = stringPreferencesKey("access_token")

    suspend fun saveToken(token: String) {
        context.dataStore.edit { it[KEY] = token }
    }

    suspend fun clear() {
        context.dataStore.edit { it.remove(KEY) }
    }

    fun getTokenBlocking(): String? = runBlocking {
        context.dataStore.data.first()[KEY]
    }
}