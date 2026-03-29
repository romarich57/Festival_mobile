package com.projetmobile.mobile.data.repository.reservation

import com.projetmobile.mobile.data.entity.ReservationDashboardRowEntity
import com.projetmobile.mobile.data.mapper.toEntity
import com.projetmobile.mobile.data.remote.reservation.ReservationApiService
import com.projetmobile.mobile.data.remote.reservation.ReservationCreatePayloadDto
import com.projetmobile.mobile.data.remote.reservation.ReservationDetailsDto
import com.projetmobile.mobile.data.remote.reservation.ReservationUpdatePayloadDto
import com.projetmobile.mobile.data.remote.reservation.ZoneTarifaireDto

class ReservationRepositoryImpl(
    private val api: ReservationApiService
) : ReservationRepository {

    override suspend fun getReservations(festivalId: Int): List<ReservationDashboardRowEntity> {
        // Appelle l'API, récupère les DTOs, et les transforme en Entities pour que le reste de l'app puisse les utiliser sans se soucier du format de l'API
        return api.getReservationsByFestival(festivalId).map { it.toEntity() }
    }

    //on appelle l'API pour créer une réservation, pas besoin de faire de mapping ici car on envoie juste les données du payload
    override suspend fun createReservation(payload: ReservationCreatePayloadDto) {
        api.createReservation(payload)
    }

    override suspend fun deleteReservation(reservationId: Int) {
        api.deleteReservation(reservationId)
    }

    override suspend fun getReservationDetails(reservationId: Int): ReservationDetailsDto {
        return api.getReservationDetails(reservationId)
    }

    override suspend fun updateReservation(reservationId: Int, payload: ReservationUpdatePayloadDto) {
        api.updateReservation(reservationId, payload)
    }

    override suspend fun getZonesTarifaires(festivalId: Int): List<ZoneTarifaireDto> {
        return api.getZonesTarifaires(festivalId)
    }




}