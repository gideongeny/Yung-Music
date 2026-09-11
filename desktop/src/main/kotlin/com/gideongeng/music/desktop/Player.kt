package com.gideongeng.music.desktop

import androidx.compose.runtime.mutableStateOf
import com.gideongeng.music.innertube.YouTube
import com.gideongeng.music.innertube.models.BrowseEndpoint
import com.gideongeng.music.innertube.models.SongItem
import com.gideongeng.music.innertube.models.WatchEndpoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.component.AudioPlayerComponent

object AudioPlayer {
    var currentlyPlaying = mutableStateOf<SongItem?>(null)
    var isPlaying = mutableStateOf(false)
    var progressSeconds = mutableStateOf(0)
    var durationSeconds = mutableStateOf(0)
    var lyricsEndpoint = mutableStateOf<BrowseEndpoint?>(null)
    var lyrics = mutableStateOf<String?>(null)
    var volume = mutableStateOf(0.7f)
    var isMiniPlayerVisible = mutableStateOf(false)

    enum class RepeatMode { OFF, ALL, ONE }

    val queueState = mutableStateOf<List<SongItem>>(emptyList())
    val currentIndexState = mutableStateOf(0)
    val shuffleOn = mutableStateOf(false)
    val repeatMode = mutableStateOf(RepeatMode.OFF)
    val showNowPlaying = mutableStateOf(false)
    val showQueue = mutableStateOf(false)
    val nowPlayingContext = mutableStateOf("")

    var queue: List<SongItem>
        get() = queueState.value
        set(value) { queueState.value = value }
    var currentIndex: Int
        get() = currentIndexState.value
        set(value) { currentIndexState.value = value }

    private var mediaPlayerComponent: AudioPlayerComponent? = null
    private val playerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val ffmpeg = FfmpegPlayback(
        onPlaying = { isPlaying.value = true },
        onPaused = { isPlaying.value = false },
        onFinished = {
            isPlaying.value = false
            playerScope.launch { playNext() }
        },
        onError = { playerScope.launch { playFallback() } },
        onTime = { progressSeconds.value = it },
    )

    private var streamQueue: List<StreamResolver.ResolvedStream> = emptyList()
    private var streamIndex = 0
    private var triedProxy = false
    private var playingSong: SongItem? = null
    private val vlcLock = Any()
    @Volatile private var usingVlc = false

    private val httpOptions = arrayOf(
        ":http-user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
        ":http-referrer=https://music.youtube.com/",
        ":http-reconnect=true",
        ":network-caching=8000",
        ":no-video",
    )

    init {
        val vlcHome = VlcBootstrap.discover()
        AudioProxy.start()
        if (vlcHome != null) {
            try {
                val args = mutableListOf("--no-video", "--intf=dummy")
                VlcBootstrap.pluginPath()?.let { args += "--plugin-path=$it" }
                mediaPlayerComponent = AudioPlayerComponent(MediaPlayerFactory(*args.toTypedArray()))
                mediaPlayerComponent?.mediaPlayer()?.events()?.addMediaPlayerEventListener(object : MediaPlayerEventAdapter() {
                    override fun playing(mediaPlayer: MediaPlayer) {
                        isPlaying.value = true
                        println("YungMusic: VLC is playing")
                    }
                    override fun paused(mediaPlayer: MediaPlayer) {
                        isPlaying.value = false
                    }
                    override fun stopped(mediaPlayer: MediaPlayer) {
                        isPlaying.value = false
                    }
                    override fun finished(mediaPlayer: MediaPlayer) {
                        isPlaying.value = false
                        playerScope.launch { playNext() }
                    }
                    override fun timeChanged(mediaPlayer: MediaPlayer, newTime: Long) {
                        progressSeconds.value = (newTime / 1000).toInt()
                    }
                    override fun error(mediaPlayer: MediaPlayer) {
                        println("YungMusic VLC Error: trying next stream / FFmpeg")
                        playerScope.launch { playFallback() }
                    }
                })
                setVolume(volume.value)
                println("YungMusic: VLC engine ready")
            } catch (e: Exception) {
                println("YungMusic: VLC init failed, FFmpeg will be used: ${e.message}")
                e.printStackTrace()
                mediaPlayerComponent = null
            }
        } else {
            println("YungMusic: FFmpeg engine ready (no VLC)")
        }
    }

