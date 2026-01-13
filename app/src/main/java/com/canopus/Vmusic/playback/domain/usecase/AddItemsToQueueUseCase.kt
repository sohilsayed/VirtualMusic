package com.canopus.Vmusic.playback.domain.usecase

import com.canopus.Vmusic.playback.domain.model.PlaybackItem
import com.canopus.Vmusic.playback.player.PlaybackController
import javax.inject.Inject

class AddItemsToQueueUseCase @Inject constructor(
    private val controller: PlaybackController
) {
    /**
     * Adds items to the playback queue.
     * @param items The items to add.
     * @param index The index to insert at. If null, appends to end (or next in shuffle).
     */
    operator fun invoke(items: List<PlaybackItem>, index: Int? = null) {
        controller.addItemsToQueue(items, index)
    }
}