package com.projetmobile.mobile.data.mapper

import com.projetmobile.mobile.data.remote.reservation.ReservationDashboardRowDto
import com.projetmobile.mobile.data.entity.ReservationDashboardRowEntity

fun ReservationDashboardRowDto.toEntity(): ReservationDashboardRowEntity {
    return ReservationDashboardRowEntity(
        id = this.id,
        // Si le backend renvoie null, on met une chaîne vide pour éviter les crashs
        reservantName = this.reservantName ?: "",
        reservantType = this.reservantType,
        workflowState = this.workflowState
    )
}