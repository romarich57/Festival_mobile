package com.projetmobile.mobile.data.remote.reservation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ZoneTarifaireDto(
    val id: Int,
    val name: String,
    @SerialName("festival_id") val festivalId: Int,
    @SerialName("nb_tables") val nbTables: Int,
    @SerialName("price_per_table") val pricePerTable: Double,
    @SerialName("nb_tables_available") val nbTablesAvailable: Int,
    @SerialName("m2_price") val m2Price: Double,
)

@Serializable
data class ReservationZoneTarifaireDto(
    @SerialName("zone_tarifaire_id") val zoneTarifaireId: Int,
    @SerialName("nb_tables_reservees") val nbTablesReservees: Int,
    @SerialName("nb_chaises_reservees") val nbChaisesReservees: Int = 0,
    @SerialName("zone_name") val zoneName: String = "",
    @SerialName("price_per_table") val pricePerTable: Double = 0.0,
    @SerialName("nb_tables_available") val nbTablesAvailable: Int = 0,
)

@Serializable
data class ReservationDetailsDto(
    val id: Int,
    @SerialName("festival_id") val festivalId: Int,
    @SerialName("start_price") val startPrice: Double = 0.0,
    @SerialName("table_discount_offered") val tableDiscountOffered: Double = 0.0,
    @SerialName("direct_discount") val directDiscount: Double = 0.0,
    @SerialName("nb_prises") val nbPrises: Int = 0,
    @SerialName("final_price") val finalPrice: Double = 0.0,
    val note: String? = null,
    @SerialName("zones_tarifaires") val zonesTarifaires: List<ReservationZoneTarifaireDto> = emptyList(),
)

@Serializable
data class ReservationZoneUpdateDto(
    @SerialName("zone_tarifaire_id") val zoneTarifaireId: Int,
    @SerialName("nb_tables_reservees") val nbTablesReservees: Int,
    @SerialName("nb_chaises_reservees") val nbChaisesReservees: Int = 0,
)

@Serializable
data class ReservationUpdatePayloadDto(
    @SerialName("start_price") val startPrice: Double,
    @SerialName("nb_prises") val nbPrises: Int,
    @SerialName("final_price") val finalPrice: Double,
    @SerialName("table_discount_offered") val tableDiscountOffered: Double,
    @SerialName("direct_discount") val directDiscount: Double,
    val note: String? = null,
    @SerialName("zones_tarifaires") val zonesTarifaires: List<ReservationZoneUpdateDto> = emptyList(),
)
