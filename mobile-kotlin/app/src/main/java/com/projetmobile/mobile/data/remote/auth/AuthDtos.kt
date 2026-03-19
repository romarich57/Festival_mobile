package com.projetmobile.mobile.data.remote.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val identifier: String,
    val password: String,
)

@Serializable
data class RegisterRequestDto(
    val login: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val phone: String?,
)

@Serializable
data class ResendVerificationRequestDto(
    val email: String,
)

@Serializable
data class ForgotPasswordRequestDto(
    val email: String,
)

@Serializable
data class ResetPasswordRequestDto(
    val token: String,
    val password: String,
)

@Serializable
data class AuthUserDto(
    val id: Int,
    val login: String,
    val role: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String?,
    val avatarUrl: String?,
    val emailVerified: Boolean,
    val createdAt: String,
)

@Serializable
data class LoginResponseDto(
    val message: String,
    val user: AuthUserDto,
)

@Serializable
data class CurrentUserResponseDto(
    val user: AuthUserDto,
)

@Serializable
data class MessageResponseDto(
    val message: String,
)
