/**
 * Rôle : Transformer le DTO de jeu initialement reçu de l'API en configuration d'édition (State).
 *
 * Précondition : Recevoir le jeu via mode d'édition ou une trame vide.
 *
 * Postcondition : Le formulaire se charge proprement en mode draft.
 */
package com.projetmobile.mobile.ui.screens.games

import com.projetmobile.mobile.data.entity.games.GameDetail

/**
 * Rôle : Définit le contrat du module les jeux formulaire.
 */
internal interface GameFormPrefillMapper {
    /**
     * Rôle : Exécute l'action to champs du module les jeux formulaire.
     *
     * Précondition : La source à transformer ou à analyser doit être fournie.
     *
     * Postcondition : Le résultat retourné est normalisé et exploitable par l'UI.
     */
    fun toFields(game: GameDetail): GameFormFields
}

/**
 * Rôle : Décrit le composant default jeu formulaire prefill mapper du module les jeux formulaire.
 */
internal class DefaultGameFormPrefillMapper : GameFormPrefillMapper {
    /**
     * Rôle : Exécute l'action to champs du module les jeux formulaire.
     *
     * Précondition : La source à transformer ou à analyser doit être fournie.
     *
     * Postcondition : Le résultat retourné est normalisé et exploitable par l'UI.
     */
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
