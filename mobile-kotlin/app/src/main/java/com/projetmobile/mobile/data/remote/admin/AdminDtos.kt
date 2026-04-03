package com.projetmobile.mobile.data.remote.admin

import com.projetmobile.mobile.data.remote.auth.AuthUserDto
import kotlinx.serialization.Serializable

@Serializable
data class AdminCreateUserRequestDto(
    val login: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String? = null,
    val role: String = "benevole",
)

@Serializable
data class AdminUpdateUserRequestDto(
    val login: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val role: String? = null,
    val emailVerified: Boolean? = null,
)

@Serializable
data class AdminCreateUserResponseDto(
    val message: String,
)

@Serializable
data class AdminUpdateUserResponseDto(
    val message: String,
    val user: AuthUserDto,
)

@Serializable
data class AdminDeleteUserResponseDto(
    val message: String,
)
