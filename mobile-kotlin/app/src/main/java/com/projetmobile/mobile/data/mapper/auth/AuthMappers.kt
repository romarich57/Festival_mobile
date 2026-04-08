package com.projetmobile.mobile.data.mapper.auth

import com.projetmobile.mobile.data.entity.auth.AuthUser
import com.projetmobile.mobile.data.remote.auth.AuthUserDto

/**
 * Rôle : Mappe un DTO utilisateur provenant de l'API (`AuthUserDto`) vers un modèle de domaine d'authentification (`AuthUser`).
 * 
 * Précondition : Le DTO doit contenir les informations d'un utilisateur renvoyé par le backend.
 * Postcondition : Retourne l'entité de domaine `AuthUser` équivalente pour l'utilisation interne de l'application.
 */
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
