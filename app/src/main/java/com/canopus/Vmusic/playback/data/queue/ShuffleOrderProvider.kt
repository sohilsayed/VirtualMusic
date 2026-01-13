package com.canopus.Vmusic.playback.data.queue

import com.canopus.Vmusic.playback.domain.model.PlaybackItem

class ShuffleOrderProvider {
    fun createShuffledList(
        originalItems: List<PlaybackItem>,
        currentItem: PlaybackItem?
    ): List<PlaybackItem> {
        if (originalItems.isEmpty()) return emptyList()

        val mutableList = originalItems.toMutableList()
        val currentIndex = currentItem?.let { originalItems.indexOf(it) } ?: -1

        if (currentIndex >= 0) {
            val current = mutableList.removeAt(currentIndex)
            mutableList.shuffle()
            return listOf(current) + mutableList
        } else {
            mutableList.shuffle()
            return mutableList
        }
    }
}