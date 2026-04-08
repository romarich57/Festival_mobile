/**
 * Rôle : Valide les champs du formulaire de réservant avant soumission.
 * Ce fichier regroupe les règles de cohérence pour la création et la modification d'un réservant.
 * Précondition : Les champs doivent être fournis dans leur forme brute issue de l'UI.
 * Postcondition : Le formulaire reçoit des erreurs de champs immédiatement exploitables.
 */
package com.projetmobile.mobile.ui.screens.reservants

private val ReservantEmailPattern = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

/**
 * Rôle : Valide les champs saisis dans le formulaire de réservant.
 * Précondition : `fields` doit décrire l'état courant du formulaire et `isEditMode` le mode de saisie.
 * Postcondition : Retourne les erreurs de validation champ par champ, adaptées au contexte création ou édition.
 */
internal fun validateReservantForm(
    fields: ReservantFormFields,
    isEditMode: Boolean,
): ReservantFormFieldErrors {
    return ReservantFormFieldErrors(
        nameError = when {
            fields.name.trim().isBlank() -> "Le nom est requis."
            else -> null
        },
        emailError = when {
            fields.email.trim().isBlank() -> "L'email est requis."
            !ReservantEmailPattern.matches(fields.email.trim()) -> "L'email est invalide."
            else -> null
        },
        typeError = when {
            isEditMode -> null
            fields.type.isNullOrBlank() -> "Le type est requis."
            else -> null
        },
        linkedEditorError = when {
            !isEditMode &&
                fields.type == ReservantTypeChoice.Editor.value &&
                fields.linkedEditorId == null -> "Sélectionnez un éditeur."
            else -> null
        },
        phoneNumberError = null,
    )
}

/**
 * Rôle : Indique si le formulaire de réservant contient au moins une erreur.
 * Précondition : L'objet d'erreurs doit provenir d'un calcul de validation précédent.
 * Postcondition : Retourne `true` dès qu'un champ est invalide.
 */
internal fun ReservantFormFieldErrors.hasAny(): Boolean {
    return listOf(
        nameError,
        emailError,
        typeError,
        linkedEditorError,
        phoneNumberError,
    ).any { it != null }
}
