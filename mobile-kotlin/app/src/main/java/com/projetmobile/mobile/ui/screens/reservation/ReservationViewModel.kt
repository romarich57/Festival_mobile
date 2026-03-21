package com.projetmobile.mobile.ui.screens.reservation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.projetmobile.mobile.data.entity.ReservationDashboardRowEntity
import com.projetmobile.mobile.data.remote.reservation.ReservationCreatePayloadDto
import com.projetmobile.mobile.data.repository.reservation.ReservationRepository
import com.projetmobile.mobile.data.repository.reservation.ReservationRepositoryImpl
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.projetmobile.mobile.data.remote.reservation.ReservationApiService

class ReservationViewModel(
    private val repository: ReservationRepository
) : ViewModel() {

    // États
    private val _allReservations = MutableStateFlow<List<ReservationDashboardRowEntity>>(emptyList())
    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    // Filtres et tri
    val searchQuery = MutableStateFlow("")
    val typeFilter = MutableStateFlow("all")
    val sortKey = MutableStateFlow("name-asc")
    val filteredReservations = combine( //combine c'est pour combiner plusieurs flows en un seul flow qui émet des valeurs basées sur les dernières valeurs de chacun des flows combinés
        _allReservations, searchQuery, typeFilter, sortKey
    ) { reservations, query, type, sort ->
        var result = reservations

        // 1. Filtre par type
        if (type != "all") {
            result = result.filter { it.reservantType == type }
        }

        // 2. Filtre par recherche
        if (query.isNotBlank()) {
            result = result.filter { it.reservantName.contains(query, ignoreCase = true) }
        }

        // 3. Tri
        result.sortedWith { a, b ->
            if (sort == "name-desc") b.reservantName.compareTo(a.reservantName)
            else a.reservantName.compareTo(b.reservantName)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList()) //Lazily signifie que le flow ne commencera à émettre des valeurs que lorsqu'il y aura au moins un collecteur actif, et qu'il s'arrêtera lorsque le dernier collecteur se désabonnera. C'est une bonne option pour les données qui ne doivent être chargées que lorsque l'utilisateur en a besoin, comme dans ce cas où les réservations ne sont chargées que lorsque l'écran est affiché.

    fun loadReservations(festivalId: Int) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                _allReservations.value = repository.getReservations(festivalId)
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Erreur inconnue"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun createReservation(
        festivalId: Int,
        nom: String,
        email: String,
        type: String
    ) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val payload = ReservationCreatePayloadDto(
                    reservantName = nom,
                    reservantEmail = email,
                    reservantType = type,
                    festivalId = festivalId
                    // On laisse les prix à 0 pour ce test simple
                )
                repository.createReservation(payload)

                // Si ça marche, on recharge la liste pour voir la nouvelle réservation
                loadReservations(festivalId)

            } catch (e: Exception) {
                errorMessage.value = "Erreur création : ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    companion object {
        fun factory(repository: ReservationRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ReservationViewModel(repository)
            }
        }
    }

    fun deleteReservation(reservationId: Int, festivalId: Int) {
        viewModelScope.launch {
            try {
                repository.deleteReservation(reservationId) // Appelle ton API DELETE
                loadReservations(festivalId) // Rafraîchit la liste après suppression
            } catch (e: Exception) {
                errorMessage.value = "Erreur lors de la suppression"
            }
        }
    }
}