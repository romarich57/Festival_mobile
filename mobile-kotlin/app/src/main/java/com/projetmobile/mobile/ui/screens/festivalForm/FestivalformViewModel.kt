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

    fun onNameChange(value: String) {
        val error = when {
            value.isBlank() -> "Le nom est obligatoire"
            value.trim().length < 2 -> "Minimum 2 caractères"
            value.trim().length > 100 -> "Maximum 100 caractères"
            else -> null
        }
        _uiState.value = _uiState.value.copy(name = value, nameError = error)
    }

    fun onStartDateChange(value: String) {
        val error = if (value.isBlank()) "La date de début est obligatoire" else null
        _uiState.value = _uiState.value.copy(startDate = value, startDateError = error)
    }

    fun onEndDateChange(value: String) {
        val error = if (value.isBlank()) "La date de fin est obligatoire" else null
        _uiState.value = _uiState.value.copy(endDate = value, endDateError = error)
    }

    fun onStockTablesStandardChange(value: String) {
        _uiState.value = _uiState.value.copy(stockTablesStandard = value)
    }

    fun onStockTablesGrandeChange(value: String) {
        _uiState.value = _uiState.value.copy(stockTablesGrande = value)
    }

    fun onStockTablesMairieChange(value: String) {
        _uiState.value = _uiState.value.copy(stockTablesMairie = value)
    }

    fun onStockChaisesChange(value: String) {
        _uiState.value = _uiState.value.copy(stockChaises = value)
    }

    fun onPrixPrisesChange(value: String) {
        _uiState.value = _uiState.value.copy(prixPrises = value)
    }

    fun onZoneNameChange(index: Int, value: String) {
        updateZone(index) { zone ->
            val error = if (value.isBlank()) "Le nom est obligatoire" else null
            zone.copy(name = value, nameError = error)
        }
    }

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

    fun addZone() {
        val zones = _uiState.value.zonesTarifaires + ZoneTarifaireDraft()
        _uiState.value = _uiState.value.copy(
            zonesTarifaires = zones,
            zonesError = null,
        )
    }

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
     * Équivalent submit() Angular :
     * - Valide le formulaire
     * - Appelle festivalRepository.addFestival()
     * - Émet successMessage ou errorMessage
     * - onSuccess est appelé par FestivalFormScreen pour naviguer en arrière
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

    fun consumeError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun updateZone(index: Int, update: (ZoneTarifaireDraft) -> ZoneTarifaireDraft) {
        val zones = _uiState.value.zonesTarifaires.toMutableList()
        if (index !in zones.indices) return
        zones[index] = update(zones[index])
        _uiState.value = _uiState.value.copy(
            zonesTarifaires = zones,
            zonesError = if (zones.isEmpty()) "Au moins une zone tarifaire est requise" else null,
        )
    }

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

    companion object {
        fun factory(festivalRepository: FestivalRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { FestivalFormViewModel(festivalRepository) }
            }
    }
}
