/**
 * Rôle : Regroupe les helpers liés aux médias de jeux, surtout les liens YouTube et les URLs backend absolues.
 * Ce fichier sert d'adaptateur entre les données brutes de l'API et les formats attendus par l'UI.
 * Précondition : Les URLs reçues doivent être des chaînes issues du backend ou de la saisie utilisateur.
 * Postcondition : L'UI dispose de références vidéo normalisées et d'URLs prêtes à être consommées.
 */
package com.projetmobile.mobile.ui.screens.games

import com.projetmobile.mobile.BuildConfig
import java.net.URI

/**
 * Rôle : Décrit le composant youtube video reference du module les jeux partagé.
 */
internal data class YoutubeVideoReference(
    val originalUrl: String,
    val videoId: String,
) {
    val watchUrl: String = "https://www.youtube.com/watch?v=$videoId"
    val embedUrl: String = "https://www.youtube.com/embed/$videoId?autoplay=1&playsinline=1"
    val thumbnailUrl: String = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
}

/**
 * Rôle : Analyse une URL potentiellement YouTube et retourne une référence vidéo normalisée.
 * Précondition : `url` doit contenir un lien potentiellement valide ou vide.
 * Postcondition : Retourne une référence avec identifiant YouTube si l'URL est supportée, sinon `null`.
 */
internal fun youtubeVideoReference(url: String): YoutubeVideoReference? {
    val trimmedUrl = url.trim()
    val videoId = extractYoutubeVideoId(trimmedUrl) ?: return null
    return YoutubeVideoReference(
        originalUrl = trimmedUrl,
        videoId = videoId,
    )
}

/**
 * Rôle : Extrait uniquement l'identifiant vidéo YouTube depuis une URL supportée.
 * Précondition : `url` doit être un lien YouTube complet, court ou embed.
 * Postcondition : Retourne l'identifiant de la vidéo ou `null` si le format n'est pas reconnu.
 */
internal fun youtubeVideoId(url: String): String? {
    return youtubeVideoReference(url)?.videoId
}

/**
 * Rôle : Construit l'URL de lecture YouTube à partir d'une URL d'entrée.
 * Précondition : L'URL fournie doit être reconnue comme un lien YouTube valide.
 * Postcondition : Retourne l'URL `watch` standard ou `null` si la source n'est pas supportée.
 */
internal fun youtubeWatchUrl(url: String): String? {
    return youtubeVideoReference(url)?.watchUrl
}

/**
 * Rôle : Construit l'URL de miniature YouTube à partir d'une URL d'entrée.
 * Précondition : L'URL fournie doit être reconnue comme un lien YouTube valide.
 * Postcondition : Retourne l'URL de miniature haute définition ou `null` si la source n'est pas supportée.
 */
internal fun youtubeThumbnailUrl(url: String): String? {
    return youtubeVideoReference(url)?.thumbnailUrl
}

/**
 * Rôle : Construit l'URL d'intégration YouTube à partir d'une URL d'entrée.
 * Précondition : L'URL fournie doit être reconnue comme un lien YouTube valide.
 * Postcondition : Retourne l'URL embed prête pour la WebView ou `null` si la source n'est pas supportée.
 */
internal fun youtubeEmbedUrl(url: String): String? {
    return youtubeVideoReference(url)?.embedUrl
}

/**
 * Rôle : Extrait l'identifiant de vidéo YouTube à partir des variantes d'URL supportées par l'application.
 * Précondition : `url` doit être une chaîne potentiellement vide, mais idéalement un lien YouTube standard, short ou embed.
 * Postcondition : Retourne un identifiant non vide si l'URL est exploitable, sinon `null`.
 */
private fun extractYoutubeVideoId(url: String): String? {
    val trimmedUrl = url.trim()
    if (trimmedUrl.isEmpty()) return null

    val uri = runCatching { URI(trimmedUrl) }.getOrNull() ?: return null
    val host = uri.host?.removePrefix("www.")?.lowercase() ?: return null
    val path = uri.path.orEmpty().trim('/')

    return when {
        host == "youtu.be" -> path.substringBefore('/')
        host.endsWith("youtube.com") && path == "watch" -> {
            uri.query
                ?.split("&")
                ?.firstOrNull { it.startsWith("v=") }
                ?.substringAfter("v=")
        }

        host.endsWith("youtube.com") && path.startsWith("embed/") -> {
            path.substringAfter("embed/").substringBefore('/')
        }

        host.endsWith("youtube.com") && path.startsWith("shorts/") -> {
            path.substringAfter("shorts/").substringBefore('/')
        }

        else -> null
    }?.takeIf { it.isNotBlank() }
}

/**
 * Rôle : Transforme un chemin backend relatif en URL absolue consommable par l'UI.
 * Précondition : `path` doit être une route backend ou déjà une URL complète.
 * Postcondition : Retourne une URL absolue si nécessaire, sans dupliquer le préfixe API.
 */
internal fun toAbsoluteBackendUrl(path: String): String {
    return if (path.startsWith("http://") || path.startsWith("https://")) {
        path
    } else {
        BuildConfig.API_BASE_URL.removeSuffix("api/").removeSuffix("/") + path
    }
}
