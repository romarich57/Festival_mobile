package com.projetmobile.mobile.ui.screens.profile

import com.projetmobile.mobile.data.entity.auth.AuthUser

data class ProfileFormState(
    val login: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val loginError: String? = null,
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
)

sealed interface ProfileAvatarState {
    data object Unchanged : ProfileAvatarState

    data object Removed : ProfileAvatarState

    data class LocalSelection(
        val fileName: String,
        val mimeType: String,
        val bytes: ByteArray,
        val previewUriString: String,
    ) : ProfileAvatarState
}

data class AvatarSelectionPayload(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
    val previewUriString: String,
)

data class ProfileUiState(
    val profile: AuthUser? = null,
    val form: ProfileFormState = ProfileFormState(),
    val avatarState: ProfileAvatarState = ProfileAvatarState.Unchanged,
    val editingFields: Set<ProfileEditableField> = emptySet(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSaving: Boolean = false,
    val isSendingPasswordReset: Boolean = false,
    val hasPendingChanges: Boolean = false,
    val infoMessage: String? = null,
    val errorMessage: String? = null,
    val pendingSessionUserUpdate: AuthUser? = null,
) {
    val isEditing: Boolean
        get() = editingFields.isNotEmpty()

    fun isFieldEditing(field: ProfileEditableField): Boolean {
        return field in editingFields
    }
}

internal fun profileFormStateFor(user: AuthUser?): ProfileFormState {
    return if (user == null) {
        ProfileFormState()
    } else {
        ProfileFormState(
            login = user.login,
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email,
            phone = user.phone.orEmpty(),
        )
    }
}
