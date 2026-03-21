package com.projetmobile.mobile.ui.screens.reservants

import com.projetmobile.mobile.data.entity.reservants.ReservantListItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReservantsCatalogStateReducerTest {

    private val reducer = DefaultReservantsCatalogStateReducer()

    @Test
    fun onLoadSucceeded_appliesQueryTypeAndLinkedEditorFilters() {
        val state = ReservantsCatalogUiState(
            filters = ReservantsCatalogFilterState(
                query = "zen",
                selectedType = ReservantTypeChoice.Editor.value,
                linkedEditorOnly = true,
            ),
        )

        val nextState = reducer.onLoadSucceeded(
            state = state,
            items = listOf(
                sampleListItem(id = 1, name = "Zenith Games", type = "editeur", editorId = 42),
                sampleListItem(id = 2, name = "Zen Boutique", type = "boutique", editorId = 42),
                sampleListItem(id = 3, name = "Zen Maker", type = "editeur", editorId = null),
            ),
        )

        assertEquals(listOf(1), nextState.filteredItems.map { it.id })
        assertEquals(3, nextState.totalCount)
    }

    @Test
    fun onDeleteSucceeded_removesItemAndClearsDialogState() {
        val deletable = sampleListItem(id = 7, name = "Meeple Corp")
        val state = ReservantsCatalogUiState(
            allItems = listOf(deletable, sampleListItem(id = 8, name = "Blue Fox")),
            filteredItems = listOf(deletable, sampleListItem(id = 8, name = "Blue Fox")),
            pendingDeletion = deletable,
            deletingReservantId = 7,
        )

        val nextState = reducer.onDeleteSucceeded(
            state = state,
            reservant = deletable,
            message = "Réservant supprimé.",
        )

        assertEquals(listOf(8), nextState.allItems.map { it.id })
        assertEquals(listOf(8), nextState.filteredItems.map { it.id })
        assertNull(nextState.pendingDeletion)
        assertNull(nextState.deletingReservantId)
        assertEquals("Réservant supprimé.", nextState.infoMessage)
    }

    private fun sampleListItem(
        id: Int,
        name: String,
        type: String = "prestataire",
        editorId: Int? = null,
    ): ReservantListItem {
        return ReservantListItem(
            id = id,
            name = name,
            email = "$id@example.com",
            type = type,
            editorId = editorId,
            phoneNumber = "060000000$id",
            address = null,
            siret = null,
            notes = null,
        )
    }
}
