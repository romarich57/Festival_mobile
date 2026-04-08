/**
 * Rôle : Classe définissant les méthodes callbacks des actions faites sur le catalogue.
 *
 * Précondition : Utilisé par la vue parent pour déléguer les actions vers le ViewModel.
 *
 * Postcondition : Gère les clics, favoris, tris, etc.
 */
package com.projetmobile.mobile.ui.screens.games

import com.projetmobile.mobile.data.entity.games.GameListItem
import com.projetmobile.mobile.data.entity.games.GameSort

/**
 * Rôle : Décrit le composant jeux catalogue actions du module les jeux catalogue.
 */
internal data class GamesCatalogActions(
    val onTitleChanged: (String) -> Unit,
    val onTypeSelected: (String?) -> Unit,
    val onEditorSelected: (Int?) -> Unit,
    val onMinAgeChanged: (String) -> Unit,
    val onSortSelected: (GameSort) -> Unit,
    val onToggleVisibleColumn: (GameVisibleColumn) -> Unit,
    val onRefresh: () -> Unit,
    val onLoadNextPage: () -> Unit,
    val onRequestDelete: (GameListItem) -> Unit,
    val onDismissDeleteDialog: () -> Unit,
    val onConfirmDelete: () -> Unit,
    val onDismissInfoMessage: () -> Unit,
    val onDismissErrorMessage: () -> Unit,
    val onCreateGame: () -> Unit,
    val onOpenGameDetails: (Int) -> Unit,
    val onEditGame: (Int) -> Unit,
)
