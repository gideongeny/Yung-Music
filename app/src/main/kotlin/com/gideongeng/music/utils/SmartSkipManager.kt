/**
 * SmartSkipManager - Automatically skips non-music content segments during playback.
 * Powered by community-sourced segment data.
 */

package com.gideongeng.music.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.util.concurrent.TimeUnit

data class SkipSegment(
    val startMs: Long,
    val endMs: Long,
    val category: String
)

object SmartSkipManager {
    private const val TAG = "SmartSkipManager"
    private const val BASE_URL = "https://sponsor.ajay.app/api/skipSegments"

    // Categories to skip by default: sponsors, intros, outros, self-promo, interaction reminders
    private val DEFAULT_CATEGORIES = listOf("sponsor", "intro", "outro", "selfpromo", "interaction")

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    // In-memory cache: videoId -> list of segments
    private val segmentCache = HashMap<String, List<SkipSegment>>()

    /**
     * Fetches skip segments for a given video ID.
     * Returns an empty list if disabled, no internet, or no segments found.
     */
    suspend fun getSegments(
        videoId: String,
        categories: List<String> = DEFAULT_CATEGORIES,
        enabled: Boolean = true
    ): List<SkipSegment> {
        if (!enabled || videoId.isBlank()) return emptyList()

        // Return cached result if available
        segmentCache[videoId]?.let { return it }

        return withContext(Dispatchers.IO) {
            try {
                val categoryParam = categories.joinToString(",") { "\"$it\"" }
                val url = "$BASE_URL?videoID=$videoId&categories=[$categoryParam]"

                val request = Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "YungMusic/1.0")
                    .build()

                val response = client.newCall(request).execute()

                if (response.code == 404) {
                    // No segments found for this video — cache empty list to avoid re-fetching
                    segmentCache[videoId] = emptyList()
                    return@withContext emptyList()
                }

                if (!response.isSuccessful) {
                    Log.w(TAG, "Failed to fetch segments: HTTP ${response.code}")
                    return@withContext emptyList()
                }

                val body = response.body?.string() ?: return@withContext emptyList()
                val segments = parseSegments(body)
                segmentCache[videoId] = segments
                segments
            } catch (e: Exception) {
                Log.w(TAG, "Could not fetch skip segments: ${e.message}")
                emptyList()
            }
        }
    }

    private fun parseSegments(json: String): List<SkipSegment> {
        return try {
            val array = JSONArray(json)
            val result = mutableListOf<SkipSegment>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val segment = obj.getJSONArray("segment")
                val startSec = segment.getDouble(0)
                val endSec = segment.getDouble(1)
                val category = obj.getString("category")
                result.add(
                    SkipSegment(
                        startMs = (startSec * 1000).toLong(),
                        endMs = (endSec * 1000).toLong(),
                        category = category
                    )
                )
            }
            Log.d(TAG, "Parsed ${result.size} skip segments")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing skip segments: ${e.message}")
            emptyList()
        }
    }

    /**
     * Given the current playback position, returns the segment to skip to (if any).
     */
    fun getActiveSegment(positionMs: Long, segments: List<SkipSegment>): SkipSegment? {
        return segments.firstOrNull { segment ->
            positionMs >= segment.startMs && positionMs < segment.endMs
        }
    }

    /**
     * Clears the segment cache for a specific video or entirely.
     */
    fun clearCache(videoId: String? = null) {
        if (videoId != null) segmentCache.remove(videoId)
        else segmentCache.clear()
    }
}
