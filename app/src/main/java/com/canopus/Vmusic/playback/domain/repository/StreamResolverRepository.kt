
package com.canopus.Vmusic.playback.domain.repository

import com.canopus.Vmusic.playback.domain.model.StreamDetails

interface StreamResolverRepository {
    suspend fun resolveStreamUrl(videoId: String): Result<StreamDetails>
}