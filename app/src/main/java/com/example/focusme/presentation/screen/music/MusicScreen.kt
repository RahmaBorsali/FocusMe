package com.example.focusme.presentation.screen.music

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.focusme.data.api.TrackDto
import com.example.focusme.presentation.ui.components.SoftCard
import com.example.focusme.presentation.ui.theme.AppBg
import com.example.focusme.presentation.ui.theme.BorderSoft
import com.example.focusme.presentation.ui.theme.PinkPrimary
import com.example.focusme.presentation.ui.theme.TextDark
import com.example.focusme.presentation.ui.theme.TextGray
import kotlin.math.abs

private val musicHeaderGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFFDEAF3), Color(0xFFF9E7EF))
)

@Composable
fun MusicScreen(
    openPlayer: Boolean = false,
    vm: MusicViewModel = viewModel()
) {
    val ui by vm.uiState.collectAsState()
    val playback by MusicPlaybackManager.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(openPlayer, playback.currentTrack?.trackId) {
        if (openPlayer && playback.currentTrack != null) {
            vm.setPlayerExpanded(true)
        }
    }

    LaunchedEffect(ui.actionMessage, ui.actionError) {
        val message = ui.actionError ?: ui.actionMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        vm.clearActionFeedback()
    }

    Scaffold(
        containerColor = AppBg,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AppBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                MusicHeader(onRefresh = vm::refreshCurrentTab)
                Spacer(Modifier.height(16.dp))
                MusicTabs(selected = ui.currentTab, onSelected = vm::setTab)
                Spacer(Modifier.height(16.dp))

                val selectedPlaylist = ui.selectedPlaylist
                if (selectedPlaylist != null) {
                    PlaylistDetailsContent(
                        playlist = selectedPlaylist,
                        tracks = ui.playlistTracks,
                        isLoading = ui.isPlaylistLoading,
                        error = ui.playlistError,
                        playback = playback,
                        onBack = vm::closePlaylist,
                        onPlayTrack = vm::playTrack,
                        onToggleLibrary = vm::toggleLibrary,
                        onToggleDownload = vm::toggleDownload
                    )
                } else {
                    when (ui.currentTab) {
                        MusicTab.HOME -> MusicHomeContent(
                            ui = ui,
                            playback = playback,
                            onOpenPlaylist = vm::openPlaylist,
                            onPlayTrack = vm::playTrack,
                            onToggleLibrary = vm::toggleLibrary,
                            onToggleDownload = vm::toggleDownload,
                            onExpandPlayer = { vm.setPlayerExpanded(true) },
                            onQuickSearch = vm::updateQuery
                        )

                        MusicTab.SEARCH -> MusicSearchContent(
                            ui = ui,
                            playback = playback,
                            onQueryChange = vm::updateQuery,
                            onOpenPlaylist = vm::openPlaylist,
                            onPlayTrack = vm::playTrack,
                            onToggleLibrary = vm::toggleLibrary,
                            onToggleDownload = vm::toggleDownload
                        )

                        MusicTab.LIBRARY -> MusicLibraryContent(
                            ui = ui,
                            playback = playback,
                            onSelectTab = vm::setLibraryTab,
                            onPlayTrack = vm::playTrack,
                            onToggleLibrary = vm::toggleLibrary,
                            onToggleDownload = vm::toggleDownload
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = ui.isPlayerExpanded && playback.currentTrack != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                playback.currentTrack?.let { track ->
                    ExpandedMusicPlayer(
                        track = track,
                        playback = playback,
                        onClose = { vm.setPlayerExpanded(false) },
                        onTogglePlay = vm::togglePlay,
                        onNext = vm::next,
                        onPrevious = vm::previous,
                        onSeek = vm::seekTo
                    )
                }
            }
        }
    }
}

@Composable
fun MusicGlobalNowPlayingBar(
    onOpenMusic: () -> Unit,
    onDismiss: () -> Unit = {},
    onTogglePlay: () -> Unit = { MusicPlaybackManager.togglePlay() }
) {
    val playback by MusicPlaybackManager.state.collectAsState()
    val currentTrack = playback.currentTrack ?: return
    val progress = if (playback.playbackDuration > 0L) {
        playback.playbackPosition.toFloat() / playback.playbackDuration.toFloat()
    } else {
        0f
    }
    var swipeOffsetX by remember(currentTrack.trackId) { mutableFloatStateOf(0f) }
    val dismissThreshold = 140f

    SoftCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        padding = PaddingValues(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { translationX = swipeOffsetX }
                .pointerInput(currentTrack.trackId) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (swipeOffsetX <= -dismissThreshold) {
                                onDismiss()
                            } else {
                                swipeOffsetX = 0f
                            }
                        },
                        onDragCancel = {
                            swipeOffsetX = 0f
                        }
                    ) { _, dragAmount ->
                        swipeOffsetX = (swipeOffsetX + dragAmount).coerceAtMost(0f)
                        if (swipeOffsetX <= -dismissThreshold) {
                            onDismiss()
                        }
                    }
                }
        ) {
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = PinkPrimary,
                trackColor = PinkPrimary.copy(alpha = 0.12f)
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenMusic() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ArtworkThumb(track = currentTrack, modifier = Modifier.size(46.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentTrack.trackName ?: "Lecture en cours",
                            color = TextDark,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = currentTrack.artistName ?: "Focus Me Music",
                            color = TextGray,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Masquer",
                        tint = TextGray
                    )
                }
                IconButton(onClick = onTogglePlay) {
                    Icon(
                        imageVector = if (playback.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = PinkPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun MusicHeader(onRefresh: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(musicHeaderGradient)
            .padding(horizontal = 18.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                MetaChip(label = "Focus Me")
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Musique",
                    color = TextDark,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Ton player",
                    color = TextGray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.88f)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = PinkPrimary)
                }
            }
        }
    }
}

