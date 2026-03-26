package com.projetmobile.mobile.ui.screens.reservation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.projetmobile.mobile.data.repository.reservation.ReservationRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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

    fun loadReservations(festivalId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val reservations = repository.getReservations(festivalId)
                _uiState.value = _uiState.value.copy(isLoading = false, reservations = reservations)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Erreur inconnue"
                )
            }
        }
    }

    fun deleteReservation(reservationId: Int, festivalId: Int) {
        viewModelScope.launch {
            try {
                repository.deleteReservation(reservationId)
                loadReservations(festivalId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Erreur lors de la suppression")
            }
        }
    }

    companion object {
        fun factory(repository: ReservationRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ReservationDashboardViewModel(repository)
            }
        }
    }
}
