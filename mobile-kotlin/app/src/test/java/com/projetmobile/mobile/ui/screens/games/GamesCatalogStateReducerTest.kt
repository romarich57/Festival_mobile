package com.projetmobile.mobile.ui.screens.games

import com.projetmobile.mobile.testutils.sampleGameListItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GamesCatalogStateReducerTest {

    private val reducer = DefaultGamesCatalogStateReducer()

    @Test
    fun onToggleVisibleColumn_keepsAtLeastOneColumnVisible() {
        var state = GamesCatalogUiState(visibleColumns = setOf(GameVisibleColumn.Type))

        state = reducer.onToggleVisibleColumn(state, GameVisibleColumn.Type)

        assertEquals(GameVisibleColumn.entries.toSet(), state.visibleColumns)
    }

    @Test
    fun onMinAgeChanged_ignoresNonNumericInput() {
        val initial = GamesCatalogUiState(
            filters = GameCatalogFilterState(minAgeInput = "12"),
        )

        val updated = reducer.onMinAgeChanged(initial, "12a")

        assertEquals("12", updated.filters.minAgeInput)
    }

    @Test
    fun onDeleteSucceeded_removesGameAndPublishesMessage() {
        val game = sampleGameListItem(id = 8, title = "Harmonies")
        val state = GamesCatalogUiState(
            items = listOf(game, sampleGameListItem(id = 9, title = "Akropolis")),
            total = 2,
            deletingGameId = 8,
            pendingDeletion = game,
        )

        val updated = reducer.onDeleteSucceeded(state, game, "Supprimé")

        assertEquals(1, updated.items.size)
        assertEquals(1, updated.total)
        assertEquals(null, updated.deletingGameId)
        assertEquals("Supprimé", updated.infoMessage)
        assertTrue(updated.items.none { it.id == 8 })
    }

    @Test
    fun onRefreshFailed_withExistingItemsAndOfflineMessagePublishesInfoInsteadOfError() {
        val state = GamesCatalogUiState(
            items = listOf(sampleGameListItem(id = 1, title = "Akropolis")),
        )

        val updated = reducer.onRefreshFailed(
            state,
            "Mode hors-ligne: jeux locaux affichés.",
        )

        assertEquals("Mode hors-ligne: jeux locaux affichés.", updated.infoMessage)
        assertEquals(null, updated.errorMessage)
    }

    @Test
    fun onRefreshFailed_withExistingItemsAndBackendUnreachableMessagePublishesInfoInsteadOfError() {
        val state = GamesCatalogUiState(
            items = listOf(sampleGameListItem(id = 1, title = "Akropolis")),
        )

        val updated = reducer.onRefreshFailed(
            state,
            "Serveur inaccessible: jeux locaux affichés.",
        )

        assertEquals("Serveur inaccessible: jeux locaux affichés.", updated.infoMessage)
        assertEquals(null, updated.errorMessage)
    }
}
