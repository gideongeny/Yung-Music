/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.gideongeng.music.lyrics

import android.content.Context
import com.metrolist.simpmusic.SimpMusicLyrics
import com.gideongeng.music.constants.EnableSimpMusicKey
import com.gideongeng.music.utils.dataStore
import com.gideongeng.music.utils.get

object SimpMusicLyricsProvider : LyricsProvider {
    override val name = "SimpMusic"

    override fun isEnabled(context: Context): Boolean = context.dataStore[EnableSimpMusicKey] ?: true

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
        album: String?,
    ): Result<String> = SimpMusicLyrics.getLyrics(id, duration)

    override suspend fun getAllLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
        album: String?,
        callback: (String) -> Unit,
    ) {
        SimpMusicLyrics.getAllLyrics(id, duration, callback)
    }
}
