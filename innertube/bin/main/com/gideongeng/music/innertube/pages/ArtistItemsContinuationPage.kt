package com.gideongeng.music.innertube.pages

import com.gideongeng.music.innertube.models.YTItem

data class ArtistItemsContinuationPage(
    val items: List<YTItem>,
    val continuation: String?,
)
