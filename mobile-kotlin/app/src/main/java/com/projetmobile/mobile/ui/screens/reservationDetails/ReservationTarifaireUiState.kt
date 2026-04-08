package com.projetmobile.mobile.ui.screens.reservationDetails

/**
 * Rôle : Décrit l'état modifiable et initial de chaque zone de tarification concernant l'éditeur.
 *
 * Précondition : Met à jour constamment le nombre de tables initialement réservées et lues par [ReservationTarifaireUiState].
 *
 * Postcondition : Affiche ces informations au sein de la sous-section ou tab du profil de la réservation.
 */
data class ZoneTarifaireFormState(
    val id: Int,
    val name: String,
    val pricePerTable: Double,
    val m2Price: Double,
    val totalTables: Int,
    val availableTables: Int,
    val reservedTables: String,
    val reservedTablesInitial: Int,
)

sealed interface ReservationTarifaireUiState {
    object Loading : ReservationTarifaireUiState

    data class Success(
        val festivalId: Int,
        val zones: List<ZoneTarifaireFormState>,
        val prixPrises: Double,
        val nbPrises: String,
        val tableDiscountOffered: String,
        val directDiscount: String,
        val note: String,
        val isSaving: Boolean = false,
        val userMessage: String? = null,
    ) : ReservationTarifaireUiState

    data class Error(val message: String) : ReservationTarifaireUiState
}
