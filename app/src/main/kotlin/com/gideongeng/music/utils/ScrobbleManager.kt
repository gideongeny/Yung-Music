package com.gideongeng.music.utils

import kotlinx.coroutines.CoroutineScope

class ScrobbleManager(
    scope: CoroutineScope,
    minSongDuration: Int,
    scrobbleDelayPercent: Float,
    scrobbleDelaySeconds: Int
) {
    var scrobbleDelayPercent: Float = 50f
    var minSongDuration: Int = 30000
    var scrobbleDelaySeconds: Int = 30
    var useNowPlaying: Boolean = false

    fun destroy() {}
    fun onSongStop() {}
    fun onSongStart(metadata: Any?, duration: Long = 0L) {}
    fun onPlayerStateChanged(isPlaying: Boolean, metadata: Any?, duration: Long = 0L) {}
}
