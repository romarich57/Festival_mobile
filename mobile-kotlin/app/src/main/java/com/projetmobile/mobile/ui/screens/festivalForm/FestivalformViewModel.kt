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
 * ViewModel du formulaire de création de festival.
 *
 * Équivalents Angular (FestivalFormComponent) :
 *  - onNameChange()      = FormControl 'name' avec Validators.required + minLength(2)
 *  - onStartDateChange() = FormControl 'start_date' avec Validators.required
 *  - onEndDateChange()   = FormControl 'end_date' avec Validators.required
 *  - submit()            = submit() → festivalService.addFestival()
 *
 * Les zones tarifaires sont reportées (TODO).
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

    // ── Soumission ────────────────────────────────────────────────────────────

    /**
     * Équivalent submit() Angular :
     * - Valide le formulaire
     * - Appelle festivalRepository.addFestival()
     * - Émet successMessage ou errorMessage
     * - onSuccess est appelé par FestivalFormScreen pour naviguer en arrière
     */
    fun submit(onSuccess: () -> Unit) {
        val state = _uiState.value
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
            )

            festivalRepository.addFestival(dto)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        successMessage = "Festival créé avec succès !",
                    )
                    onSuccess()
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

    // ── Factory ───────────────────────────────────────────────────────────────

    companion object {
        fun factory(festivalRepository: FestivalRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { FestivalFormViewModel(festivalRepository) }
            }
    }
}
