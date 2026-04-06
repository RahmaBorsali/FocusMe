package com.example.focusme.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class AudiusResponse<T>(
    val data: T? = null
)

data class AudiusArtworkDto(
    @SerializedName("150x150") val small: String? = null,
    @SerializedName("480x480") val medium: String? = null,
    @SerializedName("1000x1000") val large: String? = null
)

data class AudiusUserDto(
    val name: String? = null,
    val handle: String? = null
)

data class AudiusTrackNetworkDto(
    val id: String? = null,
    @SerializedName("title") val title: String? = null,
    val duration: Long? = null,
    val genre: String? = null,
    @SerializedName("release_date") val releaseDate: String? = null,
    val downloadable: Boolean? = null,
    @SerializedName("play_count") val playCount: Int? = null,
    val artwork: AudiusArtworkDto? = null,
    val user: AudiusUserDto? = null
)

data class AudiusPlaylistNetworkDto(
    val id: String? = null,
    @SerializedName("playlist_name") val playlistName: String? = null,
    val description: String? = null,
    val artwork: AudiusArtworkDto? = null,
    @SerializedName("track_count") val trackCount: Int? = null,
    val user: AudiusUserDto? = null
)

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
    @GET("tracks/trending")
    suspend fun getTrendingTracks(
        @Query("limit") limit: Int = 20
    ): AudiusResponse<List<AudiusTrackNetworkDto>>

    @GET("tracks/search")
    suspend fun searchTracks(
        @Query("query") query: String,
        @Query("limit") limit: Int = 20
    ): AudiusResponse<List<AudiusTrackNetworkDto>>

    @GET("playlists/trending")
    suspend fun getTrendingPlaylists(
        @Query("limit") limit: Int = 10
    ): AudiusResponse<List<AudiusPlaylistNetworkDto>>

    @GET("playlists/{id}/tracks")
    suspend fun getPlaylistTracks(
        @Path("id") id: String
    ): AudiusResponse<List<AudiusTrackNetworkDto>>
}
