package com.projetmobile.mobile.data.remote.reservation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReservationDashboardRowDto(
    @SerialName("id") val id: Int,
    @SerialName("reservant_name") val reservantName: String?,
    @SerialName("reservant_type") val reservantType: String,
    @SerialName("workflow_state") val workflowState: String
)