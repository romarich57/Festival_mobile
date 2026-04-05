package com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan.addzone

data class ZoneTarifaireOptionState(
    val id: Int,
    val name: String,
    val nbTables: Int,
)

data class AddZoneFormState(
    val name: String = "",
    val selectedZoneTarifaireId: Int? = null,
    val nbTables: String = "",
    // Max tables autorisé selon les tables restantes dans la zone tarifaire
    val maxTables: Int = Int.MAX_VALUE,
)
