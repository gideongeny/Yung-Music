package com.gideongeng.music.desktop

import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.FrameGrabber
import java.nio.ShortBuffer
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.FloatControl
import javax.sound.sampled.SourceDataLine
import kotlin.math.ln

/**
 * Bundled FFmpeg + Java Sound backend so playback works when libVLC is absent.
 */
class FfmpegPlayback(
    private val onPlaying: () -> Unit,
    private val onPaused: () -> Unit,
    private val onFinished: () -> Unit,
    private val onError: () -> Unit,
    private val onTime: (seconds: Int) -> Unit,
) {
    private val lock = Any()
    private var worker: Thread? = null
    @Volatile private var running = false
    @Volatile private var paused = false
    @Volatile private var volume = 0.7f
    @Volatile private var seekUs: Long? = null
    private var line: SourceDataLine? = null
    private var gain: FloatControl? = null

    fun play(url: String) {
        stopInternal(join = Thread.currentThread() != worker)
        running = true
        paused = false
        worker = Thread({
            try {
                playLoop(url)
            } catch (e: Exception) {
                println("YungMusic FFmpeg error: ${e.message}")
                e.printStackTrace()
                onError()
            }
        }, "YungFfmpeg").apply {
            isDaemon = true
            start()
        }
    }

    private fun playLoop(url: String) {
        val grabber = FFmpegFrameGrabber(url)
        grabber.setOption(
            "user_agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
        )
        grabber.setOption("referer", "https://music.youtube.com/")
        grabber.setOption("rw_timeout", "20000000")
        grabber.setOption("reconnect", "1")
        grabber.setOption("reconnect_streamed", "1")
        grabber.setOption("reconnect_delay_max", "5")
        grabber.sampleMode = FrameGrabber.SampleMode.SHORT
        grabber.start()
        try {
            val channels = grabber.audioChannels.coerceAtLeast(1)
            val sampleRate = grabber.sampleRate.coerceAtLeast(44100).toFloat()
            val format = AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                sampleRate,
                16,
                channels,
                channels * 2,
                sampleRate,
                false,
            )
            val sdl = AudioSystem.getSourceDataLine(format)
            sdl.open(format)
            sdl.start()
            synchronized(lock) {
                line = sdl
                gain = if (sdl.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    sdl.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
                } else {
                    null
                }
                applyVolume()
            }
            onPlaying()
            while (running) {
                if (paused) {
                    onPaused()
                    while (paused && running) Thread.sleep(30)
                    if (!running) break
                    onPlaying()
                }
                val target = seekUs
                if (target != null) {
                    seekUs = null
                    grabber.timestamp = target
                    sdl.flush()
                }
                val frame = grabber.grabSamples()
                if (frame == null) {
                    if (running) onFinished()
                    break
                }
                onTime((grabber.timestamp / 1_000_000L).toInt())
                val samples = frame.samples ?: continue
                val sb = samples[0] as? ShortBuffer ?: continue
                val bytes = shortsToLeBytes(sb)
                var off = 0
                while (off < bytes.size && running && !paused) {
                    val written = sdl.write(bytes, off, bytes.size - off)
                    if (written <= 0) break
                    off += written
                }
            }
        } finally {
            runCatching { grabber.stop() }
            runCatching { grabber.release() }
            synchronized(lock) {
                runCatching { line?.stop() }
                runCatching { line?.close() }
                line = null
                gain = null
            }
        }
    }

    fun togglePause() {
        if (!running) return
        paused = !paused
        if (paused) onPaused() else onPlaying()
    }

    fun seekMs(ms: Long) {
        seekUs = ms * 1000
    }

    fun setVolume(v: Float) {
        volume = v
        synchronized(lock) { applyVolume() }
    }

    fun stop() = stopInternal(join = false)

    fun release() = stopInternal(join = true)

    private fun applyVolume() {
        val g = gain ?: return
        val db = if (volume <= 0.0001f) {
            g.minimum
        } else {
            (20.0 * ln(volume.toDouble()) / ln(10.0)).toFloat().coerceIn(g.minimum, g.maximum)
        }
        g.value = db
    }

    private fun stopInternal(join: Boolean) {
        running = false
        paused = false
        synchronized(lock) {
            runCatching { line?.stop() }
            runCatching { line?.flush() }
        }
        if (join) worker?.join(2500)
        worker = null
    }

    private fun shortsToLeBytes(sb: ShortBuffer): ByteArray {
        val dup = sb.duplicate()
        val out = ByteArray(dup.remaining() * 2)
        var i = 0
        while (dup.hasRemaining()) {
            val s = dup.get().toInt()
            out[i++] = (s and 0xFF).toByte()
            out[i++] = (s shr 8).toByte()
        }
        return out
    }
}
