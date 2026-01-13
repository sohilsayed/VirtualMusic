package com.canopus.Vmusic.data.model

data class ChannelSearchResult(
    val channelId: String,
    val name: String,
    val thumbnailUrl: String?,
    val subscriberCount: String?
)