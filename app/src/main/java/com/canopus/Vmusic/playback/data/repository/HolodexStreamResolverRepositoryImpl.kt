package com.canopus.Vmusic.playback.data.repository

import com.canopus.Vmusic.data.model.AudioStreamDetails
import com.canopus.Vmusic.data.repository.YouTubeStreamRepository
import com.canopus.Vmusic.playback.domain.model.StreamDetails
import com.canopus.Vmusic.playback.domain.repository.StreamResolverRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HolodexStreamResolverRepositoryImpl @Inject constructor(
    private val youtubeStreamRepository: YouTubeStreamRepository
) : StreamResolverRepository {

    override suspend fun resolveStreamUrl(videoId: String): Result<StreamDetails> {
        return try {
            val youtubeResult: Result<AudioStreamDetails> =
                youtubeStreamRepository.getAudioStreamDetails(videoId)

            youtubeResult.map { oldDetails ->
                val streamUrl = oldDetails.streamUrl
                    ?: throw IllegalStateException("Stream URL was null from YouTubeStreamRepository for videoId: $videoId")
                StreamDetails(
                    url = streamUrl,
                    format = oldDetails.format,
                    quality = oldDetails.quality
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}