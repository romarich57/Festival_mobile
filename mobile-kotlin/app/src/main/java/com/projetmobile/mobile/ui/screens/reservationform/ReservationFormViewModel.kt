/**
 * Rôle : Porte l'état et la logique du module le formulaire de réservation pour l'écran Compose associé.
 */

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

/**
 * Rôle : Préremplit, valide et sauvegarde un formulaire lié à une demande de `Reservation`.
 *
 * Précondition : [uiState] s'active sur base de dépôts externes avec un `festivalId` particulier.
 *
 * Postcondition : Avertit si la liste des éditeurs est chargée, exécute des enregistrements en aval des API liés.
 */
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

    /**
     * Rôle : Gère la modification du champ use existing réservant.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onUseExistingReservantChanged(useExisting: Boolean) {
        val currentState = _uiState.value
        val selectedReservant = currentState.selectedReservantId
            ?.let { selectedId -> currentState.reservantOptions.firstOrNull { it.id == selectedId } }

        _uiState.value = currentState.copy(
            useExistingReservant = useExisting,
            // Quand on bascule vers un réservant existant, on réinjecte ses champs pour garder le formulaire cohérent.
            nom = if (useExisting && selectedReservant != null) selectedReservant.name else currentState.nom,
            email = if (useExisting && selectedReservant != null) selectedReservant.email else currentState.email,
            type = if (useExisting && selectedReservant != null) selectedReservant.type else currentState.type,
            errorMessage = null,
        )
    }

    /**
     * Rôle : Gère la modification du champ selected réservant.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onSelectedReservantChanged(reservantId: Int) {
        val currentState = _uiState.value
        val selectedReservant = currentState.reservantOptions.firstOrNull { it.id == reservantId } ?: return

        _uiState.value = currentState.copy(
            // Une sélection valide synchronise immédiatement les champs dérivés affichés dans l'UI.
            selectedReservantId = reservantId,
            nom = selectedReservant.name,
            email = selectedReservant.email,
            type = selectedReservant.type,
            errorMessage = null,
        )
    }

    /**
     * Rôle : Gère la modification du champ nom.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onNomChanged(nom: String) {
        _uiState.value = _uiState.value.copy(nom = nom)
    }

    /**
     * Rôle : Gère la modification du champ email.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    /**
     * Rôle : Gère la modification du champ type.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onTypeChanged(type: String) {
        _uiState.value = _uiState.value.copy(type = type)
    }

    /**
     * Rôle : Crée réservation.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun createReservation(festivalId: Int) {
        val state = _uiState.value

        if (state.useExistingReservant && state.selectedReservantId == null) {
            // Le mode "réservant existant" est invalide sans sélection explicite.
            _uiState.value = state.copy(
                errorMessage = "Sélectionnez un réservant existant ou passez en mode nouveau réservant.",
            )
            return
        }

        if (!state.useExistingReservant && (state.nom.isBlank() || state.email.isBlank() || state.type.isBlank())) {
            // Le mode de création libre exige toutes les informations minimales avant l'appel réseau.
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
            // Le type envoyé à l'API est normalisé pour rester stable quel que soit le libellé affiché.
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

    /**
     * Rôle : Exécute l'action réinitialisation success du module le formulaire de réservation.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun resetSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }

    /**
     * Rôle : Exécute l'action observe réservants du module le formulaire de réservation.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    private fun observeReservants() {
        viewModelScope.launch {
            reservantsRepository.observeReservants().collect { reservants ->
                // Les options sont triées avant toute synchronisation pour conserver une liste déterministe.
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

    /**
     * Rôle : Rafraîchit réservants.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
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

    /**
     * Rôle : Exécute l'action tri réservants du module le formulaire de réservation.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    private fun sortReservants(reservants: List<ReservantListItem>): List<ReservantListItem> {
        return reservants.sortedWith(
            compareBy<ReservantListItem>(
                { it.name.lowercase(Locale.ROOT) },
                { it.name },
                { it.id },
            ),
        )
    }

    /**
     * Rôle : Expose un singleton de support pour le module le formulaire de réservation.
     */
    companion object {
        /**
         * Rôle : Exécute l'action factory du module le formulaire de réservation.
         *
         * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
         *
         * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
         */
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
