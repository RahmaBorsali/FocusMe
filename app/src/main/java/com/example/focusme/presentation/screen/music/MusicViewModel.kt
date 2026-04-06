package com.example.focusme.presentation.screen.music

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusme.data.api.TrackDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MusicUiState(
    val currentTab: MusicTab = MusicTab.HOME,
    val libraryTab: MusicLibraryTab = MusicLibraryTab.SAVED,
    val homeData: MusicHomeData = MusicHomeData(),
    val isHomeLoading: Boolean = true,
    val homeError: String? = null,
    val query: String = "",
    val searchResults: List<TrackDto> = emptyList(),
    val isSearching: Boolean = false,
    val searchError: String? = null,
    val libraryTracks: List<TrackDto> = emptyList(),
    val isLibraryLoading: Boolean = false,
    val libraryError: String? = null,
    val selectedPlaylist: MusicPlaylist? = null,
    val playlistTracks: List<TrackDto> = emptyList(),
    val isPlaylistLoading: Boolean = false,
    val playlistError: String? = null,
    val isPlayerExpanded: Boolean = false,
    val actionMessage: String? = null,
    val actionError: String? = null
)

class MusicViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = MusicRepository(app)

    private val _uiState = MutableStateFlow(MusicUiState())
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        MusicPlaybackManager.initialize(app)
        loadHome()
        loadLibrary()
    }

    fun setTab(tab: MusicTab) {
        _uiState.update { it.copy(currentTab = tab, actionMessage = null, actionError = null) }
        when (tab) {
            MusicTab.HOME -> loadHome()
            MusicTab.SEARCH -> if (uiState.value.query.isNotBlank()) updateQuery(uiState.value.query)
            MusicTab.LIBRARY -> loadLibrary()
        }
    }

    fun setLibraryTab(tab: MusicLibraryTab) {
        _uiState.update { it.copy(libraryTab = tab) }
    }

    fun refreshCurrentTab() {
        when (uiState.value.currentTab) {
            MusicTab.HOME -> loadHome(force = true)
            MusicTab.SEARCH -> updateQuery(uiState.value.query)
            MusicTab.LIBRARY -> loadLibrary(force = true)
        }
    }

    fun updateQuery(value: String) {
        _uiState.update {
            it.copy(
                currentTab = MusicTab.SEARCH,
                query = value,
                searchError = null,
                actionError = null,
                actionMessage = null
            )
        }

        searchJob?.cancel()
        if (value.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false, searchError = null) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(350)
            _uiState.update { it.copy(isSearching = true, searchError = null) }
            runCatching { repo.search(value.trim()) }
                .onSuccess { results ->
                    _uiState.update {
                        it.copy(
                            searchResults = results,
                            isSearching = false,
                            searchError = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            searchError = throwable.toMusicMessage()
                        )
                    }
                }
        }
    }

    fun openPlaylist(playlist: MusicPlaylist) {
        _uiState.update {
            it.copy(
                selectedPlaylist = playlist,
                playlistTracks = emptyList(),
                isPlaylistLoading = true,
                playlistError = null
            )
        }
        viewModelScope.launch {
            runCatching { repo.getPlaylistTracks(playlist) }
                .onSuccess { tracks ->
                    _uiState.update {
                        it.copy(
                            playlistTracks = tracks,
                            isPlaylistLoading = false
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isPlaylistLoading = false,
                            playlistError = throwable.toMusicMessage()
                        )
                    }
                }
        }
    }

    fun closePlaylist() {
        _uiState.update {
            it.copy(
                selectedPlaylist = null,
                playlistTracks = emptyList(),
                isPlaylistLoading = false,
                playlistError = null
            )
        }
    }

    fun playTrack(track: TrackDto, fromList: List<TrackDto>) {
        MusicPlaybackManager.playTracks(fromList, fromList.indexOfFirst { it.trackId == track.trackId }.coerceAtLeast(0))
        viewModelScope.launch {
            repo.markAsPlayed(track)
            loadHome(force = true)
            loadLibrary(force = true)
        }
    }

    fun togglePlay() = MusicPlaybackManager.togglePlay()

    fun next() = MusicPlaybackManager.next()

    fun previous() = MusicPlaybackManager.previous()

    fun seekTo(positionMs: Long) = MusicPlaybackManager.seekTo(positionMs)

    fun togglePlayerExpanded() {
        _uiState.update { it.copy(isPlayerExpanded = !it.isPlayerExpanded) }
    }

    fun setPlayerExpanded(expanded: Boolean) {
        _uiState.update { it.copy(isPlayerExpanded = expanded) }
    }

    fun toggleLibrary(track: TrackDto) {
        viewModelScope.launch {
            runCatching { repo.toggleLibrary(track) }
                .onSuccess { updated ->
                    applyTrackUpdate(updated)
                    _uiState.update {
                        it.copy(actionMessage = if (updated.isInLibrary) "Ajoute a ta bibliotheque." else "Retire de ta bibliotheque.")
                    }
                    loadLibrary(force = true)
                    loadHome(force = true)
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(actionError = throwable.toMusicMessage()) }
                }
        }
    }

    fun toggleDownload(track: TrackDto) {
        if (!track.isDownloaded && !track.isDownloadAllowed) {
            _uiState.update { it.copy(actionError = "Ce morceau n'autorise pas le telechargement hors ligne.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(actionError = null, actionMessage = null) }
            runCatching { repo.toggleDownload(track) }
                .onSuccess { updated ->
                    applyTrackUpdate(updated)
                    _uiState.update {
                        it.copy(
                            actionMessage = if (updated.isDownloaded) {
                                "Morceau telecharge pour une ecoute hors ligne."
                            } else {
                                "Telechargement supprime."
                            }
                        )
                    }
                    loadLibrary(force = true)
                    loadHome(force = true)
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(actionError = throwable.toMusicMessage()) }
                }
        }
    }

    fun clearActionFeedback() {
        _uiState.update { it.copy(actionMessage = null, actionError = null) }
    }

    private fun loadHome(force: Boolean = false) {
        if (!force && uiState.value.homeData.playlists.isNotEmpty() && uiState.value.homeData.trendingTracks.isNotEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isHomeLoading = true, homeError = null) }
            runCatching { repo.getHomeData() }
                .onSuccess { data ->
                    _uiState.update {
                        it.copy(
                            homeData = data,
                            isHomeLoading = false
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isHomeLoading = false,
                            homeError = throwable.toMusicMessage()
                        )
                    }
                }
        }
    }

    private fun loadLibrary(force: Boolean = false) {
        if (!force && uiState.value.libraryTracks.isNotEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLibraryLoading = true, libraryError = null) }
            runCatching { repo.getLibrary() }
                .onSuccess { tracks ->
                    _uiState.update {
                        it.copy(
                            libraryTracks = tracks,
                            isLibraryLoading = false
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLibraryLoading = false,
                            libraryError = throwable.toMusicMessage()
                        )
                    }
                }
        }
    }

    private fun applyTrackUpdate(updated: TrackDto) {
        _uiState.update { state ->
            state.copy(
                homeData = state.homeData.copy(
                    recentlyPlayed = state.homeData.recentlyPlayed.replaceTrack(updated),
                    mostPlayed = state.homeData.mostPlayed.replaceTrack(updated),
                    trendingTracks = state.homeData.trendingTracks.replaceTrack(updated),
                    focusTracks = state.homeData.focusTracks.replaceTrack(updated)
                ),
                searchResults = state.searchResults.replaceTrack(updated),
                libraryTracks = state.libraryTracks.replaceTrack(updated),
                playlistTracks = state.playlistTracks.replaceTrack(updated)
            )
        }
    }
}

private fun List<TrackDto>.replaceTrack(updated: TrackDto): List<TrackDto> {
    if (isEmpty() || updated.trackId == null) return this
    return map { current ->
        if (current.trackId == updated.trackId) updated else current
    }
}

private fun Throwable.toMusicMessage(): String {
    return message?.takeIf { it.isNotBlank() } ?: "Impossible de charger la musique pour le moment."
}
