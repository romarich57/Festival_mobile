/**
 * Rôle : Porte l'état et la logique du module les jeux pour l'écran Compose associé.
 */

package com.projetmobile.mobile.ui.screens.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projetmobile.mobile.data.entity.games.canManageGames
import com.projetmobile.mobile.data.entity.games.GameListItem
import com.projetmobile.mobile.data.entity.games.GameSort
import com.projetmobile.mobile.data.repository.games.GamesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Rôle : Gère le cycle de vie, la pagination, le tri et le filtrage des jeux dans le catalogue.
 *
 * Précondition : Le référentiel (GamesRepository) et les utilitaires liés (LookupsLoader, StateReducer) sont injectés.
 *
 * Postcondition : Fournit l'état paginé des jeux et expose des méthodes pour mettre à jour les filtres, changer le tri ou supprimer un jeu.
 */
internal class GamesCatalogViewModel(
    private val gamesRepository: GamesRepository,
    private val stateReducer: GamesCatalogStateReducer,
    private val lookupsLoader: GamesCatalogLookupsLoader,
    currentUserRole: String?,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        GamesCatalogUiState(
            canManageGames = canManageGames(currentUserRole),
        ),
    )
    val uiState: StateFlow<GamesCatalogUiState> = _uiState.asStateFlow()

    private var titleSearchJob: Job? = null
    private var requestVersion: Long = 0
    private var hasPendingFilterRefresh: Boolean = false

    init {
        startObservingRoom()
        loadLookups()
        refreshGames()
    }

    /**
     * Rôle : Observe Room en continu pour maintenir le catalogue synchronisé avec le filtre de titre courant.
     *
     * Précondition : Le ViewModel doit être initialisé et `gamesRepository` doit exposer un flux d'observation valide.
     *
     * Postcondition : L'état UI reçoit les éléments Room correspondants et l'écran reste à jour sans rechargement manuel.
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun startObservingRoom() {
        viewModelScope.launch {
            _uiState
                .map { it.filters.title }
                .distinctUntilChanged()
                .flatMapLatest { title -> gamesRepository.observeGames(titleSearch = title) }
                .collect { roomItems ->
                    _uiState.update { state ->
                        state.copy(
                            items = roomItems,
                            // Affiche les données Room immédiatement si le cache est peuplé
                            isLoading = if (roomItems.isNotEmpty()) false else state.isLoading,
                        )
                    }
                }
        }
    }

    /**
     * Rôle : Gère la modification du champ title.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onTitleChanged(value: String) {
        _uiState.update { state -> stateReducer.onTitleChanged(state, value) }
        hasPendingFilterRefresh = true
        titleSearchJob?.cancel()
        titleSearchJob = viewModelScope.launch {
            delay(350)
            refreshGames()
        }
    }

    /**
     * Rôle : Gère la sélection de type.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onTypeSelected(value: String?) {
        _uiState.update { state -> stateReducer.onTypeSelected(state, value) }
        hasPendingFilterRefresh = true
        refreshGames()
    }

    /**
     * Rôle : Gère la sélection de éditeur.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onEditorSelected(editorId: Int?) {
        _uiState.update { state -> stateReducer.onEditorSelected(state, editorId) }
        hasPendingFilterRefresh = true
        refreshGames()
    }

    /**
     * Rôle : Gère la modification du champ min age.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onMinAgeChanged(value: String) {
        val previousState = _uiState.value
        _uiState.update { state -> stateReducer.onMinAgeChanged(state, value) }
        if (_uiState.value != previousState) {
            hasPendingFilterRefresh = true
            refreshGames()
        }
    }

    /**
     * Rôle : Gère la sélection de tri.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onSortSelected(sort: GameSort) {
        _uiState.update { state -> stateReducer.onSortSelected(state, sort) }
        hasPendingFilterRefresh = true
        refreshGames()
    }

    /**
     * Rôle : Inverse visible column.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun toggleVisibleColumn(column: GameVisibleColumn) {
        _uiState.update { state -> stateReducer.onToggleVisibleColumn(state, column) }
    }

    /**
     * Rôle : Déclenche la demande de suppression.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun requestDelete(game: GameListItem) {
        _uiState.update { state -> stateReducer.onRequestDelete(state, game) }
    }

    /**
     * Rôle : Ferme suppression dialogue.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun dismissDeleteDialog() {
        _uiState.update { state -> stateReducer.onDismissDeleteDialog(state) }
    }

    /**
     * Rôle : Confirme suppression.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun confirmDelete() {
        val game = _uiState.value.pendingDeletion ?: return
        if (!_uiState.value.canManageGames) {
            dismissDeleteDialog()
            return
        }

        viewModelScope.launch {
            _uiState.update { state -> stateReducer.onDeleteStarted(state, game.id) }

            gamesRepository.deleteGame(game.id)
                .onSuccess { message ->
                    var shouldRefresh = false
                    _uiState.update { state ->
                        stateReducer.onDeleteSucceeded(state, game, message).also { nextState ->
                            shouldRefresh = nextState.hasNext
                        }
                    }
                    if (shouldRefresh) {
                        refreshGames(infoMessage = _uiState.value.infoMessage)
                    }
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        stateReducer.onDeleteFailed(
                            state,
                            mapGameDeleteError(error),
                        )
                    }
                }
        }
    }

    /**
     * Rôle : Rafraîchit jeux.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun refreshGames(infoMessage: String? = null) {
        viewModelScope.launch {
            hasPendingFilterRefresh = false
            val filtersSnapshot = _uiState.value.filters.toFilters()
            val pageSize = _uiState.value.pageSize
            val version = nextRequestVersion()
            _uiState.update { state -> stateReducer.onRefreshStarted(state, infoMessage) }

            gamesRepository.refreshGames(
                filters = filtersSnapshot,
                page = 1,
                limit = pageSize,
            ).onSuccess { page ->
                if (!isLatestRequest(version, filtersSnapshot)) {
                    return@onSuccess
                }
                _uiState.update { state -> stateReducer.onRefreshSucceeded(state, page) }
            }.onFailure { error ->
                if (!isLatestRequest(version, filtersSnapshot)) {
                    return@onFailure
                }
                _uiState.update { state ->
                    stateReducer.onRefreshFailed(
                        state,
                        mapGamesCatalogLoadError(error),
                    )
                }
            }
        }
    }

    /**
     * Rôle : Charge next page.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun loadNextPage() {
        val currentState = _uiState.value
        if (currentState.isLoading ||
            currentState.isRefreshing ||
            currentState.isLoadingNextPage ||
            hasPendingFilterRefresh ||
            !currentState.hasNext
        ) {
            return
        }

        viewModelScope.launch {
            val filtersSnapshot = currentState.filters.toFilters()
            val expectedPage = currentState.currentPage + 1
            val version = requestVersion
            _uiState.update { state -> stateReducer.onLoadNextPageStarted(state) }

            gamesRepository.refreshGames(
                filters = filtersSnapshot,
                page = expectedPage,
                limit = currentState.pageSize,
            ).onSuccess { page ->
                if (!isLatestRequest(version, filtersSnapshot) || page.page != expectedPage) {
                    return@onSuccess
                }
                _uiState.update { state -> stateReducer.onLoadNextPageSucceeded(state, page) }
            }.onFailure { error ->
                if (!isLatestRequest(version, filtersSnapshot)) {
                    return@onFailure
                }
                _uiState.update { state ->
                    stateReducer.onLoadNextPageFailed(
                        state,
                        mapGamesCatalogLoadError(error),
                    )
                }
            }
        }
    }

    /**
     * Rôle : Consomme external rafraîchissement.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun consumeExternalRefresh(infoMessage: String?) {
        refreshGames(infoMessage = infoMessage)
    }

    /**
     * Rôle : Ferme information message.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun dismissInfoMessage() {
        _uiState.update { state -> stateReducer.onDismissInfoMessage(state) }
    }

    /**
     * Rôle : Ferme erreur message.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun dismissErrorMessage() {
        _uiState.update { state -> stateReducer.onDismissErrorMessage(state) }
    }

    /**
     * Rôle : Charge lookups.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    private fun loadLookups() {
        viewModelScope.launch {
            val lookups = lookupsLoader.load()
            _uiState.update { state -> stateReducer.onLookupsLoaded(state, lookups) }
        }
    }

    /**
     * Rôle : Exécute l'action next demande version du module les jeux.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    private fun nextRequestVersion(): Long {
        requestVersion += 1
        return requestVersion
    }

    /**
     * Rôle : Exécute l'action is latest demande du module les jeux.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    private fun isLatestRequest(
        version: Long,
        filters: com.projetmobile.mobile.data.entity.games.GameFilters,
    ): Boolean {
        return version == requestVersion && filters == _uiState.value.filters.toFilters()
    }
}
