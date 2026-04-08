/**
 * Rôle : Assure le parsing des URLs entrantes (Deep Linking) et les convertit en destinations internes (AppNavKey).
 * Ce fichier interprète les liens comme "festivalapp://auth/verification..." pour déclencher l'ouverture de certains écrans.
 * Précondition : Appelé par `MainActivity` lorsqu'une URL est transmise via un `Intent` d'application.
 * Postcondition : Retourne la bonne clé de navigation scellée d'après l'URI s'il est valide, sinon null.
 */
package com.projetmobile.mobile.ui.utils.navigation

import android.net.Uri
import com.projetmobile.mobile.data.entity.auth.VerificationResultStatus
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

fun parseAppDeepLink(uri: Uri?): AppNavKey? {
    return parseAppDeepLinkString(uri?.toString())
}

fun parseAppDeepLinkString(rawUri: String?): AppNavKey? {
    val parsedUri = rawUri
        ?.takeIf { it.isNotBlank() }
        ?.let {
            runCatching { URI.create(it) }.getOrNull()
        }
        ?: return null

    if (parsedUri.scheme != "festivalapp" || parsedUri.host != "auth") {
        return null
    }

    val queryParameters = parsedUri.queryParameters()

    return when (parsedUri.path) {
        "/verification" -> VerificationResult(
            status = when (queryParameters["status"]) {
                "success" -> VerificationResultStatus.Success
                "expired" -> VerificationResultStatus.Expired
                "invalid" -> VerificationResultStatus.Invalid
                else -> VerificationResultStatus.Error
            },
        )

        "/reset-password" -> ResetPassword(queryParameters["token"]?.takeIf { it.isNotBlank() })
        else -> null
    }
}

private fun URI.queryParameters(): Map<String, String?> {
    val rawQuery = query ?: return emptyMap()
    if (rawQuery.isBlank()) {
        return emptyMap()
    }

    return rawQuery
        .split("&")
        .filter { it.isNotBlank() }
        .associate { segment ->
            val parts = segment.split("=", limit = 2)
            val key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8)
            val value = parts
                .getOrNull(1)
                ?.let { URLDecoder.decode(it, StandardCharsets.UTF_8) }
            key to value
        }
}
