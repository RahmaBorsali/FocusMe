package com.example.focusme.presentation.screen.music

import com.example.focusme.data.api.MusicApiModule
import com.example.focusme.data.api.TrackDto

class MusicRepository(
    private val api: com.example.focusme.data.api.MusicApi = MusicApiModule.api
) {
    suspend fun search(term: String): List<TrackDto> {
        val res = api.searchMusic(term = term)
        return res.results
            .filter { !it.previewUrl.isNullOrBlank() }
            .take(20)
    }
}
