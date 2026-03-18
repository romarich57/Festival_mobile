package com.projetmobile.mobile.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReservationCreatePayloadDto(
    @SerialName("reservant_name") val reservantName: String,
    @SerialName("reservant_email") val reservantEmail: String,
    @SerialName("reservant_type") val reservantType: String,
    @SerialName("festival_id") val festivalId: Int,
    @SerialName("start_price") val startPrice: Double = 0.0,
    @SerialName("nb_prises") val nbPrises: Int = 0,
    @SerialName("final_price") val finalPrice: Double = 0.0
)