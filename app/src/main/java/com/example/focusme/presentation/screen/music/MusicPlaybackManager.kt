package com.example.focusme.presentation.screen.music

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.core.os.bundleOf
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.focusme.data.api.TrackDto
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object MusicPlaybackManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow(MusicPlaybackState())
    val state: StateFlow<MusicPlaybackState> = _state.asStateFlow()

    private var appContext: Context? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var pendingQueue: Pair<List<TrackDto>, Int>? = null
    private var listenerRegistered = false

    fun initialize(context: Context) {
        val applicationContext = context.applicationContext
        if (appContext != null && controllerFuture != null) return
        appContext = applicationContext

        val token = SessionToken(applicationContext, ComponentName(applicationContext, MusicService::class.java))
        controllerFuture = MediaController.Builder(applicationContext, token).buildAsync().also { future ->
            future.addListener(
                {
                    val controller = controller()
                    if (controller != null && !listenerRegistered) {
                        listenerRegistered = true
                        controller.addListener(playerListener)
                        syncFromController()
                        pendingQueue?.let { (tracks, index) ->
                            pendingQueue = null
                            playTracks(tracks, index)
                        }
                    }
                },
                MoreExecutors.directExecutor()
            )
        }

        scope.launch {
            while (true) {
                syncPlaybackProgress()
                delay(1000)
            }
        }
    }

    fun playTracks(tracks: List<TrackDto>, startIndex: Int) {
        val playableTracks = tracks.filter { it.isPlayable }
        if (playableTracks.isEmpty()) return

        val targetTrack = tracks.getOrNull(startIndex)
        val safeIndex = playableTracks.indexOfFirst { it.trackId == targetTrack?.trackId }.coerceAtLeast(0)
        val controller = controller()

        _state.update {
            it.copy(
                queue = playableTracks,
                currentIndex = safeIndex
            )
        }

        if (controller == null) {
            pendingQueue = playableTracks to safeIndex
            return
        }

        val mediaItems = playableTracks.map { it.toMediaItem() }
        val currentMediaIds = (0 until controller.mediaItemCount).map { index -> controller.getMediaItemAt(index).mediaId }
        val nextMediaIds = mediaItems.map { it.mediaId }

        if (currentMediaIds != nextMediaIds) {
            controller.setMediaItems(mediaItems, safeIndex, 0L)
            controller.prepare()
        } else if (controller.currentMediaItemIndex != safeIndex) {
            controller.seekTo(safeIndex, 0L)
        }
        controller.play()
        syncFromController()
    }

    fun togglePlay() {
        controller()?.let { player ->
            if (player.isPlaying) player.pause() else player.play()
            syncFromController()
        }
    }

    fun play() {
        controller()?.play()
        syncFromController()
    }

    fun pause() {
        controller()?.pause()
        syncFromController()
    }

    fun seekTo(positionMs: Long) {
        controller()?.seekTo(positionMs)
        syncFromController()
    }

    fun next() {
        controller()?.seekToNextMediaItem()
        syncFromController()
    }

    fun previous() {
        controller()?.seekToPreviousMediaItem()
        syncFromController()
    }

    fun currentTrack(): TrackDto? = state.value.currentTrack

    fun release() {
        scope.cancel()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
        listenerRegistered = false
        appContext = null
    }

    private fun controller(): MediaController? {
        val future = controllerFuture ?: return null
        if (!future.isDone) return null
        return runCatching { future.get() }.getOrNull()
    }

    private fun syncPlaybackProgress() {
        val controller = controller() ?: return
        _state.update {
            it.copy(
                playbackPosition = controller.currentPosition.coerceAtLeast(0L),
                playbackDuration = controller.duration.takeIf { duration -> duration > 0 } ?: 0L,
                isPlaying = controller.isPlaying,
                isConnected = true
            )
        }
    }

    private fun syncFromController() {
        val controller = controller() ?: return
        val queue = (0 until controller.mediaItemCount).mapNotNull { index ->
            controller.getMediaItemAt(index).toTrack()
        }
        _state.update {
            it.copy(
                queue = queue,
                currentIndex = controller.currentMediaItemIndex.coerceAtLeast(0),
                isPlaying = controller.isPlaying,
                playbackPosition = controller.currentPosition.coerceAtLeast(0L),
                playbackDuration = controller.duration.takeIf { duration -> duration > 0 } ?: 0L,
                isConnected = true
            )
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            syncFromController()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            syncFromController()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            syncFromController()
        }

        override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
            syncFromController()
        }
    }
}

