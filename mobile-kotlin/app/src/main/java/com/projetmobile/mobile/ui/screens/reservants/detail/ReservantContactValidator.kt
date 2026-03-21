package com.projetmobile.mobile.ui.screens.reservants

private val SimpleEmailPattern = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

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

internal fun ReservantContactFieldErrors.hasAny(): Boolean {
    return listOf(
        nameError,
        emailError,
        phoneNumberError,
        jobTitleError,
    ).any { it != null }
}
