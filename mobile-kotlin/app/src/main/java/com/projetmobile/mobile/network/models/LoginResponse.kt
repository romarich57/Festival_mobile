package com.projetmobile.mobile.network.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "id") val id: Int,
    @Json(name = "login") val login: String,
    @Json(name = "role") val role: String,
    @Json(name = "firstName") val firstName: String,
    @Json(name = "lastName") val lastName: String,
    @Json(name = "email") val email: String,
    @Json(name = "phone") val phone: String?,
    @Json(name = "avatarUrl") val avatarUrl: String?,
    @Json(name = "emailVerified") val emailVerified: Boolean,
    @Json(name = "createdAt") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    @Json(name = "message") val message: String,
    @Json(name = "user") val user: User
)
