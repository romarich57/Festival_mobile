package com.projetmobile.mobile.data.remote.auth

import kotlinx.serialization.Serializable

/**
 * Rôle : Ensemble des "Data Transfer Objects" (DTO) servant de contrats de structure 
 * entre le client Android et le serveur backend pour le domaine d'authentification.
 * Chacun est annoté @Serializable pour que le parseur JSON de Retrofit (Kotlinx.Serialization) 
 * puisse faire la conversion objet <-> texte.
 */

/**
 * Rôle : Modèle de la requête d'identification HTTP (Body de requête d'entrée)
 * 
 * Précondition : `identifier` doit correspondre à l'email ou au login choisi.
 * Postcondition : Modèle de données formatté pour une demande de token depuis l'API.
 */
@Serializable
data class LoginRequestDto(
    val identifier: String,
    val password: String,
)

/**
 * Rôle : Modèle pour l'inscription d'un nouvel utilisateur côté frontend.
 * 
 * Précondition : Champs de formulaire validés en local par l'UI.
 * Postcondition : Pris en entrée pour un endpoint HTTP Création (`register`).
 */
@Serializable
data class RegisterRequestDto(
    val login: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val phone: String?,
)

/** Rôle : Objet de requête JSON simple pour relancer la confirmation de mail. */
@Serializable
data class ResendVerificationRequestDto(
    val email: String,
)

/** Rôle : Conteneur minimal d'e-mail pour déclencher l'action de mot de passe perdu par Retrofit. */
@Serializable
data class ForgotPasswordRequestDto(
    val email: String,
)

/** Rôle : Combine le token de remplacement cryptographique (lien du mail) et la passe finale utilisateur. */
@Serializable
data class ResetPasswordRequestDto(
    val token: String,
    val password: String,
)

/**
 * Rôle : Format complet décrivant la représentation réseau d'un utilisateur identifié 
 * que l'API est susceptible de renvoyer.
 * 
 * Précondition : Utilisateur actif récupéré.
 * Postcondition : Se verra mis en cache en interne par le système Android.
 */
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

/**
 * Rôle : Corps de réponse type suite à un @POST Login.
 * 
 * Précondition : Une connexion couronnée de succès.
 * Postcondition : Apporte un profil `user` ainsi qu'un mot de passe / message symbolique.
 */
@Serializable
data class LoginResponseDto(
    val message: String,
    val user: AuthUserDto,
)

/** Rôle : Structure de packaging pour contenir le profil retourné lors d'une route `whoami` ou auto-connexion. */
@Serializable
data class CurrentUserResponseDto(
    val user: AuthUserDto,
)

/**
 * Rôle : Formes de réponses string générique du backend pour valider un succès visuellement.
 * Ex : "Email envoyé", "Mot de passe réinitialisé".
 */
@Serializable
data class MessageResponseDto(
    val message: String,
)
