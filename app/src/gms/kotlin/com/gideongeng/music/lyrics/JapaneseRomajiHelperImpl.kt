package com.gideongeng.music.lyrics

import com.atilika.kuromoji.ipadic.Tokenizer

/**
 * GMS implementation of Japanese romaji conversion using Kuromoji.
 */
class JapaneseRomajiHelperImpl : JapaneseRomajiHelper {
    private val tokenizer: Tokenizer by lazy {
        Tokenizer()
    }

    override fun romanize(text: String): String {
        val tokens = tokenizer.tokenize(text)
        return tokens.mapIndexed { index, token ->
            val currentReading = if (token.reading.isNullOrEmpty() || token.reading == "*") {
                token.surface
            } else {
                token.reading
            }
            val nextTokenReading = if (index + 1 < tokens.size) {
                tokens[index + 1].reading?.takeIf { it.isNotEmpty() && it != "*" } ?: tokens[index + 1].surface
            } else {
                null
            }
            LyricsUtils.katakanaToRomaji(currentReading, nextTokenReading)
        }.joinToString(" ")
    }
}
