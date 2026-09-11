/**
 * WaveformVisualizer - Beat-reactive audio waveform bars.
 *
 * Uses android.media.audiofx.Visualizer with the app's own audio session ID.
 * - NO RECORD_AUDIO permission required (we use our own audio session).
 * - Fails silently and hides itself on devices where Visualizer is restricted.
 */

package com.gideongeng.music.ui.component

import android.media.audiofx.Visualizer
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val NUM_BARS = 32
private const val CAPTURE_SIZE = 128

@Composable
fun WaveformVisualizer(
    audioSessionId: Int,
    accentColor: Color,
    modifier: Modifier = Modifier,
    barHeightDp: Dp = 48.dp,
    isPlaying: Boolean = true,
) {
    // Raw amplitude per bar (0f..1f)
    var rawAmplitudes by remember { mutableStateOf(FloatArray(NUM_BARS) { 0f }) }
    var isAvailable by remember { mutableStateOf(true) }

    // Attach Visualizer to the audio session — no permission needed for our own audio
    DisposableEffect(audioSessionId) {
        if (!isAvailable) return@DisposableEffect onDispose {}

        val visualizer = try {
            Visualizer(audioSessionId).also { v ->
                v.captureSize = CAPTURE_SIZE
                v.setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            v: Visualizer?,
                            waveform: ByteArray,
                            samplingRate: Int
                        ) {
                            val chunkSize = waveform.size / NUM_BARS
                            val amps = FloatArray(NUM_BARS)
                            for (i in 0 until NUM_BARS) {
                                var sum = 0f
                                for (j in 0 until chunkSize) {
                                    sum += abs(waveform[i * chunkSize + j].toInt()).toFloat()
                                }
                                amps[i] = (sum / chunkSize / 128f).coerceIn(0f, 1f)
                            }
                            rawAmplitudes = amps
                        }

                        override fun onFftDataCapture(
                            v: Visualizer?,
                            fft: ByteArray,
                            samplingRate: Int
                        ) { /* unused */ }
                    },
                    Visualizer.getMaxCaptureRate() / 2,
                    true,
                    false
                )
                v.enabled = true
            }
        } catch (e: Exception) {
            // SecurityException or UnsupportedOperationException on restricted devices — hide gracefully
            isAvailable = false
            null
        }

        onDispose {
            visualizer?.enabled = false
            visualizer?.release()
        }
    }

    // When paused, animate bars back to a minimal resting height
    LaunchedEffect(isPlaying) {
        if (!isPlaying) {
            while (!isPlaying) {
                rawAmplitudes = FloatArray(NUM_BARS) { i ->
                    (0.03f + 0.04f * kotlin.math.sin(i * 0.4f + System.currentTimeMillis() * 0.001f)).coerceIn(0.01f, 0.12f)
                }
                delay(80L)
            }
        }
    }

    if (!isAvailable) return

    // Animate each bar independently with a spring, avoiding recomposition by reading state in Canvas
    val animatedAmps = remember { List(NUM_BARS) { androidx.compose.animation.core.Animatable(0.04f) } }

    LaunchedEffect(rawAmplitudes) {
        rawAmplitudes.forEachIndexed { i, amp ->
            launch {
                animatedAmps[i].animateTo(
                    targetValue = amp.coerceAtLeast(0.04f),
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow + i * 2f
                    )
                )
            }
        }
    }

    val barGradient = Brush.verticalGradient(
        colors = listOf(
            accentColor,
            accentColor.copy(alpha = 0.5f)
        )
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeightDp)
    ) {
        val totalWidth = size.width
        val totalHeight = size.height
        val barWidth = totalWidth / (NUM_BARS * 2f - 1f)
        val gap = barWidth
        val cornerRadius = CornerRadius(barWidth / 2f)

        animatedAmps.forEachIndexed { i, animAmp ->
            val amp = animAmp.value // Read value inside draw scope
            val barHeight = (totalHeight * amp).coerceAtLeast(4f)
            val left = i * (barWidth + gap)
            val top = totalHeight - barHeight

            drawRoundRect(
                brush = barGradient,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = cornerRadius
            )
        }
    }
}
