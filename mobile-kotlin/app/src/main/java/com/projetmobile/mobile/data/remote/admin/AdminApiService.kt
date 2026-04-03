package com.projetmobile.mobile.data.remote.admin

import com.projetmobile.mobile.data.remote.auth.AuthUserDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AdminApiService {
    @GET("users")
    suspend fun getUsers(): List<AuthUserDto>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Int): AuthUserDto

    @POST("users")
    suspend fun createUser(@Body request: AdminCreateUserRequestDto): AdminCreateUserResponseDto

    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body request: AdminUpdateUserRequestDto,
    ): AdminUpdateUserResponseDto

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): AdminDeleteUserResponseDto
}
