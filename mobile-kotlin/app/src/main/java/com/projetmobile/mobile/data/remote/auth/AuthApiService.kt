package com.projetmobile.mobile.data.remote.auth

import com.projetmobile.mobile.data.remote.auth.CurrentUserResponseDto
import com.projetmobile.mobile.data.remote.auth.ForgotPasswordRequestDto
import com.projetmobile.mobile.data.remote.auth.LoginRequestDto
import com.projetmobile.mobile.data.remote.auth.LoginResponseDto
import com.projetmobile.mobile.data.remote.auth.MessageResponseDto
import com.projetmobile.mobile.data.remote.auth.RegisterRequestDto
import com.projetmobile.mobile.data.remote.auth.ResetPasswordRequestDto
import com.projetmobile.mobile.data.remote.auth.ResendVerificationRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Rôle : Interface Retrofit qui définit l'ensemble des endpoints liés à l'authentification 
 * d'un utilisateur vis-à-vis du backend (login, création de compte, gestion du mot de passe...).
 * 
 * Précondition : Retrofit doit être correctement initialisé et le cookie jar doit être en
 * place pour la conservation d'une session (Refresh Tokens en l'occurrence).
 * Postcondition : Offre des coroutines `suspend functions` permettant un accès asynchrone bas-niveau HTTP.
 */
interface AuthApiService {

    /**
     * Rôle : S'identifie auprès de l'API avec un nom/alias (ou email) et mot de passe.
     * 
     * Précondition : Un corps de requête JSON formatté en [LoginRequestDto].
     * Postcondition : Retourne la réponse de base du backend contenant un utilisateur [LoginResponseDto] et place un cookie HTTP.
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    /**
     * Rôle : Enregistre un nouveau profil utilisateur côté serveur (Inscription).
     * 
     * Précondition : Complétion validée et format JSON type [RegisterRequestDto].
     * Postcondition : Renvoie un message de succès (ou d'envoi d'email de validation) sous forme de [MessageResponseDto].
     */
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): MessageResponseDto

    /**
     * Rôle : Déclenche l'envoi d'un nouvel e-mail de confirmation d'adresse.
     * 
     * Précondition : Fournir l'adresse email associée au compte en [ResendVerificationRequestDto].
     * Postcondition : Provoque l'envoi SMTP asynchrone par le serveur.
     */
    @POST("auth/resend-verification")
    suspend fun resendVerification(@Body request: ResendVerificationRequestDto): MessageResponseDto

    /**
     * Rôle : Formule une requête d'oubli de mot de passe à l'API.
     * 
     * Précondition : Format d'email valide envoyé dans l'enveloppe [ForgotPasswordRequestDto].
     * Postcondition : Informe d'un succès potentiel sans lever de fuite d'information sur la validité de l'email.
     */
    @POST("auth/password/forgot")
    suspend fun requestPasswordReset(@Body request: ForgotPasswordRequestDto): MessageResponseDto

    /**
     * Rôle : Demande l'association du nouveau mot de passe grâce à la validation d'un lien recu.
     * 
     * Précondition : Inclut le Token d'authentification temporaire depuis l'email via URL, encapsulé dans [ResetPasswordRequestDto].
     * Postcondition : Valide l'échange de mot de passe.
     */
    @POST("auth/password/reset")
    suspend fun resetPassword(@Body request: ResetPasswordRequestDto): MessageResponseDto

    /**
     * Rôle : Invalide instantanément le jeton côté serveur et demande l'expiration du ou des cookies de session.
     * 
     * Précondition : S'exécuter avec un cookie encore rattaché pour être autorisé.
     * Postcondition : Clôture sécurisée de la session web de l'utilisateur actif.
     */
    @POST("auth/logout")
    suspend fun logout(): MessageResponseDto

    /**
     * Rôle : Questionne l'API sur l'état authentifié de l'appelant actuel (ping de session).
     * 
     * Précondition : Le cookie JWT d'autorisation valide doit être obligatoirement injecté.
     * Postcondition : Retourne l'entité complétement actualisée de l'utilisateur ou une exception HTTP 401 si expiré ou nul.
     */
    @GET("auth/whoami")
    suspend fun getCurrentUser(): CurrentUserResponseDto
}
