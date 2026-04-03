package com.projetmobile.mobile.data.entity.admin

data class AdminUserUpdateInput(
    val login: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val role: String? = null,
    val emailVerified: Boolean? = null,
)
