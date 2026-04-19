package com.example.focusme.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// --- Nouveaux DTOs pour le système d'Abonnement ---

data class MusicPackDto(
    @SerializedName("_id") val id: String,
    val name: String,
    val description: String,
    val category: String,
    val imageUrl: String,
    val isFree: Boolean = true,
    val tracksCount: Int = 0
)

data class SubscribeRequest(
    val packId: String,
    val subscribe: Boolean
)

data class SubscribeResponse(
    val success: Boolean,
    val subscriptions: List<String>
)

data class MusicTrackNetworkDto(
    @SerializedName("_id") val id: String,
    val title: String,
    val artist: String? = "FocusMe",
    val audioUrl: String,
    val artworkUrl: String? = null,
    val durationSeconds: Int = 0,
    val packName: String? = null,
    val packId: String? = null
)

// --- Modèle de Domaine (Gardé pour compatibilité UI) ---

data class TrackDto(
    val trackId: String? = null,
    val trackName: String? = null,
    val artistName: String? = null,
    val albumName: String? = null,
    val artworkUrl100: String? = null,
    val audioUrl: String? = null,
    val downloadUrl: String? = null,
    val shareUrl: String? = null,
    val releasedAt: String? = null,
    val tags: List<String> = emptyList(),
    val trackTimeMillis: Long? = null,
    val localPath: String? = null,
    val isDownloaded: Boolean = false,
    val isInLibrary: Boolean = false,
    val isDownloadAllowed: Boolean = false,
    val playCount: Int = 0,
    val lastPlayedAt: Long? = null
) {
    val playbackUrl: String?
        get() = localPath ?: audioUrl

    val isPlayable: Boolean
        get() = !playbackUrl.isNullOrBlank()

    val durationLabel: String
        get() {
            val millis = trackTimeMillis ?: return "--:--"
            val totalSeconds = millis / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}

interface MusicApi {
    @GET("music/packs")
    suspend fun getAvailablePacks(): List<MusicPackDto>

    @GET("music/my-tracks")
    suspend fun getMySubscribedTracks(): List<MusicTrackNetworkDto>

    @POST("music/subscribe")
    suspend fun updateSubscription(@Body req: SubscribeRequest): SubscribeResponse

    // Route utilitaire pour peupler la DB au premier test
    @POST("music/seed")
    suspend fun seedMusic(): Map<String, Any>
}
