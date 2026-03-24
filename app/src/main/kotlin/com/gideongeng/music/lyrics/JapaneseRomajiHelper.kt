package com.gideongeng.music.lyrics

/**
 * Interface for Japanese text tokenization and romaji conversion.
 */
interface JapaneseRomajiHelper {
    fun romanize(text: String): String
}
