package com.gideongeng.music.innertube.models.body

import com.gideongeng.music.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class SubscribeBody(
    val channelIds: List<String>,
    val context: Context,
)
