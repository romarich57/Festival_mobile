package com.projetmobile.mobile.ui.screens.reservationDetails

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
