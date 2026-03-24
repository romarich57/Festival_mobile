package com.projetmobile.mobile.data.remote.festival

import com.projetmobile.mobile.data.remote.festival.FestivalDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FestivalApiService {
    @GET("festivals")
    suspend fun getFestivals(): List<FestivalDto>

    @POST("festivals")
    suspend fun addFestival(@Body festival: FestivalDto): FestivalDto
}
