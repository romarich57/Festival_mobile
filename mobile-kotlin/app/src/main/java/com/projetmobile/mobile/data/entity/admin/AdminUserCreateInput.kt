package com.projetmobile.mobile.data.entity.admin

data class AdminUserCreateInput(
    val login: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String? = null,
    val role: String = "benevole",
)
