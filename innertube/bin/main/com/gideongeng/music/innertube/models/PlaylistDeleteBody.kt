package com.gideongeng.music.innertube.models.body

import com.gideongeng.music.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistDeleteBody(
    val context: Context,
    val playlistId: String
)
