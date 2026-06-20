package com.gideongeng.music.lyrics

/**
 * FOSS implementation of Japanese romaji conversion (No-op fallback).
 */
class JapaneseRomajiHelperImpl : JapaneseRomajiHelper {
    override fun romanize(text: String): String {
        // Return original text as we don't have the tokenizer binaries in FOSS flavor
        return text
    }
}
