/**
 * Rôle : Traduit les erreurs de repository en messages et erreurs de champs pour les écrans de réservants.
 * Ce fichier centralise les mappages d'erreurs pour le catalogue, le détail, le formulaire et les contacts liés.
 * Précondition : Les exceptions brutes doivent être transmises à ces helpers avant tout affichage utilisateur.
 * Postcondition : L'UI reçoit des messages cohérents et des erreurs de champs prêtes à être branchées sur les formulaires.
 */
package com.projetmobile.mobile.ui.screens.reservants

import com.projetmobile.mobile.data.repository.RepositoryException
import com.projetmobile.mobile.data.repository.RepositoryFailureKind
import com.projetmobile.mobile.data.repository.isOfflineFriendlyFailure

/**
 * Rôle : Représente les erreurs de champs du formulaire principal de réservant.
 * Précondition : L'instance doit être construite à partir des résultats de validation ou de mappage d'erreurs backend.
 * Postcondition : L'UI peut afficher les erreurs champ par champ.
 */
internal data class ReservantFormFieldErrors(
    val nameError: String? = null,
    val emailError: String? = null,
    val typeError: String? = null,
    val linkedEditorError: String? = null,
    val phoneNumberError: String? = null,
)

/**
 * Rôle : Regroupe les erreurs du formulaire de réservant avec un message global éventuel.
 * Précondition : Les erreurs de champs et la bannière doivent être construites par les helpers de mappage.
 * Postcondition : Le formulaire dispose d'un modèle de présentation unique pour l'affichage.
 */
internal data class ReservantFormErrorPresentation(
    val fieldErrors: ReservantFormFieldErrors = ReservantFormFieldErrors(),
    val bannerMessage: String? = null,
)

/**
 * Rôle : Représente les erreurs du sous-formulaire de contact lié à un réservant.
 * Précondition : L'instance doit venir d'une validation ou d'un mappage backend cohérent.
 * Postcondition : L'UI du contact peut afficher des erreurs ciblées sur chaque champ.
 */
internal data class ReservantContactFieldErrors(
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneNumberError: String? = null,
    val jobTitleError: String? = null,
)

/**
 * Rôle : Regroupe les erreurs du formulaire de contact avec un message global éventuel.
 * Précondition : Les erreurs de champs doivent être calculées avant la construction du modèle.
 * Postcondition : Le formulaire de contact dispose d'une présentation homogène pour l'UI.
 */
internal data class ReservantContactErrorPresentation(
    val fieldErrors: ReservantContactFieldErrors = ReservantContactFieldErrors(),
    val bannerMessage: String? = null,
)

/**
 * Rôle : Traduit les erreurs de chargement du catalogue des réservants en message lisible.
 * Précondition : L'exception doit provenir d'une requête de liste ou de pagination du catalogue.
 * Postcondition : Retourne un message prêt à être affiché avec éventuellement les données locales.
 */
internal fun mapReservantsCatalogLoadError(throwable: Throwable): String {
    val repositoryException = throwable as? RepositoryException
    return when (repositoryException?.kind) {
        RepositoryFailureKind.BackendUnreachable -> "Serveur inaccessible: réservants locaux affichés."
        RepositoryFailureKind.Offline,
        RepositoryFailureKind.Timeout -> "Mode hors-ligne: réservants locaux affichés."
        else -> "Impossible de charger les réservants."
    }
}

/**
 * Rôle : Traduit une erreur de suppression de réservant en message utilisateur.
 * Précondition : L'exception doit correspondre à une tentative de suppression.
 * Postcondition : Retourne un message explicite indiquant si la suppression est impossible, verrouillée ou introuvable.
 */
internal fun mapReservantDeleteError(throwable: Throwable): String {
    val repositoryException = throwable as? RepositoryException
    val message = repositoryException?.message?.trim()
    val details = repositoryException?.details.orEmpty().joinToString(" ")
    return when {
        repositoryException?.statusCode == 404 || message == "Réservant introuvable" || message == "Réservant non trouvé" -> {
            "Le réservant n'existe plus ou a déjà été supprimé."
        }

        repositoryException?.statusCode == 409 || details.contains("réservation", ignoreCase = true) -> {
            "Ce réservant ne peut pas être supprimé car il est encore utilisé dans une réservation."
        }

        else -> "Impossible de supprimer le réservant."
    }
}

/**
 * Rôle : Traduit les erreurs de chargement du détail d'un réservant.
 * Précondition : L'exception doit provenir du chargement du détail ou du cache local.
 * Postcondition : Retourne un message adapté aux modes en ligne et hors-ligne.
 */
