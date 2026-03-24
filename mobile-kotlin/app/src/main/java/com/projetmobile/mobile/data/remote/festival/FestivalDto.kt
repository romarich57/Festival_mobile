package com.projetmobile.mobile.data.remote.festival

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
// TODO :remarque a deplacer dans un dossier types mais plus tard afin d'eviter conflit
@Serializable
data class FestivalDto(
    val id: Int,
    val name: String,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String,
    @SerialName("stock_tables_standard") val stockTablesStandard: Int,
    @SerialName("stock_tables_grande") val stockTablesGrande: Int,
    @SerialName("stock_tables_mairie") val stockTablesMairie: Int,
    @SerialName("stock_chaises") val stockChaises: Int,
    @SerialName("prix_prises") val prixPrises: Double = 0.0,
){
    val totalTables: Int
        get() = stockTablesStandard + stockTablesGrande + stockTablesMairie
}