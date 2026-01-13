package com.canopus.Vmusic.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StarredPlaylistDao {
    @Query("SELECT * FROM starred_playlists")
    fun getStarredPlaylists(): Flow<List<StarredPlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(starredPlaylist: StarredPlaylistEntity)

    @Query("DELETE FROM starred_playlists WHERE playlist_id = :playlistId")
    suspend fun deleteById(playlistId: String)

    @Query("SELECT * FROM starred_playlists WHERE sync_status != 'SYNCED'")
    suspend fun getUnsyncedItems(): List<StarredPlaylistEntity>

    @Query("DELETE FROM starred_playlists WHERE sync_status = 'SYNCED'")
    suspend fun deleteAllSyncedItems()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<StarredPlaylistEntity>)
}