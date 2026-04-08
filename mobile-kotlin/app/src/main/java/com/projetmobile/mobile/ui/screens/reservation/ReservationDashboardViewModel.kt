/**
 * Rôle : Porte l'état et la logique du module les réservations pour l'écran Compose associé.
 */

package com.projetmobile.mobile.ui.screens.reservation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.projetmobile.mobile.data.repository.reservation.ReservationRepository
import com.projetmobile.mobile.data.repository.toRepositoryException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Rôle : Manipule la liste et le filtre des stands affectés pour le Dashboard des Réservations.
 *
 * Précondition : Construit en injectant le ReservationRepository via Factory.
 *
 * Postcondition : Envoie continuellement [uiState] et offre l'accès à la méthode updateSearch/Sort pour modifier l'affichage final.
 */
class ReservationDashboardViewModel(
    private val repository: ReservationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservationDashboardUiState())
    val uiState: StateFlow<ReservationDashboardUiState> = _uiState.asStateFlow()

    val searchQuery = MutableStateFlow("")
    val typeFilter = MutableStateFlow("all")
    val sortKey = MutableStateFlow("name-asc")

    val filteredReservations = combine(
        _uiState, searchQuery, typeFilter, sortKey
    ) { state, query, type, sort ->
        var result = state.reservations

        if (type != "all") {
            result = result.filter { it.reservantType == type }
        }

        if (query.isNotBlank()) {
            result = result.filter { it.reservantName.contains(query, ignoreCase = true) }
        }

        result.sortedWith { a, b ->
            if (sort == "name-desc") b.reservantName.compareTo(a.reservantName)
            else a.reservantName.compareTo(b.reservantName)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var observationFestivalId: Int? = null

    /**
     * Rôle : Charge réservations.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun loadReservations(festivalId: Int) {
        // Lance l'observation Room si pas encore démarrée pour ce festival
        if (observationFestivalId != festivalId) {
            observationFestivalId = festivalId
            viewModelScope.launch {
                repository.observeReservations(festivalId).collect { reservations ->
                    _uiState.value = _uiState.value.copy(
                        reservations = reservations,
                        isLoading = if (reservations.isNotEmpty()) false else _uiState.value.isLoading,
                    )
                }
            }
        }
        // Déclenche le refresh réseau → Room → Flow émet
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            repository.refreshReservations(festivalId)
                .onSuccess { _uiState.value = _uiState.value.copy(isLoading = false) }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = if (_uiState.value.reservations.isEmpty())
                            e.toRepositoryException("Impossible de charger les réservations.").localizedMessage
                                ?: "Impossible de charger les réservations."
                        else null,
                    )
                }
        }
    }

    /**
     * Rôle : Supprime réservation.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun deleteReservation(reservationId: Int, festivalId: Int) {
        viewModelScope.launch {
            repository.deleteReservation(reservationId)
                .onFailure {
                    _uiState.value = _uiState.value.copy(errorMessage = "Erreur lors de la suppression")
                }
            // Pas besoin de recharger : Room Flow met à jour la liste automatiquement
        }
    }

    /**
     * Rôle : Expose un singleton de support pour le module les réservations.
     */
    companion object {
        /**
         * Rôle : Exécute l'action factory du module les réservations.
         *
         * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
         *
         * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
         */
        fun factory(repository: ReservationRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ReservationDashboardViewModel(repository)
            }
        }
    }
}
