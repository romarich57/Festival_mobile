/**
 * Rôle : Composant de logique de réduction d'état afin de reconstruire un nouvel UiState.
 *
 * Précondition : Utilisé seulement en réception d'un signal Action/Data (Event).
 *
 * Postcondition : Offre un composant immuable à affiché mis à jour.
 */
package com.projetmobile.mobile.ui.screens.games

import com.projetmobile.mobile.data.entity.games.GameListItem
import com.projetmobile.mobile.data.entity.games.GameSort
import com.projetmobile.mobile.data.entity.games.PagedResult

/**
 * Rôle : Définit le contrat du module les jeux catalogue.
 */
internal interface GamesCatalogStateReducer {
    /**
     * Rôle : Gère la modification du champ title.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    fun onTitleChanged(state: GamesCatalogUiState, value: String): GamesCatalogUiState
    /**
     * Rôle : Gère la sélection de type.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    fun onTypeSelected(state: GamesCatalogUiState, value: String?): GamesCatalogUiState
    /**
     * Rôle : Gère la sélection de éditeur.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    fun onEditorSelected(state: GamesCatalogUiState, editorId: Int?): GamesCatalogUiState
    /**
     * Rôle : Gère la modification du champ min age.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    fun onMinAgeChanged(state: GamesCatalogUiState, value: String): GamesCatalogUiState
    /**
     * Rôle : Gère la sélection de tri.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    fun onSortSelected(state: GamesCatalogUiState, sort: GameSort): GamesCatalogUiState
    /**
     * Rôle : Exécute l'action on bascule visible column du module les jeux catalogue.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    fun onToggleVisibleColumn(state: GamesCatalogUiState, column: GameVisibleColumn): GamesCatalogUiState
    /**
     * Rôle : Exécute l'action on demande suppression du module les jeux catalogue.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    fun onRequestDelete(state: GamesCatalogUiState, game: GameListItem): GamesCatalogUiState
    /**
     * Rôle : Exécute l'action on fermeture suppression dialogue du module les jeux catalogue.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    fun onDismissDeleteDialog(state: GamesCatalogUiState): GamesCatalogUiState
    /**
     * Rôle : Exécute l'action on fermeture information message du module les jeux catalogue.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    fun onDismissInfoMessage(state: GamesCatalogUiState): GamesCatalogUiState
    /**
     * Rôle : Exécute l'action on fermeture erreur message du module les jeux catalogue.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    fun onDismissErrorMessage(state: GamesCatalogUiState): GamesCatalogUiState
    /**
     * Rôle : Gère le chargement de lookups.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    fun onLookupsLoaded(state: GamesCatalogUiState, result: GamesCatalogLookupsLoadResult): GamesCatalogUiState
    /**
     * Rôle : Exécute l'action on rafraîchissement started du module les jeux catalogue.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    fun onRefreshStarted(state: GamesCatalogUiState, infoMessage: String?): GamesCatalogUiState
    /**
     * Rôle : Exécute l'action on rafraîchissement succeeded du module les jeux catalogue.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    fun onRefreshSucceeded(
        state: GamesCatalogUiState,
        page: PagedResult<GameListItem>,
    ): GamesCatalogUiState
    /**
     * Rôle : Exécute l'action on rafraîchissement failed du module les jeux catalogue.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    fun onRefreshFailed(state: GamesCatalogUiState, message: String): GamesCatalogUiState
    /**
     * Rôle : Exécute l'action on chargement next page started du module les jeux catalogue.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    fun onLoadNextPageStarted(state: GamesCatalogUiState): GamesCatalogUiState
    /**
     * Rôle : Exécute l'action on chargement next page succeeded du module les jeux catalogue.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    fun onLoadNextPageSucceeded(
        state: GamesCatalogUiState,
        page: PagedResult<GameListItem>,
    ): GamesCatalogUiState
    /**
     * Rôle : Exécute l'action on chargement next page failed du module les jeux catalogue.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    fun onLoadNextPageFailed(state: GamesCatalogUiState, message: String): GamesCatalogUiState
    /**
     * Rôle : Exécute l'action on suppression started du module les jeux catalogue.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    fun onDeleteStarted(state: GamesCatalogUiState, gameId: Int): GamesCatalogUiState
    /**
     * Rôle : Exécute l'action on suppression succeeded du module les jeux catalogue.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    fun onDeleteSucceeded(
        state: GamesCatalogUiState,
        game: GameListItem,
        message: String,
    ): GamesCatalogUiState
    /**
     * Rôle : Exécute l'action on suppression failed du module les jeux catalogue.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    fun onDeleteFailed(state: GamesCatalogUiState, message: String): GamesCatalogUiState
}
/**
 * Rôle : Décrit le composant default jeux catalogue état reducer du module les jeux catalogue.
 */
internal class DefaultGamesCatalogStateReducer : GamesCatalogStateReducer {
    /**
     * Rôle : Gère la modification du champ title.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    override fun onTitleChanged(state: GamesCatalogUiState, value: String): GamesCatalogUiState {
        return state.copy(
            filters = state.filters.copy(title = value),
            errorMessage = null,
            infoMessage = null,
        )
    }
    /**
     * Rôle : Gère la sélection de type.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    override fun onTypeSelected(state: GamesCatalogUiState, value: String?): GamesCatalogUiState {
        return state.copy(
            filters = state.filters.copy(selectedType = value),
            errorMessage = null,
            infoMessage = null,
        )
    }
    /**
     * Rôle : Gère la sélection de éditeur.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    override fun onEditorSelected(
        state: GamesCatalogUiState,
        editorId: Int?,
    ): GamesCatalogUiState {
        return state.copy(
            filters = state.filters.copy(selectedEditorId = editorId),
            errorMessage = null,
            infoMessage = null,
        )
    }
    /**
     * Rôle : Gère la modification du champ min age.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    override fun onMinAgeChanged(state: GamesCatalogUiState, value: String): GamesCatalogUiState {
        if (value.isNotEmpty() && value.any { !it.isDigit() }) {
            // On refuse immédiatement les caractères non numériques pour conserver un champ compatible avec la validation métier.
            return state
        }
        return state.copy(
            filters = state.filters.copy(minAgeInput = value),
            errorMessage = null,
            infoMessage = null,
        )
    }
    /**
     * Rôle : Gère la sélection de tri.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    override fun onSortSelected(state: GamesCatalogUiState, sort: GameSort): GamesCatalogUiState {
        return state.copy(
            filters = state.filters.copy(sort = sort),
            errorMessage = null,
            infoMessage = null,
        )
    }
    /**
     * Rôle : Exécute l'action on bascule visible column du module les jeux catalogue.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    override fun onToggleVisibleColumn(
        state: GamesCatalogUiState,
        column: GameVisibleColumn,
    ): GamesCatalogUiState {
        val updatedColumns = if (column in state.visibleColumns) {
            state.visibleColumns - column
        } else {
            // Si la dernière colonne visible est masquée, on restaure l'ensemble des colonnes pour éviter un tableau vide.
            state.visibleColumns + column
        }
        return state.copy(
            visibleColumns = updatedColumns.ifEmpty { GameVisibleColumn.entries.toSet() },
        )
    }
    /**
     * Rôle : Exécute l'action on demande suppression du module les jeux catalogue.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    override fun onRequestDelete(state: GamesCatalogUiState, game: GameListItem): GamesCatalogUiState {
        return state.copy(
            pendingDeletion = game,
            errorMessage = null,
            infoMessage = null,
        )
    }
    /**
     * Rôle : Exécute l'action on fermeture suppression dialogue du module les jeux catalogue.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    override fun onDismissDeleteDialog(state: GamesCatalogUiState): GamesCatalogUiState {
        return state.copy(pendingDeletion = null)
    }
    /**
     * Rôle : Exécute l'action on fermeture information message du module les jeux catalogue.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    override fun onDismissInfoMessage(state: GamesCatalogUiState): GamesCatalogUiState {
        return state.copy(infoMessage = null)
    }
    /**
     * Rôle : Exécute l'action on fermeture erreur message du module les jeux catalogue.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    override fun onDismissErrorMessage(state: GamesCatalogUiState): GamesCatalogUiState {
        return state.copy(errorMessage = null)
    }
    /**
     * Rôle : Gère le chargement de lookups.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    override fun onLookupsLoaded(
        state: GamesCatalogUiState,
        result: GamesCatalogLookupsLoadResult,
    ): GamesCatalogUiState {
        // Un chargement de lookups partiellement dégradé n'écrase pas l'état utilisateur déjà visible.
        val shouldShowLookupInfo = state.items.isNotEmpty() && result.errorMessage != null
        return state.copy(
            availableTypes = result.availableTypes,
            availableEditors = result.availableEditors,
            infoMessage = if (shouldShowLookupInfo) result.errorMessage else state.infoMessage,
            errorMessage = if (shouldShowLookupInfo) {
                state.errorMessage
            } else {
                result.errorMessage ?: state.errorMessage
            },
        )
    }
    /**
     * Rôle : Exécute l'action on rafraîchissement started du module les jeux catalogue.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    override fun onRefreshStarted(
        state: GamesCatalogUiState,
        infoMessage: String?,
    ): GamesCatalogUiState {
        val hadItems = state.items.isNotEmpty()
        return state.copy(
            isLoading = !hadItems,
            isRefreshing = hadItems,
            errorMessage = null,
            infoMessage = infoMessage ?: state.infoMessage,
            pendingDeletion = null,
        )
    }
    /**
     * Rôle : Exécute l'action on rafraîchissement succeeded du module les jeux catalogue.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    override fun onRefreshSucceeded(
        state: GamesCatalogUiState,
        page: PagedResult<GameListItem>,
    ): GamesCatalogUiState {
        return state.copy(
            currentPage = page.page,
            total = page.total,
            hasNext = page.hasNext,
            isLoading = false,
            isRefreshing = false,
        )
    }
    /**
     * Rôle : Exécute l'action on rafraîchissement failed du module les jeux catalogue.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    override fun onRefreshFailed(state: GamesCatalogUiState, message: String): GamesCatalogUiState {
        // En présence de données déjà chargées, une panne réseau peut rester informative plutôt qu'échec bloquant.
        val shouldShowOfflineInfo = state.items.isNotEmpty() && (
            message.startsWith("Mode hors-ligne") ||
                message.startsWith("Serveur inaccessible")
            )
        return state.copy(
            isLoading = false,
            isRefreshing = false,
            infoMessage = if (shouldShowOfflineInfo) message else state.infoMessage,
            errorMessage = if (shouldShowOfflineInfo) null else message,
        )
    }
    /**
     * Rôle : Exécute l'action on chargement next page started du module les jeux catalogue.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    override fun onLoadNextPageStarted(state: GamesCatalogUiState): GamesCatalogUiState {
        return state.copy(
            isLoadingNextPage = true,
            errorMessage = null,
        )
    }
    /**
     * Rôle : Exécute l'action on chargement next page succeeded du module les jeux catalogue.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    override fun onLoadNextPageSucceeded(
        state: GamesCatalogUiState,
        page: PagedResult<GameListItem>,
    ): GamesCatalogUiState {
        return state.copy(
            currentPage = page.page,
            total = page.total,
            hasNext = page.hasNext,
            isLoadingNextPage = false,
        )
    }
    /**
     * Rôle : Exécute l'action on chargement next page failed du module les jeux catalogue.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    override fun onLoadNextPageFailed(
        state: GamesCatalogUiState,
        message: String,
    ): GamesCatalogUiState {
        return state.copy(
            isLoadingNextPage = false,
            errorMessage = message,
        )
    }
    /**
     * Rôle : Exécute l'action on suppression started du module les jeux catalogue.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    override fun onDeleteStarted(state: GamesCatalogUiState, gameId: Int): GamesCatalogUiState {
        return state.copy(
            deletingGameId = gameId,
            errorMessage = null,
            infoMessage = null,
        )
    }
    /**
     * Rôle : Exécute l'action on suppression succeeded du module les jeux catalogue.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    override fun onDeleteSucceeded(
        state: GamesCatalogUiState,
        game: GameListItem,
        message: String,
    ): GamesCatalogUiState {
        return state.copy(
            items = state.items.filterNot { it.id == game.id },
            total = (state.total - 1).coerceAtLeast(0),
            deletingGameId = null,
            pendingDeletion = null,
            infoMessage = message,
        )
    }
    /**
     * Rôle : Exécute l'action on suppression failed du module les jeux catalogue.
     *
     * Précondition : L'état courant et l'événement utilisateur doivent être disponibles.
     *
     * Postcondition : Un nouvel état cohérent est produit.
     */
    override fun onDeleteFailed(state: GamesCatalogUiState, message: String): GamesCatalogUiState {
        return state.copy(
            deletingGameId = null,
            errorMessage = message,
        )
    }
}
