package com.projetmobile.mobile.data.remote

import com.projetmobile.mobile.data.entity.ReservationDashboardRowEntity

interface ReservationRepository {
    suspend fun getReservations(festivalId: Int): List<ReservationDashboardRowEntity>
}