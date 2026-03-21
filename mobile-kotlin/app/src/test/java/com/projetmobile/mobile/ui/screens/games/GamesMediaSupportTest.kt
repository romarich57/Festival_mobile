package com.projetmobile.mobile.ui.screens.games

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GamesMediaSupportTest {

    @Test
    fun youtubeVideoReference_buildsCompleteReferenceForWatchUrl() {
        val reference = youtubeVideoReference("https://www.youtube.com/watch?v=abc123XYZ&t=42")

        requireNotNull(reference)
        assertEquals("https://www.youtube.com/watch?v=abc123XYZ", reference.watchUrl)
        assertEquals(
            "https://www.youtube.com/embed/abc123XYZ?autoplay=1&playsinline=1",
            reference.embedUrl,
        )
        assertEquals(
            "https://img.youtube.com/vi/abc123XYZ/hqdefault.jpg",
            reference.thumbnailUrl,
        )
    }

    @Test
    fun youtubeVideoReference_extractsShortsUrl() {
        val reference = youtubeVideoReference("https://www.youtube.com/shorts/short123")

        requireNotNull(reference)
        assertEquals("short123", reference.videoId)
        assertEquals("https://www.youtube.com/watch?v=short123", reference.watchUrl)
    }

    @Test
    fun youtubeVideoId_extractsWatchUrl() {
        assertEquals(
            "abc123XYZ",
            youtubeVideoId("https://www.youtube.com/watch?v=abc123XYZ&t=42"),
        )
    }

    @Test
    fun youtubeVideoId_extractsShortUrl() {
        assertEquals(
            "abc123XYZ",
            youtubeVideoId("https://youtu.be/abc123XYZ?si=test"),
        )
    }

    @Test
    fun youtubeVideoId_extractsEmbedUrl() {
        assertEquals(
            "abc123XYZ",
            youtubeVideoId("https://www.youtube.com/embed/abc123XYZ"),
        )
    }

    @Test
    fun youtubeVideoId_returnsNullForUnsupportedUrl() {
        assertNull(youtubeVideoId("https://vimeo.com/123456"))
        assertNull(youtubeVideoId(""))
    }

    @Test
    fun youtubeThumbnailUrl_returnsThumbnailForValidUrl() {
        assertEquals(
            "https://img.youtube.com/vi/abc123XYZ/hqdefault.jpg",
            youtubeThumbnailUrl("https://www.youtube.com/watch?v=abc123XYZ"),
        )
    }

    @Test
    fun youtubeEmbedUrl_returnsEmbedUrlForValidUrl() {
        assertEquals(
            "https://www.youtube.com/embed/abc123XYZ?autoplay=1&playsinline=1",
            youtubeEmbedUrl("https://youtu.be/abc123XYZ"),
        )
    }

    @Test
    fun youtubeWatchUrl_returnsWatchUrlForValidUrl() {
        assertEquals(
            "https://www.youtube.com/watch?v=abc123XYZ",
            youtubeWatchUrl("https://youtu.be/abc123XYZ"),
        )
    }
}
