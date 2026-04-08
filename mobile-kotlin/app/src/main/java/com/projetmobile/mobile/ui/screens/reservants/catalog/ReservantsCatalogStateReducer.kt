/**
 * Rôle : Définit les règles de transformation d'état du catalogue des réservants.
 * Ce fichier sépare le contrat du reducer et son implémentation par défaut afin de maintenir un catalogue prévisible et testable.
 * Précondition : Le state d'entrée doit représenter l'instant courant du catalogue avant une interaction utilisateur.
 * Postcondition : Chaque méthode retourne un nouvel état immuable avec les champs dérivés mis à jour.
 */
package com.projetmobile.mobile.ui.screens.reservants

import com.projetmobile.mobile.data.entity.reservants.ReservantDeleteSummary
import com.projetmobile.mobile.data.entity.reservants.ReservantListItem

/**
 * Rôle : Décrit les transformations d'état possibles pour le catalogue des réservants.
 * Précondition : Le state fourni aux méthodes doit être cohérent avec le ViewModel appelant.
 * Postcondition : Chaque fonction retourne un nouvel état dérivé, sans mutation partagée.
 */
internal interface ReservantsCatalogStateReducer {
    /**
     * Rôle : Met à jour la recherche textuelle du catalogue.
     * Précondition : `value` doit représenter la nouvelle saisie de recherche.
     * Postcondition : L'état retourné applique le nouveau filtre de requête et recalcule la liste filtrée.
     */
    fun onQueryChanged(state: ReservantsCatalogUiState, value: String): ReservantsCatalogUiState

    /**
     * Rôle : Met à jour le type de réservant sélectionné dans les filtres.
     * Précondition : `value` doit contenir un type valide ou `null` pour tous les types.
     * Postcondition : L'état retourné reflète le nouveau type filtré et recalcule la liste visible.
     */
    fun onTypeSelected(
        state: ReservantsCatalogUiState,
        value: String?,
    ): ReservantsCatalogUiState

    /**
     * Rôle : Active ou désactive le filtre limitant le catalogue aux réservants ayant un éditeur lié.
     * Précondition : `value` doit représenter l'état souhaité du toggle.
     * Postcondition : L'état retourné applique ce filtre et met à jour la liste filtrée.
     */
    fun onLinkedEditorOnlyChanged(
        state: ReservantsCatalogUiState,
        value: Boolean,
    ): ReservantsCatalogUiState

    /**
     * Rôle : Met à jour l'option de tri du catalogue.
     * Précondition : `sort` doit faire partie des options disponibles de `ReservantsSortOption`.
     * Postcondition : L'état retourné reflète le nouveau tri appliqué aux réservants visibles.
     */
    fun onSortSelected(
        state: ReservantsCatalogUiState,
        sort: ReservantsSortOption,
    ): ReservantsCatalogUiState

    /**
     * Rôle : Prépare l'état du catalogue au démarrage d'un chargement.
     * Précondition : `refreshing` indique si le chargement est silencieux ou initié par un rafraîchissement.
     * Postcondition : L'état retourne l'indicateur de chargement approprié et réinitialise les messages transitoires.
     */
    fun onLoadStarted(state: ReservantsCatalogUiState, refreshing: Boolean): ReservantsCatalogUiState

    /**
     * Rôle : Applique le résultat d'un chargement réussi au catalogue.
     * Précondition : `items` doit contenir la nouvelle liste brute reçue du repository.
     * Postcondition : L'état retourne la nouvelle collection et réinitialise les indicateurs de chargement.
     */
    fun onLoadSucceeded(
        state: ReservantsCatalogUiState,
        items: List<ReservantListItem>,
    ): ReservantsCatalogUiState

    /**
     * Rôle : Convertit un échec de chargement en état d'erreur de catalogue.
     * Précondition : `message` doit être déjà formaté pour l'affichage utilisateur.
     * Postcondition : L'état retourne une erreur visible ou un message d'information offline selon le contexte.
     */
    fun onLoadFailed(state: ReservantsCatalogUiState, message: String): ReservantsCatalogUiState

