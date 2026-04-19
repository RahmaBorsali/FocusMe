package com.example.focusme.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// --- API Audius (Utilisée comme fournisseur optionnel) ---

interface AudiusApi {
    @GET("tracks/trending")
    suspend fun getTrending(@Query("limit") limit: Int = 20): AudiusResponse<List<AudiusTrackNetworkDto>>
    
    @GET("tracks/search")
    suspend fun search(@Query("query") query: String, @Query("limit") limit: Int = 20): AudiusResponse<List<AudiusTrackNetworkDto>>
}

data class AudiusResponse<T>(val data: T? = null)
data class AudiusTrackNetworkDto(
    val id: String? = null,
    val title: String? = null,
    val duration: Long? = null,
    val genre: String? = null,
    val user: AudiusUserDto? = null,
    val artwork: AudiusArtworkDto? = null
)
data class AudiusUserDto(val name: String? = null)
data class AudiusArtworkDto(@SerializedName("480x480") val medium: String? = null)

object AudiusClient {
    val api: AudiusApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.audius.co/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AudiusApi::class.java)
    }
}
