package com.gideongeng.music.innertube.models.body

import com.gideongeng.music.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class SearchBody(
    val context: Context,
    val query: String?,
    val params: String?,
)
