package com.gideongeng.music.desktop

import com.gideongeng.music.innertube.NewPipeExtractor
import com.gideongeng.music.innertube.YouTube
import com.gideongeng.music.innertube.models.YouTubeClient
import com.gideongeng.music.innertube.models.response.PlayerResponse

/**
 * Desktop equivalent of Android [com.gideongeng.music.utils.YTPlayerUtils]:
 * Innertube player clients + NewPipe signature deobfuscation + StreamInfo fallback.
 */
object StreamResolver {
    data class ResolvedStream(
        val url: String,
        val mimeType: String,
        val bitrate: Int,
        val itag: Int,
    )

    private val mainClient = YouTubeClient.ANDROID_VR_NO_AUTH

    private val fallbackClients = listOf(
        YouTubeClient.IOS,
        YouTubeClient.IPADOS,
        YouTubeClient.ANDROID_VR_1_61_48,
        YouTubeClient.ANDROID_VR_1_43_32,
        YouTubeClient.MOBILE,
        YouTubeClient.ANDROID_CREATOR,
        YouTubeClient.TVHTML5,
        YouTubeClient.TVHTML5_SIMPLY_EMBEDDED_PLAYER,
        YouTubeClient.WEB,
        YouTubeClient.WEB_CREATOR,
        YouTubeClient.WEB_REMIX,
    )

    suspend fun resolveAudioStreams(videoId: String, playlistId: String? = null): List<ResolvedStream> {
        val signatureTimestamp = NewPipeExtractor.getSignatureTimestamp(videoId).getOrNull()
        val isLoggedIn = YouTube.cookie != null
        val clients = listOf(mainClient) + fallbackClients

        for (client in clients) {
            if (client.loginRequired && !isLoggedIn) continue

            val sig = if (client.useSignatureTimestamp) signatureTimestamp else null
            val response = YouTube.player(videoId, playlistId, client, sig).getOrNull() ?: continue
            if (response.playabilityStatus.status != "OK") {
                println(
                    "YungMusic: ${client.friendlyName ?: client.clientName} " +
                        "playability=${response.playabilityStatus.status} ${response.playabilityStatus.reason ?: ""}"
                )
                continue
            }

            val merged = YouTube.newPipePlayer(videoId, response) ?: response
            val streams = extractStreams(videoId, merged)
            if (streams.isNotEmpty()) {
                println("YungMusic: Resolved ${streams.size} audio stream(s) via ${client.friendlyName ?: client.clientName}")
                return streams
            }
        }

        val fromExtractor = YouTube.getNewPipeStreamUrls(videoId)
        if (fromExtractor.isNotEmpty()) {
            println("YungMusic: Using ${fromExtractor.size} NewPipe StreamInfo URL(s)")
            return fromExtractor
                .map { (itag, url) -> ResolvedStream(url, "audio/*", 0, itag) }
                .sortedByDescending { score(it) }
        }

        return emptyList()
    }

    private fun extractStreams(
        videoId: String,
        response: PlayerResponse,
    ): List<ResolvedStream> {
        val formats =
            (response.streamingData?.adaptiveFormats.orEmpty()) +
                (response.streamingData?.formats.orEmpty())

        val audioFormats = formats.filter { it.isAudio || it.mimeType.contains("audio", ignoreCase = true) }
        val toScan = audioFormats.ifEmpty { formats }

        return toScan.mapNotNull { fmt ->
            val url = when {
                !fmt.url.isNullOrBlank() -> fmt.url
                else -> NewPipeExtractor.getStreamUrl(fmt, videoId)
            } ?: return@mapNotNull null
            ResolvedStream(url, fmt.mimeType, fmt.bitrate, fmt.itag)
        }.distinctBy { it.itag }
            .sortedByDescending { score(it) }
    }

    private fun score(stream: ResolvedStream): Int {
        val mime = stream.mimeType.lowercase()
        val base = when {
            "audio/webm" in mime -> 300_000
            "audio/mp4" in mime -> 200_000
            "audio/" in mime -> 100_000
            else -> 0
        }
        return base + stream.bitrate / 1000
    }
}
