package com.projetmobile.mobile.data.mapper.auth

import com.projetmobile.mobile.data.entity.auth.AuthUser
import com.projetmobile.mobile.data.remote.auth.AuthUserDto

fun AuthUserDto.toAuthUser(): AuthUser = AuthUser(
    id = id,
    login = login,
    role = role,
    firstName = firstName,
    lastName = lastName,
    email = email,
    phone = phone,
    avatarUrl = avatarUrl,
    emailVerified = emailVerified,
    createdAt = createdAt,
)
