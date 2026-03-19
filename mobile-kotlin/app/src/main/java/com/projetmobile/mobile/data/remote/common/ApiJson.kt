package com.projetmobile.mobile.data.remote.common

import kotlinx.serialization.json.Json

object ApiJson {
    val instance: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }
}