    /**
     * Rôle : Ouvre le flux de suppression pour un réservant donné.
     * Précondition : `reservant` doit représenter l'élément ciblé par l'utilisateur.
     * Postcondition : L'état expose le réservant à supprimer et réinitialise les messages transitoires.
     */
    fun onRequestDelete(
        state: ReservantsCatalogUiState,
        reservant: ReservantListItem,
    ): ReservantsCatalogUiState

    /**
     * Rôle : Enregistre le résumé de suppression associé au réservant ciblé.
     * Précondition : `summary` doit provenir du chargement des dépendances de suppression.
     * Postcondition : L'état expose les compteurs et les aperçus à afficher dans la boîte de dialogue.
     */
    fun onDeleteSummaryLoaded(
        state: ReservantsCatalogUiState,
        summary: ReservantDeleteSummary,
    ): ReservantsCatalogUiState

    /**
     * Rôle : Transforme un échec de chargement du résumé de suppression en erreur affichable.
     * Précondition : `message` doit être déjà formaté pour l'utilisateur.
     * Postcondition : L'état conserve le dialogue ouvert mais expose un message d'erreur.
     */
    fun onDeleteSummaryFailed(
        state: ReservantsCatalogUiState,
        message: String,
    ): ReservantsCatalogUiState

    /**
     * Rôle : Ferme la boîte de dialogue de suppression.
     * Précondition : Le dialogue doit être actuellement ouvert.
     * Postcondition : L'état retire le réservant ciblé et le résumé associé.
     */
    fun onDismissDeleteDialog(state: ReservantsCatalogUiState): ReservantsCatalogUiState

    /**
     * Rôle : Marque la suppression comme en cours pour un réservant précis.
     * Précondition : `reservantId` doit identifier l'élément réellement supprimé.
     * Postcondition : L'état expose l'ID en cours de suppression et nettoie les messages temporaires.
     */
    fun onDeleteStarted(state: ReservantsCatalogUiState, reservantId: Int): ReservantsCatalogUiState

    /**
     * Rôle : Applique le résultat d'une suppression réussie au catalogue.
     * Précondition : `reservant` doit correspondre à l'élément supprimé et `message` au retour utilisateur attendu.
     * Postcondition : L'élément est retiré de la liste et un message de succès est affiché.
     */
    fun onDeleteSucceeded(
        state: ReservantsCatalogUiState,
        reservant: ReservantListItem,
        message: String,
    ): ReservantsCatalogUiState

    /**
     * Rôle : Transforme un échec de suppression en état d'erreur.
     * Précondition : `message` doit être prêt à être affiché à l'utilisateur.
     * Postcondition : L'état arrête la suppression en cours et expose le message d'erreur.
     */
    fun onDeleteFailed(state: ReservantsCatalogUiState, message: String): ReservantsCatalogUiState

    /**
     * Rôle : Ferme le message d'information actuellement affiché.
     * Précondition : Un message d'information doit être visible ou en attente d'affichage.
     * Postcondition : L'état ne conserve plus de message d'information.
     */
    fun onDismissInfoMessage(state: ReservantsCatalogUiState): ReservantsCatalogUiState

    /**
     * Rôle : Ferme le message d'erreur actuellement affiché.
     * Précondition : Un message d'erreur doit être visible ou en attente d'affichage.
     * Postcondition : L'état ne conserve plus de message d'erreur.
     */
    fun onDismissErrorMessage(state: ReservantsCatalogUiState): ReservantsCatalogUiState
}

/**
 * Rôle : Fournit l'implémentation par défaut des transitions d'état du catalogue des réservants.
 * Précondition : Les états passés à ses méthodes doivent être immuables et cohérents avec l'UI actuelle.
 * Postcondition : Chaque appel retourne un nouvel état dérivé sans mutation de l'instance d'origine.
 */
internal class DefaultReservantsCatalogStateReducer : ReservantsCatalogStateReducer {
    /**
     * Rôle : Met à jour la recherche textuelle du catalogue.
     * Précondition : `value` doit représenter la nouvelle saisie de recherche.
     * Postcondition : L'état retourné applique le nouveau filtre de requête et recalcule la liste filtrée.
     */
    override fun onQueryChanged(
        state: ReservantsCatalogUiState,
        value: String,
    ): ReservantsCatalogUiState {
        return state.withFilters(state.filters.copy(query = value))
    }

