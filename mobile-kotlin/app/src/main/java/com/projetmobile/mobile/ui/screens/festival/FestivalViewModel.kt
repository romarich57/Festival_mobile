/**
 * Rôle : Porte l'état et la logique du module les festivals pour l'écran Compose associé.
 */

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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Rôle : Logique de présentation des festivals (affichage, sélection, suppression).
 * Gère l'orchestration entre le FestivalRepository, la sélection des festivals et l'état de l'UI.
 *
 * Précondition : Doit être initialisé avec le FestivalRepository pour accéder aux données.
 *
 * Postcondition : Gère l'affichage, les notifications d'erreur et les effets visuels de suppression d'un festival.
 */
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
        // Observation Room (SSOT) — données disponibles immédiatement si cache peuplé
        viewModelScope.launch {
            festivalRepository.observeFestivals().collect { festivals ->
                _uiState.update { state ->
                    state.copy(
                        festivals = festivals,
                        isLoading = if (festivals.isNotEmpty()) false else state.isLoading,
                    )
                }
            }
        }
        loadFestivals()
    }

    // ── Chargement ─────────────

    /**
     * Rôle : Charge festivals.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun loadFestivals() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            festivalRepository.refreshFestivals()
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = if (_uiState.value.festivals.isEmpty()) {
                            throwable.localizedMessage ?: "Impossible de charger les festivals."
                        } else null,
                    )
                }
        }
    }

    // ── Sélection (équivalent festivalStore.setCurrentFestival) ──────────────

    /**
     * Rôle : Sélectionne un festival par son identifiant.
     *
     * Précondition : L'identifiant transmis doit correspondre à un festival exploitable par l'écran courant.
     *
     * Postcondition : `currentFestivalId` pointe vers le festival choisi et l'UI peut refléter la sélection.
     */
    fun selectFestival(id: Int) {
        _currentFestivalId.value = id
    }

    /**
     * Rôle : Désélectionne le festival courant.
     *
     * Précondition : Aucune.
     *
     * Postcondition : `currentFestivalId` redevient `null`.
     */
    fun clearSelection() {
        _currentFestivalId.value = null
    }

    // ── Suppression (équivalent requestDeleteFestival / confirmDeleteFestival) ─

    /**
     * Rôle : Ouvre la confirmation de suppression pour un festival donné.
     *
     * Précondition : L'identifiant transmis doit identifier le festival que l'utilisateur veut supprimer.
     *
     * Postcondition : `pendingDeleteFestivalId` contient l'identifiant en attente de validation.
     */
    fun requestDeleteFestival(id: Int) {
        _pendingDeleteFestivalId.value = id
    }

    /**
     * Rôle : Annule la suppression en attente.
     *
     * Précondition : Aucune.
     *
     * Postcondition : `pendingDeleteFestivalId` redevient `null`.
     */
    fun cancelDelete() {
        _pendingDeleteFestivalId.value = null
    }

    /**
     * Rôle : Confirme et exécute la suppression du festival en attente.
     *
     * Précondition : `pendingDeleteFestivalId` doit contenir un identifiant valide; sinon la méthode s'arrête sans effet.
     *
     * Postcondition : Le festival est supprimé côté repository, la sélection est nettoyée si nécessaire et le callback de succès peut être invoqué.
     */
    fun confirmDelete(onSuccess: (String) -> Unit = {}) {
        val id = _pendingDeleteFestivalId.value ?: return
        viewModelScope.launch {
            festivalRepository.deleteFestival(id)
                .onSuccess {
                    if (_currentFestivalId.value == id) clearSelection()
                    _pendingDeleteFestivalId.value = null
                    onSuccess("Suppression planifiée.")
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

    /**
     * Rôle : Consomme erreur.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun consumeError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Rôle : Consomme information message.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun consumeInfoMessage() {
        _uiState.value = _uiState.value.copy(infoMessage = null)
    }

    /**
     * Rôle : Consomme external rafraîchissement.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun consumeExternalRefresh(infoMessage: String?) {
        if (infoMessage != null) {
            _uiState.value = _uiState.value.copy(infoMessage = infoMessage)
        }
        loadFestivals()
    }

    /**
     * Rôle : Expose un singleton de support pour le module les festivals.
     */
    companion object {
        /**
         * Rôle : Exécute l'action factory du module les festivals.
         *
         * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
         *
         * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
         */
        fun factory(festivalRepository: FestivalRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { FestivalViewModel(festivalRepository) }
            }
    }
}
