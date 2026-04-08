package com.projetmobile.mobile.ui.screens.games

import com.projetmobile.mobile.data.repository.RepositoryException
import com.projetmobile.mobile.data.repository.RepositoryFailureKind
import org.junit.Assert.assertEquals
import org.junit.Test

class GamesErrorMapperTest {

    @Test
    fun mapGamesCatalogLoadError_mapsBackendUnreachableToCachedInfoMessage() {
        val message = mapGamesCatalogLoadError(
            RepositoryException(
                kind = RepositoryFailureKind.BackendUnreachable,
                message = "Serveur inaccessible pour le moment. Réessayez plus tard.",
            ),
        )

        assertEquals("Serveur inaccessible: jeux locaux affichés.", message)
    }
}