    /**
     * Rôle : Met à jour le type de réservant sélectionné dans les filtres.
     * Précondition : `value` doit contenir un type valide ou `null` pour tous les types.
     * Postcondition : L'état retourné reflète le nouveau type filtré et recalcule la liste visible.
     */
    override fun onTypeSelected(
        state: ReservantsCatalogUiState,
        value: String?,
    ): ReservantsCatalogUiState {
        return state.withFilters(state.filters.copy(selectedType = value))
    }

    /**
     * Rôle : Active ou désactive le filtre limitant le catalogue aux réservants ayant un éditeur lié.
     * Précondition : `value` doit représenter l'état souhaité du toggle.
     * Postcondition : L'état retourné applique ce filtre et met à jour la liste filtrée.
     */
    override fun onLinkedEditorOnlyChanged(
        state: ReservantsCatalogUiState,
        value: Boolean,
    ): ReservantsCatalogUiState {
        return state.withFilters(state.filters.copy(linkedEditorOnly = value))
    }

    /**
     * Rôle : Met à jour l'option de tri du catalogue.
     * Précondition : `sort` doit faire partie des options disponibles de `ReservantsSortOption`.
     * Postcondition : L'état retourné reflète le nouveau tri appliqué aux réservants visibles.
     */
    override fun onSortSelected(
        state: ReservantsCatalogUiState,
        sort: ReservantsSortOption,
    ): ReservantsCatalogUiState {
        return state.withFilters(state.filters.copy(sort = sort))
    }

    /**
     * Rôle : Prépare l'état du catalogue au démarrage d'un chargement.
     * Précondition : `refreshing` indique si le chargement est silencieux ou initié par un rafraîchissement.
     * Postcondition : L'état retourne l'indicateur de chargement approprié et réinitialise les messages transitoires.
     */
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

    /**
     * Rôle : Applique le résultat d'un chargement réussi au catalogue.
     * Précondition : `items` doit contenir la nouvelle liste brute reçue du repository.
     * Postcondition : L'état retourne la nouvelle collection et réinitialise les indicateurs de chargement.
     */
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

