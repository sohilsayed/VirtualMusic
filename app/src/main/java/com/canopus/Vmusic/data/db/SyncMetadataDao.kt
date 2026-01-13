// (Create this new file)

package com.canopus.Vmusic.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SyncMetadataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setLastSyncTimestamp(metadata: SyncMetadataEntity)

    @Query("SELECT lastSyncTimestamp FROM sync_metadata WHERE dataType = :dataType")
    suspend fun getLastSyncTimestamp(dataType: String): Long?
}