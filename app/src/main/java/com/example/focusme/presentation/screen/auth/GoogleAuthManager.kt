package com.example.focusme.presentation.screen.auth

import android.app.Activity
import androidx.credentials.CustomCredential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.focusme.BuildConfig
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import java.util.UUID

object GoogleAuthManager {
    suspend fun getIdToken(activity: Activity): Result<String> {
        val serverClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID.trim()
        if (serverClientId.isBlank()) {
            return Result.failure(
                IllegalStateException("GOOGLE_WEB_CLIENT_ID manquant. Ajoute focusmeGoogleWebClientId dans local.properties ou les variables d'environnement.")
            )
        }

        val credentialManager = CredentialManager.create(activity)
        val googleOption = GetSignInWithGoogleOption.Builder(serverClientId)
            .build()
        val request = GetCredentialRequest(
            credentialOptions = listOf(googleOption)
        )

        return try {
            val response = credentialManager.getCredential(activity, request)
            val credential = response.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val tokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                Result.success(tokenCredential.idToken)
            } else {
                Result.failure(IllegalStateException("Type de credential inconnu : ${credential.type}"))
            }
        } catch (error: GetCredentialException) {
            // Ici, on remonte l'erreur complète pour le DEBUG
            Result.failure(Exception("${error.javaClass.simpleName}: [${error.type}] ${error.message}", error))
        } catch (error: Exception) {
            Result.failure(error)
        }
    }
}
