package com.projetmobile.mobile.data.repository

import com.projetmobile.mobile.data.remote.common.ApiErrorDto
import com.projetmobile.mobile.data.remote.common.ApiJson
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import retrofit2.HttpException

class RepositoryException(
    val statusCode: Int? = null,
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
                message = backendError?.message
                    ?.let(::normalizeBackendErrorMessage)
                    ?: defaultMessage,
                details = backendError?.details.orEmpty(),
                cause = this,
            )
        }

        is SerializationException -> RepositoryException(
            message = defaultMessage,
            cause = this,
        )

        else -> RepositoryException(
            message = localizedMessage?.takeIf { it.isNotBlank() } ?: defaultMessage,
            cause = this,
        )
    }
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
