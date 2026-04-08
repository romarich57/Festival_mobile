package com.projetmobile.mobile.ui.screens.reservants

import com.projetmobile.mobile.data.entity.reservants.ReservantListItem

internal data class ReservantsCatalogFilterState(
    val query: String = "",
    val selectedType: String? = null,
    val linkedEditorOnly: Boolean = false,
    val sort: ReservantsSortOption = ReservantsSortOption.NameAsc,
)

/**
 * Rôle : Représente la liste de l'annuaire des réservants avec filtres, tri et mode d'affichage.
 *
 * Précondition : Met à jour la liste des éléments calculée dans les ViewModel en fonction des [filters].
 *
 * Postcondition : Informe la grille ou la liste d'UI sans recalcul interne.
 */
internal data class ReservantsCatalogUiState(
    val filters: ReservantsCatalogFilterState = ReservantsCatalogFilterState(),
    val allItems: List<ReservantListItem> = emptyList(),
    val filteredItems: List<ReservantListItem> = emptyList(),
    val canManageReservants: Boolean = false,
    val canDeleteReservants: Boolean = false,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val deletingReservantId: Int? = null,
    val pendingDeletion: ReservantListItem? = null,
    val pendingDeletionSummary: ReservantDeleteSummaryDialogModel? = null,
    val infoMessage: String? = null,
    val errorMessage: String? = null,
) {
    val totalCount: Int
        get() = allItems.size
}
