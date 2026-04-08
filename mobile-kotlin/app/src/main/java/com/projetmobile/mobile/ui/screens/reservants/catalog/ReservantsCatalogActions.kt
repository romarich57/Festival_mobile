/**
 * Rôle : Regroupe les callbacks d'interaction du catalogue des réservants.
 * Ce fichier sert de contrat entre l'écran Compose et la logique de présentation.
 * Précondition : Le ViewModel ou la route doit fournir un callback cohérent pour chaque interaction visible.
 * Postcondition : L'écran peut déléguer toutes ses actions à un seul objet de callbacks.
 */
package com.projetmobile.mobile.ui.screens.reservants

import com.projetmobile.mobile.data.entity.reservants.ReservantListItem

/**
 * Rôle : Rassemble toutes les actions déclenchables depuis le catalogue des réservants.
 * Précondition : Chaque callback doit être relié à une action métier ou de navigation valide.
 * Postcondition : Les composants UI manipulent un seul objet au lieu d'une longue liste de paramètres.
 */
internal data class ReservantsCatalogActions(
    val onQueryChanged: (String) -> Unit,
    val onTypeSelected: (String?) -> Unit,
    val onLinkedEditorOnlyChanged: (Boolean) -> Unit,
    val onSortSelected: (ReservantsSortOption) -> Unit,
    val onRefresh: () -> Unit,
    val onDismissDeleteDialog: () -> Unit,
    val onConfirmDelete: () -> Unit,
    val onDismissInfoMessage: () -> Unit,
    val onDismissErrorMessage: () -> Unit,
    val onRequestDelete: (ReservantListItem) -> Unit,
    val onCreateReservant: () -> Unit,
    val onOpenReservantDetails: (Int) -> Unit,
    val onEditReservant: (Int) -> Unit,
)