    /**
     * Rôle : Convertit un échec de chargement en état d'erreur de catalogue.
     * Précondition : `message` doit être déjà formaté pour l'affichage utilisateur.
     * Postcondition : L'état retourne une erreur visible ou un message d'information offline selon le contexte.
     */
    override fun onLoadFailed(state: ReservantsCatalogUiState, message: String): ReservantsCatalogUiState {
        // Quand la liste est déjà présente, on privilégie un message discret hors-ligne plutôt qu'un écran d'échec total.
        val shouldShowOfflineInfo = state.allItems.isNotEmpty() && (
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
     * Rôle : Ouvre le flux de suppression pour un réservant donné.
     * Précondition : `reservant` doit représenter l'élément ciblé par l'utilisateur.
     * Postcondition : L'état expose le réservant à supprimer et réinitialise les messages transitoires.
     */
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

    /**
     * Rôle : Enregistre le résumé de suppression associé au réservant ciblé.
     * Précondition : `summary` doit provenir du chargement des dépendances de suppression.
     * Postcondition : L'état expose les compteurs et les aperçus à afficher dans la boîte de dialogue.
     */
    override fun onDeleteSummaryLoaded(
        state: ReservantsCatalogUiState,
        summary: ReservantDeleteSummary,
    ): ReservantsCatalogUiState {
        return state.copy(pendingDeletionSummary = summary.toDialogModel())
    }

    /**
     * Rôle : Transforme un échec de chargement du résumé de suppression en erreur affichable.
     * Précondition : `message` doit être déjà formaté pour l'utilisateur.
     * Postcondition : L'état conserve le dialogue ouvert mais expose un message d'erreur.
     */
    override fun onDeleteSummaryFailed(
        state: ReservantsCatalogUiState,
        message: String,
    ): ReservantsCatalogUiState {
        return state.copy(errorMessage = message)
    }

    /**
     * Rôle : Ferme la boîte de dialogue de suppression.
     * Précondition : Le dialogue doit être actuellement ouvert.
     * Postcondition : L'état retire le réservant ciblé et le résumé associé.
     */
    override fun onDismissDeleteDialog(state: ReservantsCatalogUiState): ReservantsCatalogUiState {
        return state.copy(
            pendingDeletion = null,
            pendingDeletionSummary = null,
        )
    }

    /**
     * Rôle : Marque la suppression comme en cours pour un réservant précis.
     * Précondition : `reservantId` doit identifier l'élément réellement supprimé.
     * Postcondition : L'état expose l'ID en cours de suppression et nettoie les messages temporaires.
     */
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

    /**
     * Rôle : Applique le résultat d'une suppression réussie au catalogue.
     * Précondition : `reservant` doit correspondre à l'élément supprimé et `message` au retour utilisateur attendu.
     * Postcondition : L'élément est retiré de la liste et un message de succès est affiché.
     */
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

    /**
     * Rôle : Transforme un échec de suppression en état d'erreur.
     * Précondition : `message` doit être prêt à être affiché à l'utilisateur.
     * Postcondition : L'état arrête la suppression en cours et expose le message d'erreur.
     */
    override fun onDeleteFailed(state: ReservantsCatalogUiState, message: String): ReservantsCatalogUiState {
        return state.copy(
            deletingReservantId = null,
            errorMessage = message,
        )
    }

    /**
     * Rôle : Ferme le message d'information actuellement affiché.
     * Précondition : Un message d'information doit être visible ou en attente d'affichage.
     * Postcondition : L'état ne conserve plus de message d'information.
     */
    override fun onDismissInfoMessage(state: ReservantsCatalogUiState): ReservantsCatalogUiState {
        return state.copy(infoMessage = null)
    }

    /**
     * Rôle : Ferme le message d'erreur actuellement affiché.
     * Précondition : Un message d'erreur doit être visible ou en attente d'affichage.
     * Postcondition : L'état ne conserve plus de message d'erreur.
     */
    override fun onDismissErrorMessage(state: ReservantsCatalogUiState): ReservantsCatalogUiState {
        return state.copy(errorMessage = null)
    }

    /**
     * Rôle : Reconstruit l'état du catalogue après modification des filtres.
     * Précondition : `filters` doit contenir le nouvel état de filtrage.
     * Postcondition : L'état retourne une liste filtrée recalculée et efface les messages transitoires.
     */
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

    /**
     * Rôle : Applique la recherche, le filtre de type, le filtre éditeur et le tri à la liste brute de réservants.
     * Précondition : `filters` doit être cohérent avec les options affichées dans l'UI.
     * Postcondition : Retourne une liste triée et filtrée prête à être affichée dans le catalogue.
     */
    private fun List<ReservantListItem>.applyFilters(
        filters: ReservantsCatalogFilterState,
    ): List<ReservantListItem> {
        val query = filters.query.trim().lowercase()
        // Les filtres sont appliqués avant le tri pour garantir un résultat stable et lisible.
        return asSequence()
            .filter { reservant ->
                filters.selectedType == null || reservant.type == filters.selectedType
            }
            .filter { reservant ->
                !filters.linkedEditorOnly || reservant.editorId != null
            }
            .filter { reservant ->
                // La recherche tolère les saisies partielles sur le nom, l'email et le téléphone.
                query.isBlank() ||
                    reservant.name.lowercase().contains(query) ||
                    reservant.email.lowercase().contains(query) ||
                    reservant.phoneNumber.orEmpty().lowercase().contains(query)
            }
            .sortedWith(filters.sort.asComparator())
            .toList()
    }

    /**
     * Rôle : Convertit l'option de tri du catalogue en comparateur de liste.
     * Précondition : La valeur doit être une option de `ReservantsSortOption`.
     * Postcondition : Retourne un comparateur adapté au tri croissant ou décroissant par nom.
     */
    private fun ReservantsSortOption.asComparator(): Comparator<ReservantListItem> {
        return when (this) {
            ReservantsSortOption.NameAsc -> compareBy<ReservantListItem> { it.name.lowercase() }
            ReservantsSortOption.NameDesc -> compareByDescending<ReservantListItem> { it.name.lowercase() }
        }
    }
}
