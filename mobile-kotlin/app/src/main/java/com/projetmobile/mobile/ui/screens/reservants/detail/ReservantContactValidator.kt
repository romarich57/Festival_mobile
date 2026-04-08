/**
 * Rôle : Valide les champs du formulaire de contact réservé aux réservants.
 * Ce fichier concentre les règles simples de cohérence avant envoi vers le ViewModel ou le backend.
 * Précondition : Les champs doivent être fournis dans leur forme brute issue de l'UI.
 * Postcondition : L'écran reçoit une structure d'erreurs immédiatement exploitable pour l'affichage.
 */
package com.projetmobile.mobile.ui.screens.reservants

private val SimpleEmailPattern = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

/**
 * Rôle : Valide les champs saisis pour un contact de réservant.
 * Précondition : `fields` doit contenir l'état courant du formulaire de contact.
 * Postcondition : Retourne un ensemble d'erreurs champ par champ, ou des valeurs null si le formulaire est valide.
 */
internal fun validateReservantContact(
    fields: ReservantContactFormFields,
): ReservantContactFieldErrors {
    return ReservantContactFieldErrors(
        nameError = fields.name.trim().ifBlank { "Le nom est requis." }.takeIf { it == "Le nom est requis." },
        emailError = when {
            fields.email.isBlank() -> "L'email est requis."
            !SimpleEmailPattern.matches(fields.email.trim()) -> "L'email est invalide."
            else -> null
        },
        phoneNumberError = fields.phoneNumber.trim().ifBlank { "Le téléphone est requis." }
            .takeIf { it == "Le téléphone est requis." },
        jobTitleError = fields.jobTitle.trim().ifBlank { "Le poste est requis." }
            .takeIf { it == "Le poste est requis." },
    )
}

/**
 * Rôle : Indique si le formulaire de contact contient au moins une erreur.
 * Précondition : L'objet d'erreur doit provenir d'un calcul de validation précédent.
 * Postcondition : Retourne `true` dès qu'un champ est invalide.
 */
internal fun ReservantContactFieldErrors.hasAny(): Boolean {
    return listOf(
        nameError,
        emailError,
        phoneNumberError,
        jobTitleError,
    ).any { it != null }
}