@Composable
private fun MusicTabs(selected: MusicTab, onSelected: (MusicTab) -> Unit) {
    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        padding = PaddingValues(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MusicTab.entries.forEach { tab ->
                val isSelected = selected == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) {
                                Brush.linearGradient(listOf(Color.White, Color(0xFFFFF4F8)))
                            } else {
                                Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                            }
                        )
                        .clickable { onSelected(tab) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.label,
                        color = if (isSelected) PinkPrimary else TextGray,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun MusicHomeContent(
    ui: MusicUiState,
    playback: MusicPlaybackState,
    onOpenPlaylist: (MusicPlaylist) -> Unit,
    onPlayTrack: (TrackDto, List<TrackDto>) -> Unit,
    onToggleLibrary: (TrackDto) -> Unit,
    onToggleDownload: (TrackDto) -> Unit,
    onExpandPlayer: () -> Unit,
    onQuickSearch: (String) -> Unit
) {
    if (ui.isHomeLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PinkPrimary)
        }
        return
    }

    val featuredTrack = playback.currentTrack
        ?: ui.homeData.focusTracks.firstOrNull()
        ?: ui.homeData.trendingTracks.firstOrNull()
        ?: ui.homeData.recentlyPlayed.firstOrNull()
    val moodQueries = remember(ui.homeData) { buildBrowseQueries(ui.homeData).take(6) }
    val quickPicks = remember(ui.homeData) { buildQuickPicks(ui.homeData) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 160.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        if (featuredTrack != null) {
            item {
                FeaturedTrackHeroCard(
                    track = featuredTrack,
                    isCurrent = playback.currentTrack?.trackId == featuredTrack.trackId,
                    isPlaying = playback.isPlaying,
                    progress = if (playback.playbackDuration > 0L && playback.currentTrack?.trackId == featuredTrack.trackId) {
                        playback.playbackPosition.toFloat() / playback.playbackDuration.toFloat()
                    } else {
                        0f
                    },
                    onPrimaryAction = {
                        if (playback.currentTrack?.trackId == featuredTrack.trackId) {
                            MusicPlaybackManager.togglePlay()
                        } else {
                            onPlayTrack(featuredTrack, resolveTrackQueue(featuredTrack, ui.homeData, playback))
                        }
                    },
                    onSecondaryAction = {
                        if (playback.currentTrack?.trackId == featuredTrack.trackId) {
                            onExpandPlayer()
                        } else {
                            onQuickSearch(featuredTrack.tags.firstOrNull() ?: featuredTrack.artistName ?: "focus")
                        }
                    }
                )
            }
        }

        if (quickPicks.isNotEmpty()) {
            item {
                SectionTitle(
                    icon = Icons.Default.Headphones,
                    title = "Lancer vite",
                    subtitle = "Pour toi"
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    quickPicks.chunked(2).forEach { rowTracks ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            rowTracks.forEach { track ->
                                QuickPickCard(
                                    track = track,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onPlayTrack(track, resolveTrackQueue(track, ui.homeData, playback)) }
                                )
                            }
                            if (rowTracks.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        item {
            SectionTitle(
                icon = Icons.AutoMirrored.Filled.QueueMusic,
                title = "Playlists",
                subtitle = "Mix"
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(ui.homeData.playlists, key = { it.id }) { playlist ->
                    PlaylistCard(playlist = playlist, onClick = { onOpenPlaylist(playlist) })
                }
            }
        }

        if (ui.homeData.recentlyPlayed.isNotEmpty()) {
            item {
                SectionTitle(
                    icon = Icons.Default.Timer,
                    title = "Recents",
                    subtitle = ""
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(ui.homeData.recentlyPlayed, key = { it.trackId ?: it.trackName.orEmpty() }) { track ->
                        TrackHighlightCard(
                            track = track,
                            onClick = { onPlayTrack(track, ui.homeData.recentlyPlayed) },
                            accentLabel = "Reprendre"
                        )
                    }
                }
            }
        }

        if (ui.homeData.focusTracks.isNotEmpty()) {
            item {
                SectionTitle(
                    icon = Icons.Default.GraphicEq,
                    title = "Ambiances",
                    subtitle = ""
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(ui.homeData.focusTracks.take(10), key = { it.trackId ?: it.trackName.orEmpty() }) { track ->
                        TrackHighlightCard(
                            track = track,
                            onClick = { onPlayTrack(track, ui.homeData.focusTracks) },
                            accentLabel = track.tags.firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "Focus"
                        )
                    }
                }
            }
        }

        if (ui.homeData.mostPlayed.isNotEmpty()) {
            item {
                SectionTitle(
                    icon = Icons.Default.Star,
                    title = "Le plus ecoute",
                    subtitle = ""
                )
            }
            items(ui.homeData.mostPlayed.take(5), key = { it.trackId ?: it.trackName.orEmpty() }) { track ->
                MusicTrackRow(
                    track = track,
                    isCurrent = playback.currentTrack?.trackId == track.trackId,
                    isPlaying = playback.isPlaying && playback.currentTrack?.trackId == track.trackId,
                    onPlay = { onPlayTrack(track, ui.homeData.mostPlayed) },
                    onToggleLibrary = { onToggleLibrary(track) },
                    onToggleDownload = { onToggleDownload(track) },
                    trailingStat = "${track.playCount} ecoutes"
                )
            }
        }

        if (ui.homeData.trendingTracks.isNotEmpty()) {
            item {
                SectionTitle(
                    icon = Icons.Default.Equalizer,
                    title = "Tendances",
                    subtitle = ""
                )
            }
            items(ui.homeData.trendingTracks.take(6), key = { it.trackId ?: it.trackName.orEmpty() }) { track ->
                MusicTrackRow(
                    track = track,
                    isCurrent = playback.currentTrack?.trackId == track.trackId,
                    isPlaying = playback.isPlaying && playback.currentTrack?.trackId == track.trackId,
                    onPlay = { onPlayTrack(track, ui.homeData.trendingTracks) },
                    onToggleLibrary = { onToggleLibrary(track) },
                    onToggleDownload = { onToggleDownload(track) }
                )
            }
        }

        if (moodQueries.isNotEmpty()) {
            item {
                SectionTitle(
                    icon = Icons.Default.Search,
                    title = "Parcourir",
                    subtitle = ""
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(moodQueries, key = { it }) { mood ->
                        BrowseMoodCard(
                            label = mood,
                            modifier = Modifier.width(146.dp),
                            onClick = { onQuickSearch(mood) }
                        )
                    }
                }
            }
        }

        if (
            ui.homeData.playlists.isEmpty() &&
            ui.homeData.recentlyPlayed.isEmpty() &&
            ui.homeData.mostPlayed.isEmpty() &&
            ui.homeData.focusTracks.isEmpty() &&
            ui.homeData.trendingTracks.isEmpty()
        ) {
            item {
                MusicStateCard(
                    title = "Musique",
                    message = "Du son arrive ici."
                )
            }
        }
    }
}

@Composable
private fun MusicSearchContent(
    ui: MusicUiState,
    playback: MusicPlaybackState,
    onQueryChange: (String) -> Unit,
    onOpenPlaylist: (MusicPlaylist) -> Unit,
    onPlayTrack: (TrackDto, List<TrackDto>) -> Unit,
    onToggleLibrary: (TrackDto) -> Unit,
    onToggleDownload: (TrackDto) -> Unit
) {
    val quickQueries = remember(ui.homeData) { buildBrowseQueries(ui.homeData) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 160.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SoftCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(0.dp)) {
                OutlinedTextField(
                    value = ui.query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Titre, artiste, ambiance") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = PinkPrimary.copy(alpha = 0.30f),
                        unfocusedBorderColor = BorderSoft
                    ),
                    singleLine = true
                )
            }
        }

        if (ui.query.isBlank()) {
            item {
                SectionTitle(
                    icon = Icons.Default.GraphicEq,
                    title = "Explorer",
                    subtitle = "Ambiances"
                )
            }
            item {
                BrowseMoodGrid(
                    labels = quickQueries.take(6),
                    onClick = onQueryChange
                )
            }
            if (ui.homeData.playlists.isNotEmpty()) {
                item {
                    SectionTitle(
                        icon = Icons.AutoMirrored.Filled.QueueMusic,
                        title = "Playlists",
                        subtitle = "A lancer"
                    )
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(ui.homeData.playlists.take(6), key = { it.id }) { playlist ->
                            PlaylistCard(playlist = playlist, onClick = { onOpenPlaylist(playlist) })
                        }
                    }
                }
            }
            if (ui.homeData.trendingTracks.isNotEmpty()) {
                item {
                    SectionTitle(
                        icon = Icons.Default.Star,
                        title = "A ecouter",
                        subtitle = "Maintenant"
                    )
                }
                items(ui.homeData.trendingTracks.take(8), key = { it.trackId ?: it.trackName.orEmpty() }) { track ->
                    MusicTrackRow(
                        track = track,
                        isCurrent = playback.currentTrack?.trackId == track.trackId,
                        isPlaying = playback.isPlaying && playback.currentTrack?.trackId == track.trackId,
                        onPlay = { onPlayTrack(track, ui.homeData.trendingTracks) },
                        onToggleLibrary = { onToggleLibrary(track) },
                        onToggleDownload = { onToggleDownload(track) }
                    )
                }
            }
        } else if (ui.isSearching) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PinkPrimary)
                }
            }
        } else if (ui.searchResults.isEmpty()) {
            item {
                SectionTitle(
                    icon = Icons.Default.Star,
                    title = "Suggestions",
                    subtitle = "Pour toi"
                )
            }
            items(ui.homeData.focusTracks.take(8), key = { it.trackId ?: it.trackName.orEmpty() }) { track ->
                MusicTrackRow(
                    track = track,
                    isCurrent = playback.currentTrack?.trackId == track.trackId,
                    isPlaying = playback.isPlaying && playback.currentTrack?.trackId == track.trackId,
                    onPlay = { onPlayTrack(track, ui.homeData.focusTracks) },
                    onToggleLibrary = { onToggleLibrary(track) },
                    onToggleDownload = { onToggleDownload(track) }
                )
            }
        } else {
            item {
                SectionTitle(
                    icon = Icons.Default.Search,
                    title = "Resultats",
                    subtitle = "${ui.searchResults.size} titres"
                )
            }
            items(ui.searchResults, key = { it.trackId ?: it.trackName.orEmpty() }) { track ->
                MusicTrackRow(
                    track = track,
                    isCurrent = playback.currentTrack?.trackId == track.trackId,
                    isPlaying = playback.isPlaying && playback.currentTrack?.trackId == track.trackId,
                    onPlay = { onPlayTrack(track, ui.searchResults) },
                    onToggleLibrary = { onToggleLibrary(track) },
                    onToggleDownload = { onToggleDownload(track) }
                )
            }
        }
    }
}

