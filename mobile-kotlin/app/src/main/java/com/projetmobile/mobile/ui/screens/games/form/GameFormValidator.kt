package com.projetmobile.mobile.ui.screens.games

internal data class GameFormValidationResult(
    val fields: GameFormFields,
    val isValid: Boolean,
)

internal interface GameFormValidator {
    fun validate(fields: GameFormFields): GameFormValidationResult
}

internal class DefaultGameFormValidator : GameFormValidator {
    override fun validate(fields: GameFormFields): GameFormValidationResult {
        var nextFields = fields.copy(
            titleError = null,
            typeError = null,
            editorError = null,
            minAgeError = null,
            authorsError = null,
            minPlayersError = null,
            maxPlayersError = null,
            durationMinutesError = null,
        )
        var isValid = true

        if (fields.title.trim().isEmpty()) {
            nextFields = nextFields.copy(titleError = "Le titre est requis.")
            isValid = false
        }
        if (fields.type.trim().isEmpty()) {
            nextFields = nextFields.copy(typeError = "Le type est requis.")
            isValid = false
        }
        if (fields.editorId == null) {
            nextFields = nextFields.copy(editorError = "L'éditeur est requis.")
            isValid = false
        }
        if (fields.minAgeInput.toIntOrNull() == null) {
            nextFields = nextFields.copy(minAgeError = "L'âge minimum est requis.")
            isValid = false
        }
        if (fields.authors.trim().isEmpty()) {
            nextFields = nextFields.copy(authorsError = "Les auteurs sont requis.")
            isValid = false
        }

        val minPlayers = fields.minPlayersInput.toIntOrNull()
        val maxPlayers = fields.maxPlayersInput.toIntOrNull()
        val durationMinutes = fields.durationMinutesInput.toIntOrNull()

        if (fields.minPlayersInput.isNotBlank() && minPlayers == null) {
            nextFields = nextFields.copy(minPlayersError = "Valeur invalide.")
            isValid = false
        }
        if (fields.maxPlayersInput.isNotBlank() && maxPlayers == null) {
            nextFields = nextFields.copy(maxPlayersError = "Valeur invalide.")
            isValid = false
        }
        if (minPlayers != null && minPlayers < 1) {
            nextFields = nextFields.copy(minPlayersError = "Minimum 1 joueur.")
            isValid = false
        }
        if (maxPlayers != null && maxPlayers < 1) {
            nextFields = nextFields.copy(maxPlayersError = "Minimum 1 joueur.")
            isValid = false
        }
        if (minPlayers != null && maxPlayers != null && minPlayers > maxPlayers) {
            nextFields = nextFields.copy(maxPlayersError = "Le max doit être supérieur ou égal au min.")
            isValid = false
        }
        if (fields.durationMinutesInput.isNotBlank() && durationMinutes == null) {
            nextFields = nextFields.copy(durationMinutesError = "Valeur invalide.")
            isValid = false
        }
        if (durationMinutes != null && durationMinutes < 0) {
            nextFields = nextFields.copy(durationMinutesError = "La durée doit être positive.")
            isValid = false
        }

        return GameFormValidationResult(
            fields = nextFields,
            isValid = isValid,
        )
    }
}
