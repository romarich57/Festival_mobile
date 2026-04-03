package com.projetmobile.mobile.ui.screens.reservants

import com.projetmobile.mobile.data.repository.RepositoryException

internal data class ReservantFormFieldErrors(
    val nameError: String? = null,
    val emailError: String? = null,
    val typeError: String? = null,
    val linkedEditorError: String? = null,
    val phoneNumberError: String? = null,
)

internal data class ReservantFormErrorPresentation(
    val fieldErrors: ReservantFormFieldErrors = ReservantFormFieldErrors(),
    val bannerMessage: String? = null,
)

internal data class ReservantContactFieldErrors(
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneNumberError: String? = null,
    val jobTitleError: String? = null,
)

internal data class ReservantContactErrorPresentation(
    val fieldErrors: ReservantContactFieldErrors = ReservantContactFieldErrors(),
    val bannerMessage: String? = null,
)

internal fun mapReservantsCatalogLoadError(@Suppress("UNUSED_PARAMETER") throwable: Throwable): String {
    return "Impossible de charger les réservants."
}

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

internal fun mapReservantDetailError(throwable: Throwable): String {
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

internal fun mapReservantContactsLoadError(@Suppress("UNUSED_PARAMETER") throwable: Throwable): String {
    return "Impossible de charger les contacts."
}

internal fun mapReservantGamesLoadError(@Suppress("UNUSED_PARAMETER") throwable: Throwable): String {
    return "Impossible de charger les jeux liés."
}

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
