package com.projetmobile.mobile.testutils

import com.projetmobile.mobile.data.entity.auth.AuthUser
import com.projetmobile.mobile.data.entity.profile.AvatarUploadResult
import com.projetmobile.mobile.data.entity.profile.ProfileUpdateInput
import com.projetmobile.mobile.data.entity.profile.ProfileUpdateResult
import com.projetmobile.mobile.data.repository.profile.ProfileRepository

class FakeProfileRepository(
    initialProfile: AuthUser? = sampleProfileUser(),
) : ProfileRepository {
    var getProfileCalls: Int = 0
    var getProfileResult: Result<AuthUser> = initialProfile?.let(Result.Companion::success)
        ?: Result.failure(IllegalStateException("Aucun profil disponible."))

    var updateProfileCalls: Int = 0
    var lastUpdateInput: ProfileUpdateInput? = null
    var updateProfileResult: Result<ProfileUpdateResult> = Result.success(
        ProfileUpdateResult(
            message = "Profil mis a jour.",
            user = initialProfile ?: sampleProfileUser(),
            emailVerificationSent = false,
        ),
    )

    var uploadAvatarCalls: Int = 0
    var lastUploadedFileName: String? = null
    var lastUploadedMimeType: String? = null
    var lastUploadedBytes: ByteArray? = null
    var uploadAvatarResult: Result<AvatarUploadResult> = Result.success(
        AvatarUploadResult(
            url = "/uploads/avatars/test.png",
            message = "Avatar uploade avec succes",
        ),
    )

    var requestPasswordResetCalls: Int = 0
    var lastResetEmail: String? = null
    var requestPasswordResetResult: Result<String> = Result.success(
        "Si un compte existe, un email a ete envoye.",
    )

    override suspend fun getProfile(): Result<AuthUser> {
        getProfileCalls += 1
        return getProfileResult
    }

    override suspend fun updateProfile(input: ProfileUpdateInput): Result<ProfileUpdateResult> {
        updateProfileCalls += 1
        lastUpdateInput = input
        return updateProfileResult
    }

    override suspend fun uploadAvatar(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ): Result<AvatarUploadResult> {
        uploadAvatarCalls += 1
        lastUploadedFileName = fileName
        lastUploadedMimeType = mimeType
        lastUploadedBytes = bytes
        return uploadAvatarResult
    }

    override suspend fun requestPasswordReset(email: String): Result<String> {
        requestPasswordResetCalls += 1
        lastResetEmail = email
        return requestPasswordResetResult
    }
}

fun sampleProfileUser(
    email: String = "romain@example.com",
    emailVerified: Boolean = true,
    avatarUrl: String? = "/uploads/avatars/original.png",
): AuthUser {
    return AuthUser(
        id = 7,
        login = "romain",
        role = "organizer",
        firstName = "Romain",
        lastName = "Richard",
        email = email,
        phone = "0600000000",
        avatarUrl = avatarUrl,
        emailVerified = emailVerified,
        createdAt = "2026-03-18T09:00:00Z",
    )
}
