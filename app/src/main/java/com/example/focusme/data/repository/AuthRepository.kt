package com.example.focusme.data.repository

import android.content.Context
import com.example.focusme.data.api.ApiClient
import com.example.focusme.data.api.ApiConfig
import com.example.focusme.data.api.dto.*
import com.example.focusme.data.local.TokenStore
import retrofit2.HttpException
import java.io.IOException

class AuthRepository(context: Context) {

    private val api = ApiClient.authApi(context)
    private val tokenStore = TokenStore(context)

    suspend fun signup(username: String, email: String, password: String, confirm: String): Result<String> =
        safeCall {
            val res = api.signup(SignupRequest(username, email, password, confirm))
            res.message
        }

    suspend fun login(email: String, password: String): Result<UserDto> =
        safeCall {
            val res = api.login(LoginRequest(email, password))
            tokenStore.saveSession(res.accessToken, res.user)
            res.user
        }

    suspend fun logout(): Result<Unit> =
        safeCall {
            tokenStore.clear()
        }

    suspend fun forgotPassword(email: String): Result<String> =
        safeCall {
            api.forgotPassword(ForgotPasswordRequest(email))["message"] ?: "Check your email."
        }

    suspend fun resetPassword(token: String, password: String, confirm: String): Result<String> =
        safeCall {
            api.resetPassword(ResetPasswordRequest(token, password, confirm))["message"] ?: "Password updated."
        }

    private suspend fun <T> safeCall(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: HttpException) {
            val msg = e.response()?.errorBody()?.string() ?: e.message()
            Result.failure(RuntimeException(msg))
        } catch (e: IOException) {
            Result.failure(
                RuntimeException(
                    "${ApiConfig.connectionHelpMessage()} " +
                        "URL essayee en premier: ${ApiConfig.BASE_URL}",
                    e
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
