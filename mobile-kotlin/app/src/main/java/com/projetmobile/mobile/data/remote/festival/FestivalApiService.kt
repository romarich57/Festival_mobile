package com.projetmobile.mobile.data.remote.festival

import com.projetmobile.mobile.data.remote.festival.FestivalDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FestivalApiService {
    @GET("festivals")
    suspend fun getFestivals(): List<FestivalDto>

    @GET("festivals/{id}")
    suspend fun getFestival(@Path("id") id: Int): FestivalDto

    @POST("festivals")
    suspend fun addFestival(@Body festival: FestivalDto): FestivalDto

    @DELETE("festivals/{id}")
    suspend fun deleteFestival(@Path("id") id: Int)
}
