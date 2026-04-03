package com.projetmobile.mobile.data.entity.profile

import com.projetmobile.mobile.data.entity.auth.AuthUser

data class ProfileUpdateResult(
    val message: String,
    val user: AuthUser,
    val emailVerificationSent: Boolean,
)
