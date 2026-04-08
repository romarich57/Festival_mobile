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

enum class RepositoryFailureKind {
    Offline,
    BackendUnreachable,
    Timeout,
    Auth,
    Server,
    Validation,
    Unknown,
}

class RepositoryException(
    val statusCode: Int? = null,
    val kind: RepositoryFailureKind = RepositoryFailureKind.Unknown,
    message: String,
    val details: List<String> = emptyList(),
    cause: Throwable? = null,
) : IllegalStateException(message, cause)

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
