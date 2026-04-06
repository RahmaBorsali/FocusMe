package com.example.focusme.presentation.screen.music

import com.example.focusme.data.api.TrackDto

enum class MusicTab(val label: String) {
    HOME("Pour toi"),
    SEARCH("Recherche"),
    LIBRARY("Bibliotheque")
}

enum class MusicLibraryTab(val label: String) {
    SAVED("Sauvegardes"),
    DOWNLOADED("Telecharges"),
    RECENT("Recents")
}

data class MusicPlaylist(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val artworkUrl: String? = null,
    val tracksCount: Int = 0
)

data class MusicHomeData(
    val recentlyPlayed: List<TrackDto> = emptyList(),
    val mostPlayed: List<TrackDto> = emptyList(),
    val trendingTracks: List<TrackDto> = emptyList(),
    val focusTracks: List<TrackDto> = emptyList(),
    val playlists: List<MusicPlaylist> = emptyList()
)

data class MusicPlaybackState(
    val queue: List<TrackDto> = emptyList(),
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val playbackPosition: Long = 0,
    val playbackDuration: Long = 0,
    val isConnected: Boolean = false
) {
    val currentTrack: TrackDto?
        get() = queue.getOrNull(currentIndex)
}

fun fallbackPlaylists(): List<MusicPlaylist> = listOf(
    MusicPlaylist(id = "focus-mix", title = "Focus Mix"),
    MusicPlaylist(id = "study-flow", title = "Study Flow"),
    MusicPlaylist(id = "night-drive", title = "Night Drive"),
    MusicPlaylist(id = "soft-piano", title = "Soft Piano")
)
