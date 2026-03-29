package com.projetmobile.mobile.data.repository.reservation

import com.projetmobile.mobile.data.entity.ReservationDashboardRowEntity
import com.projetmobile.mobile.data.remote.reservation.ReservationCreatePayloadDto
import com.projetmobile.mobile.data.remote.reservation.ReservationDetailsDto
import com.projetmobile.mobile.data.remote.reservation.ReservationUpdatePayloadDto
import com.projetmobile.mobile.data.remote.reservation.ZoneTarifaireDto

interface ReservationRepository {
    suspend fun getReservations(festivalId: Int): List<ReservationDashboardRowEntity>

    suspend fun createReservation(payload: ReservationCreatePayloadDto)

    suspend fun deleteReservation(festivalId: Int)

    suspend fun getReservationDetails(reservationId: Int): ReservationDetailsDto

    suspend fun updateReservation(reservationId: Int, payload: ReservationUpdatePayloadDto)

    suspend fun getZonesTarifaires(festivalId: Int): List<ZoneTarifaireDto>


}