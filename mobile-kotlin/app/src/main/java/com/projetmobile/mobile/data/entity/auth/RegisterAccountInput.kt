package com.projetmobile.mobile.data.entity.auth

data class RegisterAccountInput(
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val phone: String?,
)
