package com.projetmobile.mobile.data.remote.reservants

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ReservantsApiService {
    @GET("reservant")
    suspend fun getReservants(): List<ReservantDto>

    @GET("reservant/{id}")
    suspend fun getReservant(@Path("id") reservantId: Int): ReservantDto

    @POST("reservant")
    suspend fun createReservant(@Body request: ReservantUpsertRequestDto): ReservantDto

    @PUT("reservant/{id}")
    suspend fun updateReservant(
        @Path("id") reservantId: Int,
        @Body request: ReservantUpsertRequestDto,
    ): ReservantDto

    @DELETE("reservant/{id}")
    suspend fun deleteReservant(@Path("id") reservantId: Int): DeleteReservantResponseDto

    @GET("reservant/{id}/delete-summary")
    suspend fun getDeleteSummary(@Path("id") reservantId: Int): ReservantDeleteSummaryDto

    @GET("reservant/{id}/contacts")
    suspend fun getContacts(@Path("id") reservantId: Int): List<ReservantContactDto>

    @POST("reservant/{id}/contacts")
    suspend fun addContact(
        @Path("id") reservantId: Int,
        @Body request: ReservantContactUpsertRequestDto,
    ): ReservantContactDto

    @GET("editors")
    suspend fun getEditors(): List<ReservantEditorDto>
}
