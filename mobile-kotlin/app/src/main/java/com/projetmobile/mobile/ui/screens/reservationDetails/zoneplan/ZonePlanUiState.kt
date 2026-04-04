package com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan

data class ZonePlanZoneState(
    val id: Int,
    val name: String,
    val zoneTarifaireName: String,
    val idZoneTarifaire: Int,
    val totalTables: Int,
    val allocatedTables: Int,
    val pricePerTable: Double,
    val m2Price: Double,
    val mySimpleAllocationTables: Int = 0,
    val mySimpleAllocationChaises: Int = 0,
    val hasReservationInLinkedZone: Boolean = false,
)

data class GameAllocationState(
    val allocationId: Int,
    val gameId: Int,
    val gameTitle: String,
    val gameType: String,
    val nbTablesOccupees: Double,
    val nbExemplaires: Double,
    val nbChaises: Int,
    val tailleTableRequise: String,
    val zonePlanId: Int?,
)

data class StockState(
    val tablesStandard: Int = 0,
    val tablesStandardOccupied: Int = 0,
    val tablesGrande: Int = 0,
    val tablesGrandeOccupied: Int = 0,
    val tablesMairie: Int = 0,
    val tablesMairieOccupied: Int = 0,
    val chaisesTotal: Int = 0,
    val chaisesAllocated: Int = 0,
) {
    fun availableForType(type: String): Int = when (type) {
        "standard" -> tablesStandard - tablesStandardOccupied
        "grande" -> tablesGrande - tablesGrandeOccupied
        "mairie" -> tablesMairie - tablesMairieOccupied
        else -> Int.MAX_VALUE
    }

    val chaisesAvailable: Int get() = chaisesTotal - chaisesAllocated
}

// Form state for creating/editing a placement (with or without game)
data class PlacementFormState(
    val zonePlanId: Int = -1,
    val withGame: Boolean = false,
    val selectedGameAllocationId: Int? = null,
    val nbTables: String = "1",
    val useM2: Boolean = false,
    val m2Value: String = "",
    val placePerCopy: String = "1",
    val nbCopies: String = "1",
    val tableType: String = "aucun",
    val nbChaises: String = "0",
)

sealed interface ZonePlanUiState {
    data object Loading : ZonePlanUiState

    data class Success(
        val reservationId: Int,
        val festivalId: Int,
        val zones: List<ZonePlanZoneState>,
        val games: List<GameAllocationState>,
        val stock: StockState,
        val isSaving: Boolean = false,
        val userMessage: String? = null,
        val showPlacementForm: Boolean = false,
        val placementForm: PlacementFormState = PlacementFormState(),
    ) : ZonePlanUiState

    data class Error(val message: String) : ZonePlanUiState
}
