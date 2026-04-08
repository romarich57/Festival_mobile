package com.projetmobile.mobile.data.repository.profile

import com.projetmobile.mobile.data.entity.profile.AvatarUploadResult
import com.projetmobile.mobile.data.entity.profile.ProfileUpdateInput
import com.projetmobile.mobile.data.entity.profile.ProfileUpdateResult
import com.projetmobile.mobile.data.mapper.auth.toAuthUser
import com.projetmobile.mobile.data.remote.auth.AuthApiService
import com.projetmobile.mobile.data.remote.auth.ForgotPasswordRequestDto
import com.projetmobile.mobile.data.remote.profile.ProfileApiService
import com.projetmobile.mobile.data.remote.profile.toDto
import com.projetmobile.mobile.data.repository.runRepositoryCall
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Rôle : Classe concrétisant les opérations "Profile" du repository en s'interfaçant avec [ProfileApiService].
 * 
 * Précondition : Initialisation par Koin avec les API clients (Auth et Profile).
 * Postcondition : Enrobe dans [runRepositoryCall] les requêtes multipart et formattées.
 */
class ProfileRepositoryImpl(
    private val profileApiService: ProfileApiService,
    private val authApiService: AuthApiService,
) : ProfileRepository {

    override suspend fun getProfile() = runRepositoryCall(
        defaultMessage = "Impossible de récupérer le profil.",
    ) {
        profileApiService.getProfile().toAuthUser()
    }

    override suspend fun updateProfile(input: ProfileUpdateInput) = runRepositoryCall(
        defaultMessage = "Impossible de mettre à jour le profil.",
    ) {
        val response = profileApiService.updateProfile(input.toDto())
        ProfileUpdateResult(
            message = response.message,
            user = response.user.toAuthUser(),
            emailVerificationSent = response.emailVerificationSent,
        )
    }

    override suspend fun uploadAvatar(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ) = runRepositoryCall(
        defaultMessage = "Impossible d'envoyer l'avatar.",
    ) {
        val requestBody = bytes.toRequestBody(mimeType.toMediaType())
        val avatarPart = MultipartBody.Part.createFormData(
            name = "avatar",
            filename = fileName,
            body = requestBody,
        )
        val response = profileApiService.uploadAvatar(avatarPart)
        AvatarUploadResult(
            url = response.url,
            message = response.message,
        )
    }

    override suspend fun requestPasswordReset(email: String) = runRepositoryCall(
        defaultMessage = "Impossible d'envoyer le lien de réinitialisation.",
    ) {
        authApiService.requestPasswordReset(
            ForgotPasswordRequestDto(email.trim()),
        ).message
    }
}
