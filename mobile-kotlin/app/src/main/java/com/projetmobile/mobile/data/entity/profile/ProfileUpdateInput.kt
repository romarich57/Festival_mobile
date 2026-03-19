package com.projetmobile.mobile.data.entity.profile

data class ProfileUpdateInput(
    val login: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phone: OptionalField<String?> = OptionalField.Unchanged,
    val avatarUrl: OptionalField<String?> = OptionalField.Unchanged,
)
