package com.projetmobile.mobile.data.entity.games

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GamesPermissionsTest {

    @Test
    fun backofficeRoles_canManageGamesAndUploads() {
        assertTrue(canManageGames("admin"))
        assertTrue(canManageGames("Organizer"))
        assertTrue(canUploadGameImages("super-organizer"))
        assertFalse(canManageGames("visitor"))
        assertFalse(canUploadGameImages(null))
    }
}
