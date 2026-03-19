package com.projetmobile.mobile.data.entity.auth

data class AuthUser(
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
