package com.projetmobile.mobile.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.projetmobile.mobile.FestivalApplication
import com.projetmobile.mobile.data.database.AppDatabase
import com.projetmobile.mobile.data.room.SyncRetryAction
import com.projetmobile.mobile.data.room.SyncStatus
import com.projetmobile.mobile.data.sync.resolveRetryAction

class FestivalSyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "festival_sync"
    }

    override suspend fun doWork(): Result {
        val app = applicationContext as FestivalApplication
        val festivalDao = AppDatabase.getInstance(applicationContext).festivalDao()
        val api = app.appContainer.festivalApiService

        val pending = festivalDao.getPending()
        if (pending.isEmpty()) {
            return Result.success()
        }

        var hasRetryableError = false

        for (entity in pending) {
            val action = resolveRetryAction(entity.syncStatus, entity.retryAction) ?: continue
            try {
                when (action) {
                    SyncRetryAction.DELETE -> {
                        api.deleteFestival(entity.id)
                        festivalDao.deleteById(entity.id)
                    }
                }
            } catch (throwable: Throwable) {
                if (action == SyncRetryAction.DELETE && throwable.isDeleteAlreadyApplied()) {
                    festivalDao.deleteById(entity.id)
                    continue
                }

                val retryable = throwable.isRetryableSyncFailure("Impossible de synchroniser la suppression du festival.")
                festivalDao.updateSyncState(
                    id = entity.id,
                    status = SyncStatus.ERROR,
                    retryAction = if (retryable) action else null,
                    lastSyncErrorMessage = throwable.toSyncFailureMessage(
                        "Impossible de synchroniser la suppression du festival.",
                    ),
                )
                if (retryable) {
                    hasRetryableError = true
                }
            }
        }

        return if (hasRetryableError) Result.retry() else Result.success()
    }
}
