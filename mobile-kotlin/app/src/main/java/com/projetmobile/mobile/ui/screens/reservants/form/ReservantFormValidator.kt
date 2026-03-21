package com.projetmobile.mobile.ui.screens.reservants

private val ReservantEmailPattern = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

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

internal fun ReservantFormFieldErrors.hasAny(): Boolean {
    return listOf(
        nameError,
        emailError,
        typeError,
        linkedEditorError,
        phoneNumberError,
    ).any { it != null }
}