internal fun mapReservantDetailError(throwable: Throwable): String {
    val repositoryException = throwable as? RepositoryException
    return if (
        repositoryException?.kind == RepositoryFailureKind.BackendUnreachable
    ) {
        "Serveur inaccessible: dernière version locale du réservant affichée."
    } else if (
        repositoryException?.kind?.isOfflineFriendlyFailure() == true
    ) {
        "Mode hors-ligne: dernière version locale du réservant affichée."
    } else if (
        repositoryException?.statusCode == 404 ||
        repositoryException?.message?.trim() == "Réservant introuvable" ||
        repositoryException?.message?.trim() == "Réservant non trouvé"
    ) {
        "Le réservant n'existe plus ou a été supprimé."
    } else {
        "Impossible de charger le réservant."
    }
}

/**
 * Rôle : Retourne un message générique lorsqu'il est impossible de charger les contacts d'un réservant.
 * Précondition : La fonction est appelée à la suite d'un échec réseau ou repository sur les contacts.
 * Postcondition : L'UI reçoit un message d'erreur stable à afficher.
 */
internal fun mapReservantContactsLoadError(@Suppress("UNUSED_PARAMETER") throwable: Throwable): String {
    return "Impossible de charger les contacts."
}

/**
 * Rôle : Retourne un message générique lorsqu'il est impossible de charger les jeux liés à un réservant.
 * Précondition : La fonction est appelée à la suite d'un échec réseau ou repository sur les jeux liés.
 * Postcondition : L'UI reçoit un message d'erreur stable à afficher.
 */
internal fun mapReservantGamesLoadError(@Suppress("UNUSED_PARAMETER") throwable: Throwable): String {
    return "Impossible de charger les jeux liés."
}

/**
 * Rôle : Traduit une erreur de création de contact en présentation exploitable par l'UI.
 * Précondition : L'exception doit provenir de l'opération d'ajout d'un contact lié à un réservant.
 * Postcondition : Retourne soit un message global, soit un message d'absence de réservant.
 */
internal fun mapReservantContactSaveError(throwable: Throwable): ReservantContactErrorPresentation {
    val repositoryException = throwable as? RepositoryException
    val message = repositoryException?.message?.trim()
    return when {
        repositoryException?.statusCode == 404 || message == "Réservant introuvable" || message == "Réservant non trouvé" -> {
            ReservantContactErrorPresentation(
                bannerMessage = "Le réservant n'existe plus ou a été supprimé.",
            )
        }

        else -> ReservantContactErrorPresentation(
            bannerMessage = "Impossible d'ajouter le contact.",
        )
    }
}

/**
 * Rôle : Traduit les erreurs de chargement du formulaire de réservant en message lisible.
 * Précondition : L'exception doit provenir d'un chargement initial ou d'une réouverture du formulaire.
 * Postcondition : Retourne un message adapté à un réservant absent ou à une erreur générique.
 */
internal fun mapReservantFormLoadError(throwable: Throwable): String {
    val repositoryException = throwable as? RepositoryException
    return if (
        repositoryException?.statusCode == 404 ||
        repositoryException?.message?.trim() == "Réservant introuvable" ||
        repositoryException?.message?.trim() == "Réservant non trouvé"
    ) {
        "Le réservant n'existe plus ou a été supprimé."
    } else {
        "Impossible de charger le réservant."
    }
}

/**
 * Rôle : Traduit une erreur d'enregistrement du formulaire de réservant en présentation d'erreur.
 * Précondition : L'exception doit provenir d'une création ou d'une mise à jour de réservant.
 * Postcondition : Retourne soit des erreurs de champs ciblées, soit une bannière globale adaptée au mode courant.
 */
internal fun mapReservantFormSaveError(
    throwable: Throwable,
    isEditMode: Boolean,
): ReservantFormErrorPresentation {
    val repositoryException = throwable as? RepositoryException
    val message = repositoryException?.message?.trim()
    return when (message) {
        "Nom et email déjà utilisés" -> ReservantFormErrorPresentation(
            fieldErrors = ReservantFormFieldErrors(
                nameError = "Ce nom est déjà utilisé.",
                emailError = "Cet email est déjà utilisé.",
            ),
        )

        "Nom déjà utilisé",
        "Un réservant avec ce nom existe déjà" -> ReservantFormErrorPresentation(
            fieldErrors = ReservantFormFieldErrors(
                nameError = "Ce nom est déjà utilisé.",
            ),
        )

        "Un réservant avec cet email existe déjà" -> ReservantFormErrorPresentation(
            fieldErrors = ReservantFormFieldErrors(
                emailError = "Cet email est déjà utilisé.",
            ),
        )

        "Éditeur inexistant" -> ReservantFormErrorPresentation(
            fieldErrors = ReservantFormFieldErrors(
                linkedEditorError = "L'éditeur sélectionné n'existe plus.",
            ),
        )

        "Réservant introuvable",
        "Réservant non trouvé" -> ReservantFormErrorPresentation(
            bannerMessage = "Le réservant n'existe plus ou a été supprimé.",
        )

        else -> ReservantFormErrorPresentation(
            bannerMessage = if (isEditMode) {
                "Impossible d'enregistrer les modifications du réservant."
            } else {
                "Impossible d'enregistrer le réservant."
            },
        )
    }
}
