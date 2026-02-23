package com.example.focusme.presentation.screen.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusme.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val success: String? = null
)

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository(app)

    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui

    fun login(email: String, password: String, onDone: () -> Unit) {
        viewModelScope.launch {
            _ui.value = AuthUiState(loading = true)
            val res = repo.login(email, password)
            _ui.value = if (res.isSuccess) AuthUiState(success = "Logged in") else AuthUiState(error = res.exceptionOrNull()?.message)
            if (res.isSuccess) onDone()
        }
    }

    fun signup(username: String, email: String, password: String, confirm: String) {
        viewModelScope.launch {
            _ui.value = AuthUiState(loading = true)
            val res = repo.signup(username, email, password, confirm)
            _ui.value = if (res.isSuccess) AuthUiState(success = res.getOrNull()) else AuthUiState(error = res.exceptionOrNull()?.message)
        }
    }
}