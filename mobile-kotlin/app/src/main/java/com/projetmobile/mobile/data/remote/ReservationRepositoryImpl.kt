package com.projetmobile.mobile.data.remote

import com.projetmobile.mobile.data.entity.ReservationDashboardRowEntity
import com.projetmobile.mobile.data.mapper.toEntity

class ReservationRepositoryImpl(
    private val api: ReservationApiService
) : ReservationRepository {

    override suspend fun getReservations(festivalId: Int): List<ReservationDashboardRowEntity> {
        // Appelle l'API, récupère les DTOs, et les transforme en Entities pour que le reste de l'app puisse les utiliser sans se soucier du format de l'API
        return api.getReservationsByFestival(festivalId).map { it.toEntity() }
    }
}