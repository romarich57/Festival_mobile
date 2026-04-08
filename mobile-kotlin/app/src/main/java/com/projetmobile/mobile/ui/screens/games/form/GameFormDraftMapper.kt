/**
 * Rôle : Mapper pour convertir le flux d'état du formulaire vers un objet réseau "Draft" Game.
 *
 * Précondition : Le formulaire doit être dans un état de validation de succès.
 *
 * Postcondition : Retourne la requête JSON/DTO attendue par le backend.
 */
package com.projetmobile.mobile.ui.screens.games

import com.projetmobile.mobile.data.entity.games.GameDraft

/**
 * Rôle : Définit le contrat du module les jeux formulaire.
 */
internal interface GameFormDraftMapper {
    /**
     * Rôle : Exécute l'action to draft du module les jeux formulaire.
     *
     * Précondition : La source à transformer ou à analyser doit être fournie.
     *
     * Postcondition : Le résultat retourné est normalisé et exploitable par l'UI.
     */
    fun toDraft(
        fields: GameFormFields,
        imageSourceMode: GameImageSourceMode,
        uploadedImageUrl: String?,
    ): GameDraft
}

/**
 * Rôle : Décrit le composant default jeu formulaire draft mapper du module les jeux formulaire.
 */
internal class DefaultGameFormDraftMapper : GameFormDraftMapper {
    /**
     * Rôle : Exécute l'action to draft du module les jeux formulaire.
     *
     * Précondition : La source à transformer ou à analyser doit être fournie.
     *
     * Postcondition : Le résultat retourné est normalisé et exploitable par l'UI.
     */
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
