package com.projetmobile.mobile.data.remote.profile

import com.projetmobile.mobile.data.entity.profile.OptionalField
import com.projetmobile.mobile.data.entity.profile.ProfileUpdateInput
import com.projetmobile.mobile.data.remote.auth.AuthUserDto
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class UpdateProfileRequestDto(
    val login: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phone: JsonElement? = null,
    val avatarUrl: JsonElement? = null,
)

@Serializable
data class ProfileUpdateResponseDto(
    val message: String,
    val user: AuthUserDto,
    val emailVerificationSent: Boolean,
)

@Serializable
data class UploadAvatarResponseDto(
    val url: String,
    val message: String,
)

internal fun ProfileUpdateInput.toDto(): UpdateProfileRequestDto {
    return UpdateProfileRequestDto(
        login = login,
        firstName = firstName,
        lastName = lastName,
        email = email,
        phone = phone.toJsonElement(),
        avatarUrl = avatarUrl.toJsonElement(),
    )
}

private fun OptionalField<String?>.toJsonElement(): JsonElement? {
    return when (this) {
        OptionalField.Unchanged -> null
        is OptionalField.Value -> value?.let(::JsonPrimitive) ?: JsonNull
    }
}
