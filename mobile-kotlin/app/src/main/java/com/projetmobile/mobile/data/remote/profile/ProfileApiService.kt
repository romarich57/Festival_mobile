package com.projetmobile.mobile.data.remote.profile

import com.projetmobile.mobile.data.remote.auth.AuthUserDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

/**
 * Rôle : Interface de contrôle Retrofit relative aux propriétés administratives d'un profil 
 * existant (accès au données personnelles et mise à jour d'avatar).
 * 
 * Précondition : Un JWT actif et valide dans le gestionnaire de cookies de la requête.
 * Postcondition : Transmet à l'API les modifications profil demandées par l'usager.
 */
interface ProfileApiService {

    /**
     * Rôle : Obtenir la définition certifiée par le cloud du profil actuel.
     * 
     * Précondition : Authentifié.
     * Postcondition : Renvoie un objet [AuthUserDto] identique à l'enregistrement en base de données.
     */
    @GET("users/me")
    suspend fun getProfile(): AuthUserDto

    /**
     * Rôle : Demander des changements de propriétés métiers de l'utilisateur actif (prénom, tel...).
     * 
     * Précondition : Requête validée avec champs null et/ou modifiés.
     * Postcondition : Retour d'état confirmant les modifications validées (notamment demande d'e-mail).
     */
    @PUT("users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequestDto): ProfileUpdateResponseDto

    /**
     * Rôle : Procédure spécialisée pour injecter la photo/avatar en multi-part files.
     * 
     * Précondition : Une image mise en forme (`MultipartBody.Part`).
     * Postcondition : Renvoie l'URL publique de la l'image validée ([UploadAvatarResponseDto]).
     */
    @Multipart
    @POST("upload/avatar")
    suspend fun uploadAvatar(@Part avatar: MultipartBody.Part): UploadAvatarResponseDto
}
