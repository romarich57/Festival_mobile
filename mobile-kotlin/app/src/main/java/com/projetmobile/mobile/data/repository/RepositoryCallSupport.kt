package com.projetmobile.mobile.data.repository

import com.projetmobile.mobile.data.remote.common.ApiErrorDto
import com.projetmobile.mobile.data.remote.common.ApiJson
import retrofit2.HttpException

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
    val message = when (this) {
        is HttpException -> parseBackendErrorMessage() ?: defaultMessage
        else -> localizedMessage?.takeIf { it.isNotBlank() } ?: defaultMessage
    }
    return IllegalStateException(message, this)
}

fun HttpException.parseBackendErrorMessage(): String? {
    val errorBody = response()?.errorBody()?.string().orEmpty()
    if (errorBody.isBlank()) {
        return null
    }

    return runCatching {
        ApiJson.instance.decodeFromString<ApiErrorDto>(errorBody)
    }.getOrNull()?.let { payload ->
        payload.error?.takeIf { it.isNotBlank() }
            ?: payload.message?.takeIf { it.isNotBlank() }
    }
}
