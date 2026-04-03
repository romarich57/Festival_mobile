package com.projetmobile.mobile.ui.screens.profile

import com.projetmobile.mobile.data.entity.auth.AuthUser

private const val MAX_AVATAR_SIZE_BYTES = 2 * 1024 * 1024

internal fun validateAvatarSelection(selection: AvatarSelectionPayload): String? {
    val normalizedMimeType = selection.mimeType.lowercase()
    if (!normalizedMimeType.startsWith("image/")) {
        return "Seules les images sont acceptées pour l'avatar."
    }
    if (selection.bytes.size > MAX_AVATAR_SIZE_BYTES) {
        return "L'image dépasse 2 Mo."
    }
    return null
}

internal fun AvatarSelectionPayload.toAvatarState(): ProfileAvatarState.LocalSelection {
    return ProfileAvatarState.LocalSelection(
        fileName = fileName,
        mimeType = mimeType,
        bytes = bytes,
        previewUriString = previewUriString,
    )
}

internal fun nextAvatarStateAfterRemoval(
    profile: AuthUser?,
    avatarState: ProfileAvatarState,
): ProfileAvatarState {
    return when {
        profile?.avatarUrl != null -> ProfileAvatarState.Removed
        avatarState is ProfileAvatarState.LocalSelection -> ProfileAvatarState.Unchanged
        else -> ProfileAvatarState.Unchanged
    }
}
