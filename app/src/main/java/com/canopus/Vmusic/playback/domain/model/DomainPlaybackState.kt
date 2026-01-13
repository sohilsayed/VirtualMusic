
package com.canopus.Vmusic.playback.domain.model

enum class DomainPlaybackState {
    IDLE,
    BUFFERING,
    PLAYING,
    PAUSED,
    ENDED,
    ERROR
}