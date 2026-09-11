package com.gideongeng.music.desktop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image as SkiaImage
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

// ─────────────────────────────────────────────────────────────────────────────
// Lightweight image cache + loader (no Coil, pure JVM)
// ─────────────────────────────────────────────────────────────────────────────

private val imageCache = ConcurrentHashMap<String, ImageBitmap>()

@Composable
fun ThumbnailImage(
    url: String?,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
) {
    var bitmap by remember(url) { mutableStateOf<ImageBitmap?>(imageCache[url ?: ""]) }

    LaunchedEffect(url) {
        if (url.isNullOrBlank()) return@LaunchedEffect
        if (imageCache.containsKey(url)) { bitmap = imageCache[url]; return@LaunchedEffect }
        try {
            val bytes = withContext(Dispatchers.IO) {
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                conn.setRequestProperty("Accept", "image/webp,image/apng,image/*,*/*;q=0.8")
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                conn.inputStream.readBytes()
            }
            val img = SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
            imageCache[url] = img
            bitmap = img
        } catch (e: Exception) {
            // silently ignore bad URLs
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!,
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier.background(Color(0xFF2A2A2A)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.MusicNote,
                contentDescription = null,
                tint = Color(0xFF555555),
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

// Convenience overloads
@Composable
fun RoundedThumbnail(url: String?, size: Dp, cornerRadius: Dp = 6.dp) {
    ThumbnailImage(
        url = url,
        modifier = Modifier.size(size).clip(RoundedCornerShape(cornerRadius)),
        iconSize = size * 0.4f
    )
}
