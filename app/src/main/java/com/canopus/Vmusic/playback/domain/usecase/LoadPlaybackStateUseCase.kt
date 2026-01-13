package com.canopus.Vmusic.playback.domain.usecase

import com.canopus.Vmusic.playback.domain.model.PersistedPlaybackData
import com.canopus.Vmusic.playback.domain.repository.PlaybackStateRepository

class LoadPlaybackStateUseCase(
    private val playbackStateRepository: PlaybackStateRepository
) {
    suspend operator fun invoke(): PersistedPlaybackData? {
        return playbackStateRepository.loadState()
    }
}