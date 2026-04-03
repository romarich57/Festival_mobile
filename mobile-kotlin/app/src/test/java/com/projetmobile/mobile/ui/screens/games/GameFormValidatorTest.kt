package com.projetmobile.mobile.ui.screens.games

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class GameFormValidatorTest {

    private val validator = DefaultGameFormValidator()

    @Test
    fun validate_flagsRequiredFields() {
        val result = validator.validate(GameFormFields())

        assertFalse(result.isValid)
        assertEquals("Le titre est requis.", result.fields.titleError)
        assertEquals("Le type est requis.", result.fields.typeError)
        assertEquals("L'éditeur est requis.", result.fields.editorError)
        assertEquals("L'âge minimum est requis.", result.fields.minAgeError)
        assertEquals("Les auteurs sont requis.", result.fields.authorsError)
    }

    @Test
    fun validate_rejectsInvalidPlayerRangeAndNegativeDuration() {
        val result = validator.validate(
            GameFormFields(
                title = "Akropolis",
                type = "Experts",
                editorId = 9,
                minAgeInput = "12",
                authors = "Designer",
                minPlayersInput = "5",
                maxPlayersInput = "2",
                durationMinutesInput = "-1",
            ),
        )

        assertFalse(result.isValid)
        assertEquals("Le max doit être supérieur ou égal au min.", result.fields.maxPlayersError)
        assertEquals("La durée doit être positive.", result.fields.durationMinutesError)
    }
}
