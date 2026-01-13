package com.canopus.Vmusic.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ParentVideoMetadataDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(metadata: ParentVideoMetadataEntity)

    @Query("SELECT * FROM parent_video_metadata WHERE videoId = :videoId")
    suspend fun getById(videoId: String): ParentVideoMetadataEntity?
}