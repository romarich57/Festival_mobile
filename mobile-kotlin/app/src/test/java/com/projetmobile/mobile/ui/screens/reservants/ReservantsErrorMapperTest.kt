package com.projetmobile.mobile.ui.screens.reservants

import com.projetmobile.mobile.data.repository.RepositoryException
import org.junit.Assert.assertEquals
import org.junit.Test

class ReservantsErrorMapperTest {

    @Test
    fun mapReservantFormSaveError_mapsDuplicateNameAndEmail() {
        val presentation = mapReservantFormSaveError(
            throwable = RepositoryException(message = "Nom et email déjà utilisés"),
            isEditMode = false,
        )

        assertEquals("Ce nom est déjà utilisé.", presentation.fieldErrors.nameError)
        assertEquals("Cet email est déjà utilisé.", presentation.fieldErrors.emailError)
        assertEquals(null, presentation.bannerMessage)
    }

    @Test
    fun mapReservantDeleteError_mapsReservationConflict() {
        val message = mapReservantDeleteError(
            RepositoryException(
                statusCode = 409,
                message = "Conflit",
                details = listOf("réservation active"),
            ),
        )

        assertEquals(
            "Ce réservant ne peut pas être supprimé car il est encore utilisé dans une réservation.",
            message,
        )
    }
}
