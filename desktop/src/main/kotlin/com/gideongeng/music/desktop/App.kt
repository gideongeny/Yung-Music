package com.gideongeng.music.desktop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gideongeng.music.innertube.models.SongItem
import com.gideongeng.music.innertube.models.AlbumItem
import com.gideongeng.music.innertube.models.ArtistItem
import com.gideongeng.music.innertube.models.PlaylistItem
import com.gideongeng.music.innertube.models.BrowseEndpoint
import com.gideongeng.music.innertube.models.YTItem
import com.gideongeng.music.innertube.pages.HomePage
import com.gideongeng.music.innertube.pages.ArtistPage
import com.gideongeng.music.innertube.pages.AlbumPage
import com.gideongeng.music.innertube.pages.PlaylistPage
import kotlinx.coroutines.launch

enum class NavDestination { HOME, SEARCH, LIBRARY }

// Navigation stack for drill-down pages
sealed class DetailPage {
    data class Artist(val browseId: String, val page: ArtistPage? = null) : DetailPage()
    data class Album(val albumId: String, val page: AlbumPage? = null) : DetailPage()
    data class Playlist(val playlistId: String, val page: PlaylistPage? = null) : DetailPage()
}

@Composable
fun App() {
    val darkColors = darkColorScheme(
        primary = Color(0xFFBB86FC),
        onPrimary = Color.Black,
        primaryContainer = Color(0xFF3700B3),
        secondary = Color(0xFF03DAC6),
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E),
        surfaceVariant = Color(0xFF2A2A2A),
        onBackground = Color(0xFFE1E1E1),
        onSurface = Color(0xFFE1E1E1),
        onSurfaceVariant = Color(0xFFAAAAAA),
    )

    MaterialTheme(colorScheme = darkColors) {
        var currentNav by remember { mutableStateOf(NavDestination.HOME) }
        var searchQuery by remember { mutableStateOf("") }
        var searchResults by remember { mutableStateOf<List<YTItem>>(emptyList()) }
        var isSearching by remember { mutableStateOf(false) }
        var homePageData by remember { mutableStateOf<HomePage?>(null) }
        var homeLoading by remember { mutableStateOf(false) }
        var selectedChip by remember { mutableStateOf<BrowseEndpoint?>(null) }
        var detailPage by remember { mutableStateOf<DetailPage?>(null) }
        val coroutineScope = rememberCoroutineScope()
        val currentSong by AudioPlayer.currentlyPlaying
        val showNowPlaying by AudioPlayer.showNowPlaying
        val nowPlayingContext by AudioPlayer.nowPlayingContext

        LaunchedEffect(Unit) {
            com.gideongeng.music.innertube.YouTube.locale =
                com.gideongeng.music.innertube.models.YouTubeLocale("US", "en")
        }

        LaunchedEffect(selectedChip) {
            homeLoading = true
            val chip = selectedChip
            homePageData = if (chip == null) fetchHomePage() else fetchChipContent(chip)
            homeLoading = false
        }

        val openArtist: (String) -> Unit = { browseId ->
            coroutineScope.launch {
                detailPage = DetailPage.Artist(browseId)
                detailPage = DetailPage.Artist(browseId, fetchArtistPage(browseId))
            }
        }

        val onItemClick: (YTItem) -> Unit = { item ->
            coroutineScope.launch {
                when (item) {
                    is SongItem -> AudioPlayer.playQueue(listOf(item), 0, item.title)
                    is AlbumItem -> {
                        detailPage = DetailPage.Album(item.id)
                        detailPage = DetailPage.Album(item.id, fetchAlbumPage(item.id))
                    }
                    is PlaylistItem -> {
                        detailPage = DetailPage.Playlist(item.id)
                        detailPage = DetailPage.Playlist(item.id, fetchPlaylistPage(item.id))
                    }
                    is ArtistItem -> {
                        detailPage = DetailPage.Artist(item.id)
                        detailPage = DetailPage.Artist(item.id, fetchArtistPage(item.id))
                    }
                    else -> {}
                }
            }
        }

        val onSongClick: (SongItem) -> Unit = { song ->
            AudioPlayer.playQueue(listOf(song), 0, song.title)
        }

        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Row(Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = Color(0xFF1A1A1A),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(72.dp)
                ) {
                    Spacer(Modifier.height(20.dp))
                    Box(Modifier.padding(horizontal = 12.dp).fillMaxWidth()
                        .clickable {
                            detailPage = null
                            currentNav = NavDestination.HOME
                        }) {
                        Image(
                            painter = androidx.compose.ui.res.painterResource("icon.png"),
                            contentDescription = "YungMusic",
                            modifier = Modifier.size(32.dp)
                                .align(androidx.compose.ui.Alignment.Center)
                                .clip(CircleShape)
                        )
                    }
                    Spacer(Modifier.height(20.dp))

                    listOf(
                        Triple(NavDestination.HOME, Icons.Filled.Home, "Home"),
                        Triple(NavDestination.SEARCH, Icons.Filled.Search, "Search"),
                        Triple(NavDestination.LIBRARY, Icons.Filled.LibraryMusic, "Library"),
                    ).forEach { (dest, icon, label) ->
                        NavigationRailItem(
                            selected = currentNav == dest && detailPage == null,
                            onClick = {
                                detailPage = null
                                currentNav = dest
                            },
                            icon = { Icon(icon, label) },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        )
                    }
                }

                Column(Modifier.weight(1f).fillMaxHeight()) {
                    Row(Modifier.weight(1f).fillMaxWidth()) {
                        Box(Modifier.weight(1f).fillMaxHeight()) {
                            when {
                                detailPage != null -> {
                                    when (val dp = detailPage) {
                                        is DetailPage.Artist -> ArtistDetailScreen(
                                            page = dp.page,
                                            isLoading = dp.page == null,
                                            onBack = { detailPage = null },
                                            onSongClick = onSongClick,
                                            onItemClick = onItemClick
                                        )
                                        is DetailPage.Album -> AlbumDetailScreen(
                                            page = dp.page,
                                            isLoading = dp.page == null,
                                            onBack = { detailPage = null },
                                            onSongClick = { song ->
                                                val songs = dp.page?.songs ?: listOf(song)
                                                AudioPlayer.playQueue(
                                                    songs,
                                                    songs.indexOfFirst { it.id == song.id }.coerceAtLeast(0),
                                                    dp.page?.album?.title ?: ""
                                                )
                                            },
                                            onPlayAll = { songs ->
                                                AudioPlayer.playQueue(songs, 0, dp.page?.album?.title ?: "Album")
                                            }
                                        )
                                        is DetailPage.Playlist -> PlaylistDetailScreen(
                                            page = dp.page,
                                            isLoading = dp.page == null,
                                            onBack = { detailPage = null },
                                            onSongClick = { song ->
                                                val songs = dp.page?.songs ?: listOf(song)
                                                AudioPlayer.playQueue(
                                                    songs,
                                                    songs.indexOfFirst { it.id == song.id }.coerceAtLeast(0),
                                                    dp.page?.playlist?.title ?: ""
                                                )
                                            },
                                            onPlayAll = { songs ->
                                                AudioPlayer.playQueue(songs, 0, dp.page?.playlist?.title ?: "Playlist")
                                            }
                                        )
                                        null -> {}
                                    }
                                }
                                currentNav == NavDestination.HOME -> HomeScreen(
                                    homePage = homePageData,
                                    isLoading = homeLoading,
                                    selectedChipEndpoint = selectedChip,
                                    onChipClick = { chip -> selectedChip = chip?.endpoint },
                                    onSongClick = onSongClick,
                                    onItemClick = onItemClick
                                )
                                currentNav == NavDestination.SEARCH -> SearchScreen(
                                    query = searchQuery,
                                    onQueryChange = { searchQuery = it },
                                    isSearching = isSearching,
                                    results = searchResults,
                                    onSearch = { q ->
                                        coroutineScope.launch {
                                            isSearching = true
                                            searchResults = searchAll(q)
                                            isSearching = false
                                        }
                                    },
                                    onSongClick = onSongClick,
                                    onItemClick = onItemClick
                                )
                                currentNav == NavDestination.LIBRARY -> LibraryScreen(
                                    onSongClick = onSongClick
                                )
                            }
                        }
                        if (showNowPlaying && currentSong != null) {
                            NowPlayingSidebar(
                                song = currentSong!!,
                                contextLabel = nowPlayingContext,
                                onClose = { AudioPlayer.showNowPlaying.value = false },
                                onArtistClick = { id ->
                                    currentNav = NavDestination.SEARCH
                                    openArtist(id)
                                }
                            )
                        }
                    }
                    val miniPlayerVisible by AudioPlayer.isMiniPlayerVisible
                    if (miniPlayerVisible) {
                        PlayerBar(onOpenNowPlaying = { AudioPlayer.showNowPlaying.value = true })
                    }
                }
            }
        }
    }
}
