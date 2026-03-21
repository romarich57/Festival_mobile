package com.projetmobile.mobile.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class ReservationDashboardRowDto(
    @SerialName("id") val id: Int,
    @SerialName("reservant_name") val reservantName: String?, //le nom du reservant peut etre nul car le backend peut ne pas le fournir, on gère ça dans le mapper
    @SerialName("reservant_type") val reservantType: String,
    @SerialName("workflow_state") val workflowState: String
)
