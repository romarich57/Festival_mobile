package com.projetmobile.mobile.data.remote.games

import com.projetmobile.mobile.data.entity.games.GameDraft
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

/**
 * Rôle : Modélisation des Data Transfer Objects (DTO) requis par Retrofit 
 * pour faire transiter les JSON liés aux jeux et catalogues associés (Éditeurs, Mécaniques).
 *
 * Précondition : En lien avec l'API /games, /mechanisms, et /editors.
 * Postcondition : Garantissent la convertibilité JSON en entités fortement typées en Kotlin.
 */

/**
 * Rôle : Récipient global typé pour abriter une réponse paginée du catalogue de jeu.
 */
@Serializable
data class GamesPageResponseDto(
    val items: List<GameDto>,
    val pagination: GamesPaginationDto,
)

/** Rôle : Objet de contexte d'une page retournée (numéro de page, limites, nombre total disponible). */
@Serializable
data class GamesPaginationDto(
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int,
    val sortBy: String,
    val sortOrder: String,
)

/** Rôle : Encapsule la forme lue exhaustive d'un Jeu validé depuis l'API. */
@Serializable
data class GameDto(
    val id: Int,
    val title: String,
    val type: String,
    @SerialName("editor_id") val editorId: Int? = null,
    @SerialName("editor_name") val editorName: String? = null,
    @SerialName("min_age") val minAge: Int,
    val authors: String,
    @SerialName("min_players") val minPlayers: Int? = null,
    @SerialName("max_players") val maxPlayers: Int? = null,
    val prototype: Boolean = false,
    @SerialName("duration_minutes") val durationMinutes: Int? = null,
    val theme: String? = null,
    val description: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("rules_video_url") val rulesVideoUrl: String? = null,
    val mechanisms: List<MechanismDto> = emptyList(),
)

/** Rôle : Modélise structurellement un Mécanisme (tag) tel que formaté par le code base Backend. */
@Serializable
data class MechanismDto(
    val id: Int,
    val name: String,
    val description: String? = null,
)

/** Rôle : Modélise un point sur un professionnel de l'édition d'un jeu (`Editor`). */
@Serializable
data class EditorDto(
    val id: Int,
    val name: String,
    val email: String? = null,
    val website: String? = null,
    val description: String? = null,
    @SerialName("logo_url") val logoUrl: String? = null,
    @SerialName("is_exhibitor") val isExhibitor: Boolean = false,
    @SerialName("is_distributor") val isDistributor: Boolean = false,
)

/** Rôle : Constat de fin renvoyé par la requête de Destructuration en cascade. */
@Serializable
data class DeleteGameResponseDto(
    val message: String,
)

/** Rôle : Résultat API contenant la racine finalisée de l'image insérée distante. */
@Serializable
data class UploadGameImageResponseDto(
    val url: String,
    val message: String,
)

/**
 * Rôle : Spécification d'un paquet de données orienté `POST`/`PUT` du formulaire métier Jeu.
 * Utilise explicitement des `JsonElement` pour ne transmettre `NULL` que des données véritablement existantes.
 */
@Serializable
data class GameUpsertRequestDto(
    val title: String,
    val type: String,
    @SerialName("editor_id") val editorId: Int?,
    @SerialName("min_age") val minAge: Int?,
    val authors: String,
    @SerialName("min_players") val minPlayers: JsonElement? = null,
    @SerialName("max_players") val maxPlayers: JsonElement? = null,
    val prototype: Boolean = false,
    @SerialName("duration_minutes") val durationMinutes: JsonElement? = null,
    val theme: JsonElement? = null,
    val description: JsonElement? = null,
    @SerialName("image_url") val imageUrl: JsonElement? = null,
    @SerialName("rules_video_url") val rulesVideoUrl: JsonElement? = null,
    val mechanismIds: List<Int> = emptyList(),
)

/**
 * Rôle : Méthode d'extension isolant la transformation d'un Brouillon (Draft) local 
 * vers une requète brute serveur en s'assurant du nettoyage et formattage d'erreur.
 * 
 * Précondition : Draft d'interface pré-rempli.
 * Postcondition : Produit des valeurs saines via des `.trim()` successifs pour le formattage.
 */
internal fun GameDraft.toRequestDto(): GameUpsertRequestDto {
    return GameUpsertRequestDto(
        title = title.trim(),
        type = type.trim(),
        editorId = editorId,
        minAge = minAge,
        authors = authors.trim(),
        minPlayers = minPlayers.toJsonElement(),
        maxPlayers = maxPlayers.toJsonElement(),
        prototype = prototype,
        durationMinutes = durationMinutes.toJsonElement(),
        theme = theme.toTrimmedJsonElement(),
        description = description.toTrimmedJsonElement(),
        imageUrl = imageUrl.toTrimmedJsonElement(),
        rulesVideoUrl = rulesVideoUrl.toTrimmedJsonElement(),
        mechanismIds = mechanismIds.distinct(),
    )
}

/** Rôle : Mapper rudimentaire d'une valeur numérale vers un format supporté en Nullity-Check par kotlinx (JsonPrimitive ou JsonNull). */
private fun Int?.toJsonElement(): JsonElement {
    return this?.let(::JsonPrimitive) ?: JsonNull
}

/** Rôle : Mappe les `String?` tout en refusant les châînes de caractères complètement neutres (ex: "  ") pour les exclure du payload via Null. */
private fun String?.toTrimmedJsonElement(): JsonElement {
    val trimmedValue = this?.trim()?.takeIf { value -> value.isNotEmpty() }
    return trimmedValue?.let(::JsonPrimitive) ?: JsonNull
}
