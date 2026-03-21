package com.projetmobile.mobile.data.remote.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ApiErrorDto(
    val error: String? = null,
    val message: String? = null,
    val details: JsonElement? = null,
)