@Composable
private fun MusicLibraryContent(
    ui: MusicUiState,
    playback: MusicPlaybackState,
    onSelectTab: (MusicLibraryTab) -> Unit,
    onPlayTrack: (TrackDto, List<TrackDto>) -> Unit,
    onToggleLibrary: (TrackDto) -> Unit,
    onToggleDownload: (TrackDto) -> Unit
) {
    val filteredTracks = remember(ui.libraryTracks, ui.libraryTab) {
        when (ui.libraryTab) {
            MusicLibraryTab.SAVED -> ui.libraryTracks.filter { it.isInLibrary }
            MusicLibraryTab.DOWNLOADED -> ui.libraryTracks.filter { it.isDownloaded }
            MusicLibraryTab.RECENT -> ui.libraryTracks.sortedByDescending { it.lastPlayedAt ?: 0L }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 160.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            LibrarySummaryCard(
                libraryTab = ui.libraryTab,
                total = filteredTracks.size
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MusicLibraryTab.entries.forEach { tab ->
                    SuggestionChip(
                        label = tab.label,
                        selected = ui.libraryTab == tab,
                        onClick = { onSelectTab(tab) }
                    )
                }
            }
        }

        if (ui.isLibraryLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PinkPrimary)
                }
            }
        } else if (filteredTracks.isEmpty()) {
            item {
                MusicStateCard(
                    title = "Bibliotheque",
                    message = when (ui.libraryTab) {
                        MusicLibraryTab.SAVED -> "Ajoute tes titres."
                        MusicLibraryTab.DOWNLOADED -> "Aucun titre hors ligne."
                        MusicLibraryTab.RECENT -> "Tes dernieres ecoutes apparaitront ici."
                    }
                )
            }
        } else {
            item {
                SectionTitle(
                    icon = Icons.Default.LibraryAdd,
                    title = when (ui.libraryTab) {
                        MusicLibraryTab.SAVED -> "Sauvegardes"
                        MusicLibraryTab.DOWNLOADED -> "Telecharges"
                        MusicLibraryTab.RECENT -> "Recents"
                    },
                    subtitle = "${filteredTracks.size} titres"
                )
            }
            items(filteredTracks, key = { it.trackId ?: it.trackName.orEmpty() }) { track ->
                MusicTrackRow(
                    track = track,
                    isCurrent = playback.currentTrack?.trackId == track.trackId,
                    isPlaying = playback.isPlaying && playback.currentTrack?.trackId == track.trackId,
                    onPlay = { onPlayTrack(track, filteredTracks) },
                    onToggleLibrary = { onToggleLibrary(track) },
                    onToggleDownload = { onToggleDownload(track) }
                )
            }
        }
    }
}

