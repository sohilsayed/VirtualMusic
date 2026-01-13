package com.canopus.Vmusic.data.download

/**
 * A specific exception thrown when a download is initiated without a
 * download location being configured in the app's settings.
 */
class NoDownloadLocationException(message: String) : Exception(message)