package com.projetmobile.mobile.ui.screens.festival

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.projetmobile.mobile.data.repository.festival.FestivalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel de la liste des festivals.
 *
 * Équivalents Angular :
 *  - loadFestivals()          = effect(() => _festivalService.loadAllFestivals())
 *  - selectFestival()         = festivalStore.setCurrentFestival(festival)
 *  - clearSelection()         = festivalStore.setCurrentFestival(null)
 *  - requestDeleteFestival()  = requestDeleteFestival() dans FestivalListComponent
 *  - confirmDelete()          = confirmDeleteFestival() → _festivalService.deleteFestival()
 *  - currentFestivalId        = computed(() => festivalStore.currentFestival()?.id)
 *  - pendingDeleteFestivalId  = signal<number | null>()
 */
class FestivalViewModel(
    private val festivalRepository: FestivalRepository,
) : ViewModel() {

    // ── État liste ────────────────────────────────────────────────────────────
    private val _uiState = MutableStateFlow(FestivalUiState())
    val uiState: StateFlow<FestivalUiState> = _uiState.asStateFlow()

    // ── Festival sélectionné (équivalent FestivalState.currentFestival) ───────
    private val _currentFestivalId = MutableStateFlow<Int?>(null)
    val currentFestivalId: StateFlow<Int?> = _currentFestivalId.asStateFlow()

    // ── Suppression en attente (équivalent pendingDeleteFestivalId signal) ────
    private val _pendingDeleteFestivalId = MutableStateFlow<Int?>(null)
    val pendingDeleteFestivalId: StateFlow<Int?> = _pendingDeleteFestivalId.asStateFlow()

    init {
        loadFestivals()
    }

    // ── Chargement ────────────────────────────────────────────────────────────

    fun loadFestivals() {
        viewModelScope.launch {
            _uiState.value = FestivalUiState(isLoading = true)
            festivalRepository.getFestivals()
                .onSuccess { festivals ->
                    _uiState.value = FestivalUiState(
                        isLoading = false,
                        festivals = festivals,
                        errorMessage = null,
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = FestivalUiState(
                        isLoading = false,
                        festivals = emptyList(),
                        errorMessage = throwable.localizedMessage
                            ?: "Impossible de charger les festivals.",
                    )
                }
        }
    }

    // ── Sélection (équivalent festivalStore.setCurrentFestival) ──────────────

    /** Sélectionne un festival par son id. */
    fun selectFestival(id: Int) {
        _currentFestivalId.value = id
    }

    /** Désélectionne le festival courant (émet null comme Angular). */
    fun clearSelection() {
        _currentFestivalId.value = null
    }

    // ── Suppression (équivalent requestDeleteFestival / confirmDeleteFestival) ─

    /** Ouvre la confirmation de suppression. */
    fun requestDeleteFestival(id: Int) {
        _pendingDeleteFestivalId.value = id
    }

    /** Annule la suppression. */
    fun cancelDelete() {
        _pendingDeleteFestivalId.value = null
    }

    /**
     * Confirme et exécute la suppression.
     * À brancher quand le repository aura deleteFestival(id).
     *
     * TODO : appeler festivalRepository.deleteFestival(id) quand disponible.
     */
    fun confirmDelete() {
        val id = _pendingDeleteFestivalId.value ?: return
        viewModelScope.launch {
            // festivalRepository.deleteFestival(id)
            //     .onSuccess { ... }
            //     .onFailure { ... }
            if (_currentFestivalId.value == id) clearSelection()
            _pendingDeleteFestivalId.value = null
        }
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    companion object {
        fun factory(festivalRepository: FestivalRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { FestivalViewModel(festivalRepository) }
            }
    }
}
