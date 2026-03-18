package com.projetmobile.mobile.data.remote.auth

import com.projetmobile.mobile.data.remote.auth.CurrentUserResponseDto
import com.projetmobile.mobile.data.remote.auth.ForgotPasswordRequestDto
import com.projetmobile.mobile.data.remote.auth.LoginRequestDto
import com.projetmobile.mobile.data.remote.auth.LoginResponseDto
import com.projetmobile.mobile.data.remote.auth.MessageResponseDto
import com.projetmobile.mobile.data.remote.auth.RegisterRequestDto
import com.projetmobile.mobile.data.remote.auth.ResetPasswordRequestDto
import com.projetmobile.mobile.data.remote.auth.ResendVerificationRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): MessageResponseDto

    @POST("auth/resend-verification")
    suspend fun resendVerification(@Body request: ResendVerificationRequestDto): MessageResponseDto

    @POST("auth/password/forgot")
    suspend fun requestPasswordReset(@Body request: ForgotPasswordRequestDto): MessageResponseDto

    @POST("auth/password/reset")
    suspend fun resetPassword(@Body request: ResetPasswordRequestDto): MessageResponseDto

    @POST("auth/logout")
    suspend fun logout(): MessageResponseDto

    @GET("auth/whoami")
    suspend fun getCurrentUser(): CurrentUserResponseDto
}
