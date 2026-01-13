
package com.canopus.Vmusic.playback.domain.model

data class DomainPlaybackProgress(
    val positionSec: Long = 0L,
    val durationSec: Long = 0L,
    val bufferedPositionSec: Long = 0L
) {
    companion object {
        val NONE = DomainPlaybackProgress()
    }
}