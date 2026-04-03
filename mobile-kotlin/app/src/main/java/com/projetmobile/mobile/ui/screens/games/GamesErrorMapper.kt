package com.projetmobile.mobile.ui.screens.games

import com.projetmobile.mobile.data.repository.RepositoryException

internal data class GameFormFieldErrors(
    val titleError: String? = null,
    val typeError: String? = null,
    val editorError: String? = null,
    val minAgeError: String? = null,
    val authorsError: String? = null,
    val minPlayersError: String? = null,
    val maxPlayersError: String? = null,
    val durationMinutesError: String? = null,
) {
    fun hasAny(): Boolean {
        return listOf(
            titleError,
            typeError,
            editorError,
            minAgeError,
            authorsError,
            minPlayersError,
            maxPlayersError,
            durationMinutesError,
        ).any { it != null }
    }
}

internal data class GameFormErrorPresentation(
    val fieldErrors: GameFormFieldErrors = GameFormFieldErrors(),
    val bannerMessage: String? = null,
)

internal fun mapGameFormSaveError(
    throwable: Throwable,
    isEditMode: Boolean,
): GameFormErrorPresentation {
    val repositoryException = throwable as? RepositoryException
    val message = repositoryException?.message?.trim().orEmpty()
    val details = repositoryException?.details.orEmpty()

    if (repositoryException?.statusCode == 409 && message == "Titre déjà utilisé") {
        return GameFormErrorPresentation(
            fieldErrors = GameFormFieldErrors(
                titleError = "Un jeu avec ce titre existe déjà.",
            ),
        )
    }

    if (repositoryException?.statusCode == 404 && message == "Jeu introuvable") {
        return GameFormErrorPresentation(
            bannerMessage = "Le jeu n'existe plus ou a été supprimé.",
        )
    }

    if (message == "Éditeur inexistant") {
        return GameFormErrorPresentation(
            fieldErrors = GameFormFieldErrors(
                editorError = "L'éditeur sélectionné n'existe plus.",
            ),
        )
    }

    if (message == "Mécanisme inexistant") {
        return GameFormErrorPresentation(
            bannerMessage = "Un ou plusieurs mécanismes n'existent plus. Rechargez le formulaire.",
        )
    }

    val mappedFieldErrors = mapGameFormFieldErrors(details)
    if (mappedFieldErrors.hasAny()) {
        return GameFormErrorPresentation(fieldErrors = mappedFieldErrors)
    }

    return GameFormErrorPresentation(
        bannerMessage = if (isEditMode) {
            "Impossible de mettre à jour le jeu."
        } else {
            "Impossible de créer le jeu."
        },
    )
}

internal fun mapGameFormLoadError(throwable: Throwable): String {
    val repositoryException = throwable as? RepositoryException
    return when {
        repositoryException?.statusCode == 404 && repositoryException.message == "Jeu introuvable" -> {
            "Le jeu n'existe plus ou a été supprimé."
        }

        else -> "Impossible de charger le jeu."
    }
}

internal fun mapGameDetailError(throwable: Throwable): String {
    val repositoryException = throwable as? RepositoryException
    return when {
        repositoryException?.statusCode == 404 && repositoryException.message == "Jeu introuvable" -> {
            "Le jeu n'existe plus ou a été supprimé."
        }

        else -> "Impossible de charger le jeu."
    }
}

internal fun mapGamesCatalogLoadError(@Suppress("UNUSED_PARAMETER") throwable: Throwable): String {
    return "Impossible de récupérer les jeux."
}

internal fun mapGameDeleteError(throwable: Throwable): String {
    val repositoryException = throwable as? RepositoryException
    return when (repositoryException?.message?.trim()) {
        "Impossible de supprimer ce jeu car il est utilisé dans une réservation" -> {
            "Ce jeu ne peut pas être supprimé car il est utilisé dans une réservation."
        }

        else -> "Impossible de supprimer le jeu."
    }
}

internal fun GameFormFields.withFieldErrors(errors: GameFormFieldErrors): GameFormFields {
    return copy(
        titleError = errors.titleError,
        typeError = errors.typeError,
        editorError = errors.editorError,
        minAgeError = errors.minAgeError,
        authorsError = errors.authorsError,
        minPlayersError = errors.minPlayersError,
        maxPlayersError = errors.maxPlayersError,
        durationMinutesError = errors.durationMinutesError,
    )
}

private fun mapGameFormFieldErrors(details: List<String>): GameFormFieldErrors {
    var errors = GameFormFieldErrors()

    details.forEach { detail ->
        when (detail.trim()) {
            "title est requis" -> errors = errors.copy(titleError = "Le titre est requis.")
            "type est requis" -> errors = errors.copy(typeError = "Le type est requis.")
            "editor_id est requis" -> errors = errors.copy(editorError = "L'éditeur est requis.")
            "min_age est requis" -> errors = errors.copy(minAgeError = "L'âge minimum est requis.")
            "authors est requis" -> errors = errors.copy(authorsError = "Les auteurs sont requis.")
            "min_age doit être positif" -> {
                errors = errors.copy(minAgeError = "L'âge minimum doit être positif.")
            }

            "min_players doit être supérieur ou égal à 1" -> {
                errors = errors.copy(minPlayersError = "Minimum 1 joueur.")
            }

            "max_players doit être supérieur ou égal à 1" -> {
                errors = errors.copy(maxPlayersError = "Minimum 1 joueur.")
            }

            "min_players ne peut pas être supérieur à max_players" -> {
                errors = errors.copy(maxPlayersError = "Le max doit être supérieur ou égal au min.")
            }

            "duration_minutes doit être positif" -> {
                errors = errors.copy(durationMinutesError = "La durée doit être positive.")
            }
        }
    }

    return errors
}
