package com.projetmobile.mobile.ui.screens.profile

import com.projetmobile.mobile.data.entity.auth.AuthUser
import com.projetmobile.mobile.data.entity.profile.OptionalField
import com.projetmobile.mobile.data.entity.profile.ProfileUpdateInput

internal fun ProfileUiState.recalculated(): ProfileUiState {
    return copy(hasPendingChanges = hasPendingProfileChanges(profile, form, avatarState))
}

internal fun hasPendingProfileChanges(
    profile: AuthUser?,
    form: ProfileFormState,
    avatarState: ProfileAvatarState,
): Boolean {
    if (profile == null) {
        return false
    }

    val phoneValue = form.phone.trim().ifBlank { null }
    val textChanged = profile.login != form.login.trim() ||
        profile.firstName != form.firstName.trim() ||
        profile.lastName != form.lastName.trim() ||
        profile.email != form.email.trim() ||
        profile.phone != phoneValue

    val avatarChanged = when (avatarState) {
        ProfileAvatarState.Unchanged -> false
        ProfileAvatarState.Removed -> profile.avatarUrl != null
        is ProfileAvatarState.LocalSelection -> true
    }

    return textChanged || avatarChanged
}

internal fun buildProfileUpdateInput(
    savedProfile: AuthUser,
    form: ProfileFormState,
    avatarState: ProfileAvatarState,
    uploadedAvatarUrl: String?,
): ProfileUpdateInput? {
    val normalizedLogin = form.login.trim()
    val normalizedFirstName = form.firstName.trim()
    val normalizedLastName = form.lastName.trim()
    val normalizedEmail = form.email.trim()
    val normalizedPhone = form.phone.trim().ifBlank { null }

    val input = ProfileUpdateInput(
        login = normalizedLogin.takeIf { it != savedProfile.login },
        firstName = normalizedFirstName.takeIf { it != savedProfile.firstName },
        lastName = normalizedLastName.takeIf { it != savedProfile.lastName },
        email = normalizedEmail.takeIf { it != savedProfile.email },
        phone = if (normalizedPhone != savedProfile.phone) {
            OptionalField.Value(normalizedPhone)
        } else {
            OptionalField.Unchanged
        },
        avatarUrl = when (avatarState) {
            ProfileAvatarState.Unchanged -> OptionalField.Unchanged
            ProfileAvatarState.Removed -> OptionalField.Value(null)
            is ProfileAvatarState.LocalSelection -> OptionalField.Value(uploadedAvatarUrl)
        },
    )

    return if (
        input.login == null &&
        input.firstName == null &&
        input.lastName == null &&
        input.email == null &&
        input.phone == OptionalField.Unchanged &&
        input.avatarUrl == OptionalField.Unchanged
    ) {
        null
    } else {
        input
    }
}
