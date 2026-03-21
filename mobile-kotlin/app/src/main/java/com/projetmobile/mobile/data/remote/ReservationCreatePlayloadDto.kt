package com.projetmobile.mobile.data.remote

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@OptIn(ExperimentalSerializationApi::class) // Nécessaire pour @EncodeDefault
data class ReservationCreatePayloadDto(
    @SerialName("reservant_name") val reservantName: String,
    @SerialName("reservant_email") val reservantEmail: String,
    @SerialName("reservant_type") val reservantType: String,
    @SerialName("festival_id") val festivalId: Int,
    @EncodeDefault @SerialName("start_price") val startPrice: Double = 0.0,
    @EncodeDefault @SerialName("nb_prises") val nbPrises: Int = 0,
    @EncodeDefault @SerialName("final_price") val finalPrice: Double = 0.0
)