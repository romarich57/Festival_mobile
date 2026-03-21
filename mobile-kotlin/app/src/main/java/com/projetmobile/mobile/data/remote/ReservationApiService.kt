package com.projetmobile.mobile.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ReservationApiService {
    @GET("/api/reservation/reservations/{festivalId}")
    suspend fun getReservationsByFestival(
        @Path("festivalId") festivalId: Int
    ): List<ReservationDashboardRowDto>

    @POST("/api/reservation/reservation")
    suspend fun createReservation(@Body payload: ReservationCreatePayloadDto)

    @DELETE("reservation/reservation/{id}")
    suspend fun deleteReservation(@Path("id") id: Int)
}