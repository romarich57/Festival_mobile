package com.projetmobile.mobile.data.repository

import com.projetmobile.mobile.data.remote.common.ApiErrorDto
import com.projetmobile.mobile.data.remote.common.ApiJson
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import retrofit2.HttpException

/**
 * Rôle : Typologie d'erreurs normalisées rencontrées par un composant Repository.
 * 
 * Précondition : Résulte de l'analyse d'une exception levée en base distante ou en local.
 * Postcondition : Utilisé par l'UI pour adapter le message affiché (Offline, Timeout, Auth...).
 */
enum class RepositoryFailureKind {
    Offline,
    BackendUnreachable,
    Timeout,
    Auth,
    Server,
    Validation,
    Unknown,
}

/**
 * Rôle : Exception pivot standardisant n'importe quelle erreur d'origine HTTP, Réseau ou Serialization.
 * 
 * Précondition : Peut encapsuler un Throwable en cause racine, un code HTTP ou des détails Backend.
 * Postcondition : Transforme l'erreur technique en une structure manipulable pour le domaine.
 */
class RepositoryException(
    val statusCode: Int? = null,
    val kind: RepositoryFailureKind = RepositoryFailureKind.Unknown,
    message: String,
    val details: List<String> = emptyList(),
    cause: Throwable? = null,
) : IllegalStateException(message, cause)

/**
 * Rôle : Encapsule l'exécution asynchrone arbitraire d'un appel (Réseau ou DB) au sein d'un composant Repository.
 * 
 * Précondition : La logique enfermée dans `block` doit obéir aux principes Coroutine.
 * Postcondition : Emarge dans une entité `Result: success ou failure` digérée convenablement. Laisse cependant s'échapper les `CancellationException`.
 */
suspend fun <T> runRepositoryCall(
    defaultMessage: String,
    block: suspend () -> T,
): Result<T> {
    return try {
        Result.success(block())
    } catch (throwable: Throwable) {
        if (throwable is CancellationException) {
            throw throwable
        }
        Result.failure(throwable.toRepositoryException(defaultMessage))
    }
}

/**
 * Rôle : Mappe de manière experte toute Throwables variées en une unique `RepositoryException` qualifiée.
 * 
 * Précondition : La cause initiale d'un disfonctionnement technique / réseau / Parsing.
 * Postcondition : Récupère intelligemment la cause profonde, le statut HTTP, et le message convivial via `ApiErrorDto`.
 */
fun Throwable.toRepositoryException(defaultMessage: String): Throwable {
    return when (this) {
        is RepositoryException -> this
        is HttpException -> {
            val backendError = parseBackendError()
            RepositoryException(
                statusCode = code(),
                kind = code().toRepositoryFailureKind(),
                message = backendError?.message
                    ?.let(::normalizeBackendErrorMessage)
                    ?: defaultMessage,
                details = backendError?.details.orEmpty(),
                cause = this,
            )
        }

        is UnknownHostException,
        is ConnectException,
        is NoRouteToHostException,
        is SSLException -> connectionFailureException(this)

        is SocketTimeoutException,
        is InterruptedIOException -> RepositoryException(
            kind = RepositoryFailureKind.Timeout,
            message = TIMEOUT_ERROR_MESSAGE,
            cause = this,
        )

        is SerializationException -> RepositoryException(
            kind = RepositoryFailureKind.Unknown,
            message = defaultMessage,
            cause = this,
        )

        else -> RepositoryException(
            kind = RepositoryFailureKind.Unknown,
            message = localizedMessage?.takeIf { it.isNotBlank() } ?: defaultMessage,
            cause = this,
        )
    }
}

private const val OFFLINE_ERROR_MESSAGE =
    "Aucune connexion internet. Réessayez lorsque vous serez de nouveau en ligne."

private const val BACKEND_UNREACHABLE_ERROR_MESSAGE =
    "Serveur inaccessible pour le moment. Réessayez plus tard."

private const val TIMEOUT_ERROR_MESSAGE =
    "Le serveur ne répond pas pour le moment. Réessayez."

/**
 * Rôle : Extension pour savoir si l'anomalie peut être contournée / expliquée par l'affichage d'un EmptyState/OfflineUI.
 * 
 * Précondition : Avoir évalué le `kind` de la `RepositoryException`.
 * Postcondition : Retourne `true` si la défaillance réseau est la cause.
 */
fun RepositoryFailureKind.isOfflineFriendlyFailure(): Boolean {
    return this == RepositoryFailureKind.Offline ||
        this == RepositoryFailureKind.Timeout ||
        this == RepositoryFailureKind.BackendUnreachable
}

private data class ParsedBackendError(
    val message: String?,
    val details: List<String>,
)

fun HttpException.parseBackendErrorMessage(): String? {
    return parseBackendError()?.message
}

private fun HttpException.parseBackendError(): ParsedBackendError? {
    val errorBody = response()?.errorBody()?.string().orEmpty()
    if (errorBody.isBlank()) {
        return null
    }

    val payload = runCatching {
        ApiJson.instance.decodeFromString<ApiErrorDto>(errorBody)
    }.getOrNull() ?: return null

    return ParsedBackendError(
        message = payload.error?.takeIf { it.isNotBlank() }
            ?: payload.message?.takeIf { it.isNotBlank() },
        details = payload.details.toErrorDetails(),
    )
}

private fun kotlinx.serialization.json.JsonElement?.toErrorDetails(): List<String> {
    return when (this) {
        null -> emptyList()
        is JsonPrimitive -> primitiveContentOrNull()?.let { listOf(it) } ?: emptyList()
        is JsonArray -> mapNotNull { element ->
            (element as? JsonPrimitive)?.primitiveContentOrNull()
        }

        else -> emptyList()
    }
}

private fun JsonPrimitive.primitiveContentOrNull(): String? {
    return content.takeUnless { it == "null" && !isString }
}

private fun normalizeBackendErrorMessage(message: String): String {
    return when (message.trim()) {
        "Token manquant",
        "Token invalide ou expiré",
        "Refresh token manquant",
        "Refresh token invalide ou expiré" -> "Session expirée. Veuillez vous reconnecter."
        else -> message
    }
}

private fun connectionFailureException(cause: Throwable): RepositoryException {
    val isNetworkValidated = RepositoryNetworkStatus.hasValidatedNetwork()
    return RepositoryException(
        kind = if (isNetworkValidated) {
            RepositoryFailureKind.BackendUnreachable
        } else {
            RepositoryFailureKind.Offline
        },
        message = if (isNetworkValidated) {
            BACKEND_UNREACHABLE_ERROR_MESSAGE
        } else {
            OFFLINE_ERROR_MESSAGE
        },
        cause = cause,
    )
}

private fun Int.toRepositoryFailureKind(): RepositoryFailureKind {
    return when (this) {
        400, 409, 422 -> RepositoryFailureKind.Validation
        401, 403 -> RepositoryFailureKind.Auth
        in 500..599 -> RepositoryFailureKind.Server
        else -> RepositoryFailureKind.Unknown
    }
}
