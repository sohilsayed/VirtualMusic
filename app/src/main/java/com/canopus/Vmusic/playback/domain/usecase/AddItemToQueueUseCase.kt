package com.canopus.Vmusic.playback.domain.usecase

import com.canopus.Vmusic.playback.domain.model.PlaybackItem
import com.canopus.Vmusic.playback.player.PlaybackController
import javax.inject.Inject

class AddItemToQueueUseCase @Inject constructor(
    private val controller: PlaybackController
) {
    operator fun invoke(item: PlaybackItem, index: Int? = null) {
        controller.addItemsToQueue(listOf(item), index)
    }
}