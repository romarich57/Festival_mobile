package com.projetmobile.mobile.data.remote.games

import com.projetmobile.mobile.data.entity.games.GameDraft
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class GamesPageResponseDto(
    val items: List<GameDto>,
    val pagination: GamesPaginationDto,
)

@Serializable
data class GamesPaginationDto(
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int,
    val sortBy: String,
    val sortOrder: String,
)

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

@Serializable
data class MechanismDto(
    val id: Int,
    val name: String,
    val description: String? = null,
)

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

@Serializable
data class DeleteGameResponseDto(
    val message: String,
)

@Serializable
data class UploadGameImageResponseDto(
    val url: String,
    val message: String,
)

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

private fun Int?.toJsonElement(): JsonElement {
    return this?.let(::JsonPrimitive) ?: JsonNull
}

private fun String?.toTrimmedJsonElement(): JsonElement {
    val trimmedValue = this?.trim()?.takeIf { value -> value.isNotEmpty() }
    return trimmedValue?.let(::JsonPrimitive) ?: JsonNull
}
