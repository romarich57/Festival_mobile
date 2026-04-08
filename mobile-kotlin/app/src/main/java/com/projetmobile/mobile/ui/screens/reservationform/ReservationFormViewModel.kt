package com.projetmobile.mobile.ui.screens.reservationform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.projetmobile.mobile.data.entity.reservants.ReservantListItem
import com.projetmobile.mobile.data.remote.reservation.ReservationCreatePayloadDto
import com.projetmobile.mobile.data.repository.reservation.ReservationRepository
import com.projetmobile.mobile.data.repository.reservants.ReservantsRepository
import com.projetmobile.mobile.data.repository.toRepositoryException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Locale

class ReservationFormViewModel(
    private val reservationRepository: ReservationRepository,
    private val reservantsRepository: ReservantsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservationFormUiState())
    val uiState: StateFlow<ReservationFormUiState> = _uiState.asStateFlow()

    init {
        observeReservants()
        refreshReservants()
    }

    fun onUseExistingReservantChanged(useExisting: Boolean) {
        val currentState = _uiState.value
        val selectedReservant = currentState.selectedReservantId
            ?.let { selectedId -> currentState.reservantOptions.firstOrNull { it.id == selectedId } }

        _uiState.value = currentState.copy(
            useExistingReservant = useExisting,
            nom = if (useExisting && selectedReservant != null) selectedReservant.name else currentState.nom,
            email = if (useExisting && selectedReservant != null) selectedReservant.email else currentState.email,
            type = if (useExisting && selectedReservant != null) selectedReservant.type else currentState.type,
            errorMessage = null,
        )
    }

    fun onSelectedReservantChanged(reservantId: Int) {
        val currentState = _uiState.value
        val selectedReservant = currentState.reservantOptions.firstOrNull { it.id == reservantId } ?: return

        _uiState.value = currentState.copy(
            selectedReservantId = reservantId,
            nom = selectedReservant.name,
            email = selectedReservant.email,
            type = selectedReservant.type,
            errorMessage = null,
        )
    }

    fun onNomChanged(nom: String) {
        _uiState.value = _uiState.value.copy(nom = nom)
    }

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun onTypeChanged(type: String) {
        _uiState.value = _uiState.value.copy(type = type)
    }

    fun createReservation(festivalId: Int) {
        val state = _uiState.value

        if (state.useExistingReservant && state.selectedReservantId == null) {
            _uiState.value = state.copy(
                errorMessage = "Sélectionnez un réservant existant ou passez en mode nouveau réservant.",
            )
            return
        }

        if (!state.useExistingReservant && (state.nom.isBlank() || state.email.isBlank() || state.type.isBlank())) {
            _uiState.value = state.copy(
                errorMessage = "Nom, email et type sont obligatoires pour créer un nouveau réservant.",
            )
            return
        }

        val selectedReservant = state.selectedReservantId
            ?.let { selectedId -> state.reservantOptions.firstOrNull { it.id == selectedId } }

        val reservantName = if (state.useExistingReservant) {
            selectedReservant?.name.orEmpty()
        } else {
            state.nom.trim()
        }

        val reservantEmail = if (state.useExistingReservant) {
            selectedReservant?.email.orEmpty()
        } else {
            state.email.trim()
        }

        val reservantType = if (state.useExistingReservant) {
            selectedReservant?.type.orEmpty()
        } else {
            state.type.trim().lowercase(Locale.ROOT)
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            val payload = ReservationCreatePayloadDto(
                reservantName = reservantName,
                reservantEmail = reservantEmail,
                reservantType = reservantType,
                reservantId = if (state.useExistingReservant) selectedReservant?.id else null,
                festivalId = festivalId,
            )
            reservationRepository.createReservation(payload)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.toRepositoryException("Impossible de créer la réservation.")
                            .localizedMessage
                            ?: "Impossible de créer la réservation.",
                    )
                }
        }
    }

    fun resetSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }

    private fun observeReservants() {
        viewModelScope.launch {
            reservantsRepository.observeReservants().collect { reservants ->
                val sortedReservants = sortReservants(reservants)
                val currentState = _uiState.value
                val selectedReservantId = currentState.selectedReservantId
                    ?.takeIf { selectedId -> sortedReservants.any { option -> option.id == selectedId } }

                val selectedReservant = selectedReservantId
                    ?.let { selectedId -> sortedReservants.firstOrNull { it.id == selectedId } }

                _uiState.value = currentState.copy(
                    reservantOptions = sortedReservants,
                    selectedReservantId = selectedReservantId,
                    nom = if (currentState.useExistingReservant && selectedReservant != null) selectedReservant.name else currentState.nom,
                    email = if (currentState.useExistingReservant && selectedReservant != null) selectedReservant.email else currentState.email,
                    type = if (currentState.useExistingReservant && selectedReservant != null) selectedReservant.type else currentState.type,
                )
            }
        }
    }

    private fun refreshReservants() {
        viewModelScope.launch {
            reservantsRepository.refreshReservants().onFailure {
                if (_uiState.value.reservantOptions.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Impossible de charger les réservants.",
                    )
                }
            }
        }
    }

    private fun sortReservants(reservants: List<ReservantListItem>): List<ReservantListItem> {
        return reservants.sortedWith(
            compareBy<ReservantListItem>(
                { it.name.lowercase(Locale.ROOT) },
                { it.name },
                { it.id },
            ),
        )
    }

    companion object {
        fun factory(
            reservationRepository: ReservationRepository,
            reservantsRepository: ReservantsRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ReservationFormViewModel(reservationRepository, reservantsRepository)
            }
        }
    }
}
