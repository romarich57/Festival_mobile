package com.projetmobile.mobile.data.repository.reservation

import com.projetmobile.mobile.data.entity.ReservationDashboardRowEntity
import com.projetmobile.mobile.data.remote.reservation.ReservationCreatePayloadDto
import com.projetmobile.mobile.data.remote.reservation.WorkflowUpdatePayload

interface ReservationRepository {
    suspend fun getReservations(festivalId: Int): List<ReservationDashboardRowEntity>

    suspend fun createReservation(payload: ReservationCreatePayloadDto)

    suspend fun deleteReservation(festivalId: Int)


}