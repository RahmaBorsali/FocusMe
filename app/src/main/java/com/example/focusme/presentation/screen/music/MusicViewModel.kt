package com.example.focusme.presentation.screen.music

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusme.data.api.TrackDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MusicUiState(
    val query: String = "lofi",
    val isLoading: Boolean = false,
    val error: String? = null,
    val tracks: List<TrackDto> = emptyList(),
    val currentIndex: Int = 0,
    val isPlaying: Boolean = false
) {
    val currentTrack: TrackDto? get() = tracks.getOrNull(currentIndex)
}

class MusicViewModel(
    private val repo: MusicRepository = MusicRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MusicUiState())
    val uiState: StateFlow<MusicUiState> = _uiState

    private var job: Job? = null

    init {
        search("lofi")
    }

    fun search(term: String) {
        job?.cancel()
        _uiState.update { it.copy(query = term, isLoading = true, error = null) }

        job = viewModelScope.launch {
            runCatching { repo.search(term) }
                .onSuccess { list ->
                    _uiState.update {
                        it.copy(
                            tracks = list,
                            currentIndex = 0,
                            isLoading = false,
                            error = if (list.isEmpty()) "Aucun résultat" else null,
                            isPlaying = false
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false, error = "Erreur réseau") }
                }
        }
    }

    fun setPlaying(isPlaying: Boolean) {
        _uiState.update { it.copy(isPlaying = isPlaying) }
    }

    fun next() {
        _uiState.update { st ->
            if (st.tracks.isEmpty()) st
            else st.copy(currentIndex = (st.currentIndex + 1).coerceAtMost(st.tracks.lastIndex), isPlaying = true)
        }
    }

    fun prev() {
        _uiState.update { st ->
            if (st.tracks.isEmpty()) st
            else st.copy(currentIndex = (st.currentIndex - 1).coerceAtLeast(0), isPlaying = true)
        }
    }

    fun pick(index: Int) {
        _uiState.update { st ->
            st.copy(currentIndex = index.coerceIn(0, (st.tracks.size - 1).coerceAtLeast(0)), isPlaying = true)
        }
    }
}
