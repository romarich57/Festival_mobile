package com.projetmobile.mobile.ui.screens.games

import com.projetmobile.mobile.testutils.sampleGameDetail
import com.projetmobile.mobile.testutils.sampleMechanismOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameFormPrefillMapperTest {

    private val mapper = DefaultGameFormPrefillMapper()

    @Test
    fun toFields_mapsGameDetailIntoEditableState() {
        val fields = mapper.toFields(
            sampleGameDetail(
                id = 7,
                title = "Ark Nova",
                mechanisms = listOf(
                    sampleMechanismOption(id = 2, name = "Draft"),
                    sampleMechanismOption(id = 5, name = "Engine building"),
                ),
            ),
        )

        assertEquals("Ark Nova", fields.title)
        assertEquals("Experts", fields.type)
        assertEquals(9, fields.editorId)
        assertEquals("12", fields.minAgeInput)
        assertEquals("Designer", fields.authors)
        assertEquals("1", fields.minPlayersInput)
        assertEquals("4", fields.maxPlayersInput)
        assertEquals("45", fields.durationMinutesInput)
        assertTrue(fields.selectedMechanismIds.containsAll(listOf(2, 5)))
    }
}
