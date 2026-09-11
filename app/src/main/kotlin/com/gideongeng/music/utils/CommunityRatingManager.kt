/**
 * CommunityRatingManager - Fetches community-sourced vote data for tracks.
 * Shows real engagement metrics beyond what the streaming platform exposes.
 */

package com.gideongeng.music.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class CommunityRating(
    val likes: Long,
    val dislikes: Long,
    val rating: Double,        // 0.0 to 5.0
    val viewCount: Long
) {
    val likePercentage: Int
        get() {
            val total = likes + dislikes
            return if (total == 0L) 0 else ((likes.toDouble() / total) * 100).toInt()
        }

    val dislikePercentage: Int
        get() = 100 - likePercentage

    fun formattedDislikes(): String = formatCount(dislikes)
    fun formattedLikes(): String = formatCount(likes)

    private fun formatCount(count: Long): String = when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}

object CommunityRatingManager {
    private const val TAG = "CommunityRatingManager"
    private const val BASE_URL = "https://returnyoutubedislikeapi.com/votes"

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    // In-memory cache: videoId -> CommunityRating
    private val ratingCache = HashMap<String, CommunityRating>()

    /**
     * Fetches community rating for a given video ID.
     * Returns null if disabled, no internet, or an error occurs.
     */
    suspend fun getRating(videoId: String, enabled: Boolean = true): CommunityRating? {
        if (!enabled || videoId.isBlank()) return null

        // Return cached result if available
        ratingCache[videoId]?.let { return it }

        return withContext(Dispatchers.IO) {
            try {
                val url = "$BASE_URL?videoId=$videoId"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "YungMusic/1.0")
                    .build()

                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    Log.w(TAG, "Failed to fetch community rating: HTTP ${response.code}")
                    return@withContext null
                }

                val body = response.body?.string() ?: return@withContext null
                val rating = parseRating(body)
                if (rating != null) ratingCache[videoId] = rating
                rating
            } catch (e: Exception) {
                Log.w(TAG, "Could not fetch community rating: ${e.message}")
                null
            }
        }
    }

    private fun parseRating(json: String): CommunityRating? {
        return try {
            val obj = JSONObject(json)
            CommunityRating(
                likes = obj.optLong("likes", 0L),
                dislikes = obj.optLong("dislikes", 0L),
                rating = obj.optDouble("rating", 0.0),
                viewCount = obj.optLong("viewCount", 0L)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing community rating: ${e.message}")
            null
        }
    }

    /**
     * Clears the rating cache for a specific video or entirely.
     */
    fun clearCache(videoId: String? = null) {
        if (videoId != null) ratingCache.remove(videoId)
        else ratingCache.clear()
    }
}
