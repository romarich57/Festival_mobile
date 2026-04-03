package com.projetmobile.mobile.ui.screens.games

import com.projetmobile.mobile.data.entity.games.GameDraft

internal interface GameFormDraftMapper {
    fun toDraft(
        fields: GameFormFields,
        imageSourceMode: GameImageSourceMode,
        uploadedImageUrl: String?,
    ): GameDraft
}

internal class DefaultGameFormDraftMapper : GameFormDraftMapper {
    override fun toDraft(
        fields: GameFormFields,
        imageSourceMode: GameImageSourceMode,
        uploadedImageUrl: String?,
    ): GameDraft {
        val resolvedImageUrl = when (imageSourceMode) {
            GameImageSourceMode.Url -> fields.imageUrl.trim().takeIf { it.isNotEmpty() }
            GameImageSourceMode.File -> uploadedImageUrl ?: fields.imageUrl.trim().takeIf { it.isNotEmpty() }
        }

        return GameDraft(
            title = fields.title,
            type = fields.type,
            editorId = fields.editorId,
            minAge = fields.minAgeInput.toIntOrNull(),
            authors = fields.authors,
            minPlayers = fields.minPlayersInput.toIntOrNull(),
            maxPlayers = fields.maxPlayersInput.toIntOrNull(),
            prototype = fields.prototype,
            durationMinutes = fields.durationMinutesInput.toIntOrNull(),
            theme = fields.theme,
            description = fields.description,
            imageUrl = resolvedImageUrl,
            rulesVideoUrl = fields.rulesVideoUrl.trim().takeIf { it.isNotEmpty() },
            mechanismIds = fields.selectedMechanismIds.toList(),
        )
    }
}
