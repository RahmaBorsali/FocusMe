package com.example.focusme.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Entity(tableName = "music_tracks")
data class MusicTrackEntity(
    @androidx.room.PrimaryKey val id: String,
    val name: String,
    val artist: String,
    val albumName: String?,
    val artworkUrl: String?,
    val audioUrl: String?,
    val downloadUrl: String?,
    val shareUrl: String?,
    val releasedAt: String?,
    val localPath: String?,
    val trackTimeMillis: Long?,
    val tags: String?,
    val isDownloaded: Boolean = false,
    val isInLibrary: Boolean = false,
    val isDownloadAllowed: Boolean = false,
    val playCount: Int = 0,
    val lastPlayedAt: Long? = null
)

@Dao
interface MusicDao {
    @Query(
        "SELECT * FROM music_tracks " +
            "WHERE isInLibrary = 1 OR isDownloaded = 1 " +
            "ORDER BY COALESCE(lastPlayedAt, 0) DESC, playCount DESC, name ASC"
    )
    suspend fun getLibrary(): List<MusicTrackEntity>

    @Query(
        "SELECT * FROM music_tracks " +
            "WHERE lastPlayedAt IS NOT NULL " +
            "ORDER BY lastPlayedAt DESC " +
            "LIMIT 12"
    )
    suspend fun getRecentlyPlayed(): List<MusicTrackEntity>

    @Query(
        "SELECT * FROM music_tracks " +
            "WHERE playCount > 0 " +
            "ORDER BY playCount DESC, COALESCE(lastPlayedAt, 0) DESC " +
            "LIMIT 12"
    )
    suspend fun getMostPlayed(): List<MusicTrackEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: MusicTrackEntity)

    @Update
    suspend fun updateTrack(track: MusicTrackEntity)

    @Query("SELECT * FROM music_tracks WHERE id = :id")
    suspend fun getTrackById(id: String): MusicTrackEntity?

    @Query("DELETE FROM music_tracks WHERE id = :id")
    suspend fun deleteTrack(id: String)
}
