package com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan.placement

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
