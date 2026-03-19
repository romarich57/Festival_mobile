package com.projetmobile.mobile.data.remote.common

import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorDto(
    val error: String? = null,
    val message: String? = null,
)
