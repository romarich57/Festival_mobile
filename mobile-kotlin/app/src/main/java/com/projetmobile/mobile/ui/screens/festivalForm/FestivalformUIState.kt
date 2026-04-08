package com.projetmobile.mobile.ui.screens.festivalForm

/**
 * Rôle : Modèle d'état du formulaire de création de festival.
 *
 * Précondition : Utilisé par FestivalformViewModel pour stocker les saisiesutilisateur ainsi que les éventuelles erreurs de validation.
 *
 * Postcondition : Représente de façon immuable toutes les données nécessaires à l'UI pour dessiner le formulaire.
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

    val zonesTarifaires: List<ZoneTarifaireDraft> = listOf(ZoneTarifaireDraft()),
    val zonesError: String? = null,

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
                && zonesError == null
                && zonesTarifaires.isNotEmpty()
                && zonesTarifaires.all { it.isValid }
}

data class ZoneTarifaireDraft(
    val name: String = "",
    val nbTables: String = "0",
    val pricePerTable: String = "0",
    val nameError: String? = null,
    val nbTablesError: String? = null,
    val pricePerTableError: String? = null,
) {
    val isValid: Boolean
        get() = name.isNotBlank()
                && (nbTables.toIntOrNull() ?: 0) > 0
                && (pricePerTable.toDoubleOrNull() ?: 0.0) > 0.0
                && nameError == null
                && nbTablesError == null
                && pricePerTableError == null
}