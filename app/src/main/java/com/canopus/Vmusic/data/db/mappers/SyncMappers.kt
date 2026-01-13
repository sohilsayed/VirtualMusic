package com.canopus.Vmusic.data.db.mappers

import com.canopus.Vmusic.data.api.PlaylistDto
import com.canopus.Vmusic.data.db.PlaylistEntity
import com.canopus.Vmusic.data.db.SyncStatus


fun PlaylistDto.toEntity(): PlaylistEntity {
    return PlaylistEntity(
        playlistId = 0, // Let Room auto-generate the local ID
        serverId = this.id,
        name = this.title,
        description = this.description,
        owner = this.owner,
        type = this.type ?: "ugp",
        createdAt = this.createdAt,
        last_modified_at = this.updatedAt,
        isDeleted = false,
        syncStatus = SyncStatus.SYNCED // Data from server is considered synced
    )
}