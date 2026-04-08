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
     * Observe Room en continu (SSOT offline-first).
     * Utilise flatMapLatest pour redémarrer l'observation quand le filtre titre change.
     * Items mis à jour dès qu'une écriture locale ou réseau modifie Room.
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

    fun onTitleChanged(value: String) {
        _uiState.update { state -> stateReducer.onTitleChanged(state, value) }
        hasPendingFilterRefresh = true
        titleSearchJob?.cancel()
        titleSearchJob = viewModelScope.launch {
            delay(350)
            refreshGames()
        }
    }

    fun onTypeSelected(value: String?) {
        _uiState.update { state -> stateReducer.onTypeSelected(state, value) }
        hasPendingFilterRefresh = true
        refreshGames()
    }

    fun onEditorSelected(editorId: Int?) {
        _uiState.update { state -> stateReducer.onEditorSelected(state, editorId) }
        hasPendingFilterRefresh = true
        refreshGames()
    }

    fun onMinAgeChanged(value: String) {
        val previousState = _uiState.value
        _uiState.update { state -> stateReducer.onMinAgeChanged(state, value) }
        if (_uiState.value != previousState) {
            hasPendingFilterRefresh = true
            refreshGames()
        }
    }

    fun onSortSelected(sort: GameSort) {
        _uiState.update { state -> stateReducer.onSortSelected(state, sort) }
        hasPendingFilterRefresh = true
        refreshGames()
    }

    fun toggleVisibleColumn(column: GameVisibleColumn) {
        _uiState.update { state -> stateReducer.onToggleVisibleColumn(state, column) }
    }

    fun requestDelete(game: GameListItem) {
        _uiState.update { state -> stateReducer.onRequestDelete(state, game) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { state -> stateReducer.onDismissDeleteDialog(state) }
    }

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

    fun consumeExternalRefresh(infoMessage: String?) {
        refreshGames(infoMessage = infoMessage)
    }

    fun dismissInfoMessage() {
        _uiState.update { state -> stateReducer.onDismissInfoMessage(state) }
    }

    fun dismissErrorMessage() {
        _uiState.update { state -> stateReducer.onDismissErrorMessage(state) }
    }

    private fun loadLookups() {
        viewModelScope.launch {
            val lookups = lookupsLoader.load()
            _uiState.update { state -> stateReducer.onLookupsLoaded(state, lookups) }
        }
    }

    private fun nextRequestVersion(): Long {
        requestVersion += 1
        return requestVersion
    }

    private fun isLatestRequest(
        version: Long,
        filters: com.projetmobile.mobile.data.entity.games.GameFilters,
    ): Boolean {
        return version == requestVersion && filters == _uiState.value.filters.toFilters()
    }
}
