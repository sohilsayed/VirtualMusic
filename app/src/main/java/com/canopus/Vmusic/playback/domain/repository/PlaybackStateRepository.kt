
package com.canopus.Vmusic.playback.domain.repository

import com.canopus.Vmusic.playback.domain.model.PersistedPlaybackData

interface PlaybackStateRepository {
    suspend fun saveState(data: PersistedPlaybackData)
    suspend fun loadState(): PersistedPlaybackData?
    suspend fun clearState()
}