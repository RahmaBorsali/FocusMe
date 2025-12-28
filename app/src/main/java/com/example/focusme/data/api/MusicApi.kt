package com.example.focusme.data.api

import retrofit2.http.GET
import retrofit2.http.Query

data class MusicSearchResponse(
    val resultCount: Int = 0,
    val results: List<TrackDto> = emptyList()
)

data class TrackDto(
    val trackId: Long? = null,
    val trackName: String? = null,
    val artistName: String? = null,
    val artworkUrl100: String? = null,
    val previewUrl: String? = null
)

interface MusicApi {
    @GET("search")
    suspend fun searchMusic(
        @Query("term") term: String,
        @Query("media") media: String = "music",
        @Query("limit") limit: Int = 20
    ): MusicSearchResponse
}
