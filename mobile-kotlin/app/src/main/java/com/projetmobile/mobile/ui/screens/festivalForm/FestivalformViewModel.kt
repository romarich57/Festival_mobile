/**
 * Rôle : Porte l'état et la logique du module le formulaire de festival pour l'écran Compose associé.
 */

package com.projetmobile.mobile.ui.screens.festivalForm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.projetmobile.mobile.data.remote.festival.FestivalDto
import com.projetmobile.mobile.data.repository.festival.FestivalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Rôle : Gère la logique de validation et d'enregistrement des festivals.
 *
 * Précondition : Appelé par FestivalformScreen avec les saisies utilisateur pour créer un festival et ses zones tarifaires.
 *
 * Postcondition : Envoie les données au dépôt et notifie l'interface utilisateur de la réussite ou de l'échec de la création.
 */
class FestivalFormViewModel(
    private val festivalRepository: FestivalRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FestivalFormUiState())
    val uiState: StateFlow<FestivalFormUiState> = _uiState.asStateFlow()

    // ── Mise à jour des champs ────────────────────────────────────────────────

    /**
     * Rôle : Exécute l'action on name modification du module le formulaire de festival.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onNameChange(value: String) {
        val error = when {
            value.isBlank() -> "Le nom est obligatoire"
            value.trim().length < 2 -> "Minimum 2 caractères"
            value.trim().length > 100 -> "Maximum 100 caractères"
            else -> null
        }
        _uiState.value = _uiState.value.copy(name = value, nameError = error)
    }

    /**
     * Rôle : Exécute l'action on démarrage date modification du module le formulaire de festival.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onStartDateChange(value: String) {
        val error = if (value.isBlank()) "La date de début est obligatoire" else null
        _uiState.value = _uiState.value.copy(startDate = value, startDateError = error)
    }

    /**
     * Rôle : Exécute l'action on end date modification du module le formulaire de festival.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onEndDateChange(value: String) {
        val error = if (value.isBlank()) "La date de fin est obligatoire" else null
        _uiState.value = _uiState.value.copy(endDate = value, endDateError = error)
    }

    /**
     * Rôle : Exécute l'action on stock tables standard modification du module le formulaire de festival.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onStockTablesStandardChange(value: String) {
        _uiState.value = _uiState.value.copy(stockTablesStandard = value)
    }

    /**
     * Rôle : Exécute l'action on stock tables grande modification du module le formulaire de festival.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onStockTablesGrandeChange(value: String) {
        _uiState.value = _uiState.value.copy(stockTablesGrande = value)
    }

    /**
     * Rôle : Exécute l'action on stock tables mairie modification du module le formulaire de festival.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onStockTablesMairieChange(value: String) {
        _uiState.value = _uiState.value.copy(stockTablesMairie = value)
    }

    /**
     * Rôle : Exécute l'action on stock chaises modification du module le formulaire de festival.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onStockChaisesChange(value: String) {
        _uiState.value = _uiState.value.copy(stockChaises = value)
    }

    /**
     * Rôle : Exécute l'action on prix prises modification du module le formulaire de festival.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onPrixPrisesChange(value: String) {
        _uiState.value = _uiState.value.copy(prixPrises = value)
    }

    /**
     * Rôle : Exécute l'action on zone name modification du module le formulaire de festival.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onZoneNameChange(index: Int, value: String) {
        updateZone(index) { zone ->
            val error = if (value.isBlank()) "Le nom est obligatoire" else null
            zone.copy(name = value, nameError = error)
        }
    }

    /**
     * Rôle : Exécute l'action on zone nb tables modification du module le formulaire de festival.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onZoneNbTablesChange(index: Int, value: String) {
        updateZone(index) { zone ->
            val nbTables = value.toIntOrNull()
            val error = when {
                value.isBlank() -> "Le nombre de tables est obligatoire"
                nbTables == null -> "Nombre invalide"
                nbTables <= 0 -> "Doit être supérieur à 0"
                else -> null
            }
            zone.copy(nbTables = value, nbTablesError = error)
        }
    }

    /**
     * Rôle : Exécute l'action on zone price per table modification du module le formulaire de festival.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onZonePricePerTableChange(index: Int, value: String) {
        updateZone(index) { zone ->
            val price = value.toDoubleOrNull()
            val error = when {
                value.isBlank() -> "Le prix est obligatoire"
                price == null -> "Prix invalide"
                price <= 0.0 -> "Doit être supérieur à 0"
                else -> null
            }
            zone.copy(pricePerTable = value, pricePerTableError = error)
        }
    }

    /**
     * Rôle : Exécute l'action add zone du module le formulaire de festival.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun addZone() {
        val zones = _uiState.value.zonesTarifaires + ZoneTarifaireDraft()
        _uiState.value = _uiState.value.copy(
            zonesTarifaires = zones,
            zonesError = null,
        )
    }

    /**
     * Rôle : Supprime zone.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun removeZone(index: Int) {
        val current = _uiState.value.zonesTarifaires
        if (current.size <= 1) return
        val zones = current.toMutableList().also { it.removeAt(index) }
        _uiState.value = _uiState.value.copy(
            zonesTarifaires = zones,
            zonesError = if (zones.isEmpty()) "Au moins une zone tarifaire est requise" else null,
        )
    }

    // ── Soumission ────────────────────────────────────────────────────────────

    /**
     * Rôle : Valide le formulaire puis crée un nouveau festival via le repository.
     *
     * Précondition : L'état du formulaire doit être cohérent et `onSuccess` doit être fourni par l'écran appelant.
     *
     * Postcondition : Un festival est créé si la validation passe; sinon l'état UI expose les erreurs de validation ou de création.
     */
    fun submit(onSuccess: (String) -> Unit) {
        val state = validateForm()
        if (!state.isValid) return

        viewModelScope.launch {
            _uiState.value = state.copy(isSubmitting = true, errorMessage = null)

            val dto = FestivalDto(
                id = null, // l'id est null pour la création
                name = state.name.trim(),
                startDate = state.startDate,
                endDate = state.endDate,
                stockTablesStandard = state.stockTablesStandard.toIntOrNull() ?: 0,
                stockTablesGrande = state.stockTablesGrande.toIntOrNull() ?: 0,
                stockTablesMairie = state.stockTablesMairie.toIntOrNull() ?: 0,
                stockChaises = state.stockChaises.toIntOrNull() ?: 0,
                prixPrises = state.prixPrises.toDoubleOrNull() ?: 0.0,
                zonesTarifaires = state.zonesTarifaires.map { zone ->
                    FestivalDto.ZoneTarifaireCreateDto(
                        name = zone.name.trim(),
                        nbTables = zone.nbTables.toIntOrNull() ?: 0,
                        pricePerTable = zone.pricePerTable.toDoubleOrNull() ?: 0.0,
                    )
                },
            )

            festivalRepository.addFestival(dto)
                .onSuccess {
                    val successMessage = "Festival créé."
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        successMessage = successMessage,
                    )
                    onSuccess(successMessage)
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = when {
                            throwable.message?.contains("409") == true ->
                                "Un festival avec ce nom existe déjà."
                            else ->
                                throwable.localizedMessage ?: "Erreur lors de la création."
                        },
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
     * Rôle : Exécute l'action mise à jour zone du module le formulaire de festival.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    private fun updateZone(index: Int, update: (ZoneTarifaireDraft) -> ZoneTarifaireDraft) {
        val zones = _uiState.value.zonesTarifaires.toMutableList()
        if (index !in zones.indices) return
        zones[index] = update(zones[index])
        _uiState.value = _uiState.value.copy(
            zonesTarifaires = zones,
            zonesError = if (zones.isEmpty()) "Au moins une zone tarifaire est requise" else null,
        )
    }

    /**
     * Rôle : Valide formulaire.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    private fun validateForm(): FestivalFormUiState {
        val state = _uiState.value
        val zones = state.zonesTarifaires.map { zone ->
            zone.copy(
                nameError = if (zone.name.isBlank()) "Le nom est obligatoire" else null,
                nbTablesError = when {
                    zone.nbTables.isBlank() -> "Le nombre de tables est obligatoire"
                    zone.nbTables.toIntOrNull() == null -> "Nombre invalide"
                    (zone.nbTables.toIntOrNull() ?: 0) <= 0 -> "Doit être supérieur à 0"
                    else -> null
                },
                pricePerTableError = when {
                    zone.pricePerTable.isBlank() -> "Le prix est obligatoire"
                    zone.pricePerTable.toDoubleOrNull() == null -> "Prix invalide"
                    (zone.pricePerTable.toDoubleOrNull() ?: 0.0) <= 0.0 -> "Doit être supérieur à 0"
                    else -> null
                },
            )
        }

        val zonesError = if (zones.isEmpty()) "Au moins une zone tarifaire est requise" else null
        val updated = state.copy(zonesTarifaires = zones, zonesError = zonesError)
        _uiState.value = updated
        return updated
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    /**
     * Rôle : Expose un singleton de support pour le module le formulaire de festival.
     */
    companion object {
        /**
         * Rôle : Exécute l'action factory du module le formulaire de festival.
         *
         * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
         *
         * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
         */
        fun factory(festivalRepository: FestivalRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { FestivalFormViewModel(festivalRepository) }
            }
    }
}
