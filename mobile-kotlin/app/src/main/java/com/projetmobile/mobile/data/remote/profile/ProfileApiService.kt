package com.projetmobile.mobile.data.remote.profile

import com.projetmobile.mobile.data.remote.auth.AuthUserDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

interface ProfileApiService {
    @GET("users/me")
    suspend fun getProfile(): AuthUserDto

    @PUT("users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequestDto): ProfileUpdateResponseDto

    @Multipart
    @POST("upload/avatar")
    suspend fun uploadAvatar(@Part avatar: MultipartBody.Part): UploadAvatarResponseDto
}
