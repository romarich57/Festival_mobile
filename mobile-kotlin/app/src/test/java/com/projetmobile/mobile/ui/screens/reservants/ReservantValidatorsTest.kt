package com.projetmobile.mobile.ui.screens.reservants

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReservantValidatorsTest {

    @Test
    fun validateReservantForm_requiresTypeAndEditorOnCreate() {
        val result = validateReservantForm(
            fields = ReservantFormFields(
                name = "Blue Fox",
                email = "contact@bluefox.test",
                type = ReservantTypeChoice.Editor.value,
            ),
            isEditMode = false,
        )

        assertEquals("Sélectionnez un éditeur.", result.linkedEditorError)
    }

    @Test
    fun validateReservantForm_ignoresTypeOnEdit() {
        val result = validateReservantForm(
            fields = ReservantFormFields(
                name = "Blue Fox",
                email = "contact@bluefox.test",
                type = null,
            ),
            isEditMode = true,
        )

        assertTrue(!result.hasAny())
    }

    @Test
    fun validateReservantContact_rejectsMissingFields() {
        val result = validateReservantContact(ReservantContactFormFields())

        assertEquals("Le nom est requis.", result.nameError)
        assertEquals("L'email est requis.", result.emailError)
        assertEquals("Le téléphone est requis.", result.phoneNumberError)
        assertEquals("Le poste est requis.", result.jobTitleError)
    }
}
