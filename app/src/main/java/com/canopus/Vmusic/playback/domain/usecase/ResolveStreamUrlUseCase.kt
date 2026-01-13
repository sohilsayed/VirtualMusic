package com.canopus.Vmusic.playback.domain.usecase

import com.canopus.Vmusic.playback.domain.model.StreamDetails
import com.canopus.Vmusic.playback.domain.repository.StreamResolverRepository


class ResolveStreamUrlUseCase(
    private val streamResolverRepository: StreamResolverRepository
) {
    suspend operator fun invoke(videoId: String): Result<StreamDetails> {
        return streamResolverRepository.resolveStreamUrl(videoId)
    }
}