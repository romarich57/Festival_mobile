package com.projetmobile.mobile.data.sync

import com.projetmobile.mobile.data.room.SyncRetryAction
import com.projetmobile.mobile.data.room.SyncStatus

fun resolveRetryAction(syncStatus: String, retryAction: String?): String? {
    return retryAction ?: when (syncStatus) {
        SyncStatus.PENDING_CREATE -> SyncRetryAction.CREATE
        SyncStatus.PENDING_UPDATE -> SyncRetryAction.UPDATE
        SyncStatus.PENDING_DELETE -> SyncRetryAction.DELETE
        else -> null
    }
}

fun shouldHideFromCollections(syncStatus: String, retryAction: String?): Boolean {
    val resolvedAction = resolveRetryAction(syncStatus, retryAction)
    return syncStatus == SyncStatus.PENDING_DELETE ||
        (syncStatus == SyncStatus.ERROR && resolvedAction == SyncRetryAction.DELETE)
}

fun shouldPreserveLocalDuringRefresh(syncStatus: String, retryAction: String?): Boolean {
    return resolveRetryAction(syncStatus, retryAction) != null
}

fun shouldKeepLocalOnlyEntity(
    id: Int,
    syncStatus: String,
    retryAction: String?,
): Boolean {
    return id < 0 && (
        shouldPreserveLocalDuringRefresh(syncStatus, retryAction) ||
            syncStatus == SyncStatus.ERROR
        )
}
