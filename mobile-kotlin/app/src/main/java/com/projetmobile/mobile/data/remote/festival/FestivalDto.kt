package com.projetmobile.mobile.data.remote.festival

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FestivalDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "start_date") val startDate: String,
    @Json(name = "end_date") val endDate: String,
    @Json(name = "stock_tables_standard") val stockTablesStandard: Int,
    @Json(name = "stock_tables_grande") val stockTablesGrande: Int,
    @Json(name = "stock_tables_mairie") val stockTablesMairie: Int,
    @Json(name = "stock_chaises") val stockChaises: Int,
    @Json(name = "prix_prises") val prixPrises: Double = 0.0,
)
