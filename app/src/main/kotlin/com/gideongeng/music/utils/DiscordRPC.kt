package com.gideongeng.music.utils

import android.content.Context

class DiscordRPC(context: Context, key: String?) {
    constructor(context: Context, useDetails: Boolean) : this(context, null)
    fun closeRPC() {}
    fun close() {}
    fun updateSong(song: Any?, position: Long, speed: Float, useDetails: Boolean) {}
    fun isRpcRunning(): Boolean = false
}
