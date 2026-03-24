package com.gideongeng.music.innertube.pages

import com.gideongeng.music.innertube.models.SongItem

data class PlaylistContinuationPage(
    val songs: List<SongItem>,
    val continuation: String?,
)
