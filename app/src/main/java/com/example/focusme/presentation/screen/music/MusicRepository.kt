package com.example.focusme.presentation.screen.music

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.focusme.data.api.AudiusPlaylistNetworkDto
import com.example.focusme.data.api.AudiusTrackNetworkDto
import com.example.focusme.data.api.MusicApi
import com.example.focusme.data.api.MusicApiModule
import com.example.focusme.data.api.TrackDto
import com.example.focusme.data.local.DbProvider
import com.example.focusme.data.local.MusicTrackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

class MusicRepository(
    private val context: Context,
    private val api: MusicApi = MusicApiModule.api
) {
    private val dao = DbProvider.get(context).musicDao()

    suspend fun getHomeData(): MusicHomeData = withContext(Dispatchers.IO) {
        coroutineScope {
            val recentDeferred = async { dao.getRecentlyPlayed().map { it.toDto() } }
            val mostPlayedDeferred = async { dao.getMostPlayed().map { it.toDto() } }
            val trendingDeferred = async { fetchTrendingTracks(limit = 12) }
            val focusDeferred = async { fetchSearchTracks(query = "focus", limit = 12) }
            val playlistsDeferred = async { fetchTrendingPlaylists(limit = 8) }

            MusicHomeData(
                recentlyPlayed = recentDeferred.await(),
                mostPlayed = mostPlayedDeferred.await(),
                trendingTracks = trendingDeferred.await(),
                focusTracks = focusDeferred.await(),
                playlists = playlistsDeferred.await().ifEmpty { fallbackPlaylists() }
            )
        }
    }

    suspend fun search(term: String): List<TrackDto> = withContext(Dispatchers.IO) {
        if (term.isBlank()) return@withContext emptyList()
        val directResults = fetchSearchTracks(query = term.trim(), limit = 30)
        if (directResults.isNotEmpty()) {
            directResults
        } else {
            fetchTrendingTracks(limit = 18)
        }
    }

    suspend fun getPlaylistTracks(playlist: MusicPlaylist): List<TrackDto> = withContext(Dispatchers.IO) {
        try {
            val playlistTracks = api.getPlaylistTracks(playlist.id).data.orEmpty()
                .map { it.toDomain() }
                .map { enrichWithLocalState(it) }
                .filter { it.isPlayable && (it.trackTimeMillis ?: 0L) >= MIN_TRACK_DURATION_MS }
            if (playlistTracks.isNotEmpty()) {
                playlistTracks
            } else {
                fetchSearchTracks(query = playlist.title, limit = 24)
            }
        } catch (throwable: Throwable) {
            Log.e("MusicRepository", "Playlist tracks failed", throwable)
            fetchSearchTracks(query = playlist.title, limit = 24)
        }
    }

    suspend fun getLibrary(): List<TrackDto> = withContext(Dispatchers.IO) {
        dao.getLibrary().map { it.toDto() }
    }

    suspend fun toggleLibrary(track: TrackDto): TrackDto = withContext(Dispatchers.IO) {
        val trackId = track.trackId ?: return@withContext track
        val existing = dao.getTrackById(trackId)
        val updated = if (existing == null) {
            track.toEntity().copy(isInLibrary = true)
        } else {
            existing.copy(isInLibrary = !existing.isInLibrary)
        }
        if (!updated.isInLibrary && !updated.isDownloaded && updated.playCount == 0) {
            dao.deleteTrack(trackId)
            track.copy(isInLibrary = false)
        } else {
            dao.insertTrack(updated)
            updated.toDto()
        }
    }

    suspend fun toggleDownload(track: TrackDto): TrackDto = withContext(Dispatchers.IO) {
        val trackId = track.trackId ?: return@withContext track
        val existing = dao.getTrackById(trackId)
        val safeFileId = trackId.filter { it.isLetterOrDigit() || it == '_' || it == '-' }.ifBlank { "track" }

        if ((existing?.isDownloaded == true) || track.isDownloaded) {
            val file = File(context.getExternalFilesDir("music"), "track_$safeFileId.mp3")
            if (file.exists()) file.delete()
            val base = existing ?: track.toEntity()
            val updated = base.copy(isDownloaded = false, localPath = null)
            if (!updated.isInLibrary && updated.playCount == 0) {
                dao.deleteTrack(trackId)
                return@withContext track.copy(isDownloaded = false, localPath = null)
            }
            dao.insertTrack(updated)
            return@withContext updated.toDto()
        }

        val remoteUrl = track.downloadUrl ?: track.audioUrl ?: return@withContext track
        val file = File(context.getExternalFilesDir("music"), "track_$safeFileId.mp3")
        file.parentFile?.mkdirs()

        URL(remoteUrl).openStream().use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        val updated = (existing ?: track.toEntity()).copy(
            isDownloaded = true,
            isInLibrary = true,
            localPath = file.absolutePath
        )
        dao.insertTrack(updated)
        updated.toDto()
    }

    suspend fun markAsPlayed(track: TrackDto) = withContext(Dispatchers.IO) {
        val trackId = track.trackId ?: return@withContext
        val existing = dao.getTrackById(trackId)
        val updated = if (existing == null) {
            track.toEntity().copy(playCount = 1, lastPlayedAt = System.currentTimeMillis())
        } else {
            existing.copy(
                playCount = existing.playCount + 1,
                lastPlayedAt = System.currentTimeMillis(),
                name = track.trackName ?: existing.name,
                artist = track.artistName ?: existing.artist,
                albumName = track.albumName ?: existing.albumName,
                artworkUrl = track.artworkUrl100 ?: existing.artworkUrl,
                audioUrl = track.audioUrl ?: existing.audioUrl,
                downloadUrl = track.downloadUrl ?: existing.downloadUrl,
                shareUrl = track.shareUrl ?: existing.shareUrl,
                releasedAt = track.releasedAt ?: existing.releasedAt,
                trackTimeMillis = track.trackTimeMillis ?: existing.trackTimeMillis,
                tags = track.tags.joinToString(",").ifBlank { existing.tags },
                isDownloadAllowed = track.isDownloadAllowed || existing.isDownloadAllowed
            )
        }
        dao.insertTrack(updated)
    }

    private suspend fun fetchTrendingTracks(limit: Int): List<TrackDto> {
        return try {
            api.getTrendingTracks(limit = limit).data.orEmpty()
                .map { it.toDomain() }
                .map { enrichWithLocalState(it) }
                .filter { it.isPlayable && (it.trackTimeMillis ?: 0L) >= MIN_TRACK_DURATION_MS }
        } catch (throwable: Throwable) {
            Log.e("MusicRepository", "Trending tracks failed", throwable)
            emptyList()
        }
    }

    private suspend fun fetchSearchTracks(query: String, limit: Int): List<TrackDto> {
        return try {
            api.searchTracks(query = query, limit = limit).data.orEmpty()
                .map { it.toDomain() }
                .map { enrichWithLocalState(it) }
                .filter { it.isPlayable && (it.trackTimeMillis ?: 0L) >= MIN_TRACK_DURATION_MS }
        } catch (throwable: Throwable) {
            Log.e("MusicRepository", "Search tracks failed", throwable)
            emptyList()
        }
    }

    private suspend fun fetchTrendingPlaylists(limit: Int): List<MusicPlaylist> {
        return try {
            api.getTrendingPlaylists(limit = limit).data.orEmpty()
                .map { it.toPlaylist() }
        } catch (throwable: Throwable) {
            Log.e("MusicRepository", "Trending playlists failed", throwable)
            emptyList()
        }
    }

    private suspend fun enrichWithLocalState(track: TrackDto): TrackDto {
        val trackId = track.trackId ?: return track
        val local = dao.getTrackById(trackId) ?: return track
        return track.copy(
            localPath = local.localPath,
            isDownloaded = local.isDownloaded,
            isInLibrary = local.isInLibrary,
            playCount = local.playCount,
            lastPlayedAt = local.lastPlayedAt
        )
    }

    private fun AudiusTrackNetworkDto.toDomain(): TrackDto {
        val streamId = id?.takeIf { it.isNotBlank() }
        val streamUrl = streamId?.let { buildStreamUrl(it) }
        val tags = listOfNotNull(genre?.takeIf { it.isNotBlank() }).distinct()
        return TrackDto(
            trackId = streamId,
            trackName = title,
            artistName = user?.name ?: user?.handle,
            artworkUrl100 = artwork?.large ?: artwork?.medium ?: artwork?.small,
            audioUrl = streamUrl,
            downloadUrl = if (downloadable == true) streamUrl else null,
            shareUrl = streamId?.let { "https://audius.co/tracks/$it" },
            releasedAt = releaseDate,
            tags = tags,
            trackTimeMillis = duration?.times(1000),
            isDownloadAllowed = downloadable == true
        )
    }

    private fun AudiusPlaylistNetworkDto.toPlaylist(): MusicPlaylist {
        val artworkUrl = artwork?.large ?: artwork?.medium ?: artwork?.small
        val shortSubtitle = when {
            !user?.name.isNullOrBlank() -> user?.name
            !description.isNullOrBlank() -> description
            else -> null
        }
        return MusicPlaylist(
            id = id.orEmpty(),
            title = playlistName ?: "Playlist",
            subtitle = shortSubtitle,
            artworkUrl = artworkUrl,
            tracksCount = trackCount ?: 0
        )
    }

    private fun TrackDto.toEntity(): MusicTrackEntity {
        return MusicTrackEntity(
            id = trackId ?: (audioUrl ?: shareUrl ?: "${trackName.orEmpty()}_${artistName.orEmpty()}"),
            name = trackName ?: "Titre inconnu",
            artist = artistName ?: "Artiste inconnu",
            albumName = albumName,
            artworkUrl = artworkUrl100,
            audioUrl = audioUrl,
            downloadUrl = downloadUrl,
            shareUrl = shareUrl,
            releasedAt = releasedAt,
            localPath = localPath,
            trackTimeMillis = trackTimeMillis,
            tags = tags.joinToString(",").ifBlank { null },
            isDownloaded = isDownloaded,
            isInLibrary = isInLibrary,
            isDownloadAllowed = isDownloadAllowed,
            playCount = playCount,
            lastPlayedAt = lastPlayedAt
        )
    }

    private fun MusicTrackEntity.toDto(): TrackDto {
        return TrackDto(
            trackId = id,
            trackName = name,
            artistName = artist,
            albumName = albumName,
            artworkUrl100 = artworkUrl,
            audioUrl = audioUrl,
            downloadUrl = downloadUrl,
            shareUrl = shareUrl,
            releasedAt = releasedAt,
            tags = tags?.split(",")?.filter { it.isNotBlank() }.orEmpty(),
            trackTimeMillis = trackTimeMillis,
            localPath = localPath,
            isDownloaded = isDownloaded,
            isInLibrary = isInLibrary,
            isDownloadAllowed = isDownloadAllowed,
            playCount = playCount,
            lastPlayedAt = lastPlayedAt
        )
    }

    private fun buildStreamUrl(trackId: String): String {
        return Uri.Builder()
            .scheme("https")
            .authority("api.audius.co")
            .appendPath("v1")
            .appendPath("tracks")
            .appendPath(trackId)
            .appendPath("stream")
            .appendQueryParameter("app_name", APP_NAME)
            .build()
            .toString()
    }

    companion object {
        private const val APP_NAME = "FocusMe"
        private const val MIN_TRACK_DURATION_MS = 60_000L
    }
}
