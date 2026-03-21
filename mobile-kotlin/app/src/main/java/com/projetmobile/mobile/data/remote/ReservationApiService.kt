package com.projetmobile.mobile.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface ReservationApiService {
    @GET("/api/reservation/reservations/{festivalId}")
    suspend fun getReservationsByFestival(
        @Path("festivalId") festivalId: Int
    ): List<ReservationDashboardRowDto>
}