package com.projetmobile.mobile.data.remote.reservation

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ReservationApiService {
    @GET("reservation/reservations/{festivalId}")
    suspend fun getReservationsByFestival(
        @Path("festivalId") festivalId: Int
    ): List<ReservationDashboardRowDto>

    @POST("reservation/reservation")
    suspend fun createReservation(@Body payload: ReservationCreatePayloadDto)

    @DELETE("reservation/reservation/{id}")
    suspend fun deleteReservation(@Path("id") id: Int)

    @GET("workflow/reservation/{reservationId}")
    suspend fun getWorkflowByReservationId(
        @Path("reservationId") reservationId: Int
    ): WorkflowDto

    // PUT /workflow/:id
    @PUT("workflow/{id}")
    suspend fun updateWorkflow(
        @Path("id") id: Int,
        @Body workflowData: WorkflowUpdatePayload
    ): WorkflowDto

    // POST /workflow/:id/contact
    @POST("workflow/{id}/contact")
    suspend fun addContactDate(
        @Path("id") id: Int
    ): List<String>

    @GET("reservation/detail/{reservationId}")
    suspend fun getReservationDetails(
        @Path("reservationId") reservationId: Int
    ): ReservationDetailsDto

    @PUT("reservation/reservation/{id}")
    suspend fun updateReservation(
        @Path("id") id: Int,
        @Body payload: ReservationUpdatePayloadDto
    )

    @GET("zones-tarifaires/{festivalId}")
    suspend fun getZonesTarifaires(
        @Path("festivalId") festivalId: Int
    ): List<ZoneTarifaireDto>
}
