package com.projetmobile.mobile.ui.screens.festivalForm

/**
 * État UI du formulaire de création de festival.
 *
 * Équivalent Angular : état du FormGroup + loading/error dans FestivalFormComponent.
 */
data class FestivalFormUiState(
    // Champs du formulaire
    val name: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val stockTablesStandard: String = "0",
    val stockTablesGrande: String = "0",
    val stockTablesMairie: String = "0",
    val stockChaises: String = "0",
    val prixPrises: String = "0",

    // État de soumission
    val isSubmitting: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,

    // Validation (équivalent Validators Angular)
    val nameError: String? = null,
    val startDateError: String? = null,
    val endDateError: String? = null,
) {
    /** Équivalent festivalForm.valid — true si aucune erreur et champs requis remplis. */
    val isValid: Boolean
        get() = name.isNotBlank()
                && startDate.isNotBlank()
                && endDate.isNotBlank()
                && nameError == null
                && startDateError == null
                && endDateError == null
}