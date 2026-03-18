package com.projetmobile.mobile.data.remote.auth

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequestDto(
    @Json(name = "identifier") val identifier: String,
    @Json(name = "password") val password: String,
)

@JsonClass(generateAdapter = true)
data class RegisterRequestDto(
    @Json(name = "login") val login: String,
    @Json(name = "firstName") val firstName: String,
    @Json(name = "lastName") val lastName: String,
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "phone") val phone: String?,
)

@JsonClass(generateAdapter = true)
data class ResendVerificationRequestDto(
    @Json(name = "email") val email: String,
)

@JsonClass(generateAdapter = true)
data class ForgotPasswordRequestDto(
    @Json(name = "email") val email: String,
)

@JsonClass(generateAdapter = true)
data class ResetPasswordRequestDto(
    @Json(name = "token") val token: String,
    @Json(name = "password") val password: String,
)

@JsonClass(generateAdapter = true)
data class AuthUserDto(
    @Json(name = "id") val id: Int,
    @Json(name = "login") val login: String,
    @Json(name = "role") val role: String,
    @Json(name = "firstName") val firstName: String,
    @Json(name = "lastName") val lastName: String,
    @Json(name = "email") val email: String,
    @Json(name = "phone") val phone: String?,
    @Json(name = "avatarUrl") val avatarUrl: String?,
    @Json(name = "emailVerified") val emailVerified: Boolean,
    @Json(name = "createdAt") val createdAt: String,
)

@JsonClass(generateAdapter = true)
data class LoginResponseDto(
    @Json(name = "message") val message: String,
    @Json(name = "user") val user: AuthUserDto,
)

@JsonClass(generateAdapter = true)
data class CurrentUserResponseDto(
    @Json(name = "user") val user: AuthUserDto,
)

@JsonClass(generateAdapter = true)
data class MessageResponseDto(
    @Json(name = "message") val message: String,
)
