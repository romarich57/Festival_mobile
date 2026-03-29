package com.projetmobile.mobile.data.remote.festival

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FestivalDto(
    val id: Int? = null,
    val name: String = "",
    @SerialName("start_date") val startDate: String = "",
    @SerialName("end_date") val endDate: String = "",
    @SerialName("stock_tables_standard") val stockTablesStandard: Int = 0,
    @SerialName("stock_tables_grande") val stockTablesGrande: Int = 0,
    @SerialName("stock_tables_mairie") val stockTablesMairie: Int = 0,
    @SerialName("stock_chaises") val stockChaises: Int = 0,
    @SerialName("prix_prises") val prixPrises: Double = 0.0,
    @SerialName("zones_tarifaires") val zonesTarifaires: List<ZoneTarifaireCreateDto> = emptyList(),
){
    val totalTables: Int
        get() = stockTablesStandard + stockTablesGrande + stockTablesMairie

    @Serializable
    data class ZoneTarifaireCreateDto(
        val name: String,
        @SerialName("nb_tables") val nbTables: Int,
        @SerialName("price_per_table") val pricePerTable: Double,
    )
}
