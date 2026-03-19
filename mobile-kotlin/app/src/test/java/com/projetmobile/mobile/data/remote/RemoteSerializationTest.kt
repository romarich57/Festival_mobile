package com.projetmobile.mobile.data.remote

import com.projetmobile.mobile.data.entity.profile.OptionalField
import com.projetmobile.mobile.data.entity.profile.ProfileUpdateInput
import com.projetmobile.mobile.data.remote.common.ApiJson
import com.projetmobile.mobile.data.remote.festival.FestivalDto
import com.projetmobile.mobile.data.remote.profile.toDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteSerializationTest {

    @Test
    fun festivalDto_decodesSnakeCasePayloadWithKotlinSerialization() {
        val payload = """
            {
              "id": 3,
              "name": "Festival Test",
              "start_date": "2026-07-01",
              "end_date": "2026-07-02",
              "stock_tables_standard": 10,
              "stock_tables_grande": 2,
              "stock_tables_mairie": 1,
              "stock_chaises": 42,
              "prix_prises": 18.5
            }
        """.trimIndent()

        val dto = ApiJson.instance.decodeFromString<FestivalDto>(payload)

        assertEquals(3, dto.id)
        assertEquals("2026-07-01", dto.startDate)
        assertEquals(18.5, dto.prixPrises, 0.0)
    }

    @Test
    fun updateProfileDto_keepsExplicitNullsForNullableFields() {
        val dto = ProfileUpdateInput(
            email = "new@example.com",
            phone = OptionalField.Value(null),
            avatarUrl = OptionalField.Value("/uploads/avatars/new.png"),
        ).toDto()

        val encoded = ApiJson.instance.encodeToString(dto)

        assertTrue(encoded.contains("\"email\":\"new@example.com\""))
        assertTrue(encoded.contains("\"phone\":null"))
        assertTrue(encoded.contains("\"avatarUrl\":\"/uploads/avatars/new.png\""))
    }
}
