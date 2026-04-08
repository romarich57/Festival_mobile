package com.projetmobile.mobile.data.remote.profile

import com.projetmobile.mobile.data.entity.profile.OptionalField
import com.projetmobile.mobile.data.entity.profile.ProfileUpdateInput
import com.projetmobile.mobile.data.remote.auth.AuthUserDto
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

/**
 * Rôle : Collection de classes et fonctions DTO se chargeant du formatage des données JSON API
 * pour l'édition de Profil.
 * 
 * Précondition : Appartenant au namespace backend lié aux requêtes `users/`.
 * Postcondition : Transforme en formats compréhensibles par les Endpoints API.
 */

/**
 * Rôle : DTO du body envoyé pour les modifications d'un profil. Utilise `JsonElement` 
 * afin de distinguer un champ manquant d'un champ délibérément `null`.
 */
@Serializable
data class UpdateProfileRequestDto(
    val login: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phone: JsonElement? = null,
    val avatarUrl: JsonElement? = null,
)

/** Rôle : Objet de réponse suite à une édition reussie. Précise si une vérification email auto a été lancée. */
@Serializable
data class ProfileUpdateResponseDto(
    val message: String,
    val user: AuthUserDto,
    val emailVerificationSent: Boolean,
)

/** Rôle : Retour d'un upload de fichier d'avatar contenant son URL distant. */
@Serializable
data class UploadAvatarResponseDto(
    val url: String,
    val message: String,
)

/**
 * Rôle : Interprète et traduit l'objet Input depuis la vue vers la classe Retrofit DTO.
 * 
 * Précondition : Saisie validée sur le ViewModel Profile.
 * Postcondition : Uniquement le différentiel sera envoyé avec conversion en objets Json.
 */
internal fun ProfileUpdateInput.toDto(): UpdateProfileRequestDto {
    return UpdateProfileRequestDto(
        login = login,
        firstName = firstName,
        lastName = lastName,
        email = email,
        phone = phone.toJsonElement(),
        avatarUrl = avatarUrl.toJsonElement(),
    )
}

/**
 * Rôle : Traduction spécifique vers la sur-couche `JsonElement` d'un OptionalField
 * qui indique un statut "Ne pas changer" (absent du JSON) vs "Vider le champ".
 */
private fun OptionalField<String?>.toJsonElement(): JsonElement? {
    return when (this) {
        OptionalField.Unchanged -> null
        is OptionalField.Value -> value?.let(::JsonPrimitive) ?: JsonNull
    }
}
