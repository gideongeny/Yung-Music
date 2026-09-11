package com.gideongeng.music.desktop

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gideongeng.music.innertube.models.SongItem
import kotlinx.coroutines.launch

data class LyricLine(val timeSec: Double, val text: String)

fun parseSyncedLyrics(raw: String?): List<LyricLine> {
    if (raw.isNullOrBlank()) return emptyList()
    val timed = Regex("""\[(\d{1,2}):(\d{2})(?:[.:](\d{1,3}))?]\s*(.*)""")
    val lines = mutableListOf<LyricLine>()
    raw.lineSequence().forEach { line ->
        val m = timed.find(line) ?: return@forEach
        val min = m.groupValues[1].toInt()
        val sec = m.groupValues[2].toInt()
        val frac = m.groupValues[3]
        val extra = when {
            frac.isEmpty() -> 0.0
            frac.length == 1 -> frac.toInt() / 10.0
            frac.length == 2 -> frac.toInt() / 100.0
            else -> frac.toInt() / 1000.0
        }
        val text = m.groupValues[4].trim()
        if (text.isNotBlank()) lines += LyricLine(min * 60 + sec + extra, text)
    }
    if (lines.isNotEmpty()) return lines.sortedBy { it.timeSec }
    return raw.lines().mapIndexed { i, t -> LyricLine(i * 4.0, t.trim()) }.filter { it.text.isNotBlank() }
}

