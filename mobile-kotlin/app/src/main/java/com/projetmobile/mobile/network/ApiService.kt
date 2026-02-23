package com.projetmobile.mobile.network

import com.projetmobile.mobile.network.models.LoginRequest
import com.projetmobile.mobile.network.models.LoginResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("health")
    suspend fun getHealth(): HealthResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}
