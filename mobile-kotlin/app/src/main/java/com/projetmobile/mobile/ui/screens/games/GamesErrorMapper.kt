/**
 * Rôle : Centralise la traduction des erreurs techniques du dépôt en messages et erreurs d'interface pour les écrans de jeux.
 * Ce fichier regroupe les modèles d'erreur du formulaire de jeu ainsi que les mappages utilisés par le détail, le catalogue et la suppression.
 * Précondition : Les exceptions brutes provenant de la couche repository doivent être passées à ces helpers sans transformation préalable.
 * Postcondition : L'UI reçoit des messages et des erreurs de champs cohérents, prêts à être affichés à l'utilisateur.
 */
package com.projetmobile.mobile.ui.screens.games

import com.projetmobile.mobile.data.repository.RepositoryException
import com.projetmobile.mobile.data.repository.RepositoryFailureKind
import com.projetmobile.mobile.data.repository.isOfflineFriendlyFailure

/**
 * Rôle : Décrit le composant jeu formulaire champ erreurs du module les jeux.
 */
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
    /**
     * Rôle : Indique si au moins un champ du formulaire contient une erreur.
     * Précondition : L'instance doit représenter un ensemble d'erreurs déjà calculé.
     * Postcondition : Retourne `true` si au moins un message d'erreur est présent, sinon `false`.
     */
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

/**
 * Rôle : Décrit le composant jeu formulaire erreur presentation du module les jeux.
 */
internal data class GameFormErrorPresentation(
    val fieldErrors: GameFormFieldErrors = GameFormFieldErrors(),
    val bannerMessage: String? = null,
)

/**
 * Rôle : Transforme une erreur levée pendant l'enregistrement d'un jeu en présentation d'erreur exploitable par l'écran de formulaire.
 * Précondition : L'exception reçue doit provenir d'une opération de création ou de mise à jour de jeu.
 * Postcondition : Retourne soit des erreurs de champs, soit un message global, soit un message spécifique pour les cas gérés en amont.
 */
internal fun mapGameFormSaveError(
    throwable: Throwable,
    isEditMode: Boolean,
): GameFormErrorPresentation {
    val repositoryException = throwable as? RepositoryException
    val message = repositoryException?.message?.trim().orEmpty()
    val details = repositoryException?.details.orEmpty()

    // Le conflit de titre remonte comme une contrainte d'unicité explicite.
    if (repositoryException?.statusCode == 409 && message == "Titre déjà utilisé") {
        return GameFormErrorPresentation(
            fieldErrors = GameFormFieldErrors(
                titleError = "Un jeu avec ce titre existe déjà.",
            ),
        )
    }

    // Un jeu supprimé ou absent doit produire un message de bannière clair.
    if (repositoryException?.statusCode == 404 && message == "Jeu introuvable") {
        return GameFormErrorPresentation(
            bannerMessage = "Le jeu n'existe plus ou a été supprimé.",
        )
    }

    // L'éditeur est une dépendance fonctionnelle du formulaire, donc l'erreur doit viser le champ concerné.
    if (message == "Éditeur inexistant") {
        return GameFormErrorPresentation(
            fieldErrors = GameFormFieldErrors(
                editorError = "L'éditeur sélectionné n'existe plus.",
            ),
        )
    }

    // Quand les mécanismes ont disparu, on alerte l'utilisateur sur la validité globale du formulaire.
    if (message == "Mécanisme inexistant") {
        return GameFormErrorPresentation(
            bannerMessage = "Un ou plusieurs mécanismes n'existent plus. Rechargez le formulaire.",
        )
    }

    val mappedFieldErrors = mapGameFormFieldErrors(details)
    if (mappedFieldErrors.hasAny()) {
        return GameFormErrorPresentation(fieldErrors = mappedFieldErrors)
    }

    // Cas générique quand aucun détail exploitable n'est retourné par le backend.
    return GameFormErrorPresentation(
        bannerMessage = if (isEditMode) {
            "Impossible de mettre à jour le jeu."
        } else {
            "Impossible de créer le jeu."
        },
    )
}

