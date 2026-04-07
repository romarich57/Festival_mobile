package com.projetmobile.mobile.data.entity.festival

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FestivalPermissionsTest {

    @Test
    fun backofficeRoles_matchFestivalPolicies() {
        assertTrue(canManageFestivals("admin"))
        assertTrue(canManageFestivals("Organizer"))
        assertTrue(canDeleteFestivals("super-organizer"))
        assertFalse(canDeleteFestivals("organizer"))
        assertFalse(canManageFestivals("visitor"))
    }
}
