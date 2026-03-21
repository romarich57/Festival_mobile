package com.projetmobile.mobile.ui.screens.games

import com.projetmobile.mobile.data.entity.games.GameDetail

internal interface GameFormPrefillMapper {
    fun toFields(game: GameDetail): GameFormFields
}

internal class DefaultGameFormPrefillMapper : GameFormPrefillMapper {
    override fun toFields(game: GameDetail): GameFormFields {
        return GameFormFields(
            title = game.title,
            type = game.type,
            editorId = game.editorId,
            minAgeInput = game.minAge.toString(),
            authors = game.authors,
            minPlayersInput = game.minPlayers?.toString().orEmpty(),
            maxPlayersInput = game.maxPlayers?.toString().orEmpty(),
            durationMinutesInput = game.durationMinutes?.toString().orEmpty(),
            prototype = game.prototype,
            theme = game.theme.orEmpty(),
            description = game.description.orEmpty(),
            imageUrl = game.imageUrl.orEmpty(),
            rulesVideoUrl = game.rulesVideoUrl.orEmpty(),
            selectedMechanismIds = game.mechanisms.mapTo(linkedSetOf()) { it.id },
        )
    }
}
