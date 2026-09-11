package com.gideongeng.music.desktop

import com.gideongeng.music.innertube.models.SongItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object LibraryManager {
    private val historyFile = File("history.json")
    private val gson = Gson()
    private val type = object : TypeToken<List<SongItem>>() {}.type

    fun getHistory(): List<SongItem> {
        if (!historyFile.exists()) return emptyList()
        return try {
            gson.fromJson(historyFile.readText(), type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addSongToHistory(song: SongItem) {
        val history = getHistory().toMutableList()
        history.removeAll { it.id == song.id }
        history.add(0, song)
        
        if (history.size > 500) {
            history.removeAt(history.lastIndex)
        }
        
        try {
            historyFile.writeText(gson.toJson(history))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val favoritesFile = File("favorites.json")

    fun getFavorites(): List<SongItem> {
        if (!favoritesFile.exists()) return emptyList()
        return try {
            gson.fromJson(favoritesFile.readText(), type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun isFavorite(songId: String): Boolean {
        return getFavorites().any { it.id == songId }
    }

    fun toggleFavorite(song: SongItem) {
        val favorites = getFavorites().toMutableList()
        if (favorites.any { it.id == song.id }) {
            favorites.removeAll { it.id == song.id }
        } else {
            favorites.add(0, song)
        }
        try {
            favoritesFile.writeText(gson.toJson(favorites))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
