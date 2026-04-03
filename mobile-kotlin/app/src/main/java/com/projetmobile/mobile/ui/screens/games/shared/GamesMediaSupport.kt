package com.projetmobile.mobile.ui.screens.games

import com.projetmobile.mobile.BuildConfig
import java.net.URI

internal data class YoutubeVideoReference(
    val originalUrl: String,
    val videoId: String,
) {
    val watchUrl: String = "https://www.youtube.com/watch?v=$videoId"
    val embedUrl: String = "https://www.youtube.com/embed/$videoId?autoplay=1&playsinline=1"
    val thumbnailUrl: String = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
}

internal fun youtubeVideoReference(url: String): YoutubeVideoReference? {
    val trimmedUrl = url.trim()
    val videoId = extractYoutubeVideoId(trimmedUrl) ?: return null
    return YoutubeVideoReference(
        originalUrl = trimmedUrl,
        videoId = videoId,
    )
}

internal fun youtubeVideoId(url: String): String? {
    return youtubeVideoReference(url)?.videoId
}

internal fun youtubeWatchUrl(url: String): String? {
    return youtubeVideoReference(url)?.watchUrl
}

internal fun youtubeThumbnailUrl(url: String): String? {
    return youtubeVideoReference(url)?.thumbnailUrl
}

internal fun youtubeEmbedUrl(url: String): String? {
    return youtubeVideoReference(url)?.embedUrl
}

private fun extractYoutubeVideoId(url: String): String? {
    val trimmedUrl = url.trim()
    if (trimmedUrl.isEmpty()) return null

    val uri = runCatching { URI(trimmedUrl) }.getOrNull() ?: return null
    val host = uri.host?.removePrefix("www.")?.lowercase() ?: return null
    val path = uri.path.orEmpty().trim('/')

    return when {
        host == "youtu.be" -> path.substringBefore('/')
        host.endsWith("youtube.com") && path == "watch" -> {
            uri.query
                ?.split("&")
                ?.firstOrNull { it.startsWith("v=") }
                ?.substringAfter("v=")
        }

        host.endsWith("youtube.com") && path.startsWith("embed/") -> {
            path.substringAfter("embed/").substringBefore('/')
        }

        host.endsWith("youtube.com") && path.startsWith("shorts/") -> {
            path.substringAfter("shorts/").substringBefore('/')
        }

        else -> null
    }?.takeIf { it.isNotBlank() }
}

internal fun toAbsoluteBackendUrl(path: String): String {
    return if (path.startsWith("http://") || path.startsWith("https://")) {
        path
    } else {
        BuildConfig.API_BASE_URL.removeSuffix("api/").removeSuffix("/") + path
    }
}
