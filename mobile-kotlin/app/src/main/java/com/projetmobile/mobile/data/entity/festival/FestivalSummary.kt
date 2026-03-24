package com.projetmobile.mobile.data.entity.festival

data class FestivalSummary(
    val id: Int,
    val name: String,
    val startDate: String,
    val endDate: String,
    val stockTablesStandard: Int,
    val stockTablesGrande: Int,
    val stockTablesMairie: Int,
    val stockChaises: Int,
    val prixPrises: Double,
) {
    val totalTables: Int
        get() = stockTablesStandard + stockTablesGrande + stockTablesMairie
}