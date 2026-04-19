package com.example.focusme.presentation.screen.music

import android.content.Context
import android.util.Log
import com.example.focusme.data.api.ApiClient
import com.example.focusme.data.api.MusicApi
import com.example.focusme.data.api.MusicPackDto
import com.example.focusme.data.api.MusicTrackNetworkDto
import com.example.focusme.data.api.SubscribeRequest
import com.example.focusme.data.api.TrackDto
import com.example.focusme.data.api.AudiusClient
import com.example.focusme.data.api.AudiusTrackNetworkDto
import com.example.focusme.data.local.DbProvider
import com.example.focusme.data.local.MusicTrackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

class MusicRepository(
    private val context: Context
) {
    private val api: MusicApi = ApiClient.createRetrofit(context).create(MusicApi::class.java)
    private val dao = DbProvider.get(context).musicDao()

    suspend fun getHomeData(): MusicHomeData = withContext(Dispatchers.IO) {
        coroutineScope {
            // Auto-seed si la base est vide pour ne pas avoir d'écran blanc
            val packs = fetchAvailablePacks()
            if (packs.isEmpty()) { seedIfEmpty() }

            val recentDeferred = async { dao.getRecentlyPlayed().map { it.toDto() } }
            val myTracksDeferred = async { fetchMyTracks() }
            
            val myTracks = myTracksDeferred.await()
            val externalTracks = if (isSubscribedToAudius()) fetchAudiusTrending() else emptyList()

            MusicHomeData(
                recentlyPlayed = recentDeferred.await(),
                trendingTracks = (myTracks + externalTracks).take(30),
                focusTracks = myTracks.filter { it.tags.any { t -> t.contains("lofi", true) || t.contains("focus", true) } },
                playlists = packs.map { it.toPlaylist() }
            )
        }
    }

    private fun isSubscribedToAudius(): Boolean = true // Activé par défaut pour "le plus de chansons possible"

    private suspend fun fetchAudiusTrending(): List<TrackDto> {
        return try {
            AudiusClient.api.getTrending().data.orEmpty().map { it.toDomain() }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun fetchAvailablePacks(): List<MusicPackDto> = withContext(Dispatchers.IO) {
        try { api.getAvailablePacks() } catch (e: Exception) { emptyList() }
    }

    suspend fun updateSubscription(packId: String, subscribe: Boolean) = withContext(Dispatchers.IO) {
        try { api.updateSubscription(SubscribeRequest(packId, subscribe)) } catch (e: Exception) { }
    }

    suspend fun search(term: String): List<TrackDto> = withContext(Dispatchers.IO) {
        val localResults = fetchMyTracks().filter { 
            it.trackName?.contains(term, true) == true || it.artistName?.contains(term, true) == true 
        }
        
        val externalResults = if (isSubscribedToAudius()) {
            try { AudiusClient.api.search(term).data.orEmpty().map { it.toDomain() } } catch (e: Exception) { emptyList() }
        } else emptyList()

        (localResults + externalResults).distinctBy { it.trackId }
    }

    suspend fun getPlaylistTracks(playlist: MusicPlaylist): List<TrackDto> = withContext(Dispatchers.IO) {
        fetchMyTracks().filter { it.shareUrl == playlist.id }
    }

    suspend fun fetchMyTracks(): List<TrackDto> = withContext(Dispatchers.IO) {
        try { api.getMySubscribedTracks().map { it.toDomain() }.map { enrichWithLocalState(it) } } catch (e: Exception) { emptyList() }
    }

    suspend fun getLibrary(): List<TrackDto> = withContext(Dispatchers.IO) {
        dao.getLibrary().map { it.toDto() }
    }

    suspend fun toggleLibrary(track: TrackDto): TrackDto = withContext(Dispatchers.IO) {
        val trackId = track.trackId ?: return@withContext track
        val existing = dao.getTrackById(trackId)
        val updated = if (existing == null) track.toEntity().copy(isInLibrary = true) else existing.copy(isInLibrary = !existing.isInLibrary)
        dao.insertTrack(updated); updated.toDto()
    }

    suspend fun toggleDownload(track: TrackDto): TrackDto = withContext(Dispatchers.IO) {
        val trackId = track.trackId ?: return@withContext track
        val existing = dao.getTrackById(trackId)
        val safeFileId = trackId.filter { it.isLetterOrDigit() }
        if (existing?.isDownloaded == true) {
            File(context.getExternalFilesDir("music"), "t_$safeFileId.mp3").delete()
            val updated = existing.copy(isDownloaded = false, localPath = null)
            dao.insertTrack(updated); return@withContext updated.toDto()
        }
        val remoteUrl = track.audioUrl ?: return@withContext track
        val file = File(context.getExternalFilesDir("music"), "t_$safeFileId.mp3")
        try {
            URL(remoteUrl).openStream().use { it.copyTo(file.outputStream()) }
            val updated = (existing ?: track.toEntity()).copy(isDownloaded = true, localPath = file.absolutePath)
            dao.insertTrack(updated); updated.toDto()
        } catch (e: Exception) { track }
    }

    suspend fun markAsPlayed(track: TrackDto) = withContext(Dispatchers.IO) {
        val trackId = track.trackId ?: return@withContext
        val existing = dao.getTrackById(trackId) ?: track.toEntity()
        dao.insertTrack(existing.copy(playCount = existing.playCount + 1, lastPlayedAt = System.currentTimeMillis()))
    }

    suspend fun seedIfEmpty() = withContext(Dispatchers.IO) {
        try { api.seedMusic() } catch (e: Exception) { }
    }

    private suspend fun enrichWithLocalState(track: TrackDto): TrackDto {
        val local = track.trackId?.let { dao.getTrackById(it) } ?: return track
        return track.copy(localPath = local.localPath, isDownloaded = local.isDownloaded, isInLibrary = local.isInLibrary, playCount = local.playCount)
    }

    private fun MusicTrackNetworkDto.toDomain() = TrackDto(
        trackId = id, trackName = title, artistName = artist ?: "FocusMe",
        artworkUrl100 = artworkUrl, audioUrl = audioUrl, shareUrl = packId,
        trackTimeMillis = durationSeconds.toLong() * 1000, isDownloadAllowed = true
    )

    private fun AudiusTrackNetworkDto.toDomain() = TrackDto(
        trackId = id, trackName = title, artistName = user?.name,
        artworkUrl100 = artwork?.medium, audioUrl = id?.let { "https://api.audius.co/v1/tracks/$it/stream?app_name=FocusMe" },
        trackTimeMillis = duration?.times(1000), isDownloadAllowed = false
    )

    private fun MusicPackDto.toPlaylist() = MusicPlaylist(id = id, title = name, subtitle = description, artworkUrl = imageUrl, tracksCount = tracksCount)

    private fun MusicTrackEntity.toDto() = TrackDto(
        trackId = id, trackName = name, artistName = artist, albumName = albumName, artworkUrl100 = artworkUrl, audioUrl = audioUrl,
        downloadUrl = downloadUrl, shareUrl = shareUrl, releasedAt = releasedAt, tags = tags?.split(",")?.filter { it.isNotBlank() }.orEmpty(),
        trackTimeMillis = trackTimeMillis, localPath = localPath, isDownloaded = isDownloaded, isInLibrary = isInLibrary, isDownloadAllowed = isDownloadAllowed, playCount = playCount, lastPlayedAt = lastPlayedAt
    )

    private fun TrackDto.toEntity() = MusicTrackEntity(
        id = trackId ?: "", name = trackName ?: "", artist = artistName ?: "", albumName = albumName, artworkUrl = artworkUrl100, audioUrl = audioUrl,
        downloadUrl = downloadUrl, shareUrl = shareUrl, releasedAt = releasedAt, localPath = localPath, trackTimeMillis = trackTimeMillis, tags = tags.joinToString(",").ifBlank { null },
        isDownloaded = isDownloaded, isInLibrary = isInLibrary, isDownloadAllowed = isDownloadAllowed, playCount = playCount, lastPlayedAt = lastPlayedAt
    )
}
