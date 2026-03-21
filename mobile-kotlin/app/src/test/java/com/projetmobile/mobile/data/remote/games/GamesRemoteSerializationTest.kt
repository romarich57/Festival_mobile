package com.projetmobile.mobile.data.remote.games

import com.projetmobile.mobile.data.entity.games.GameDraft
import com.projetmobile.mobile.data.remote.common.ApiJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GamesRemoteSerializationTest {

    @Test
    fun gamesPageResponseDto_decodesNestedPaginationContract() {
        val payload = """
            {
              "items": [
                {
                  "id": 42,
                  "title": "Akropolis",
                  "type": "Experts",
                  "editor_id": 7,
                  "editor_name": "Gigamic",
                  "min_age": 8,
                  "authors": "Jules Messaud",
                  "min_players": 1,
                  "max_players": 4,
                  "prototype": false,
                  "duration_minutes": 25,
                  "theme": "City building",
                  "description": "Compose the best city.",
                  "image_url": "/uploads/games/akropolis.png",
                  "rules_video_url": "https://example.test/rules",
                  "mechanisms": [
                    { "id": 3, "name": "Draft", "description": "Pick and pass" }
                  ]
                }
              ],
              "pagination": {
                "page": 2,
                "limit": 10,
                "total": 25,
                "totalPages": 3,
                "sortBy": "title",
                "sortOrder": "asc"
              }
            }
        """.trimIndent()

        val dto = ApiJson.instance.decodeFromString<GamesPageResponseDto>(payload)

        assertEquals(1, dto.items.size)
        assertEquals(2, dto.pagination.page)
        assertEquals(10, dto.pagination.limit)
        assertEquals(25, dto.pagination.total)
        assertEquals(3, dto.pagination.totalPages)
        assertEquals("title", dto.pagination.sortBy)
        assertEquals("asc", dto.pagination.sortOrder)
        assertEquals("Draft", dto.items.single().mechanisms.single().name)
    }

    @Test
    fun gameDto_decodesSnakeCasePayloadWithMechanisms() {
        val payload = """
            {
              "id": 42,
              "title": "Akropolis",
              "type": "Experts",
              "editor_id": 7,
              "editor_name": "Gigamic",
              "min_age": 8,
              "authors": "Jules Messaud",
              "min_players": 1,
              "max_players": 4,
              "prototype": false,
              "duration_minutes": 25,
              "theme": "City building",
              "description": "Compose the best city.",
              "image_url": "/uploads/games/akropolis.png",
              "rules_video_url": "https://example.test/rules",
              "mechanisms": [
                { "id": 3, "name": "Draft", "description": "Pick and pass" }
              ]
            }
        """.trimIndent()

        val dto = ApiJson.instance.decodeFromString<GameDto>(payload)

        assertEquals(42, dto.id)
        assertEquals(7, dto.editorId)
        assertEquals("Gigamic", dto.editorName)
        assertEquals(25, dto.durationMinutes)
        assertEquals(1, dto.mechanisms.size)
        assertEquals("Draft", dto.mechanisms.single().name)
    }

    @Test
    fun gameTypesEndpoint_decodesArrayOfStrings() {
        val payload = """["Ambiance","Experts"]"""

        val values = ApiJson.instance.decodeFromString<List<String>>(payload)

        assertEquals(listOf("Ambiance", "Experts"), values)
    }

    @Test
    fun gameUpsertRequest_keepsExplicitNullsAndDistinctMechanismIds() {
        val dto = GameDraft(
            title = "Akropolis Deluxe",
            type = "Experts",
            editorId = 7,
            minAge = 8,
            authors = "Jules Messaud",
            minPlayers = null,
            maxPlayers = 4,
            prototype = false,
            durationMinutes = null,
            theme = null,
            description = "   ",
            imageUrl = "/uploads/games/akropolis-deluxe.png",
            rulesVideoUrl = null,
            mechanismIds = listOf(3, 8, 8),
        ).toRequestDto()

        val encoded = ApiJson.instance.encodeToString(dto)

        assertTrue(encoded.contains("\"title\":\"Akropolis Deluxe\""))
        assertTrue(encoded.contains("\"min_players\":null"))
        assertTrue(encoded.contains("\"theme\":null"))
        assertTrue(encoded.contains("\"description\":null"))
        assertTrue(encoded.contains("\"image_url\":\"/uploads/games/akropolis-deluxe.png\""))
        assertTrue(encoded.contains("\"mechanismIds\":[3,8]"))
    }
}
