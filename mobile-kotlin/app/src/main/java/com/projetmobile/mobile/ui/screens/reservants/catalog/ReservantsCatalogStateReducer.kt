package com.projetmobile.mobile.ui.screens.reservants

import com.projetmobile.mobile.data.entity.reservants.ReservantDeleteSummary
import com.projetmobile.mobile.data.entity.reservants.ReservantListItem

internal interface ReservantsCatalogStateReducer {
    fun onQueryChanged(state: ReservantsCatalogUiState, value: String): ReservantsCatalogUiState

    fun onTypeSelected(
        state: ReservantsCatalogUiState,
        value: String?,
    ): ReservantsCatalogUiState

    fun onLinkedEditorOnlyChanged(
        state: ReservantsCatalogUiState,
        value: Boolean,
    ): ReservantsCatalogUiState

    fun onSortSelected(
        state: ReservantsCatalogUiState,
        sort: ReservantsSortOption,
    ): ReservantsCatalogUiState

    fun onLoadStarted(state: ReservantsCatalogUiState, refreshing: Boolean): ReservantsCatalogUiState

    fun onLoadSucceeded(
        state: ReservantsCatalogUiState,
        items: List<ReservantListItem>,
    ): ReservantsCatalogUiState

    fun onLoadFailed(state: ReservantsCatalogUiState, message: String): ReservantsCatalogUiState

    fun onRequestDelete(
        state: ReservantsCatalogUiState,
        reservant: ReservantListItem,
    ): ReservantsCatalogUiState

    fun onDeleteSummaryLoaded(
        state: ReservantsCatalogUiState,
        summary: ReservantDeleteSummary,
    ): ReservantsCatalogUiState

    fun onDeleteSummaryFailed(
        state: ReservantsCatalogUiState,
        message: String,
    ): ReservantsCatalogUiState

    fun onDismissDeleteDialog(state: ReservantsCatalogUiState): ReservantsCatalogUiState

    fun onDeleteStarted(state: ReservantsCatalogUiState, reservantId: Int): ReservantsCatalogUiState

    fun onDeleteSucceeded(
        state: ReservantsCatalogUiState,
        reservant: ReservantListItem,
        message: String,
    ): ReservantsCatalogUiState

    fun onDeleteFailed(state: ReservantsCatalogUiState, message: String): ReservantsCatalogUiState

    fun onDismissInfoMessage(state: ReservantsCatalogUiState): ReservantsCatalogUiState

    fun onDismissErrorMessage(state: ReservantsCatalogUiState): ReservantsCatalogUiState
}

internal class DefaultReservantsCatalogStateReducer : ReservantsCatalogStateReducer {
    override fun onQueryChanged(
        state: ReservantsCatalogUiState,
        value: String,
    ): ReservantsCatalogUiState {
        return state.withFilters(state.filters.copy(query = value))
    }

    override fun onTypeSelected(
        state: ReservantsCatalogUiState,
        value: String?,
    ): ReservantsCatalogUiState {
        return state.withFilters(state.filters.copy(selectedType = value))
    }

    override fun onLinkedEditorOnlyChanged(
        state: ReservantsCatalogUiState,
        value: Boolean,
    ): ReservantsCatalogUiState {
        return state.withFilters(state.filters.copy(linkedEditorOnly = value))
    }

    override fun onSortSelected(
        state: ReservantsCatalogUiState,
        sort: ReservantsSortOption,
    ): ReservantsCatalogUiState {
        return state.withFilters(state.filters.copy(sort = sort))
    }

    override fun onLoadStarted(
        state: ReservantsCatalogUiState,
        refreshing: Boolean,
    ): ReservantsCatalogUiState {
        return state.copy(
            isLoading = !refreshing && state.allItems.isEmpty(),
            isRefreshing = refreshing || state.allItems.isNotEmpty(),
            errorMessage = null,
            pendingDeletion = null,
            pendingDeletionSummary = null,
        )
    }

