package com.projetmobile.mobile.data.repository.profile

import com.projetmobile.mobile.data.entity.auth.AuthUser
import com.projetmobile.mobile.data.entity.profile.AvatarUploadResult
import com.projetmobile.mobile.data.entity.profile.ProfileUpdateInput
import com.projetmobile.mobile.data.entity.profile.ProfileUpdateResult

/**
 * Rôle : Contrat dictant la gestion du profil personnel de l'utilisateur connecté.
 * 
 * Précondition : Autorisation requise (token d'authentification valide dans l'intercepteur).
 * Postcondition : Opère les mises à jour (Update/Avatar) et lectures (Get).
 */
interface ProfileRepository {
    
    /**
     * Rôle : Demander au serveur l'état actuel de son propre compte user.
     * 
     * Précondition : Session logguée.
     * Postcondition : Fournit l'objet métier de la personne identifiée.
     */
    suspend fun getProfile(): Result<AuthUser>

    /**
     * Rôle : Pousser des altérations de champs personnels (nom, mot de passe...).
     * 
     * Précondition : Fiche profil valide sans incohérences.
     * Postcondition : Réponse confirmant le succès de la mise à jour SQL distante.
     */
    suspend fun updateProfile(input: ProfileUpdateInput): Result<ProfileUpdateResult>

    /**
     * Rôle : Action d'envoi multipart pour changer la photo de profil (Avatar).
     * 
     * Précondition : Fichier binaire correctement résolu (ByteArray et mimeType).
     * Postcondition : Le backend stocke l'image et renvoie l'URL publique de l'avatar.
     */
    suspend fun uploadAvatar(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ): Result<AvatarUploadResult>

    /**
     * Rôle : Exiger un email de restauration de mot de passe (si l'utilisateur 
     * l'initie depuis l'intérieur du profil).
     * 
     * Précondition : Fournir l'email utilisé pour le profil.
     * Postcondition : Renvoie un texte de confirmation.
     */
    suspend fun requestPasswordReset(email: String): Result<String>
}
