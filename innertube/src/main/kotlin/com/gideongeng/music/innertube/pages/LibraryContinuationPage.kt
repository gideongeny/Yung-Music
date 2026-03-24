package com.gideongeng.music.innertube.pages

import com.gideongeng.music.innertube.models.YTItem

data class LibraryContinuationPage(
    val items: List<YTItem>,
    val continuation: String?,
)
