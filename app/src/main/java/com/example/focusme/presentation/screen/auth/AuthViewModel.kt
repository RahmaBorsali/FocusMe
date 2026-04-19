package com.example.focusme.presentation.screen.auth

import android.app.Application
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusme.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val usernameError: String? = null,
    val confirmError: String? = null,
    val error: String? = null,
    val success: String? = null,
    val isLoggedIn: Boolean = false,
    val isNewUser: Boolean = false,
    val showLoginSignupRedirect: Boolean = false,
    val showSignupEmailExistsDialog: Boolean = false,
    val googleEmail: String? = null,
    val googleName: String? = null,
    val googleSub: String? = null,
    val resetEmail: String? = null
)

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository(app)

    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui

    fun login(email: String, password: String, onDone: () -> Unit) {
        val emailValue = email.trim()
        val validation = validateLogin(emailValue, password)
        if (validation != null) {
            _ui.value = validation
            return
        }

        viewModelScope.launch {
            _ui.value = AuthUiState(loading = true)
            val res = repo.login(email, password)
            _ui.value = if (res.isSuccess) {
                AuthUiState(success = "Connexion reussie.")
            } else {
                AuthUiState(error = normalizeAuthError(res.exceptionOrNull()?.message, isSignup = false))
            }
            if (res.isSuccess) onDone()
        }
    }

    fun signup(username: String, email: String, password: String, confirm: String, onDone: (String) -> Unit) {
        val usernameValue = username.trim()
        val emailValue = email.trim()
        val validation = validateSignup(usernameValue, emailValue, password, confirm)
        if (validation != null) {
            _ui.value = validation
            return
        }

        viewModelScope.launch {
            _ui.value = AuthUiState(loading = true)
            val res = repo.signup(username, email, password, confirm)
            if (res.isSuccess) {
                _ui.value = AuthUiState(success = "Compte cree. Merci de verifier ton email.", resetEmail = emailValue)
                onDone(emailValue)
            } else {
                _ui.value = AuthUiState(error = normalizeAuthError(res.exceptionOrNull()?.message, isSignup = true))
            }
        }
    }

    fun verifySignupEmail(email: String, code: String, onDone: () -> Unit) {
        if (email.isBlank()) {
            _ui.update { it.copy(error = "E-mail manquant. Recommence.") }
            return
        }

        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null, success = null) }
            val res = repo.verifyEmail(email, code)
            if (res.isSuccess) {
                _ui.update { it.copy(loading = false, success = "Email verifier avec succes !") }
                onDone()
            } else {
                _ui.update { it.copy(loading = false, error = normalizeAuthError(res.exceptionOrNull()?.message, false)) }
            }
        }
    }

    fun loginWithGoogle(activity: ComponentActivity, mode: String) {
        viewModelScope.launch {
            _ui.value = AuthUiState(loading = true)

            val idTokenResult = GoogleAuthManager.getIdToken(activity)
            if (idTokenResult.isFailure) {
                _ui.value = AuthUiState(
                    error = normalizeGoogleError(idTokenResult.exceptionOrNull()?.message)
                )
                return@launch
            }

            val res = repo.loginWithGoogle(idTokenResult.getOrThrow(), mode)
            if (res.isSuccess) {
                _ui.value = AuthUiState(
                    isLoggedIn = true,
                    success = "Connexion Google réussie."
                )
            } else {
                val errorMsg = res.exceptionOrNull()?.message.orEmpty()
                when {
                    errorMsg.contains("444") || errorMsg.contains("USER_NOT_FOUND") -> {
                        _ui.value = AuthUiState(showLoginSignupRedirect = true)
                    }
                    errorMsg.contains("409") || errorMsg.contains("EMAIL_EXISTS") -> {
                        _ui.value = AuthUiState(showSignupEmailExistsDialog = true)
                    }
                    else -> {
                        _ui.value = AuthUiState(
                            error = normalizeGoogleError(errorMsg)
                        )
                    }
                }
            }
        }
    }

    fun dismissDialogs() {
        _ui.update { it.copy(showLoginSignupRedirect = false, showSignupEmailExistsDialog = false) }
    }

    fun forgotPassword(email: String, onDone: (String) -> Unit) {
        val emailValue = email.trim()
        if (emailValue.isBlank()) {
            _ui.value = AuthUiState(emailError = "Entre ton email.")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
            _ui.value = AuthUiState(emailError = "Email invalide.")
            return
        }

        viewModelScope.launch {
            _ui.value = AuthUiState(loading = true)
            val result = repo.forgotPassword(emailValue)
            _ui.value = if (result.isSuccess) {
                AuthUiState(success = result.getOrNull() ?: "Verification code sent.", resetEmail = emailValue)
            } else {
                AuthUiState(error = normalizeAuthError(result.exceptionOrNull()?.message, isSignup = false))
            }
            if (result.isSuccess) onDone(emailValue)
        }
    }

    fun resetPassword(email: String, code: String, password: String, confirm: String, onDone: () -> Unit) {
        if (email.isBlank()) {
            _ui.update { it.copy(error = "E-mail manquant. Recommence.") }
            return
        }

        if (password.length < 8) {
            _ui.update { it.copy(passwordError = "Minimum 8 caractères.") }
            return
        }
        if (password != confirm) {
            _ui.update { it.copy(confirmError = "Pas identique.") }
            return
        }

        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null, success = null) }
            val res = repo.resetPassword(email, code, password, confirm)
            if (res.isSuccess) {
                _ui.update { it.copy(loading = false, success = "Mot de passe changer.") }
                onDone()
            } else {
                _ui.update { it.copy(loading = false, error = normalizeAuthError(res.exceptionOrNull()?.message, false)) }
            }
        }
    }

    fun clearFieldErrors() {
        _ui.update {
            it.copy(
                emailError = null,
                passwordError = null,
                usernameError = null,
                confirmError = null,
                error = null,
                success = null
            )
        }
    }

    private fun validateLogin(email: String, password: String): AuthUiState? {
        var hasError = false
        val emailError = when {
            email.isBlank() -> {
                hasError = true
                "Entre ton email."
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                hasError = true
                "Email invalide."
            }
            else -> null
        }
        val passwordError = when {
            password.isBlank() -> {
                hasError = true
                "Entre ton mot de passe."
            }
            else -> null
        }

        return if (hasError) {
            AuthUiState(
                emailError = emailError,
                passwordError = passwordError,
                error = if (emailError == null && passwordError == null) {
                    "Verifie ton email ou ton mot de passe."
                } else null
            )
        } else {
            null
        }
    }

    private fun validateSignup(username: String, email: String, password: String, confirm: String): AuthUiState? {
        var hasError = false
        val usernameError = when {
            username.isBlank() -> {
                hasError = true
                "Entre ton nom."
            }
            username.length < 3 -> {
                hasError = true
                "Le nom doit contenir au moins 3 caracteres."
            }
            else -> null
        }
        val emailError = when {
            email.isBlank() -> {
                hasError = true
                "Entre ton email."
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                hasError = true
                "Email invalide."
            }
            else -> null
        }
        val passwordError = when {
            password.isBlank() -> {
                hasError = true
                "Entre un mot de passe."
            }
            password.length < 8 -> {
                hasError = true
                "Le mot de passe doit contenir au moins 8 caracteres."
            }
            else -> null
        }
        val confirmError = when {
            confirm.isBlank() -> {
                hasError = true
                "Confirme ton mot de passe."
            }
            confirm != password -> {
                hasError = true
                "Les mots de passe ne correspondent pas."
            }
            else -> null
        }

        return if (hasError) {
            AuthUiState(
                emailError = emailError,
                passwordError = passwordError,
                usernameError = usernameError,
                confirmError = confirmError
            )
        } else {
            null
        }
    }

    private fun normalizeAuthError(rawMessage: String?, isSignup: Boolean): String {
        val message = rawMessage.orEmpty().lowercase()
        return when {
            message.contains("invalid credentials") ||
                message.contains("incorrect") ||
                (message.contains("email") && message.contains("password")) -> {
                "Email ou mot de passe incorrect."
            }
            message.contains("user not found") || message.contains("email not found") -> {
                "Aucun compte trouve avec cet email."
            }
            message.contains("already exists") ||
                message.contains("already registered") ||
                message.contains("email already") ||
                message.contains("duplicate") -> {
                "Cet email est deja utilise."
            }
            message.contains("username already") -> {
                "Ce nom d'utilisateur est deja pris."
            }
            message.contains("verified") || message.contains("confirmer") -> {
                "Ton email n'est pas encore verifie. Verifie tes mails."
            }
            message.contains("code") || message.contains("otp") || message.contains("token") -> {
                "Code invalide ou expirer."
            }
            message.contains("network") ||
                message.contains("unable to resolve host") ||
                message.contains("timeout") ||
                message.contains("failed to connect") ||
                message.contains("impossible de joindre le serveur") -> {
                "Connexion au serveur impossible. Verifie ton internet ou le serveur."
            }
            isSignup -> "Inscription impossible pour le moment. Verifie les informations saisies."
            else -> "Connexion impossible. Verifie ton email ou ton mot de passe."
        }
    }

    private fun normalizeGoogleError(rawMessage: String?): String {
        val message = rawMessage.orEmpty().lowercase()
        return when {
            message.contains("cancel") || message.contains("annule") -> "Connexion Google annulée."
            message.contains("409") || message.contains("conflict") || message.contains("already used") || message.contains("classique") ->
                "Cet email est déjà lié à un compte classique. Connecte-toi avec ton mot de passe."
            message.contains("network") || message.contains("server") || message.contains("impossible de joindre le serveur") ->
                "Connexion Google impossible. Vérifie ton internet ou le serveur."
            else -> "Connexion Google impossible pour le moment."
        }
    }
}