@Composable
private fun PlaylistDetailsContent(
    playlist: MusicPlaylist,
    tracks: List<TrackDto>,
    isLoading: Boolean,
    error: String?,
    playback: MusicPlaybackState,
    onBack: () -> Unit,
    onPlayTrack: (TrackDto, List<TrackDto>) -> Unit,
    onToggleLibrary: (TrackDto) -> Unit,
    onToggleDownload: (TrackDto) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 160.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PlaylistHeroHeader(
                playlist = playlist,
                tracks = tracks,
                onBack = onBack,
                onPlayAll = {
                    if (tracks.isNotEmpty()) {
                        onPlayTrack(tracks.first(), tracks)
                    }
                }
            )
        }

        when {
            isLoading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PinkPrimary)
                    }
                }
            }

            error != null -> {
                item { MusicStateCard(title = playlist.title, message = "Selection en preparation.") }
            }

            tracks.isEmpty() -> {
                item { MusicStateCard(title = playlist.title, message = "Quelques titres arrivent ici.") }
            }

            else -> {
                item {
                    SectionTitle(
                        icon = Icons.Default.Headphones,
                        title = "Titres",
                        subtitle = "${tracks.size} pistes"
                    )
                }
                items(tracks, key = { it.trackId ?: it.trackName.orEmpty() }) { track ->
                    MusicTrackRow(
                        track = track,
                        isCurrent = playback.currentTrack?.trackId == track.trackId,
                        isPlaying = playback.isPlaying && playback.currentTrack?.trackId == track.trackId,
                        onPlay = { onPlayTrack(track, tracks) },
                        onToggleLibrary = { onToggleLibrary(track) },
                        onToggleDownload = { onToggleDownload(track) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturedTrackHeroCard(
    track: TrackDto,
    isCurrent: Boolean,
    isPlaying: Boolean,
    progress: Float,
    onPrimaryAction: () -> Unit,
    onSecondaryAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFFF2DCE6))
    ) {
        if (!track.artworkUrl100.isNullOrBlank()) {
            AsyncImage(
                model = track.artworkUrl100,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x33FFFFFF),
                            Color(0xAA24121A),
                            Color(0xEE1E1016)
                        )
                    )
                )
        )
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White.copy(alpha = 0.9f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isCurrent) "En lecture" else "Focus du moment",
                            color = PinkPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = track.trackName ?: "Titre inconnu",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = track.artistName ?: "Artiste inconnu",
                        color = Color.White.copy(alpha = 0.82f),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(14.dp))
                ArtworkThumb(
                    track = track,
                    modifier = Modifier
                        .size(92.dp)
                        .clip(RoundedCornerShape(22.dp))
                )
            }
            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = PinkPrimary,
                trackColor = PinkPrimary.copy(alpha = 0.12f)
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PrimaryMusicButton(
                    text = if (isCurrent && isPlaying) "Pause" else "Ecouter",
                    onClick = onPrimaryAction,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(
                    onClick = onSecondaryAction,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        if (isCurrent) "Ouvrir" else "Explorer",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistCard(playlist: MusicPlaylist, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(176.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .background(Color(0xFFF1DDE6))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (!playlist.artworkUrl.isNullOrBlank()) {
                AsyncImage(
                    model = playlist.artworkUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(musicGradient(playlist.id)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.75f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null, tint = PinkPrimary)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xCC1E1016))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
            ) {
                Text(
                    text = playlist.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                playlist.subtitle?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = it,
                        color = Color.White.copy(alpha = 0.82f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (playlist.tracksCount > 0) {
                    Spacer(Modifier.height(8.dp))
                    MetaChip(label = "${playlist.tracksCount} titres")
                }
            }
        }
    }
}

@Composable
private fun TrackHighlightCard(track: TrackDto, onClick: () -> Unit, accentLabel: String) {
    Box(
        modifier = Modifier
            .width(158.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable { onClick() }
    ) {
        ArtworkThumb(
            track = track,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(22.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xAA24121A))
                    )
                )
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.88f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = accentLabel,
                        color = PinkPrimary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = track.trackName ?: "Titre inconnu",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artistName ?: "Artiste inconnu",
                    color = Color.White.copy(alpha = 0.82f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun QuickPickCard(
    track: TrackDto,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    SoftCard(
        modifier = modifier.clickable { onClick() },
        padding = PaddingValues(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ArtworkThumb(
                track = track,
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.trackName ?: "Titre",
                    color = TextDark,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = track.artistName ?: "Artiste",
                    color = TextGray,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun BrowseMoodGrid(labels: List<String>, onClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        labels.chunked(2).forEach { rowLabels ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowLabels.forEach { label ->
                    BrowseMoodCard(
                        label = label,
                        modifier = Modifier.weight(1f),
                        onClick = { onClick(label) }
                    )
                }
                if (rowLabels.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun BrowseMoodCard(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(musicGradient(label))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Headphones, contentDescription = null, tint = PinkPrimary)
            }
            Text(
                text = label.replaceFirstChar { it.uppercase() },
                color = TextDark,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LibrarySummaryCard(libraryTab: MusicLibraryTab, total: Int) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = when (libraryTab) {
                        MusicLibraryTab.SAVED -> "Ta bibliotheque"
                        MusicLibraryTab.DOWNLOADED -> "Hors ligne"
                        MusicLibraryTab.RECENT -> "Historique"
                    },
                    color = TextDark,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$total titres",
                    color = TextGray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            MetaChip(
                label = when (libraryTab) {
                    MusicLibraryTab.SAVED -> "Ajoutes"
                    MusicLibraryTab.DOWNLOADED -> "Hors ligne"
                    MusicLibraryTab.RECENT -> "Recents"
                }
            )
        }
    }
}

@Composable
private fun PlaylistHeroHeader(
    playlist: MusicPlaylist,
    tracks: List<TrackDto>,
    onBack: () -> Unit,
    onPlayAll: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(musicGradient(playlist.id))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = TextDark)
                }
                if (tracks.isNotEmpty()) {
                    MetaChip(label = "${tracks.size} titres")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!playlist.artworkUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = playlist.artworkUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(104.dp)
                            .clip(RoundedCornerShape(24.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = null,
                            tint = PinkPrimary,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = playlist.title,
                        color = TextDark,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    playlist.subtitle?.takeIf { it.isNotBlank() }?.let {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = it,
                            color = TextGray,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    PrimaryMusicButton(
                        text = "Tout lire",
                        onClick = onPlayAll,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun MusicTrackRow(
    track: TrackDto,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onToggleLibrary: () -> Unit,
    onToggleDownload: () -> Unit,
    trailingStat: String? = null
) {
    SoftCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ArtworkThumb(track = track, modifier = Modifier.size(64.dp).clip(RoundedCornerShape(18.dp)))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = track.trackName ?: "Titre inconnu",
                        color = TextDark,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isCurrent) {
                        Text(
                            text = if (isPlaying) "En lecture" else "En pause",
                            color = PinkPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = buildString {
                        append(track.artistName ?: "Artiste inconnu")
                        track.albumName?.takeIf { it.isNotBlank() }?.let {
                            append(" • ")
                            append(it)
                        }
                    },
                    color = TextGray,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    MetaChip(label = track.durationLabel)
                    if (track.isDownloaded) {
                        MetaChip(label = "Hors ligne")
                    } else if (track.isDownloadAllowed) {
                        MetaChip(label = "Telechargeable")
                    }
                    if (!trailingStat.isNullOrBlank()) {
                        MetaChip(label = trailingStat)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = onToggleLibrary) {
                        Icon(
                            imageVector = Icons.Default.LibraryAdd,
                            contentDescription = null,
                            tint = if (track.isInLibrary) PinkPrimary else TextGray
                        )
                    }
                    IconButton(onClick = onToggleDownload) {
                        Icon(
                            imageVector = if (track.isDownloaded) Icons.Default.DownloadDone else Icons.Default.Download,
                            contentDescription = null,
                            tint = if (track.isDownloaded) PinkPrimary else TextGray
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(PinkPrimary.copy(alpha = if (isCurrent) 1f else 0.12f))
                        .clickable { onPlay() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCurrent && isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (isCurrent) Color.White else PinkPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(PinkPrimary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = PinkPrimary)
        }
        Column {
            Text(
                text = title,
                color = TextDark,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            if (subtitle.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = TextGray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun SuggestionChip(label: String, selected: Boolean = false, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) PinkPrimary.copy(alpha = 0.12f) else Color.White)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = if (selected) PinkPrimary else TextDark,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun MetaChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(PinkPrimary.copy(alpha = 0.08f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = PinkPrimary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MusicStateCard(title: String, message: String) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = title,
                color = TextDark,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                color = TextGray,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun PrimaryMusicButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(PinkPrimary)
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ArtworkThumb(track: TrackDto, modifier: Modifier = Modifier) {
    val art = track.artworkUrl100
    if (art.isNullOrBlank()) {
        Box(
            modifier = modifier
                .background(Brush.linearGradient(listOf(PinkPrimary.copy(alpha = 0.25f), Color.White))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Headphones, contentDescription = null, tint = PinkPrimary)
        }
        return
    }

    AsyncImage(
        model = art,
        contentDescription = null,
        modifier = modifier.background(Color.White),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun ExpandedMusicPlayer(
    track: TrackDto,
    playback: MusicPlaybackState,
    onClose: () -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit
) {
    var sliderPosition by remember(playback.playbackPosition, playback.playbackDuration) {
        mutableFloatStateOf(
            if (playback.playbackDuration > 0L) {
                playback.playbackPosition.toFloat() / playback.playbackDuration.toFloat()
            } else {
                0f
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFFCE8F1), Color(0xFFF8EFF4), Color.White)))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 18.dp),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = TextDark)
                    }
                    Text(
                        text = "Lecture",
                        color = TextDark,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.size(48.dp))
                }
            }
            item {
                ArtworkThumb(
                    track = track,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(28.dp))
                )
            }
            item {
                Column {
                    Text(
                        text = track.trackName ?: "Titre inconnu",
                        color = TextDark,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = track.artistName ?: "Artiste inconnu",
                        color = TextGray,
                        style = MaterialTheme.typography.titleMedium
                    )
                    track.albumName?.takeIf { it.isNotBlank() }?.let {
                        Spacer(Modifier.height(2.dp))
                        Text(it, color = TextGray, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetaChip(label = track.durationLabel)
                    track.releasedAt?.takeIf { it.isNotBlank() }?.let { MetaChip(label = it) }
                    if (track.isDownloaded) {
                        MetaChip(label = "Hors ligne")
                    }
                }
            }
            item {
                Column {
                    Slider(
                        value = sliderPosition,
                        onValueChange = { sliderPosition = it },
                        onValueChangeFinished = {
                            val duration = playback.playbackDuration
                            onSeek((sliderPosition * duration).toLong())
                        }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatDuration(playback.playbackPosition), color = TextGray)
                        Text(formatDuration(playback.playbackDuration), color = TextGray)
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPrevious) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = null, tint = TextDark, modifier = Modifier.size(40.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(82.dp)
                            .clip(CircleShape)
                            .background(PinkPrimary)
                            .clickable { onTogglePlay() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (playback.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                    IconButton(onClick = onNext) {
                        Icon(Icons.Default.SkipNext, contentDescription = null, tint = TextDark, modifier = Modifier.size(40.dp))
                    }
                }
            }
            if (track.tags.isNotEmpty()) {
                item {
                    SoftCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Ambiance du morceau",
                                color = TextDark,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(track.tags.take(8)) { tag ->
                                    MetaChip(label = tag.replaceFirstChar { it.uppercase() })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0L) return "--:--"
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

private fun buildBrowseQueries(homeData: MusicHomeData): List<String> {
    val dynamic = (homeData.focusTracks + homeData.trendingTracks)
        .flatMap { track ->
            buildList {
                track.tags.firstOrNull()?.takeIf { it.isNotBlank() }?.let { add(it) }
                track.artistName?.takeIf { it.isNotBlank() }?.let { add(it) }
            }
        }
        .map { it.lowercase() }
        .distinct()

    return (listOf(
        "deep focus",
        "lofi study",
        "soft piano",
        "ambient coding",
        "jazz lounge",
        "soundtrack"
    ) + dynamic)
        .distinct()
        .take(8)
}

private fun buildQuickPicks(homeData: MusicHomeData): List<TrackDto> {
    return (homeData.recentlyPlayed + homeData.focusTracks + homeData.trendingTracks)
        .distinctBy { it.trackId ?: "${it.trackName}_${it.artistName}" }
        .take(6)
}

private fun resolveTrackQueue(
    track: TrackDto,
    homeData: MusicHomeData,
    playback: MusicPlaybackState
): List<TrackDto> {
    if (playback.currentTrack?.trackId == track.trackId && playback.queue.isNotEmpty()) {
        return playback.queue
    }

    return when {
        homeData.focusTracks.any { it.trackId == track.trackId } -> homeData.focusTracks
        homeData.trendingTracks.any { it.trackId == track.trackId } -> homeData.trendingTracks
        homeData.recentlyPlayed.any { it.trackId == track.trackId } -> homeData.recentlyPlayed
        homeData.mostPlayed.any { it.trackId == track.trackId } -> homeData.mostPlayed
        else -> listOf(track)
    }
}

private fun musicGradient(seed: String): Brush {
    val palettes = listOf(
        listOf(Color(0xFFFFD5E6), Color(0xFFFFF2F7)),
        listOf(Color(0xFFDDF2FF), Color(0xFFF1FAFF)),
        listOf(Color(0xFFFFE7D7), Color(0xFFFFF5ED)),
        listOf(Color(0xFFE5ECFF), Color(0xFFF6F8FF)),
        listOf(Color(0xFFE6F7EF), Color(0xFFF4FBF8))
    )
    val palette = palettes[abs(seed.hashCode()) % palettes.size]
    return Brush.linearGradient(palette)
}
