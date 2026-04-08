package com.projetmobile.mobile.data.worker

import com.projetmobile.mobile.data.repository.RepositoryException
import com.projetmobile.mobile.data.repository.RepositoryFailureKind
import com.projetmobile.mobile.data.repository.parseBackendErrorMessage
import com.projetmobile.mobile.data.repository.toRepositoryException
import retrofit2.HttpException

internal fun Throwable.isDeleteAlreadyApplied(): Boolean {
    if (this is HttpException && (code() == 404 || code() == 410)) {
        return true
    }

    val backendMessage = (this as? HttpException)
        ?.parseBackendErrorMessage()
        ?.lowercase()
        .orEmpty()

    return backendMessage.contains("introuv") ||
        backendMessage.contains("not found") ||
        backendMessage.contains("already deleted") ||
        backendMessage.contains("deja supprim")
}

internal fun Throwable.isRetryableSyncFailure(defaultMessage: String): Boolean {
    if (isDeleteAlreadyApplied()) {
        return false
    }

    val repositoryException = toRepositoryException(defaultMessage) as? RepositoryException
        ?: return true

    return when (repositoryException.kind) {
        RepositoryFailureKind.Offline,
        RepositoryFailureKind.BackendUnreachable,
        RepositoryFailureKind.Timeout,
        RepositoryFailureKind.Server,
        RepositoryFailureKind.Unknown -> true

        RepositoryFailureKind.Auth,
        RepositoryFailureKind.Validation -> false
    }
}

internal fun Throwable.toSyncFailureMessage(defaultMessage: String): String {
    val repositoryException = toRepositoryException(defaultMessage) as? RepositoryException
    return repositoryException?.message?.takeIf { it.isNotBlank() }
        ?: localizedMessage?.takeIf { it.isNotBlank() }
        ?: defaultMessage
}
