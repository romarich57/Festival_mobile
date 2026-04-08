package com.projetmobile.mobile.data.remote.reservants

import com.projetmobile.mobile.data.entity.reservants.ReservantContactDraft
import com.projetmobile.mobile.data.entity.reservants.ReservantDraft
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Rôle : Fichier regroupant les "Data Transfer Objects" (DTOs) en rapport aux échanges 
 * sur la route des Réservants, ainsi que les méthodes mappant des instances de 
 * brouillons (Draft) depuis le domaine métier Kotlin vers les requêtes API (Dto).
 */

/**
 * Rôle : Modélise la réponse JSON de définition propre d'un Réservant retourné par l'API.
 * 
 * Précondition : Converti automatiquement de snake_case vers camelCase via `SerialName`.
 * Postcondition : Exploitable en Kotlin via le Repository.
 */
@Serializable
data class ReservantDto(
    val id: Int,
    val name: String,
    val email: String,
    val type: String,
    @SerialName("editor_id") val editorId: Int? = null,
    @SerialName("phone_number") val phoneNumber: String? = null,
    val address: String? = null,
    val siret: String? = null,
    val notes: String? = null,
)

/**
 * Rôle : Payload qui encapsule les champs obligatoires et facultatifs pour 
 * sauvegarder de façon permanente un réservant (Post / Put).
 */
@Serializable
data class ReservantUpsertRequestDto(
    val name: String,
    val email: String,
    val type: String,
    @SerialName("editor_id") val editorId: Int? = null,
    @SerialName("phone_number") val phoneNumber: String? = null,
    val address: String? = null,
    val siret: String? = null,
    val notes: String? = null,
)

/** Rôle : Modèle abstrait en réponse d'une commande Delete (e.g. { "message": "Deleted" }). */
@Serializable
data class DeleteReservantResponseDto(
    val message: String,
)

/** Rôle : Réceptionne l'information complète décrivant un point de contact sous-jacent. */
@Serializable
data class ReservantContactDto(
    val id: Int,
    val name: String,
    val email: String,
    @SerialName("phone_number") val phoneNumber: String,
    @SerialName("job_title") val jobTitle: String,
    val priority: Int,
)

/** Rôle : Payload assembant la requête d'ajout de point de contact. */
@Serializable
data class ReservantContactUpsertRequestDto(
    val name: String,
    val email: String,
    @SerialName("phone_number") val phoneNumber: String,
    @SerialName("job_title") val jobTitle: String,
    val priority: Int,
)

/**
 * Rôle : DTO du rapport d'incident : récapitule les entités qui vont mourir 
 * en cascade en cas de suppression forcée du Réservant.
 */
@Serializable
data class ReservantDeleteSummaryDto(
    @SerialName("reservant_id") val reservantId: Int,
    val contacts: List<ReservantDeleteContactSummaryDto> = emptyList(),
    val workflows: List<ReservantDeleteWorkflowSummaryDto> = emptyList(),
    val reservations: List<ReservantDeleteReservationSummaryDto> = emptyList(),
)

@Serializable
data class ReservantDeleteContactSummaryDto(
    val id: Int,
    val name: String,
    val email: String? = null,
)

@Serializable
data class ReservantDeleteWorkflowSummaryDto(
    val id: Int,
    @SerialName("festival_id") val festivalId: Int? = null,
    val state: String? = null,
    @SerialName("festival_name") val festivalName: String? = null,
)

@Serializable
data class ReservantDeleteReservationSummaryDto(
    val id: Int,
    @SerialName("festival_id") val festivalId: Int? = null,
    @SerialName("statut_paiement") val paymentStatus: String? = null,
    @SerialName("festival_name") val festivalName: String? = null,
    val relation: String? = null,
)

@Serializable
data class ReservantEditorDto(
    val id: Int,
    val name: String,
    val email: String? = null,
    val website: String? = null,
    val description: String? = null,
    @SerialName("logo_url") val logoUrl: String? = null,
    @SerialName("is_exhibitor") val isExhibitor: Boolean = false,
    @SerialName("is_distributor") val isDistributor: Boolean = false,
)

/**
 * Rôle : Convertit le cache ou brouillon métier ([ReservantDraft]) en un format
 * conforme pour une requête de synchronisation avec l'API ([ReservantUpsertRequestDto]).
 * 
 * Précondition : Extension Kotlin attachée à la classe métier `ReservantDraft`.
 * Postcondition : Assainit les libellés via un trim, cast en lowercase, et rend null les champs invalides.
 */
internal fun ReservantDraft.toRequestDto(): ReservantUpsertRequestDto {
    val normalizedType = type.trim().lowercase()
    return ReservantUpsertRequestDto(
        name = name.trim(),
        email = email.trim(),
        type = normalizedType,
        editorId = editorId.takeIf { normalizedType == "editeur" },
        phoneNumber = phoneNumber.trimmedOrNull(),
        address = address.trimmedOrNull(),
        siret = siret.trimmedOrNull(),
        notes = notes.trimmedOrNull(),
    )
}

/**
 * Rôle : Fomate de même un draft de contact métier vers un payload API pour l'envoi réseau.
 */
internal fun ReservantContactDraft.toRequestDto(): ReservantContactUpsertRequestDto {
    return ReservantContactUpsertRequestDto(
        name = name.trim(),
        email = email.trim(),
        phoneNumber = phoneNumber.trim(),
        jobTitle = jobTitle.trim(),
        priority = priority,
    )
}

/** 
 * Rôle : Méthode outil pour effacer le vide d'une String si elle est purement invisible.
 * Évite d'envoyer " " en backend pour l'annuler en "null".
 */
private fun String?.trimmedOrNull(): String? {
    return this?.trim()?.takeIf { value -> value.isNotEmpty() }
}
