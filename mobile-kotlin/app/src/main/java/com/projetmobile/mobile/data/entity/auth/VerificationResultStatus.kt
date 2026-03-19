package com.projetmobile.mobile.data.entity.auth

import kotlinx.serialization.Serializable

@Serializable
enum class VerificationResultStatus {
    Success,
    Expired,
    Invalid,
    Error,
}