fun currentLyricIndex(lines: List<LyricLine>, progressSec: Int): Int {
    if (lines.isEmpty()) return -1
    val t = progressSec.toDouble()
    var idx = 0
    for (i in lines.indices) {
        if (lines[i].timeSec <= t) idx = i else break
    }
    return idx
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Modifier.sidewaysScroll(state: LazyListState): Modifier {
    val scope = rememberCoroutineScope()
    return this.onPointerEvent(PointerEventType.Scroll) { event ->
        val change = event.changes.firstOrNull() ?: return@onPointerEvent
        val delta = if (kotlin.math.abs(change.scrollDelta.x) > 0.01f) {
            change.scrollDelta.x
        } else {
            change.scrollDelta.y
        }
        scope.launch { state.scrollBy(delta * 48f) }
        change.consume()
    }
}

@Composable
fun NowPlayingSidebar(
    song: SongItem,
    contextLabel: String,
    onClose: () -> Unit,
    onArtistClick: (String) -> Unit,
) {
    val lyricsRaw by AudioPlayer.lyrics
    val progress by AudioPlayer.progressSeconds
    val lines = remember(lyricsRaw, song.id) { parseSyncedLyrics(lyricsRaw) }
    val currentLine = currentLyricIndex(lines, progress)
    val queue by AudioPlayer.queueState
    val queueIndex by AudioPlayer.currentIndexState
    var showQueue by remember { mutableStateOf(AudioPlayer.showQueue.value) }
    var isFavorite by remember(song.id) { mutableStateOf(LibraryManager.isFavorite(song.id)) }

    LaunchedEffect(song.id, lyricsRaw) { /* recompose lyrics */ }

    Surface(color = Color(0xFF161616), modifier = Modifier.width(340.dp).fillMaxHeight()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.KeyboardArrowDown, "Close", tint = Color(0xFFBBBBBB))
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("NOW PLAYING", fontSize = 10.sp, color = Color(0xFF888888),
                        fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp)
                    if (contextLabel.isNotBlank()) {
                        Text(contextLabel, fontSize = 11.sp, color = Color(0xFFAAAAAA),
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                IconButton(onClick = { showQueue = !showQueue; AudioPlayer.showQueue.value = showQueue }) {
                    Icon(
                        Icons.AutoMirrored.Filled.QueueMusic, "Queue",
                        tint = if (showQueue) MaterialTheme.colorScheme.secondary else Color.White
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (showQueue) {
                Text("Queue · ${queue.size} tracks", fontWeight = FontWeight.SemiBold, color = Color.White)
                Spacer(Modifier.height(8.dp))
                val listState = rememberLazyListState()
                LazyColumn(Modifier.weight(1f), state = listState) {
                    itemsIndexed(queue, key = { i, s -> "${s.id}-$i" }) { i, item ->
                        val playing = i == queueIndex
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                .background(if (playing) Color(0xFF2A2A2A) else Color.Transparent)
                                .clickable { AudioPlayer.playAt(i) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ThumbnailImage(item.thumbnail, Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)))
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.title, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    color = if (playing) MaterialTheme.colorScheme.secondary else Color.White,
                                    fontSize = 13.sp, fontWeight = if (playing) FontWeight.Bold else FontWeight.Normal)
                                Text(item.artists.joinToString(", ") { it.name }, maxLines = 1,
                                    overflow = TextOverflow.Ellipsis, color = Color(0xFF888888), fontSize = 11.sp)
                            }
                            if (playing) Icon(Icons.Filled.VolumeUp, null,
                                tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            } else {
                ThumbnailImage(
                    url = song.thumbnail,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(12.dp)),
                    iconSize = 64.dp
                )
                Spacer(Modifier.height(16.dp))
                SyncedLyricLine(lines = lines, currentIndex = currentLine)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(song.title, fontWeight = FontWeight.Bold, color = Color.White,
                            fontSize = 18.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(
                            song.artists.joinToString(", ") { it.name },
                            color = Color(0xFFAAAAAA),
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable {
                                song.artists.firstOrNull()?.id?.let(onArtistClick)
                            }
                        )
                    }
                    IconButton(onClick = {
                        LibraryManager.toggleFavorite(song)
                        isFavorite = LibraryManager.isFavorite(song.id)
                    }) {
                        Icon(
                            if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            "Like",
                            tint = if (isFavorite) MaterialTheme.colorScheme.secondary else Color.White
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun SyncedLyricLine(lines: List<LyricLine>, currentIndex: Int) {
    val text = when {
        lines.isEmpty() -> "Lyrics will appear here"
        currentIndex in lines.indices -> lines[currentIndex].text
        else -> lines.first().text
    }
    Text(
        text,
        color = if (lines.isEmpty()) Color(0xFF666666) else Color.White,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        maxLines = 3,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun ProgressSection() {
    val progress by AudioPlayer.progressSeconds
    val duration by AudioPlayer.durationSeconds

    Column(Modifier.fillMaxWidth()) {
        Slider(
            value = if (duration > 0) progress.toFloat() / duration else 0f,
            onValueChange = { fraction ->
                if (duration > 0) AudioPlayer.seekTo((fraction * duration).toInt())
            },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color(0xFF555555)
            )
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("%d:%02d".format(progress / 60, progress % 60),
                color = Color(0xFF888888), fontSize = 12.sp)
            Text(if (duration > 0) "%d:%02d".format(duration / 60, duration % 60) else "--:--",
                color = Color(0xFF888888), fontSize = 12.sp)
        }
    }
}

@Composable
fun PlayerBar(onOpenNowPlaying: () -> Unit) {
    val currentSong by AudioPlayer.currentlyPlaying
    val isPlaying by AudioPlayer.isPlaying
    val progress by AudioPlayer.progressSeconds
    val duration by AudioPlayer.durationSeconds
    val shuffle by AudioPlayer.shuffleOn
    val repeat by AudioPlayer.repeatMode
    val accent = MaterialTheme.colorScheme.secondary

    Surface(color = Color(0xFF1A1A1A), tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
        Column {
            if (currentSong != null && duration > 0) {
                LinearProgressIndicator(
                    progress = { progress.toFloat() / duration },
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = accent,
                    trackColor = Color(0xFF3A3A3A),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().height(88.dp).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f).clickable(enabled = currentSong != null) { onOpenNowPlaying() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(Modifier.size(56.dp).clip(RoundedCornerShape(6.dp))) {
                        ThumbnailImage(currentSong?.thumbnail, Modifier.fillMaxSize())
                    }
                    if (currentSong != null) {
                        Column(Modifier.widthIn(max = 240.dp)) {
                            Text(currentSong!!.title, fontWeight = FontWeight.SemiBold,
                                color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(currentSong!!.artists.joinToString(", ") { it.name },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFAAAAAA), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        var isFavorite by remember(currentSong!!.id) { mutableStateOf(LibraryManager.isFavorite(currentSong!!.id)) }
                        IconButton(onClick = {
                            LibraryManager.toggleFavorite(currentSong!!)
                            isFavorite = LibraryManager.isFavorite(currentSong!!.id)
                        }) {
                            Icon(if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, "Like",
                                tint = if (isFavorite) accent else Color(0xFF888888), modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1.2f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = { AudioPlayer.toggleShuffle() }) {
                            Icon(Icons.Filled.Shuffle, "Shuffle",
                                tint = if (shuffle) accent else Color(0xFF888888), modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { AudioPlayer.playPrevious() }) {
                            Icon(Icons.Filled.SkipPrevious, "Prev", tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        IconButton(
                            onClick = { AudioPlayer.togglePlayPause() },
                            modifier = Modifier.size(46.dp).background(Color.White, CircleShape)
                        ) {
                            Icon(
                                if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, null,
                                tint = Color.Black, modifier = Modifier.size(26.dp)
                            )
                        }
                        IconButton(onClick = { AudioPlayer.playNext() }) {
                            Icon(Icons.Filled.SkipNext, "Next", tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        IconButton(onClick = { AudioPlayer.toggleRepeat() }) {
                            Icon(
                                when (repeat) {
                                    AudioPlayer.RepeatMode.ONE -> Icons.Filled.RepeatOne
                                    else -> Icons.Filled.Repeat
                                },
                                "Repeat",
                                tint = if (repeat != AudioPlayer.RepeatMode.OFF) accent else Color(0xFF888888),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    if (currentSong != null) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("%d:%02d".format(progress / 60, progress % 60), color = Color(0xFF888888), fontSize = 11.sp)
                            Slider(
                                value = if (duration > 0) progress.toFloat() / duration else 0f,
                                onValueChange = { if (duration > 0) AudioPlayer.seekTo((it * duration).toInt()) },
                                modifier = Modifier.weight(1f).height(20.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.White,
                                    activeTrackColor = Color.White,
                                    inactiveTrackColor = Color(0xFF444444)
                                )
                            )
                            Text(if (duration > 0) "%d:%02d".format(duration / 60, duration % 60) else "--:--",
                                color = Color(0xFF888888), fontSize = 11.sp)
                        }
                    }
                }

                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = {
                        AudioPlayer.showQueue.value = true
                        onOpenNowPlaying()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.QueueMusic, "Queue", tint = Color(0xFF888888), modifier = Modifier.size(18.dp))
                    }
                    var vol by remember { mutableStateOf(AudioPlayer.volume.value) }
                    Icon(Icons.Filled.VolumeUp, null, tint = Color(0xFF888888), modifier = Modifier.size(16.dp))
                    Slider(
                        value = vol,
                        onValueChange = { vol = it; AudioPlayer.setVolume(it) },
                        modifier = Modifier.width(90.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = accent,
                            activeTrackColor = accent,
                            inactiveTrackColor = Color(0xFF3A3A3A)
                        )
                    )
                    IconButton(onClick = { AudioPlayer.showNowPlaying.value = !AudioPlayer.showNowPlaying.value }) {
                        Icon(
                            if (AudioPlayer.showNowPlaying.value) Icons.Filled.KeyboardArrowRight else Icons.Filled.OpenInFull,
                            "Now playing",
                            tint = Color(0xFF888888), modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = { AudioPlayer.stopCurrent() }) {
                        Icon(Icons.Filled.Close, "Close player", tint = Color(0xFF888888), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
