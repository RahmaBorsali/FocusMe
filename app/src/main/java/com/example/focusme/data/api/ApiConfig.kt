package com.example.focusme.data.api

import android.os.Build
import com.example.focusme.BuildConfig
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

object ApiConfig {
    private const val RENDER_BASE_URL = "https://focusmebackend.onrender.com/"
    private const val EMULATOR_BASE_URL = "http://10.0.2.2:4000/"
    private const val LOCALHOST_BASE_URL = "http://127.0.0.1:4000/"
    private const val LOCALHOST_NAME_BASE_URL = "http://localhost:4000/"

    val BASE_URL: String
        get() = candidateBaseUrls().first()

    fun candidateBaseUrls(): List<String> {
        val configured = normalizeBaseUrl(BuildConfig.API_BASE_URL_OVERRIDE)
        if (configured != null) {
            return listOf(configured)
        }

        return if (isProbablyEmulator()) {
            listOf(RENDER_BASE_URL, EMULATOR_BASE_URL, LOCALHOST_BASE_URL, LOCALHOST_NAME_BASE_URL)
        } else {
            listOf(RENDER_BASE_URL, LOCALHOST_BASE_URL, LOCALHOST_NAME_BASE_URL)
        }
    }

    fun candidateBaseHttpUrls(): List<HttpUrl> =
        candidateBaseUrls().map { it.toHttpUrl() }

    fun connectionHelpMessage(): String =
        buildString {
            append("Impossible de joindre le serveur. ")
            append("Emulateur: l'app essaie automatiquement 10.0.2.2 puis localhost. ")
            append("Telephone Android: utilise `adb reverse tcp:4000 tcp:4000` pour du debug USB, ")
            append("ou ajoute `focusmeApiBaseUrl=http://IP_DU_PC:4000/` dans local.properties ")
            append("pour que l'emulateur et le telephone utilisent la meme adresse reseau.")
        }

    private fun normalizeBaseUrl(value: String?): String? {
        val trimmed = value?.trim().orEmpty()
        if (trimmed.isBlank()) return null
        return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
    }

    private fun isProbablyEmulator(): Boolean {
        val fingerprint = Build.FINGERPRINT.lowercase()
        val model = Build.MODEL.lowercase()
        val manufacturer = Build.MANUFACTURER.lowercase()
        val brand = Build.BRAND.lowercase()
        val device = Build.DEVICE.lowercase()
        val product = Build.PRODUCT.lowercase()

        return fingerprint.startsWith("generic") ||
            fingerprint.contains("emulator") ||
            fingerprint.contains("sdk_gphone") ||
            model.contains("emulator") ||
            model.contains("android sdk built for") ||
            manufacturer.contains("genymotion") ||
            (brand.startsWith("generic") && device.startsWith("generic")) ||
            product.contains("sdk")
    }
}