    fun updateQueue(songs: List<SongItem>, startIndex: Int = 0) {
        queue = songs
        currentIndex = startIndex.coerceIn(0, (songs.size - 1).coerceAtLeast(0))
    }

    fun playQueue(songs: List<SongItem>, startIndex: Int = 0, context: String = "") {
        if (songs.isEmpty()) return
        nowPlayingContext.value = context
        updateQueue(songs, startIndex)
        showNowPlaying.value = true
        playerScope.launch { playSong(songs[currentIndex]) }
    }

    fun playAt(index: Int) {
        if (index !in queue.indices) return
        currentIndex = index
        playerScope.launch { playSong(queue[index]) }
    }

    fun addToQueue(song: SongItem) {
        queue = queue + song
    }

    fun playNextInQueue(song: SongItem) {
        val insert = (currentIndex + 1).coerceAtMost(queue.size)
        queue = queue.toMutableList().apply { add(insert, song) }
    }

    fun removeFromQueue(index: Int) {
        if (index !in queue.indices) return
        val next = queue.toMutableList().also { it.removeAt(index) }
        if (index < currentIndex) currentIndex--
        queue = next
        if (next.isEmpty()) stopCurrent()
    }

    fun toggleShuffle() {
        shuffleOn.value = !shuffleOn.value
        if (!shuffleOn.value || queue.size < 2) return
        val current = queue.getOrNull(currentIndex) ?: return
        val rest = queue.filterIndexed { i, _ -> i != currentIndex }.shuffled()
        queue = listOf(current) + rest
        currentIndex = 0
    }