    override fun onLoadSucceeded(
        state: ReservantsCatalogUiState,
        items: List<ReservantListItem>,
    ): ReservantsCatalogUiState {
        return state.copy(
            allItems = items,
            filteredItems = items.applyFilters(state.filters),
            isLoading = false,
            isRefreshing = false,
        )
    }

    override fun onLoadFailed(state: ReservantsCatalogUiState, message: String): ReservantsCatalogUiState {
        return state.copy(
            isLoading = false,
            isRefreshing = false,
            errorMessage = message,
        )
    }

    override fun onRequestDelete(
        state: ReservantsCatalogUiState,
        reservant: ReservantListItem,
    ): ReservantsCatalogUiState {
        return state.copy(
            pendingDeletion = reservant,
            pendingDeletionSummary = null,
            infoMessage = null,
            errorMessage = null,
        )
    }

    override fun onDeleteSummaryLoaded(
        state: ReservantsCatalogUiState,
        summary: ReservantDeleteSummary,
    ): ReservantsCatalogUiState {
        return state.copy(pendingDeletionSummary = summary.toDialogModel())
    }

    override fun onDeleteSummaryFailed(
        state: ReservantsCatalogUiState,
        message: String,
    ): ReservantsCatalogUiState {
        return state.copy(errorMessage = message)
    }

    override fun onDismissDeleteDialog(state: ReservantsCatalogUiState): ReservantsCatalogUiState {
        return state.copy(
            pendingDeletion = null,
            pendingDeletionSummary = null,
        )
    }

    override fun onDeleteStarted(
        state: ReservantsCatalogUiState,
        reservantId: Int,
    ): ReservantsCatalogUiState {
        return state.copy(
            deletingReservantId = reservantId,
            errorMessage = null,
            infoMessage = null,
        )
    }

    override fun onDeleteSucceeded(
        state: ReservantsCatalogUiState,
        reservant: ReservantListItem,
        message: String,
    ): ReservantsCatalogUiState {
        val remainingItems = state.allItems.filterNot { it.id == reservant.id }
        return state.copy(
            allItems = remainingItems,
            filteredItems = remainingItems.applyFilters(state.filters),
            deletingReservantId = null,
            pendingDeletion = null,
            pendingDeletionSummary = null,
            infoMessage = message.ifBlank { "Réservant supprimé." },
        )
    }

    override fun onDeleteFailed(state: ReservantsCatalogUiState, message: String): ReservantsCatalogUiState {
        return state.copy(
            deletingReservantId = null,
            errorMessage = message,
        )
    }

    override fun onDismissInfoMessage(state: ReservantsCatalogUiState): ReservantsCatalogUiState {
        return state.copy(infoMessage = null)
    }

    override fun onDismissErrorMessage(state: ReservantsCatalogUiState): ReservantsCatalogUiState {
        return state.copy(errorMessage = null)
    }

    private fun ReservantsCatalogUiState.withFilters(
        filters: ReservantsCatalogFilterState,
    ): ReservantsCatalogUiState {
        return copy(
            filters = filters,
            filteredItems = allItems.applyFilters(filters),
            errorMessage = null,
            infoMessage = null,
        )
    }

    private fun List<ReservantListItem>.applyFilters(
        filters: ReservantsCatalogFilterState,
    ): List<ReservantListItem> {
        val query = filters.query.trim().lowercase()
        return asSequence()
            .filter { reservant ->
                filters.selectedType == null || reservant.type == filters.selectedType
            }
            .filter { reservant ->
                !filters.linkedEditorOnly || reservant.editorId != null
            }
            .filter { reservant ->
                query.isBlank() ||
                    reservant.name.lowercase().contains(query) ||
                    reservant.email.lowercase().contains(query) ||
                    reservant.phoneNumber.orEmpty().lowercase().contains(query)
            }
            .sortedWith(filters.sort.asComparator())
            .toList()
    }

    private fun ReservantsSortOption.asComparator(): Comparator<ReservantListItem> {
        return when (this) {
            ReservantsSortOption.NameAsc -> compareBy<ReservantListItem> { it.name.lowercase() }
            ReservantsSortOption.NameDesc -> compareByDescending<ReservantListItem> { it.name.lowercase() }
        }
    }
}
