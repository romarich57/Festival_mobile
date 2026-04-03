package com.projetmobile.mobile.ui.screens.profile

import com.projetmobile.mobile.ui.utils.validation.ProfileUpdateValidationResult

internal fun ProfileUiState.withValidationErrors(
    validation: ProfileUpdateValidationResult,
): ProfileUiState {
    val validatedForm = form.withValidation(validation)
    return copy(
        form = validatedForm,
        editingFields = editingFields + editableFieldsWithErrors(validatedForm),
        errorMessage = null,
        infoMessage = null,
    ).recalculated()
}

private fun ProfileFormState.withValidation(
    validation: ProfileUpdateValidationResult,
): ProfileFormState {
    return copy(
        loginError = validation.usernameError,
        firstNameError = validation.firstNameError,
        lastNameError = validation.lastNameError,
        emailError = validation.emailError,
        phoneError = validation.phoneError,
    )
}