private fun TrackDto.toMediaItem(): MediaItem {
    val extras = bundleOf(
        EXTRA_TRACK_ID to trackId,
        EXTRA_TRACK_NAME to trackName,
        EXTRA_ARTIST_NAME to artistName,
        EXTRA_ALBUM_NAME to albumName,
        EXTRA_ARTWORK_URL to artworkUrl100,
        EXTRA_AUDIO_URL to audioUrl,
        EXTRA_DOWNLOAD_URL to downloadUrl,
        EXTRA_LOCAL_PATH to localPath,
        EXTRA_RELEASED_AT to releasedAt,
        EXTRA_TAGS to tags.toTypedArray(),
        EXTRA_DURATION_MS to (trackTimeMillis ?: 0L),
        EXTRA_IS_DOWNLOADED to isDownloaded,
        EXTRA_IS_IN_LIBRARY to isInLibrary,
        EXTRA_IS_DOWNLOAD_ALLOWED to isDownloadAllowed,
        EXTRA_PLAY_COUNT to playCount,
        EXTRA_LAST_PLAYED_AT to (lastPlayedAt ?: 0L)
    )

    val metadata = MediaMetadata.Builder()
        .setTitle(trackName ?: "Titre inconnu")
        .setArtist(artistName ?: "Artiste inconnu")
        .setAlbumTitle(albumName)
        .setArtworkUri(artworkUrl100?.let(Uri::parse))
        .setExtras(extras)
        .build()

    return MediaItem.Builder()
        .setMediaId(trackId ?: (trackName ?: System.currentTimeMillis().toString()))
        .setUri(playbackUrl ?: "")
        .setMediaMetadata(metadata)
        .build()
}

private fun MediaItem.toTrack(): TrackDto? {
    val extras = mediaMetadata.extras ?: return null
    val tags = extras.getStringArray(EXTRA_TAGS)?.toList().orEmpty()
    val lastPlayed = extras.getLong(EXTRA_LAST_PLAYED_AT).takeIf { it > 0L }
    return TrackDto(
        trackId = extras.getString(EXTRA_TRACK_ID),
        trackName = extras.getString(EXTRA_TRACK_NAME),
        artistName = extras.getString(EXTRA_ARTIST_NAME),
        albumName = extras.getString(EXTRA_ALBUM_NAME),
        artworkUrl100 = extras.getString(EXTRA_ARTWORK_URL),
        audioUrl = extras.getString(EXTRA_AUDIO_URL),
        downloadUrl = extras.getString(EXTRA_DOWNLOAD_URL),
        releasedAt = extras.getString(EXTRA_RELEASED_AT),
        localPath = extras.getString(EXTRA_LOCAL_PATH),
        tags = tags,
        trackTimeMillis = extras.getLong(EXTRA_DURATION_MS).takeIf { it > 0L },
        isDownloaded = extras.getBoolean(EXTRA_IS_DOWNLOADED),
        isInLibrary = extras.getBoolean(EXTRA_IS_IN_LIBRARY),
        isDownloadAllowed = extras.getBoolean(EXTRA_IS_DOWNLOAD_ALLOWED),
        playCount = extras.getInt(EXTRA_PLAY_COUNT),
        lastPlayedAt = lastPlayed
    )
}

private const val EXTRA_TRACK_ID = "music_track_id"
private const val EXTRA_TRACK_NAME = "music_track_name"
private const val EXTRA_ARTIST_NAME = "music_artist_name"
private const val EXTRA_ALBUM_NAME = "music_album_name"
private const val EXTRA_ARTWORK_URL = "music_artwork_url"
private const val EXTRA_AUDIO_URL = "music_audio_url"
private const val EXTRA_DOWNLOAD_URL = "music_download_url"
private const val EXTRA_LOCAL_PATH = "music_local_path"
private const val EXTRA_RELEASED_AT = "music_released_at"
private const val EXTRA_TAGS = "music_tags"
private const val EXTRA_DURATION_MS = "music_duration_ms"
private const val EXTRA_IS_DOWNLOADED = "music_is_downloaded"
private const val EXTRA_IS_IN_LIBRARY = "music_is_in_library"
private const val EXTRA_IS_DOWNLOAD_ALLOWED = "music_is_download_allowed"
private const val EXTRA_PLAY_COUNT = "music_play_count"
private const val EXTRA_LAST_PLAYED_AT = "music_last_played_at"
