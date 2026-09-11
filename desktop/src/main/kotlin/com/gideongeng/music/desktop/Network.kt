package com.gideongeng.music.desktop

import com.gideongeng.music.innertube.YouTube
import com.gideongeng.music.innertube.models.*
import com.gideongeng.music.innertube.pages.*
import com.gideongeng.music.lrclib.LrcLib
import com.gideongeng.music.kugou.KuGou

suspend fun searchSongs(query: String): List<SongItem> {
    if (query.isBlank()) return emptyList()
    val result = YouTube.search(query, YouTube.SearchFilter.FILTER_SONG)
    return result.getOrNull()?.items?.filterIsInstance<SongItem>() ?: emptyList()
}

suspend fun searchAll(query: String): List<YTItem> {
    if (query.isBlank()) return emptyList()
    // Search for songs
    val songs = YouTube.search(query, YouTube.SearchFilter.FILTER_SONG).getOrNull()?.items ?: emptyList()
    // Search for artists
    val artists = YouTube.search(query, YouTube.SearchFilter.FILTER_ARTIST).getOrNull()?.items ?: emptyList()
    // Search for albums
    val albums = YouTube.search(query, YouTube.SearchFilter.FILTER_ALBUM).getOrNull()?.items ?: emptyList()
    return songs + artists + albums
}

suspend fun fetchHomePage(): HomePage? {
    return try {
        YouTube.home().getOrNull()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

suspend fun fetchChipContent(endpoint: BrowseEndpoint): HomePage? {
    return try {
        YouTube.home(params = endpoint.params).getOrNull()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

suspend fun fetchAlbum(albumId: String): List<SongItem> {
    return try {
        val page = YouTube.album(albumId).getOrNull()
        page?.songs ?: emptyList()
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

suspend fun fetchAlbumPage(albumId: String): AlbumPage? {
    return try {
        YouTube.album(albumId).getOrNull()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

suspend fun fetchPlaylist(playlistId: String): List<SongItem> {
    return try {
        val page = YouTube.playlist(playlistId).getOrNull()
        page?.songs ?: emptyList()
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

suspend fun fetchPlaylistPage(playlistId: String): PlaylistPage? {
    return try {
        YouTube.playlist(playlistId).getOrNull()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

suspend fun fetchArtistPage(browseId: String): ArtistPage? {
    return try {
        YouTube.artist(browseId).getOrNull()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

suspend fun fetchSearchSuggestions(query: String) =
    YouTube.searchSuggestions(query).getOrNull()

suspend fun fetchRadioSongs(endpoint: WatchEndpoint): List<SongItem> {
    return try {
        YouTube.next(endpoint).getOrNull()?.items ?: emptyList()
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

suspend fun fetchArtistItems(endpoint: BrowseEndpoint): List<YTItem> {
    return try {
        val page = YouTube.artistItems(endpoint).getOrNull() ?: return emptyList()
        val items = page.items.toMutableList()
        var cont = page.continuation
        var hops = 0
        while (cont != null && hops < 4) {
            val next = YouTube.artistItemsContinuation(cont).getOrNull() ?: break
            items += next.items
            cont = next.continuation
            hops++
        }
        items
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

suspend fun fetchLyricsAllSources(song: SongItem, fallbackEndpoint: BrowseEndpoint?): String? {
    // 1. Try LrcLib
    try {
        val duration = song.duration ?: -1
        val artist = song.artists.firstOrNull()?.name ?: ""
        val lrc = LrcLib.getLyrics(song.title, artist, duration, song.album?.name).getOrNull()
        if (!lrc.isNullOrBlank()) return lrc
    } catch (e: Exception) { }

    // 2. Try KuGou
    try {
        val duration = song.duration ?: -1
        val artist = song.artists.firstOrNull()?.name ?: ""
        val kugou = KuGou.getLyrics(song.title, artist, duration, song.album?.name).getOrNull()
        if (!kugou.isNullOrBlank()) return kugou
    } catch (e: Exception) {}

    // 3. Try YouTube InnerTube
    if (fallbackEndpoint != null) {
        try {
            val yt = YouTube.lyrics(fallbackEndpoint).getOrNull()
            if (!yt.isNullOrBlank()) return yt
        } catch (e: Exception) {}
    }

    return null
}
