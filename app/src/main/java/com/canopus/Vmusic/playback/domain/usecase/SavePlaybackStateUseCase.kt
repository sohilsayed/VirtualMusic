package com.canopus.Vmusic.playback.domain.usecase

import com.canopus.Vmusic.playback.domain.model.PersistedPlaybackData
import com.canopus.Vmusic.playback.domain.repository.PlaybackStateRepository

class SavePlaybackStateUseCase(
    private val playbackStateRepository: PlaybackStateRepository
) {
    suspend operator fun invoke(data: PersistedPlaybackData) {
        playbackStateRepository.saveState(data)
    }
}