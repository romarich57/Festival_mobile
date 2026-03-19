package com.projetmobile.mobile.ui.screens.profile

enum class ProfileEditableField {
    Login,
    FirstName,
    LastName,
    Email,
    Phone,
}

internal fun editableFieldsWithErrors(form: ProfileFormState): Set<ProfileEditableField> {
    val fields = mutableSetOf<ProfileEditableField>()
    if (form.loginError != null) {
        fields += ProfileEditableField.Login
    }
    if (form.firstNameError != null) {
        fields += ProfileEditableField.FirstName
    }
    if (form.lastNameError != null) {
        fields += ProfileEditableField.LastName
    }
    if (form.emailError != null) {
        fields += ProfileEditableField.Email
    }
    if (form.phoneError != null) {
        fields += ProfileEditableField.Phone
    }
    return fields
}