    fun toggleRepeat() {
        repeatMode.value = when (repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    fun playNext() {
        if (queue.isEmpty()) return
        if (repeatMode.value == RepeatMode.ONE) {
            playerScope.launch { playSong(queue[currentIndex]) }
            return
        }
        if (currentIndex < queue.size - 1) {
            currentIndex++
            playerScope.launch { playSong(queue[currentIndex]) }
        } else if (repeatMode.value == RepeatMode.ALL) {
            currentIndex = 0
            playerScope.launch { playSong(queue[0]) }
        } else {
            stopCurrent()
        }
    }

    fun playPrevious() {
        if (queue.isEmpty()) return
        if (progressSeconds.value > 3) {
            seekTo(0)
            return
        }
        if (currentIndex > 0) {
            currentIndex--
            playerScope.launch { playSong(queue[currentIndex]) }
        } else if (repeatMode.value == RepeatMode.ALL) {
            currentIndex = queue.lastIndex
            playerScope.launch { playSong(queue[currentIndex]) }
        } else {
            playerScope.launch { playSong(queue[0]) }
        }
    }

    fun togglePlayPause() {
        if (usingVlc) {
            val player = mediaPlayerComponent?.mediaPlayer() ?: return
            if (player.status().isPlaying) player.controls().pause() else player.controls().play()
        } else {
            ffmpeg.togglePause()
        }
    }

    fun seekTo(seconds: Int) {
        progressSeconds.value = seconds
        if (usingVlc) {
            mediaPlayerComponent?.mediaPlayer()?.controls()?.setTime((seconds * 1000).toLong())
        } else {
            ffmpeg.seekMs(seconds * 1000L)
        }
    }

    fun setVolume(v: Float) {
        volume.value = v
        mediaPlayerComponent?.mediaPlayer()?.audio()?.setVolume((v * 100).toInt())
        ffmpeg.setVolume(v)
    }

    fun stopCurrent() {
        usingVlc = false
        mediaPlayerComponent?.mediaPlayer()?.controls()?.stop()
        ffmpeg.stop()
        isPlaying.value = false
        progressSeconds.value = 0
        currentlyPlaying.value = null
        isMiniPlayerVisible.value = false
        streamQueue = emptyList()
        playingSong = null
    }

    fun startPlayback(song: SongItem, streams: List<StreamResolver.ResolvedStream>) {
        currentlyPlaying.value = song
        isMiniPlayerVisible.value = true
        durationSeconds.value = song.duration ?: 0
        progressSeconds.value = 0
        playingSong = song
        streamQueue = streams
        streamIndex = 0
        triedProxy = false
        forceFfmpeg = false
        playCurrentStream()
    }

    private fun playCurrentStream() {
        val stream = streamQueue.getOrNull(streamIndex) ?: return
        val mediaUrl = if (triedProxy) AudioProxy.wrap(stream.url) else stream.url
        val useVlcNow = mediaPlayerComponent != null && !ffmpegForced()
        println(
            "YungMusic: Playing '${playingSong?.title}' via ${if (useVlcNow) "VLC" else "FFmpeg"} " +
                "itag=${stream.itag} ${stream.mimeType} proxy=$triedProxy"
        )
        if (useVlcNow) {
            usingVlc = true
            ffmpeg.stop()
            val player = mediaPlayerComponent?.mediaPlayer() ?: return playWithFfmpeg(mediaUrl)
            synchronized(vlcLock) {
                player.controls().stop()
                player.media().play(mediaUrl, *httpOptions)
            }
        } else {
            playWithFfmpeg(mediaUrl)
        }
    }

    private var forceFfmpeg = false

    private fun ffmpegForced(): Boolean = forceFfmpeg || mediaPlayerComponent == null

    private fun playWithFfmpeg(url: String) {
        usingVlc = false
        mediaPlayerComponent?.mediaPlayer()?.controls()?.stop()
        ffmpeg.setVolume(volume.value)
        ffmpeg.play(url)
    }

    private fun playFallback() {
        if (streamQueue.isEmpty()) return
        if (!triedProxy) {
            triedProxy = true
            playCurrentStream()
            return
        }
        if (streamIndex < streamQueue.lastIndex) {
            streamIndex++
            triedProxy = false
            playCurrentStream()
            return
        }
        if (mediaPlayerComponent != null && !forceFfmpeg) {
            println("YungMusic: VLC exhausted streams, switching to bundled FFmpeg")
            forceFfmpeg = true
            streamIndex = 0
            triedProxy = false
            playCurrentStream()
            return
        }
        println("YungMusic: All stream fallbacks failed for ${playingSong?.id}")
    }

    fun release() {
        mediaPlayerComponent?.release()
        ffmpeg.release()
        playerScope.cancel()
    }
}

suspend fun playSong(song: SongItem) {
    AudioPlayer.currentlyPlaying.value = song
    AudioPlayer.isPlaying.value = false
    AudioPlayer.progressSeconds.value = 0
    AudioPlayer.durationSeconds.value = song.duration ?: 0
    AudioPlayer.lyricsEndpoint.value = null
    AudioPlayer.lyrics.value = null
    AudioPlayer.isMiniPlayerVisible.value = true

    try { LibraryManager.addSongToHistory(song) } catch (_: Exception) {}

    withContext(Dispatchers.IO) {
        val nextResult = YouTube.next(WatchEndpoint(videoId = song.id)).getOrNull()
        val ep = nextResult?.lyricsEndpoint
        AudioPlayer.lyricsEndpoint.value = ep

        launch {
            AudioPlayer.lyrics.value = fetchLyricsAllSources(song, ep)
        }

        val streams = try {
            StreamResolver.resolveAudioStreams(song.id)
        } catch (e: Exception) {
            println("YungMusic: Stream resolve failed: ${e.message}")
            emptyList()
        }

        if (streams.isEmpty()) {
            println("YungMusic: No playable audio URL for ${song.id}")
            return@withContext
        }

        println("YungMusic: Starting playback of '${song.title}' via ${streams.first().mimeType}")
        AudioPlayer.startPlayback(song, streams)
    }
}
