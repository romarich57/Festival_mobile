package com.projetmobile.mobile.data.remote
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("health")
    suspend fun getHealth(): HealthResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}