/**
 * Rôle : Traduit une erreur de chargement du formulaire de jeu en message utilisateur simple.
 * Précondition : La fonction doit recevoir une exception liée à la lecture initiale ou à l'édition d'un jeu.
 * Postcondition : Retourne une chaîne prête à afficher dans l'UI de chargement.
 */
internal fun mapGameFormLoadError(throwable: Throwable): String {
    val repositoryException = throwable as? RepositoryException
    return when {
        repositoryException?.statusCode == 404 && repositoryException.message == "Jeu introuvable" -> {
            "Le jeu n'existe plus ou a été supprimé."
        }

        else -> "Impossible de charger le jeu."
    }
}

/**
 * Rôle : Traduit les erreurs de chargement de la page détail d'un jeu.
 * Précondition : L'appelant doit fournir l'exception remontée par la lecture du détail ou du cache local.
 * Postcondition : Retourne un message adapté au contexte en ligne ou hors-ligne.
 */
internal fun mapGameDetailError(throwable: Throwable): String {
    val repositoryException = throwable as? RepositoryException
    return when {
        repositoryException?.kind == RepositoryFailureKind.BackendUnreachable -> {
            "Serveur inaccessible: dernière version locale du jeu affichée."
        }

        repositoryException?.kind?.isOfflineFriendlyFailure() == true -> {
            "Mode hors-ligne: dernière version locale du jeu affichée."
        }

        repositoryException?.statusCode == 404 && repositoryException.message == "Jeu introuvable" -> {
            "Le jeu n'existe plus ou a été supprimé."
        }

        else -> "Impossible de charger le jeu."
    }
}

/**
 * Rôle : Traduit les erreurs de chargement du catalogue de jeux.
 * Précondition : L'exception doit provenir d'une requête de liste ou de pagination du catalogue.
 * Postcondition : Retourne un message qui explique si l'app utilise les données locales ou si le chargement a totalement échoué.
 */
internal fun mapGamesCatalogLoadError(throwable: Throwable): String {
    val repositoryException = throwable as? RepositoryException
    return when (repositoryException?.kind) {
        RepositoryFailureKind.BackendUnreachable -> "Serveur inaccessible: jeux locaux affichés."
        RepositoryFailureKind.Offline,
        RepositoryFailureKind.Timeout -> "Mode hors-ligne: jeux locaux affichés."
        else -> "Impossible de récupérer les jeux."
    }
}

/**
 * Rôle : Traduit une erreur de suppression de jeu en message utilisateur.
 * Précondition : L'erreur doit correspondre à une tentative de suppression côté serveur ou local.
 * Postcondition : Retourne un message indiquant si la suppression a échoué à cause d'une dépendance ou d'un autre problème.
 */
internal fun mapGameDeleteError(throwable: Throwable): String {
    val repositoryException = throwable as? RepositoryException
    return when (repositoryException?.message?.trim()) {
        "Impossible de supprimer ce jeu car il est utilisé dans une réservation" -> {
            "Ce jeu ne peut pas être supprimé car il est utilisé dans une réservation."
        }

        else -> "Impossible de supprimer le jeu."
    }
}

/**
 * Rôle : Injecte les erreurs de champs calculées dans l'état du formulaire de jeu.
 * Précondition : L'état du formulaire doit contenir les champs à jour au moment de l'appel.
 * Postcondition : Retourne une copie de l'état avec les erreurs de champs alignées sur la validation reçue.
 */
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

/**
 * Rôle : Convertit les détails textuels de validation backend en erreurs de champs de formulaire de jeu.
 * Précondition : `details` doit contenir les messages bruts transmis par le backend.
 * Postcondition : Retourne un objet d'erreurs de champs prêt à être branché sur le formulaire.
 */
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
