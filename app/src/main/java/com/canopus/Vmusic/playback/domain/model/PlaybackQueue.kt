
package com.canopus.Vmusic.playback.domain.model

data class PlaybackQueue(
    val items: List<PlaybackItem> = emptyList(),
    val currentIndex: Int = -1,
    val repeatMode: DomainRepeatMode = DomainRepeatMode.NONE,
    val shuffleMode: DomainShuffleMode = DomainShuffleMode.OFF,
    val queueId: String = "default_queue"
) {
    val currentItem: PlaybackItem?
        get() = items.getOrNull(currentIndex)
}