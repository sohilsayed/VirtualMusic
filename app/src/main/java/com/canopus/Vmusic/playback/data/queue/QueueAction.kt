package com.canopus.Vmusic.playback.data.queue

import com.canopus.Vmusic.playback.domain.model.DomainRepeatMode
import com.canopus.Vmusic.playback.domain.model.DomainShuffleMode
import com.canopus.Vmusic.playback.domain.model.PlaybackItem

sealed class QueueAction {
    data class SetQueue(
        val items: List<PlaybackItem>,
        val startIndex: Int,
        val startPositionMs: Long = 0L,
        val shouldShuffle: Boolean = false,
        val restoredShuffleMode: DomainShuffleMode? = null,
        val restoredRepeatMode: DomainRepeatMode? = null,
        val restoredShuffledList: List<PlaybackItem>? = null
    ) : QueueAction()

    object ToggleShuffle : QueueAction()
    data class SetRepeatMode(val repeatMode: DomainRepeatMode) : QueueAction()
    data class ReorderItem(val fromIndex: Int, val toIndex: Int) : QueueAction()
    data class AddItem(val item: PlaybackItem, val index: Int?) : QueueAction()
    data class AddItems(val items: List<PlaybackItem>, val index: Int?) : QueueAction()
    data class RemoveItem(val index: Int) : QueueAction()
    data class SetCurrentIndex(val newIndex: Int) : QueueAction()
    object ClearQueue : QueueAction()
    data class UpdateItemInQueue(val updatedItem: PlaybackItem) : QueueAction()
    object SkipToNext : QueueAction()
    object SkipToPrevious : QueueAction()
}