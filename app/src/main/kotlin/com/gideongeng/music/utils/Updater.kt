/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.gideongeng.music.utils

import com.gideongeng.music.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import org.json.JSONObject

object Updater {
    private val client = HttpClient()
    var lastCheckTime = -1L
        private set
    private var latestReleaseJson: JSONObject? = null

    /**
     * Compares two version strings.
     * Returns: 1 if v1 > v2, -1 if v1 < v2, 0 if equal
     */
    private fun compareVersions(v1: String, v2: String): Int {
        val v1Parts = v1.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
        val v2Parts = v2.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
        val maxLength = maxOf(v1Parts.size, v2Parts.size)
        
        for (i in 0 until maxLength) {
            val part1 = v1Parts.getOrNull(i) ?: 0
            val part2 = v2Parts.getOrNull(i) ?: 0
            when {
                part1 > part2 -> return 1
                part1 < part2 -> return -1
            }
        }
        return 0
    }

    /**
     * Checks if the latest version is newer than the current version.
     * Returns true if an update is available (latestVersion > currentVersion)
     */
    fun isUpdateAvailable(currentVersion: String, latestVersion: String): Boolean {
        return compareVersions(latestVersion, currentVersion) > 0
    }

    suspend fun getLatestVersionName(): Result<String> =
        runCatching {
            val response =
                client.get("https://api.github.com/repos/gideongeny/Yung-Music/releases/latest")
                    .bodyAsText()
            val json = JSONObject(response)
            latestReleaseJson = json
            val versionName = json.getString("name")
            lastCheckTime = System.currentTimeMillis()
            versionName
        }

    fun getLatestDownloadUrl(): String {
        val json = latestReleaseJson
        if (json != null && json.has("assets")) {
            val assets = json.getJSONArray("assets")
            val architecture = BuildConfig.ARCHITECTURE
            val isGmsVariant = BuildConfig.CAST_AVAILABLE
            
            // 1. Try to find exact match for architecture and variant
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                val name = asset.getString("name").lowercase()
                val url = asset.getString("browser_download_url")
                
                if (name.endsWith(".apk")) {
                    val isUniversal = "universal" in name
                    val isArm64 = "arm64" in name
                    val isX86 = "x86" in name
                    val isGms = "gms" in name || "cast" in name
                    
                    // Simple matching logic
                    if (architecture == "universal" && isUniversal) return url
                    if (architecture == "arm64" && isArm64) return url
                    // Fallback to the first APK found if specific logic fails, 
                    // or refine this if you have specific naming conventions.
                }
            }
            
            // 2. Fallback: Find ANY apk
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                val name = asset.getString("name").lowercase()
                if (name.endsWith(".apk")) {
                    return asset.getString("browser_download_url")
                }
            }
        }

        // 3. Last resort fallback (manual construction)
        val baseUrl = "https://github.com/gideongeny/Yung-Music/releases/latest/download/"
        val architecture = BuildConfig.ARCHITECTURE
        // ... unused logic kept simple or removed ...
        return baseUrl + "app-release.apk" // generic fallback
    }
}
