package com.canopus.Vmusic.data.db

import androidx.room.Embedded

/**
 * A highly optimized projection for Lists and Feeds.
 */
data class UnifiedItemWithStatus(
    @Embedded val metadata: UnifiedMetadataEntity,

    // Computed columns from SQL
    val isLiked: Boolean,
    val isDownloaded: Boolean,
    val downloadStatus: String?,
    val localFilePath: String?,
    val historyId: Long? = null
)