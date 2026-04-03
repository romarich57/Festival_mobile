package com.projetmobile.mobile.ui.screens.reservants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.projetmobile.mobile.data.entity.reservants.ReservantListItem
import com.projetmobile.mobile.data.entity.reservants.canDeleteReservants
import com.projetmobile.mobile.data.entity.reservants.canManageReservants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ReservantsCatalogViewModel(
    private val loadReservants: ReservantsLoader,
    private val observeReservants: Flow<List<ReservantListItem>>,
    private val loadDeleteSummary: ReservantDeleteSummaryLoader,
    private val deleteReservant: ReservantDelete,
    private val stateReducer: ReservantsCatalogStateReducer,
    currentUserRole: String?,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ReservantsCatalogUiState(
            canManageReservants = canManageReservants(currentUserRole),
            canDeleteReservants = canDeleteReservants(currentUserRole),
        ),
    )
    val uiState: StateFlow<ReservantsCatalogUiState> = _uiState.asStateFlow()

    init {
        // Observation Room (SSOT) — données dispo immédiatement si cache peuplé
        viewModelScope.launch {
            observeReservants.collect { items ->
                _uiState.update { state -> stateReducer.onLoadSucceeded(state, items) }
            }
        }
        refreshReservants()
    }

    fun onQueryChanged(value: String) {
        _uiState.update { state -> stateReducer.onQueryChanged(state, value) }
    }

    fun onTypeSelected(value: String?) {
        _uiState.update { state -> stateReducer.onTypeSelected(state, value) }
    }

    fun onLinkedEditorOnlyChanged(value: Boolean) {
        _uiState.update { state -> stateReducer.onLinkedEditorOnlyChanged(state, value) }
    }

    fun onSortSelected(sort: ReservantsSortOption) {
        _uiState.update { state -> stateReducer.onSortSelected(state, sort) }
    }

    fun refreshReservants() {
        viewModelScope.launch {
            _uiState.update { state ->
                stateReducer.onLoadStarted(
                    state = state,
                    refreshing = state.allItems.isNotEmpty(),
                )
            }

            loadReservants()
                .onSuccess { items ->
                    _uiState.update { state -> stateReducer.onLoadSucceeded(state, items) }
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        stateReducer.onLoadFailed(
                            state,
                            mapReservantsCatalogLoadError(error),
                        )
                    }
                }
        }
    }

    fun requestDelete(reservant: ReservantListItem) {
        if (!_uiState.value.canDeleteReservants) {
            return
        }
        viewModelScope.launch {
            _uiState.update { state -> stateReducer.onRequestDelete(state, reservant) }
            loadDeleteSummary(reservant.id)
                .onSuccess { summary ->
                    _uiState.update { state -> stateReducer.onDeleteSummaryLoaded(state, summary) }
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        stateReducer.onDeleteSummaryFailed(
                            state,
                            mapReservantDeleteError(error),
                        )
                    }
                }
        }
    }

    fun dismissDeleteDialog() {
        _uiState.update { state -> stateReducer.onDismissDeleteDialog(state) }
    }

    fun confirmDelete() {
        val reservant = _uiState.value.pendingDeletion ?: return
        if (!_uiState.value.canDeleteReservants) {
            dismissDeleteDialog()
            return
        }

        viewModelScope.launch {
            _uiState.update { state -> stateReducer.onDeleteStarted(state, reservant.id) }
            deleteReservant(reservant.id)
                .onSuccess { message ->
                    _uiState.update { state ->
                        stateReducer.onDeleteSucceeded(state, reservant, message)
                    }
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        stateReducer.onDeleteFailed(
                            state,
                            mapReservantDeleteError(error),
                        )
                    }
                }
        }
    }

    fun consumeExternalRefresh(infoMessage: String?) {
        refreshReservants()
        if (infoMessage != null) {
            _uiState.update { state -> state.copy(infoMessage = infoMessage) }
        }
    }

    fun dismissInfoMessage() {
        _uiState.update { state -> stateReducer.onDismissInfoMessage(state) }
    }

    fun dismissErrorMessage() {
        _uiState.update { state -> stateReducer.onDismissErrorMessage(state) }
    }
}

internal fun reservantsCatalogViewModelFactory(
    loadReservants: ReservantsLoader,
    observeReservants: Flow<List<ReservantListItem>>,
    loadDeleteSummary: ReservantDeleteSummaryLoader,
    deleteReservant: ReservantDelete,
    currentUserRole: String?,
): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReservantsCatalogViewModel(
                loadReservants = loadReservants,
                observeReservants = observeReservants,
                loadDeleteSummary = loadDeleteSummary,
                deleteReservant = deleteReservant,
                stateReducer = DefaultReservantsCatalogStateReducer(),
                currentUserRole = currentUserRole,
            ) as T
        }
    }
}
