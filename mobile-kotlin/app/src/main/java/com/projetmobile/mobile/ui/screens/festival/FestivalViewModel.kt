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

class FestivalViewModel(
    private val festivalRepository: FestivalRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FestivalUiState())
    val uiState: StateFlow<FestivalUiState> = _uiState.asStateFlow()

    private val _currentFestivalId = MutableStateFlow<Int?>(null)
    val currentFestivalId: StateFlow<Int?> = _currentFestivalId.asStateFlow()

    private val _pendingDeleteFestivalId = MutableStateFlow<Int?>(null)
    val pendingDeleteFestivalId: StateFlow<Int?> = _pendingDeleteFestivalId.asStateFlow()

    init {
        // 1. AJOUT : On commence à écouter Room immédiatement.
        // Si des données existent déjà en local, elles s'affichent instantanément.
        observeFestivals()

        // 2. MODIFICATION : On lance le refresh réseau en arrière-plan.
        loadFestivals()
    }

    // ── NOUVELLE MÉTHODE : Observation Réactive ──────────────────────────────

    /**
     * Collecte le Flow venant du Repository.
     * Dès que Room est mis à jour (via un insert ou delete), l'UI réagit.
     */
    private fun observeFestivals() {
        viewModelScope.launch {
            festivalRepository.observeFestivals().collect { festivalsFromRoom ->
                _uiState.value = _uiState.value.copy(festivals = festivalsFromRoom)
            }
        }
    }

    // ── Chargement (Modifié pour ne plus écraser l'UI brutalement) ─────────────

    fun loadFestivals() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            festivalRepository.getFestivals()
                .onSuccess {
                    // On ne met à jour QUE le statut loading.
                    // observeFestivals() s'occupera de mettre à jour la liste.
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        // On n'affiche l'erreur que si la liste est vide (vrai échec)
                        errorMessage = if (_uiState.value.festivals.isEmpty()) {
                            throwable.localizedMessage ?: "Impossible de charger les festivals."
                        } else null
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
     */
    fun confirmDelete() {
        val id = _pendingDeleteFestivalId.value ?: return
        viewModelScope.launch {
            festivalRepository.deleteFestival(id)
                .onSuccess {
                    if (_currentFestivalId.value == id) clearSelection()
                    _pendingDeleteFestivalId.value = null
                    loadFestivals() // Recharger la liste après suppression
                }
                .onFailure { throwable ->
                    _pendingDeleteFestivalId.value = null
                    // On garde la liste des festivals et on affiche juste l'erreur
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Suppression impossible : ${throwable.localizedMessage}. Vérifiez vos droits."
                    )
                }
        }
    }

    fun consumeError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    companion object {
        fun factory(festivalRepository: FestivalRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { FestivalViewModel(festivalRepository) }
            }
    }
}
