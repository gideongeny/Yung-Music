/**
 * CrossfadeController - Smooth audio crossfade between tracks using ExoPlayer volume ramping.
 * Gives a DJ-style transition feel between songs.
 */

package com.gideongeng.music.playback

import android.util.Log
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CrossfadeController(
    private val player: ExoPlayer,
    private val scope: CoroutineScope
) {
    private val TAG = "CrossfadeController"

    // Duration of crossfade in milliseconds (set by user preference, default 3s)
    var crossfadeDurationMs: Long = 3000L
    var enabled: Boolean = false

    private var fadeOutJob: Job? = null
    
    private val _isCrossfading = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isCrossfading: kotlinx.coroutines.flow.StateFlow<Boolean> = _isCrossfading

    /**
     * Call this from Player.Listener.onPositionDiscontinuity or a position polling coroutine.
     * When the track is within [crossfadeDurationMs] of ending, begin fading out.
     */
    fun onPlaybackPositionUpdate(positionMs: Long, durationMs: Long, isMuted: Boolean, masterVolume: Float) {
        if (!enabled || durationMs <= 0 || _isCrossfading.value) return

        val timeUntilEndMs = durationMs - positionMs

        // Start fade-out when we're within the crossfade window
        if (timeUntilEndMs in 1..crossfadeDurationMs) {
            startFadeOut(masterVolume, timeUntilEndMs, isMuted)
        }
    }

    /**
     * Gradually fade the volume from current level down to 0 over [fadeTimeMs].
     */
    private fun startFadeOut(masterVolume: Float, fadeTimeMs: Long, isMuted: Boolean) {
        if (isMuted) return
        _isCrossfading.value = true
        fadeOutJob?.cancel()

        fadeOutJob = scope.launch(Dispatchers.Main) {
            val steps = 30
            val stepDelay = fadeTimeMs / steps
            val volumeStep = masterVolume / steps

            Log.d(TAG, "Starting crossfade out over ${fadeTimeMs}ms")

            for (i in steps downTo 0) {
                if (!isActive) break
                val newVolume = (volumeStep * i).coerceIn(0f, masterVolume)
                player.volume = newVolume
                delay(stepDelay)
            }

            // Make sure it ends at 0
            player.volume = 0f
            _isCrossfading.value = false
        }
    }

    /**
     * Called when a new track begins. Fades volume back up from 0 to [masterVolume].
     * Call this from onMediaItemTransition.
     */
    fun onTrackTransition(masterVolume: Float, isMuted: Boolean) {
        if (!enabled || isMuted) {
            // Reset volume immediately if crossfade is disabled
            player.volume = if (isMuted) 0f else masterVolume
            return
        }

        // Cancel any ongoing fade-out
        fadeOutJob?.cancel()
        _isCrossfading.value = false

        // Start fade-in on the new track
        startFadeIn(masterVolume)
    }

    /**
     * Gradually fade volume from 0 up to [masterVolume] over [crossfadeDurationMs].
     */
    private fun startFadeIn(masterVolume: Float) {
        scope.launch(Dispatchers.Main) {
            val steps = 30
            val stepDelay = crossfadeDurationMs / steps
            val volumeStep = masterVolume / steps

            // Start from silence
            player.volume = 0f

            Log.d(TAG, "Starting crossfade in over ${crossfadeDurationMs}ms")

            for (i in 1..steps) {
                if (!isActive) break
                val newVolume = (volumeStep * i).coerceIn(0f, masterVolume)
                player.volume = newVolume
                delay(stepDelay)
            }

            // Ensure we end at exact master volume
            player.volume = masterVolume
        }
    }

    /**
     * Cancel any ongoing fade and reset volume immediately.
     */
    fun reset(masterVolume: Float, isMuted: Boolean) {
        fadeOutJob?.cancel()
        _isCrossfading.value = false
        player.volume = if (isMuted) 0f else masterVolume
    }

    companion object {
        // Preset crossfade durations in seconds for the settings UI
        val PRESET_DURATIONS_SECONDS = listOf(1, 2, 3, 5, 8, 10)
    }
}
