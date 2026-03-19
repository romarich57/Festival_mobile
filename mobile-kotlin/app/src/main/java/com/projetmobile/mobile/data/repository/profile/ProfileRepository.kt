package com.projetmobile.mobile.data.repository.profile

import com.projetmobile.mobile.data.entity.auth.AuthUser
import com.projetmobile.mobile.data.entity.profile.AvatarUploadResult
import com.projetmobile.mobile.data.entity.profile.ProfileUpdateInput
import com.projetmobile.mobile.data.entity.profile.ProfileUpdateResult

interface ProfileRepository {
    suspend fun getProfile(): Result<AuthUser>

    suspend fun updateProfile(input: ProfileUpdateInput): Result<ProfileUpdateResult>

    suspend fun uploadAvatar(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ): Result<AvatarUploadResult>

    suspend fun requestPasswordReset(email: String): Result<String>
}
